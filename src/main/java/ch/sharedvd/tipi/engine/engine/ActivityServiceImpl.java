package ch.sharedvd.tipi.engine.engine;

import ch.vd.registre.base.hqlbuilder.Expr;
import ch.vd.registre.base.hqlbuilder.srv.HqlBuilderService;
import ch.vd.registre.base.hqlbuilder.srv.ResultListWithCount;
import ch.vd.registre.base.jpa.PersistenceContextService;
import ch.vd.registre.base.tx.TxCallback;
import ch.vd.registre.base.tx.TxCallbackWithoutResult;
import ch.vd.registre.base.tx.TxTemplate;
import ch.vd.registre.base.utils.Assert;
import ch.vd.registre.base.utils.StackableStopWatch;
import ch.vd.registre.tipi.AppLog;
import ch.vd.registre.tipi.action.ActivityFacade;
import ch.vd.registre.tipi.action.ActivityMetaModel;
import ch.vd.registre.tipi.action.TopProcess;
import ch.vd.registre.tipi.client.*;
import ch.vd.registre.tipi.command.CommandService;
import ch.vd.registre.tipi.command.MetaModelHelper;
import ch.vd.registre.tipi.command.impl.ResumeActivityCommand;
import ch.vd.registre.tipi.command.impl.ResumeAllActivitiesCommand;
import ch.vd.registre.tipi.command.impl.RunExecutingActivitiesCommand;
import ch.vd.registre.tipi.criteria.DbActivityCriteria;
import ch.vd.registre.tipi.criteria.DbActivityProperty;
import ch.vd.registre.tipi.criteria.DbTopProcessCriteria;
import ch.vd.registre.tipi.criteria.DbTopProcessProperty;
import ch.vd.registre.tipi.meta.TopProcessMetaModel;
import ch.vd.registre.tipi.model.ActivityState;
import ch.vd.registre.tipi.model.DbActivity;
import ch.vd.registre.tipi.model.DbTopProcess;
import ch.vd.registre.tipi.model.svc.ActivityPersistenceService;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ActivityServiceImpl implements InitializingBean {

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
	@Qualifier("tipiSessionFactory")
	private SessionFactory sessionFactory;

	@Autowired
	@Qualifier("tipiHqlBuilderService")
	private HqlBuilderService hqlBuilder;

	@Autowired
	@Qualifier("tipiPersistenceContextService")
	private PersistenceContextService persist;

	@Autowired
	@Qualifier("tipiTransactionManager")
	// Spring TX manager
	private PlatformTransactionManager ptmTxManager;
	private TxTemplate txTemplate;

	@Override
	public void afterPropertiesSet() throws Exception {
		txTemplate = new TxTemplate(ptmTxManager);
	}

	private static final String GET_TOP_PROC_NAMES_WITH_EXECUTABLE_ACTIVITIES_SQL = "" + "select distinct p.NAME from TP_ACTIVITY a "
			+ "	join TP_ACTIVITY p on (p.ID=a.PROCESS_FK or p.ID=a.ID) "
			+ "where p.DTYPE='process' and a.STATE=?1 and a.REQUEST_END_EXECUTION=?2";

	@SuppressWarnings("unchecked")
	public List<String> getTopProcessNamesWithExecutingActivities() {
		final StackableStopWatch watch = new StackableStopWatch();
		watch.start("getTopProcessNamesWithExecutingActivities");

		try {
			return persist.listSqlQuery(GET_TOP_PROC_NAMES_WITH_EXECUTABLE_ACTIVITIES_SQL, ActivityState.EXECUTING.name(), Boolean.FALSE);
		}
		finally {
			watch.stop();
			LOGGER.debug("Temps pour la requete: " + watch);
		}
	}

	// package
	List<DbActivity> getExecutingActivities(String aTopProcessName, Collection<Long> aRunningActivities, int max) {
		final StackableStopWatch watch = new StackableStopWatch();
		watch.start("getExecutingActivities");

		try {

			DbTopProcessCriteria tpc = new DbTopProcessCriteria();
			tpc.addAndExpression(tpc.fqn().eq(aTopProcessName));
			tpc.restrictSelect(DbTopProcessProperty.Id);
			DbActivityCriteria amc = new DbActivityCriteria();
			amc.addAndExpression(Expr.or(amc.process__Id().in(tpc), amc.id().in(tpc)), amc.state().eq(ActivityState.EXECUTING), amc
					.requestEndExecution().eq(false));

			if ((null != aRunningActivities) && !aRunningActivities.isEmpty()) {
				amc.addAndExpression(amc.id().notIn(aRunningActivities));
			}
			amc.addOrder(DbActivityProperty.NbRetryDone, true);
			amc.addOrder(DbActivityProperty.Id, true);

			return hqlBuilder.getResultList(DbActivity.class, amc, max);
		}
		finally {
			watch.stop();
			LOGGER.debug("Temps pour la requete: " + watch);
		}
	}

	public long launch(final ActivityMetaModel meta, final VariableMap vars) {

		final long id = txTemplate.doInTransaction(new TxCallback<Long>() {
			@Override
			public Long execute(TransactionStatus status) throws Exception {

				Assert.notNull(meta);
				Assert.notNull(meta, "Meta not found: " + meta);

				return launch(sessionFactory.getCurrentSession(), meta, vars);
			}
		});

		return id;
	}

	public long launch(final Class<? extends TopProcess> cls, final VariableMap vars) {

		final long id = txTemplate.doInTransaction(new TxCallback<Long>() {
			@Override
			public Long execute(TransactionStatus status) throws Exception {

				TopProcessMetaModel meta = MetaModelHelper.getTopProcessMetaModel(cls);

				Assert.notNull(meta);
				Assert.notNull(meta, "Meta not found: " + meta);

				return launch(sessionFactory.getCurrentSession(), meta, vars);
			}
		});

		return id;
	}

	/**
	 * Prends une session en paramètre pour permettre de démarrer un process meme Si on n'a pas de session par défaut (Spring) ou qu'on est
	 * dans le flush() ou le commit() (Démarrage de TaskProcess)
	 *
	 * @param aSession
	 * @param meta
	 * @param vars
	 * @return
	 */
	public long launch(Session aSession, ActivityMetaModel meta, VariableMap vars) {
		// RCPERS-352 Workaround: just to have the connection at the beginning of the transaction...
		aSession.get(DbActivity.class, 1L);

		Assert.notNull(meta);
		DbActivity model = MetaModelHelper.createModelFromMeta(meta, true, vars, activityPersistenceService);
		aSession.persist(model);

		// State
		ActivityStateChangeService.executingFirstActivity(model);

		// Start it
		// On démarre toutes les activités qui sont en EXECUTING
		commandService.sendCommand(new RunExecutingActivitiesCommand());

		return model.getId();
	}

	public ResultListWithCount<TipiTopProcessInfos> getAllProcesses(final int maxHits) {
		return activityPersistenceService.getAllProcesses(maxHits);
	}

	public ResultListWithCount<TipiTopProcessInfos> getRunningProcesses(final int maxHits) {
		return activityPersistenceService.getRunningProcesses(maxHits);
	}

	public TipiActivityInfos getActivityInfos(final long id, final boolean loadVariables) {
		return activityPersistenceService.getActivityInfos(id, loadVariables);
	}

	public ResultListWithCount<TipiActivityInfos> searchActivities(final TipiCriteria criteria, final int maxHits) {
		return activityPersistenceService.searchActivities(criteria, maxHits);
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
		return txTemplate.doInTransaction(new TxCallback<Boolean>() {
			@Override
			public Boolean execute(TransactionStatus status) throws Exception {

				DbActivity activity = persist.get(DbActivity.class, id);
				return null != activity && activity.isResumable();
			}
		});
	}

	public void resume(final long id, final VariableMap vars) {
		txTemplate.doInTransaction(new TxCallbackWithoutResult() {
			@Override
			public void execute(TransactionStatus status) throws Exception {

				DbActivity aActivity = persist.get(DbActivity.class, id);

				// On mets les variables
				if (vars != null) {
					for (String key : vars.keySet()) {
						activityPersistenceService.putVariable(aActivity, key, vars.get(key));
					}
				}

				commandService.sendCommand(new ResumeActivityCommand(id, vars));
			}
		});
	}

	public String getStringVariable(final long id, final String key) {
		return txTemplate.doInTransaction(new TxCallback<String>() {
			@Override
			public String execute(TransactionStatus status) throws Exception {
				DbActivity act = persist.get(DbActivity.class, id);
				Assert.notNull(act);
				ActivityFacade facade = new ActivityFacade(act.getId(), activityPersistenceService);
				return (String) facade.getVariable(key);
			}
		});
	}

	public boolean isRunning(final long aid) {
		return txTemplate.doInTransaction(new TxCallback<Boolean>() {
			@Override
			public Boolean execute(TransactionStatus status) throws Exception {
				DbActivity act = persist.get(DbActivity.class, aid);
				return ((act != null) && !act.isTerminated() && !act.isAborted() && !act.isTerminatedWithError()
						&& !act.isTerminatedSuspended());
			}
		});
	}

	public boolean isProcessRunning(final long id) {
		return txTemplate.doInTransaction(new TxCallback<Boolean>() {
			@Override
			public Boolean execute(TransactionStatus status) throws Exception {
				DbActivity act = persist.get(DbActivity.class, id);
				return ((null != act) && isRunning(act.getProcessOrThis().getId()));
			}
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
		return txTemplate.doInTransaction(new TxCallback<Boolean>() {
			@Override
			public Boolean execute(TransactionStatus status) throws Exception {

				final DbActivity act = persist.get(DbActivity.class, aid);
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
			}

			/**
			 * Détermine si l'activité spécifiée - ou n'importe quelle sous-activité de cette activité - est en cours d'exécution (pour de vrai) dans un
			 * thread.
			 *
			 * @param launcher le launcher de l'activité
			 * @param aid      l'id de l'activité
			 * @return <b>vrai</b> si l'activité est en cours d'exécution; <b>faux</b> autrement.
			 */
			private boolean isSelfOrAnyChildRunning(TopProcessGroupLauncher launcher, long aid) {
				if (launcher.isRunning(aid)) {
					return true;
				}
				else {
					final List<ActivityFacade> children = activityPersistenceService.getChildren(aid);
					for (ActivityFacade child : children) {
						if (isSelfOrAnyChildRunning(launcher, child.getId())) {
							return true;
						}
					}
				}
				return false;
			}
		});
	}

	public boolean deleteProcess(final long processId) {
		return txTemplate.doInTransaction(new TxCallback<Boolean>() {
			@Override
			public Boolean execute(TransactionStatus status) throws Exception {
				DbActivity model = persist.get(DbActivity.class, processId);
				if ((null != model) && (model instanceof DbTopProcess)) {
					return deleteProcess((DbTopProcess) model);
				}
				return Boolean.TRUE; // Pas de process -> OK
			}
		});
	}

	public boolean deleteProcess(final DbTopProcess process) {

		ProcessDeleter deleter = new ProcessDeleter(process, hqlBuilder, persist);
		return deleter.delete();
	}

	public void abortProcess(final long processId) {

		// D'abord le processus lui-même
		txTemplate.doInTransaction(new TxCallbackWithoutResult() {
			@Override
			public void execute(TransactionStatus status) throws Exception {

				AppLog.info(LOGGER, "Aborting process " + processId);
				DbActivity act = persist.get(DbTopProcess.class, processId);
				LOGGER.debug("Process " + act.getId() + "/" + act.getFqn() + " -> state=ABORTED");
				act.setState(ActivityState.ABORTED);
			}
		});

		AppLog.info(LOGGER, "Aborting process " + processId + " sub-processess");

		// Ensuite les sous-activités du processus
		txTemplate.doInTransaction(new TxCallbackWithoutResult() {
			@Override
			public void execute(TransactionStatus status) throws Exception {
				LOGGER.debug("Activities of process " + processId + " -> state=ABORTED");
				persist.executeUpdate("update DbActivity a set a.state = 'ABORTED' where a.process.id = ?1", processId);
			}
		});
	}

	public List<ActivityThreadInfos> getThreadsInfos() {
		return groupManager.getThreadsInfos();
	}

	public List<ConnectionCupInfos> getAllConnectionCupInfos() {
		List<ConnectionCupInfos> connCupInfos = new ArrayList<ConnectionCupInfos>();
		for (ConnectionCap ct : connectionsCup.getCaps()) {
			connCupInfos.add(new ConnectionCupInfos(ct, connectionsCup));
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
