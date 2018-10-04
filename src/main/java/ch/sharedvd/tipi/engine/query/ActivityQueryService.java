package ch.sharedvd.tipi.engine.query;

import ch.sharedvd.tipi.engine.infos.ActivityThreadInfos;
import ch.sharedvd.tipi.engine.infos.ConnectionCapInfos;
import ch.sharedvd.tipi.engine.infos.TipiActivityInfos;
import ch.sharedvd.tipi.engine.infos.TipiTopProcessInfos;
import ch.sharedvd.tipi.engine.meta.MetaModelHelper;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import ch.sharedvd.tipi.engine.model.ActivityState;
import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.sharedvd.tipi.engine.model.DbSubProcess;
import ch.sharedvd.tipi.engine.model.DbTopProcess;
import ch.sharedvd.tipi.engine.repository.ActivityRepository;
import ch.sharedvd.tipi.engine.runner.ConnectionCap;
import ch.sharedvd.tipi.engine.runner.ConnectionCapManager;
import ch.sharedvd.tipi.engine.runner.TopProcessGroupManager;
import ch.sharedvd.tipi.engine.utils.Assert;
import ch.sharedvd.tipi.engine.utils.ResultListWithCount;
import ch.sharedvd.tipi.engine.utils.TixTemplate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ActivityQueryService {

    @Autowired
    private TixTemplate txTemplate;
    @Autowired
    private ActivityRepository activityRepository;
    @Autowired
    private EntityManager em;

    @Autowired
    private ConnectionCapManager connectionCapManager;

    @Autowired
    private TopProcessGroupManager topProcessGroupManager;

    public List<Long> getActivitiesForCorrelationId(String aCorrelationId) {
        final HqlQuery hq = new HqlQuery();
        hq.select("a.id");
        hq.from("DbActivity a");
        hq.where("a.correlationId = :correlationId", "correlationId", aCorrelationId);
        return hq.getResultList(em);
    }

    public TipiActivityInfos getActivityInfos(final long id, final boolean loadVariables) {
        final TipiActivityInfos infos = txTemplate.txWith((s) -> {
            final DbActivity am = activityRepository.findById(id).orElse(null);
            return buildActivityOrProcessInfos(am, loadVariables, true);
        });
        return infos;
    }

    @SuppressWarnings("unchecked")
    public ResultListWithCount<TipiActivityInfos> searchActivities(final TipiCriteria criteria, final int maxHits) {
        final ResultListWithCount<TipiActivityInfos> infos = txTemplate.txWith((s) -> {
            final HqlQuery hq = generateCriteria(criteria);
            final ResultListWithCount<DbActivity> results = hq.getResultListWithCount(em, maxHits);

            final List<TipiActivityInfos> list = new ArrayList<>();
            for (DbActivity am : results.getResult()) {
                list.add(buildActivityOrProcessInfos(am, false, false));
            }
            ResultListWithCount<TipiActivityInfos> l = new ResultListWithCount<TipiActivityInfos>(list, results.getCount());
            return l;

        });
        return infos;
    }

    private HqlQuery generateCriteria(final TipiCriteria criteria) {
        final HqlQuery hq = new HqlQuery();
        hq.select("a");
        hq.from("DbActivity a");
        hq.where("1 = 1 ");

        // Préparation des conditions pour la recherche

        if (criteria.getProcessId() != null) {
            hq.where(" and (a.process.id = :processId or a.id = :processId) ", "processId", criteria.getProcessId());
        }

        if (criteria.getParentId() != null) {
            hq.where(" and a.parent.id = :parentId ", "parentId", criteria.getParentId());
        }

        if (criteria.getId() != null) {
            hq.where(" and a.id = :id ", "id", criteria.getId());
        }

        if (StringUtils.isNotBlank(criteria.getNameOrProcessName())) {
            final String processName = criteria.getNameOrProcessName();
            hq.where(" and a.processName like '%:id' ", "processName", processName);
        }

        if (criteria.getDemandeFinExecution() != null) {
            hq.where(" and a.requestEndExecution = :reqEnd ", "reqEnd", criteria.getDemandeFinExecution());
        }

        if (criteria.getStatesSelectionnes() != null && criteria.getStatesSelectionnes().length >= 1) {
            hq.where(" and a.state in (:state) ", "state", criteria.getStatesSelectionnes());
        }

        if (StringUtils.isNotBlank(criteria.getIdCorrelation())) {
            hq.where(" and a.correlationId = :correlationId ", "correlationId", criteria.getIdCorrelation());
        }

        if (StringUtils.isNotBlank(criteria.getVariableName()) && criteria.getVariableValue() != null) {
            hq.from("join a.variables vars");
            hq.where("and vars.key = :varkey", "varkey", criteria.getVariableName());
            if (criteria.getVariableValue() instanceof Long) {
                hq.where("and vars.longValue = :varvalue", "varvalue", criteria.getVariableValue());
            } else if (criteria.getVariableValue() instanceof String) {
                hq.where("and vars.stringValue = :varvalue", "varvalue", criteria.getVariableValue());
            } else {
                Assert.fail("Other variable types to implement");
            }
        } else if (StringUtils.isNotBlank(criteria.getVariableName())) {
            hq.from("join a.variables vars");
            hq.where("and vars.key = :varkey", "varkey", criteria.getVariableName());
        }

        // On met un ordre par défaut pour être sûr d'avoir à peu près toujours la même liste et on choisit
        // la date de création pour avoir, par défaut, les plus anciens éléments en premier
        hq.order("a.creation");
        return hq;
    }

    private TipiActivityInfos buildActivityOrProcessInfos(DbActivity am, boolean loadVariables,
                                                          boolean gatherChildActivities) {
        if (null == am)
            return null;
        if (am instanceof DbSubProcess) {
            return getRunningProcessInfos((DbSubProcess) am, loadVariables, gatherChildActivities);
        } else {
            final TopProcessMetaModel meta = MetaModelHelper.getTopProcessMeta(am.getFqn());
            return new TipiActivityInfos(am, meta.getDescription(), loadVariables);
        }
    }

    private TipiTopProcessInfos getRunningProcessInfos(DbSubProcess process, boolean loadVariables,
                                                       boolean gatherChildActivities) {
        final TopProcessMetaModel meta = MetaModelHelper.getTopProcessMeta(process.getFqn());
        final TipiTopProcessInfos infos = new TipiTopProcessInfos(process, meta.getDescription(), loadVariables);
        if (process instanceof DbTopProcess) {
            infos.incActivitiesFromState(process.getState(), process.isRequestEndExecution(), process.getNbRetryDone());
        }

        if (gatherChildActivities) {
            // Toutes les activités de ce process
            if (process instanceof DbTopProcess) {
                final long pid = process.getId().longValue();
                updateDateFinExecution(infos, getOlderProcessEndExecution(pid, false));
                infos.incNbActivitesTotal(countActivitiesByState(pid, null, false));
                infos.incNbActivitesInitial(countActivitiesByState(pid, ActivityState.INITIAL, false));
                infos.incNbActivitesExecuting(countActivitiesByState(pid, ActivityState.EXECUTING, false));
                infos.incNbActivitesRetry(countActivitiesRetry(pid, false));
                infos.incNbActivitesAborted(countActivitiesByState(pid, ActivityState.ABORTED, false));
                infos.incNbActivitesFinished(countActivitiesByState(pid, ActivityState.FINISHED, false));
                infos.incNbActivitesError(countActivitiesByState(pid, ActivityState.ERROR, false));
                infos.incNbActivitesWaiting(countActivitiesByState(pid, ActivityState.WAIT_ON_CHILDREN, false));
                infos.incNbActivitesSuspended(countActivitiesByState(pid, ActivityState.SUSPENDED, false));
                infos.incNbActivitesRequestEndExecution(countActivitiesRequestEndExecution(pid, false));
            }
        }
        return infos;
    }

    public Date getOlderProcessEndExecution(final Long processId, final boolean subProcessOnly) {
        final Date infos = txTemplate.txWith((s) -> {
            {
                final HqlQuery hq = new HqlQuery();
                hq.select("count(*)");
                hq.from("DbActivity a");
                addSubProcessExpr(hq, processId, subProcessOnly);
                hq.where(" and a.dateEndExecute is not null");
                List<DbActivity> resultSet = hq.getResultList(em);
                if (resultSet.size() == 1) {
                    // Trouvé un seul
                    return null;
                }
            }
            {
                final HqlQuery hq = new HqlQuery();
                hq.select("count(*)");
                hq.from("DbActivity a");
                addSubProcessExpr(hq, processId, subProcessOnly);
                hq.where(" and a.dateEndExecute = true");
                List<DbActivity> resultSet = hq.getResultList(em);
                if (resultSet.size() == 1) {
                    // Trouvé un seul
                    DbActivity am = resultSet.get(0);
                    return am.getDateEndExecute();
                }
                return null;
            }
        });
        return infos;
    }

    public List<ActivityThreadInfos> getThreadsInfos() {
        return topProcessGroupManager.getThreadsInfos();
    }

    public List<ConnectionCapInfos> getAllConnectionCupInfos() {
        List<ConnectionCapInfos> connCupInfos = new ArrayList<>();
        for (ConnectionCap ct : connectionCapManager.getCaps()) {
            connCupInfos.add(new ConnectionCapInfos(ct, connectionCapManager));
        }
        return connCupInfos;
    }

    @SuppressWarnings("unchecked")
    public ResultListWithCount<TipiTopProcessInfos> getRunningProcesses(final int maxHits) {
        return txTemplate.txWith((s) -> {
            final HqlQuery hq = new HqlQuery();
            hq.from("DbTopProcess p");

            hq.where("p.parent.id is null "); // Process
            hq.where(" and p.state in :state", "state", Arrays.asList(ActivityState.INITIAL, ActivityState.EXECUTING, ActivityState.WAIT_ON_CHILDREN));
            hq.order("creation desc");
            final ResultListWithCount<DbTopProcess> results = hq.getResultListWithCount(em, maxHits);

            final List<TipiTopProcessInfos> list = new ArrayList<>();
            for (DbTopProcess am : results) {
                final TipiTopProcessInfos i = getRunningProcessInfos(am, false, true);
                list.add(i);
            }
            return new ResultListWithCount<>(list, results.getCount());
        });
    }

    @SuppressWarnings("unchecked")
    public ResultListWithCount<TipiTopProcessInfos> getAllProcesses(final int maxHits) {
        final ResultListWithCount<TipiTopProcessInfos> infos = txTemplate.txWith((s) -> {
            final List<TipiTopProcessInfos> list = new ArrayList<>();

            // On fait 2 requetes:
            // - D'abord maxHits des process ABORTED et FINISHED
            // - Ensuite maxHits de tous les processes

            int count = 0;
            // ABORTED et FINISHED
            {
                final HqlQuery hq = new HqlQuery();
                hq.from("DbTopProcess p");
                hq.where("p.parent.id is null");// Process

                hq.where(" and p.state in :state", "state", Arrays.asList(ActivityState.ABORTED, ActivityState.FINISHED));
                hq.order("creation asc");
                ResultListWithCount<DbActivity> results = hq.getResultListWithCount(em, maxHits);
                count += results.getCount();

                for (DbActivity am : results.getResult()) {
                    TipiTopProcessInfos i = getRunningProcessInfos((DbTopProcess) am, false, true);
                    list.add(i);
                }
            }
            // TOUS les autres, le plus récent en premier
            {
                final HqlQuery hq = new HqlQuery();
                hq.from("DbTopProcess p");
                hq.where("p.parent.id is null");// Process
                hq.where(" and p.state NOT in :state", "state", Arrays.asList(ActivityState.ABORTED, ActivityState.FINISHED));
                hq.order("creation desc");
                ResultListWithCount<DbActivity> results = hq.getResultListWithCount(em, maxHits);
                count += results.getCount();

                for (DbActivity am : results.getResult()) {
                    TipiTopProcessInfos i = getRunningProcessInfos((DbTopProcess) am, false, true);
                    list.add((TipiTopProcessInfos) i);
                }
            }
            return new ResultListWithCount<>(list, count);
        });
        return infos;
    }

    public int countActivitiesByState(final Long processId, final ActivityState state,
                                      final boolean subProcessOnly) {
        final int infos = txTemplate.txWith((s) -> {
            final HqlQuery hq = new HqlQuery("count(*)", "DbActivity a");
            addSubProcessExpr(hq, processId, subProcessOnly);
            if (state != null) {
                hq.where(" and state = :state", "state", state);
            }

            final int result = ((Long) hq.getSingleResult(em)).intValue();
            return result;
        });
        return infos;
    }

    public int countActivitiesRequestEndExecution(final Long processId, final boolean subProcessOnly) {
        final int infos = txTemplate.txWith((s) -> {
            final HqlQuery hq = new HqlQuery("count(*)", "DbActivity a");
            addSubProcessExpr(hq, processId, subProcessOnly);
            hq.where(" and a.requestEndExecution = true");
            final int result = ((Long) hq.getSingleResult(em)).intValue();
            return result;
        });
        return infos;
    }

    public int countActivitiesRetry(final Long processId, final boolean subProcessOnly) {
        final int infos = txTemplate.txWith((s) -> {
            final HqlQuery hq = new HqlQuery("count(*)", "DbActivity a");
            addSubProcessExpr(hq, processId, subProcessOnly);
            hq.where(" and a.nbRetryDone > 0");
            final int result = ((Long) hq.getSingleResult(em)).intValue();
            return result;
        });
        return infos;
    }

    private HqlQuery addSubProcessExpr(HqlQuery hq, Long processId, boolean subProcessOnly) {
        if (subProcessOnly) {
            hq.where(" a.parent.id = :parentId ", "parentId", processId);
        } else {
            hq.where(" a.process.id = :processId ", "processId", processId);
        }
        return hq;
    }

    private void updateDateFinExecution(TipiTopProcessInfos aInfos, Date aChildDate) {
        if (null != aInfos.getDateEndExecute()) {
            if ((null != aChildDate) && (aInfos.getDateEndExecute().before(aChildDate))) {
                aInfos.setDateEndExecute(aChildDate);
            }
        }
    }
}
