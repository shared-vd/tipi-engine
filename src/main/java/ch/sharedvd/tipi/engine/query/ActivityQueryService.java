package ch.sharedvd.tipi.engine.query;

import ch.sharedvd.tipi.engine.infos.ActivityThreadInfos;
import ch.sharedvd.tipi.engine.infos.ConnectionCapInfos;
import ch.sharedvd.tipi.engine.infos.TipiActivityInfos;
import ch.sharedvd.tipi.engine.infos.TipiTopProcessInfos;
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
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
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
//        DbActivityCriteria actc = new DbActivityCriteria();
//        actc.addAndExpression(actc.correlationId().eq(aCorrelationId));
//        actc.restrictSelect(DbActivityProperty.Id);
//        return hqlBuilder.getResultList(Long.class, actc);
        Assert.fail("");
        return null;
    }

    public TipiActivityInfos getActivityInfos(final long id, final boolean loadVariables) {
        final TipiActivityInfos infos = txTemplate.txWith((s) -> {
            final DbActivity am = activityRepository.findOne(id);
            return buildActivityOrProcessInfos(am, loadVariables, true);
        });
        return infos;
    }

    @SuppressWarnings("unchecked")
    public ResultListWithCount<TipiActivityInfos> searchActivities(final TipiCriteria criteria, final int maxHits) {
        Assert.fail("");
        return null;
//        final ResultListWithCount<TipiActivityInfos> infos = txTemplate.txWith((s) -> {
//            final DbActivityCriteria crit = generateCriteria(criteria);
//            final ResultListWithCount<DbActivity> results;
//            if (maxHits > 0) {
//                results = hqlBuilder.getResultListWithCount(crit, maxHits);
//            } else {
//                List<DbActivity> r = hqlBuilder.getResultList(crit);
//                results = new ResultListWithCount<DbActivity>(r, r.size());
//                results.setResult(r);
//                results.setCount(r.size());
//            }
//            // log.debug("Actis found: "+results.getCount()+" - "+crit.buildHqlQuery().getHqlQuery());
//
//            final List<TipiActivityInfos> list = new ArrayList<TipiActivityInfos>();
//            for (DbActivity am : results.getResult()) {
//                list.add(buildActivityOrProcessInfos(am, false, false));
//            }
//            ResultListWithCount<TipiActivityInfos> l = new ResultListWithCount<TipiActivityInfos>(list, results.getCount());
//            return l;
//
//        });
//        return infos;
    }

//    public DbActivityCriteria generateCriteria(final TipiCriteria criteria) {
//        final DbActivityCriteria actCriteria = new DbActivityCriteria();
//
//        // Préparation des conditions pour la recherche
//
//        if (criteria.getProcessId() != null) {
//            actCriteria.addAndExpression(Expr.or(actCriteria.process__Id().eq(criteria.getProcessId()),
//                    actCriteria.id().eq(criteria.getProcessId())));
//        }
//
//        if (criteria.getParentId() != null) {
//            actCriteria.addAndExpression(actCriteria.parent__Id().eq(criteria.getParentId()));
//        }
//
//        if (criteria.getId() != null) {
//            actCriteria.addAndExpression(actCriteria.id().eq(criteria.getId()));
//        }
//
//        if (StringUtils.isNotBlank(criteria.getNameOrProcessName())) {
//            String processName = criteria.getNameOrProcessName();
//            actCriteria.addAndExpression(Expr.or(actCriteria.fqn().compare(criteria.getOperatorForNameOrProcessName(), processName),
//                    actCriteria.processName().compare(criteria.getOperatorForNameOrProcessName(), processName)));
//        }
//
//        if (criteria.getDemandeFinExecution() != null) {
//            actCriteria.addAndExpression(actCriteria.requestEndExecution().eq(criteria.getDemandeFinExecution()));
//        }
//
//        if (criteria.getStatesSelectionnes() != null && criteria.getStatesSelectionnes().length >= 1) {
//            actCriteria.addAndExpression(actCriteria.state().in(Arrays.asList(criteria.getStatesSelectionnes())));
//        }
//
//        if (StringUtils.isNotBlank(criteria.getIdCorrelation())) {
//            actCriteria.addAndExpression(actCriteria.correlationId().compare(criteria.getOperatorForIdCorrelation(),
//                    criteria.getIdCorrelation()));
//        }
//
//        if (StringUtils.isNotBlank(criteria.getVariableName()) && criteria.getVariableValue() != null) {
//            VariableCriteria varCrit = actCriteria.newVariablesCriteria(JoinType.INNER);
//
//            EqualsExpr eq = null;
//            if (criteria.getVariableValue() instanceof Long) {
//                eq = varCrit.inheritedLongVariableLongValue().eq(criteria.getVariableValue());
//            }
//            else {
//                Assert.fail("Les autres types : a implementer");
//            }
//            if (null != eq) {
//                varCrit.addAndExpression(eq);
//            }
//        }
//
//        // On met un ordre par défaut pour être sûr d'avoir à peu près toujours la même liste et on choisit
//        // la date de création pour avoir, par défaut, les plus anciens éléments
//        actCriteria.addAscendingOrder(DbActivityProperty.CreationDate);
//        return actCriteria;
//    }

    private TipiActivityInfos buildActivityOrProcessInfos(DbActivity am, boolean loadVariables, boolean gatherChildActivities) {
        if (null == am)
            return null;
        if (am instanceof DbSubProcess) {
            return getRunningProcessInfos((DbSubProcess) am, loadVariables, gatherChildActivities);
        } else {
            return new TipiActivityInfos(am, loadVariables);
        }
    }

    private TipiTopProcessInfos getRunningProcessInfos(DbSubProcess process, boolean loadVariables, boolean gatherChildActivities) {

        final TipiTopProcessInfos infos = new TipiTopProcessInfos(process, loadVariables);
        if (process instanceof DbTopProcess) {
            infos.incActivitiesFromState(process.getState(), process.isRequestEndExecution(), process.getNbRetryDone());
        }

        if (gatherChildActivities) {
            // Tous les subProcess directs
            updateDateFinExecution(infos, getOlderProcessEndExecution(process.getId().longValue(), true));
            infos.incNbChildrenTotal(countActivitiesByState(process.getId().longValue(), null, true));
            infos.incNbChildrenInitial(countActivitiesByState(process.getId().longValue(), ActivityState.INITIAL, true));
            infos.incNbChildrenExecuting(countActivitiesByState(process.getId().longValue(), ActivityState.EXECUTING, true));
            infos.incNbChildrenRetry(countActivitiesRetry(process.getId().longValue(), true));
            infos.incNbChildrenAborted(countActivitiesByState(process.getId().longValue(), ActivityState.ABORTED, true));
            infos.incNbChildrenFinished(countActivitiesByState(process.getId().longValue(), ActivityState.FINISHED, true));
            infos.incNbChildrenError(countActivitiesByState(process.getId().longValue(), ActivityState.ERROR, true));
            infos.incNbChildrenWaiting(countActivitiesByState(process.getId().longValue(), ActivityState.WAIT_ON_CHILDREN, true));
            infos.incNbChildrenSuspended(countActivitiesByState(process.getId().longValue(), ActivityState.SUSPENDED, true));
            infos.incNbChildrenRequestEndExecution(countActivitiesRequestEndExecution(process.getId().longValue(), true));

            // Toutes les activités de ce process
            if (process instanceof DbTopProcess) {
                TipiTopProcessInfos ttpi = infos;

                updateDateFinExecution(infos, getOlderProcessEndExecution(process.getId().longValue(), false));
                ttpi.incNbActivitesTotal(countActivitiesByState(process.getId().longValue(), null, false));
                ttpi.incNbActivitesInitial(countActivitiesByState(process.getId().longValue(), ActivityState.INITIAL, false));
                ttpi.incNbActivitesExecuting(countActivitiesByState(process.getId().longValue(), ActivityState.EXECUTING, false));
                ttpi.incNbActivitesRetry(countActivitiesRetry(process.getId().longValue(), false));
                ttpi.incNbActivitesAborted(countActivitiesByState(process.getId().longValue(), ActivityState.ABORTED, false));
                ttpi.incNbActivitesFinished(countActivitiesByState(process.getId().longValue(), ActivityState.FINISHED, false));
                ttpi.incNbActivitesError(countActivitiesByState(process.getId().longValue(), ActivityState.ERROR, false));
                ttpi.incNbActivitesWaiting(countActivitiesByState(process.getId().longValue(), ActivityState.WAIT_ON_CHILDREN, false));
                ttpi.incNbActivitesSuspended(countActivitiesByState(process.getId().longValue(), ActivityState.SUSPENDED, false));
                ttpi.incNbActivitesRequestEndExecution(countActivitiesRequestEndExecution(process.getId().longValue(), false));
            }
        }
        return infos;
    }

    public Date getOlderProcessEndExecution(final Long processId, final boolean subProcessOnly) {
        final Date infos = txTemplate.txWith((s) -> {
            {
                final HqlQuery hq = new HqlQuery("select count(*) from DbActivity a where ");
                addSubProcessExpr(hq, processId, subProcessOnly);
                hq.append(" and a.dateEndExecute is not null");
                final Query q = hq.createQuery(em);
                List<DbActivity> resultSet = q.getResultList();
                if (resultSet.size() == 1) {
                    // Trouvé un seul
                    return null;
                }
            }
            {
                final HqlQuery hq = new HqlQuery("select count(*) from DbActivity a where ");
                addSubProcessExpr(hq, processId, subProcessOnly);
                hq.append(" and a.dateEndExecute = true");
                final Query q = hq.createQuery(em);
                List<DbActivity> resultSet = q.getResultList();
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

//    private EqualsExpr addSubProcessExpr(DbActivityCriteria crit, Long processId, boolean subProcessOnly) {
//        EqualsExpr eqExpr;
//        if (subProcessOnly) {
//            eqExpr = crit.parent__Id().eq(processId);
//        } else {
//            eqExpr = crit.process__Id().eq(processId);
//        }
//        return eqExpr;
//    }

    private HqlQuery addSubProcessExpr(HqlQuery hq, Long processId, boolean subProcessOnly) {
        if (subProcessOnly) {
            hq.append(" a.parent.id = :parentId ", "parentId", processId);
        } else {
            hq.append(" a.process.id = :processId ", "processId", processId);
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

//    @SuppressWarnings("unchecked")
//    public ResultListWithCount<TipiTopProcessInfos> getRunningProcesses(final int maxHits) {
//        final DbTopProcessCriteria criteria = new DbTopProcessCriteria();
//        criteria.addAndExpression(criteria.parent__Id().isNull()); // Process
//        criteria.addAndExpression(criteria.state().in(ActivityState.INITIAL, ActivityState.EXECUTING,
//                ActivityState.WAIT_ON_CHILDREN));
//        criteria.addDescendingOrder(DbTopProcessProperty.CreationDate);
//        final ResultListWithCount<DbActivity> results = hqlBuilder.getResultListWithCount(criteria, maxHits);
//
//        final List<TipiTopProcessInfos> list = new ArrayList<TipiTopProcessInfos>();
//        for (DbActivity am : results) {
//            TipiTopProcessInfos i = getRunningProcessInfos((DbTopProcess) am, false, false);
//            list.add((TipiTopProcessInfos) i);
//        }
//        return new ResultListWithCount<TipiTopProcessInfos>(list, results.getCount());
//    }

//    @SuppressWarnings("unchecked")
//    public ResultListWithCount<TipiTopProcessInfos> getAllProcesses(final int maxHits) {
//        final ResultListWithCount<TipiTopProcessInfos> infos = txTemplate.txWith((s) -> {
//            final List<TipiTopProcessInfos> list = new ArrayList<TipiTopProcessInfos>();
//
//            // On fait 2 requetes:
//            // - D'abord maxHits des process ABORTED et FINISHED
//            // - Ensuite maxHits de tous les processes
//
//            int count = 0;
//            // ABORTED et FINISHED
//            {
//                final DbTopProcessCriteria criteria = new DbTopProcessCriteria();
//                criteria.addAndExpression(criteria.parent__Id().isNull(), // Process
//                        Expr.or(criteria.state().eq(ActivityState.ABORTED), criteria.state().eq(ActivityState.FINISHED)));
//                criteria.addAscendingOrder(DbTopProcessProperty.CreationDate);
//                ResultListWithCount<DbActivity> results = hqlBuilder.getResultListWithCount(criteria, maxHits);
//                count += results.getCount();
//
//                for (DbActivity am : results.getResult()) {
//                    TipiTopProcessInfos i = getRunningProcessInfos((DbTopProcess) am, false, true);
//                    list.add((TipiTopProcessInfos) i);
//                }
//            }
//            // TOUS les autres, le plus récent en premier
//            {
//                final DbTopProcessCriteria criteria = new DbTopProcessCriteria();
//                criteria.addAndExpression(
//                        criteria.parent__Id().isNull(), // Process
//                        Expr.not(Expr.or(criteria.state().eq(ActivityState.ABORTED), criteria.state()
//                                .eq(ActivityState.FINISHED))));
//                criteria.addDescendingOrder(DbTopProcessProperty.CreationDate);
//                ResultListWithCount<DbActivity> results = hqlBuilder.getResultListWithCount(criteria, maxHits);
//                count += results.getCount();
//
//                for (DbActivity am : results.getResult()) {
//                    TipiTopProcessInfos i = getRunningProcessInfos((DbTopProcess) am, false, true);
//                    list.add((TipiTopProcessInfos) i);
//                }
//            }
//            return new ResultListWithCount<TipiTopProcessInfos>(list, count);
//        });
//        return infos;
//    }

    public Long countActivitiesByState(final Long processId, final ActivityState state, final boolean subProcessOnly) {
        final Long infos = txTemplate.txWith((s) -> {

            final HqlQuery hq = new HqlQuery("select count(*) from DbActivity a where ");
            addSubProcessExpr(hq, processId, subProcessOnly);
            if (state != null) {
                hq.append(" and state = :state", "state", state);
            }

            final Query q = hq.createQuery(em);
            final Long result = (Long) q.getSingleResult();
            return result;
        });
        return infos;
    }

    public Long countActivitiesRequestEndExecution(final Long processId, final boolean subProcessOnly) {
        final Long infos = txTemplate.txWith((s) -> {
            final HqlQuery hq = new HqlQuery("select count(*) from DbActivity a where ");
            addSubProcessExpr(hq, processId, subProcessOnly);
            hq.append(" and a.requestEndExecution = true");
            final Query q = hq.createQuery(em);
            return (Long) q.getSingleResult();
        });
        return infos;
    }

    public Long countActivitiesRetry(final Long processId, final boolean subProcessOnly) {
        final Long infos = txTemplate.txWith((s) -> {
            final HqlQuery hq = new HqlQuery("select count(*) from DbActivity a where ");
            addSubProcessExpr(hq, processId, subProcessOnly);
            hq.append(" and a.nbRetryDone > 0");
            final Query q = hq.createQuery(em);
            return (Long) q.getSingleResult();
        });
        return infos;
    }
}
