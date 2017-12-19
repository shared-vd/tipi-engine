package ch.sharedvd.tipi.engine.engine;

import ch.sharedvd.tipi.engine.action.ActivityFacade;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.client.VariableMap;
import ch.sharedvd.tipi.engine.command.CommandService;
import ch.sharedvd.tipi.engine.command.MetaModelHelper;
import ch.sharedvd.tipi.engine.command.impl.ResumeActivityCommand;
import ch.sharedvd.tipi.engine.command.impl.ResumeAllActivitiesCommand;
import ch.sharedvd.tipi.engine.command.impl.RunExecutingActivitiesCommand;
import ch.sharedvd.tipi.engine.infos.ActivityThreadInfos;
import ch.sharedvd.tipi.engine.infos.ConnectionCapInfos;
import ch.sharedvd.tipi.engine.meta.ActivityMetaModel;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import ch.sharedvd.tipi.engine.model.ActivityState;
import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.sharedvd.tipi.engine.model.DbTopProcess;
import ch.sharedvd.tipi.engine.repository.ActivityRepository;
import ch.sharedvd.tipi.engine.repository.TopProcessRepository;
import ch.sharedvd.tipi.engine.svc.ActivityPersistenceService;
import ch.sharedvd.tipi.engine.utils.Assert;
import ch.sharedvd.tipi.engine.utils.TxTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ActivityServiceImpl {

    private Logger LOGGER = LoggerFactory.getLogger(ActivityServiceImpl.class);

    @Autowired
    private TopProcessGroupManager groupManager;

    @Autowired
    private ConnectionCapManager connectionsCup;

    @Autowired
    private ActivityPersistenceService activityPersistenceService;

    @Autowired
    private CommandService commandService;

    @Autowired
    private ActivityRepository activityRepository;
    @Autowired
    private TopProcessRepository topProcessRepository;

    @Autowired
    private EntityManager em;
    @Autowired
    private TxTemplate txTemplate;

    @SuppressWarnings("unchecked")
    public List<String> getTopProcessNamesWithExecutingActivities() {
        return activityRepository.findTopProcessNamesByStateAndReqEnd(ActivityState.EXECUTING, false);
    }

    // package
    List<DbActivity> getExecutingActivities(String aTopProcessName, Collection<Long> aRunningActivities, int max) {
//		DbTopProcessCriteria tpc = new DbTopProcessCriteria();
//		tpc.addAndExpression(tpc.fqn().eq(aTopProcessName));
//		tpc.restrictSelect(DbTopProcessProperty.Id);
//		DbActivityCriteria amc = new DbActivityCriteria();
//		amc.addAndExpression(Expr.or(amc.process__Id().in(tpc), amc.id().in(tpc)), amc.state().eq(ActivityState.EXECUTING), amc
//				.requestEndExecution().eq(false));
//
//		if ((null != aRunningActivities) && !aRunningActivities.isEmpty()) {
//			amc.addAndExpression(amc.id().notIn(aRunningActivities));
//		}
//		amc.addOrder(DbActivityProperty.NbRetryDone, true);
//		amc.addOrder(DbActivityProperty.Id, true);
//
//		return hqlBuilder.getResultList(DbActivity.class, amc, max);
        return activityRepository.findExecutingActivities(aTopProcessName);
    }

    public long launch(final ActivityMetaModel meta, final VariableMap vars) {
        final long id = txTemplate.txWith((s) -> {
            Assert.notNull(meta);
            Assert.notNull(meta, "Meta not found: " + meta);
            return launch(activityRepository, meta, vars);
        });
        return id;
    }

    public long launch(final Class<? extends TopProcess> cls, final VariableMap vars) {
        final long id = txTemplate.txWith((status) -> {
            TopProcessMetaModel meta = MetaModelHelper.getTopProcessMetaModel(cls);

            Assert.notNull(meta);
            Assert.notNull(meta, "Meta not found: " + meta);

            return launch(activityRepository, meta, vars);

        });

        return id;
    }

    /**
     * Prends une session en paramètre pour permettre de démarrer un process meme Si on n'a pas de session par défaut (Spring) ou qu'on est
     * dans le flush() ou le commit() (Démarrage de TaskProcess)
     *
     * @param activityRepository
     * @param meta
     * @param vars
     * @return
     */
    public long launch(ActivityRepository activityRepository, ActivityMetaModel meta, VariableMap vars) {
        // RCPERS-352 Workaround: just to have the connection at the beginning of the transaction...
        activityRepository.findOne(1L);

        Assert.notNull(meta);
        DbActivity model = MetaModelHelper.createModelFromMeta(meta, true, vars, activityPersistenceService);
        model = activityRepository.save(model);

        // State
        ActivityStateChangeService.executingFirstActivity(model);

        // Start it
        // On démarre toutes les activités qui sont en EXECUTING
        commandService.sendCommand(new RunExecutingActivitiesCommand());

        return model.getId();
    }

    public void resumeAllError() {
        commandService.sendCommand(new ResumeAllActivitiesCommand(ActivityState.ERROR));
    }

    public void resumeErrors(String groupName) {
        commandService.sendCommand(new ResumeAllActivitiesCommand(ActivityState.ERROR, groupName));
    }

    public void resumeAllSuspended() {
        commandService.sendCommand(new ResumeAllActivitiesCommand(ActivityState.SUSPENDED));
    }

    public boolean isResumable(final long id) {
        return txTemplate.txWith((status) -> {
            DbActivity activity = activityRepository.findOne(id);
            return null != activity && activity.isResumable();
        });
    }

    public void resume(final long id, final VariableMap vars) {
        txTemplate.txWithout((status) -> {
            DbActivity aActivity = activityRepository.findOne(id);

            // On mets les variables
            if (vars != null) {
                for (String key : vars.keySet()) {
                    activityPersistenceService.putVariable(aActivity, key, vars.get(key));
                }
            }

            commandService.sendCommand(new ResumeActivityCommand(id, vars));
        });
    }

    public String getStringVariable(final long id, final String key) {
        return txTemplate.txWith((status) -> {
            DbActivity act = activityRepository.findOne(id);
            Assert.notNull(act);
            ActivityFacade facade = new ActivityFacade(act.getId(), activityPersistenceService);
            return (String) facade.getVariable(key);
        });
    }

    public boolean isRunning(final long aid) {
        return txTemplate.txWith((status) -> {
            DbActivity act = activityRepository.findOne(aid);
            return ((act != null) && !act.isTerminated() && !act.isAborted() && !act.isTerminatedWithError()
                    && !act.isTerminatedSuspended());
        });
    }

    public boolean isProcessRunning(final long id) {
        return txTemplate.txWith((status) -> {
            DbActivity act = activityRepository.findOne(id);
            return ((null != act) && isRunning(act.getProcessOrThis().getId()));
        });
    }

    /**
     * Détermine si l'activité spécifiée est en cours d'exécution dans un thread actuellement (une activité pour être dans l'état EXECUTING, mais néanmoins en
     * attente si tous les threads d'exécution sont occupés, par exemple).
     *
     * @param aid l'id d'une activité
     * @return <b>vrai</b> si l'activité est en cours d'exécution; <b>faux</b> autrement.
     */
    public boolean isProcessScheduled(final long aid) {
        return txTemplate.txWith((status) -> {

            final DbActivity act = activityRepository.findOne(aid);
            if (act == null) {
                return false;

            }

            if (act.isTerminated() || act.isAborted() || act.isTerminatedWithError() || act.isTerminatedSuspended()) {
                return false;
            }

            // on regarde maintenant si l'activité est bien en cours d'exécution (pour de vrai) dans un thread
            // (l'activité pour être dans l'état EXECUTING, mais néanmoins en attente si tous les threads d'exécution sont occupés, par exemple)
            final TopProcessGroupLauncher launcher = groupManager.getLauncher(act.getProcessOrThis().getFqn());
            return launcher.isRunning(aid);
        });
    }

    public boolean deleteProcess(final long processId) {
        return txTemplate.txWith((status) -> {
            DbActivity model = activityRepository.findOne(processId);
            if ((null != model) && (model instanceof DbTopProcess)) {
                return deleteProcess((DbTopProcess) model);
            }
            return Boolean.TRUE; // Pas de process -> OK
        });
    }

    public boolean deleteProcess(final DbTopProcess process) {
        ProcessDeleter deleter = new ProcessDeleter(process, em, topProcessRepository);
        return deleter.delete();
    }

    public void abortProcess(final long processId) {

        // D'abord le processus lui-même
        txTemplate.txWithout((status) -> {
            LOGGER.info("Aborting process " + processId);
            DbActivity act = topProcessRepository.findOne(processId);
            LOGGER.debug("Process " + act.getId() + "/" + act.getFqn() + " -> state=ABORTED");
            act.setState(ActivityState.ABORTED);
        });

        LOGGER.info("Aborting process " + processId + " sub-processess");

        // Ensuite les sous-activités du processus
        txTemplate.txWithout((status) -> {
                LOGGER.debug("Activities of process " + processId + " -> state=ABORTED");
                Query q = em.createQuery("update DbActivity a set a.state = 'ABORTED' where a.process.id = :processId");
                q.setParameter("processId", processId);
                q.executeUpdate();
        });
    }

    public List<ActivityThreadInfos> getThreadsInfos() {
        return groupManager.getThreadsInfos();
    }

    public List<ConnectionCapInfos> getAllConnectionCupInfos() {
        List<ConnectionCapInfos> connCupInfos = new ArrayList<>();
        for (ConnectionCap ct : connectionsCup.getCaps()) {
            connCupInfos.add(new ConnectionCapInfos(ct, connectionsCup));
        }
        return connCupInfos;
    }

    public void setMaxConnections(String aConnectionType, int aNbMaxConnections) {
        connectionsCup.setNbMaxConcurrent(aConnectionType, aNbMaxConnections);

        commandService.sendCommand(new RunExecutingActivitiesCommand());
    }

    public void setMaxConcurrentActivitiesForGroup(String aGroupName, int aNbMaxConnections) {
        groupManager.setMaxConcurrentActivitiesForGroup(aGroupName, aNbMaxConnections);

        commandService.sendCommand(new RunExecutingActivitiesCommand());
    }

    public void setPriorityForGroup(String aGroupName, int aPrio) {
        groupManager.setPriorityForGroup(aGroupName, aPrio);

        commandService.sendCommand(new RunExecutingActivitiesCommand());
    }
}
