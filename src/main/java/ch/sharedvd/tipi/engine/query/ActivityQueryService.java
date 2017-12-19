package ch.sharedvd.tipi.engine.query;

import ch.sharedvd.tipi.engine.engine.ConnectionCap;
import ch.sharedvd.tipi.engine.engine.ConnectionCapManager;
import ch.sharedvd.tipi.engine.engine.TopProcessGroupManager;
import ch.sharedvd.tipi.engine.infos.*;
import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.sharedvd.tipi.engine.model.DbSubProcess;
import ch.sharedvd.tipi.engine.model.DbTopProcess;
import ch.sharedvd.tipi.engine.repository.ActivityRepository;
import ch.sharedvd.tipi.engine.utils.Assert;
import ch.sharedvd.tipi.engine.utils.ResultListWithCount;
import ch.sharedvd.tipi.engine.utils.TxTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class ActivityQueryService {

    @Autowired
    private TxTemplate txTemplate;
    @Autowired
    private ActivityRepository activityRepository;

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

    private TipiSubProcessInfos getRunningProcessInfos(DbSubProcess process, boolean loadVariables, boolean gatherChildActivities) {

        final TipiSubProcessInfos infos;
        if (process instanceof DbTopProcess) {
            infos = new TipiTopProcessInfos(process, loadVariables);
            ((TipiTopProcessInfos) infos).incActivitiesFromState(process.getState(), process.isRequestEndExecution(),
                    process.getNbRetryDone());
        } else {
            infos = new TipiSubProcessInfos(process, loadVariables);
        }

        if (gatherChildActivities) {
            //Assert.fail("");
            // Tous les subProcess directs
//            updateDateFinExecution(infos, getOlderProcessEndExecution(process.getId().longValue(), true));
//            infos.incNbChildrenTotal(countActivitiesByState(process.getId().longValue(), null, true));
//            infos.incNbChildrenInitial(countActivitiesByState(process.getId().longValue(), ActivityState.INITIAL, true));
//            infos.incNbChildrenExecuting(countActivitiesByState(process.getId().longValue(), ActivityState.EXECUTING, true));
//            infos.incNbChildrenRetry(countActivitiesRetry(process.getId().longValue(), true));
//            infos.incNbChildrenAborted(countActivitiesByState(process.getId().longValue(), ActivityState.ABORTED, true));
//            infos.incNbChildrenFinished(countActivitiesByState(process.getId().longValue(), ActivityState.FINISHED, true));
//            infos.incNbChildrenError(countActivitiesByState(process.getId().longValue(), ActivityState.ERROR, true));
//            infos.incNbChildrenWaiting(countActivitiesByState(process.getId().longValue(), ActivityState.WAIT_ON_CHILDREN, true));
//            infos.incNbChildrenSuspended(countActivitiesByState(process.getId().longValue(), ActivityState.SUSPENDED, true));
//            infos.incNbChildrenRequestEndExecution(countActivitiesRequestEndExecution(process.getId().longValue(), true));
//
//            // Toutes les activités de ce process
//            if (process instanceof DbTopProcess) {
//                TipiTopProcessInfos ttpi = (TipiTopProcessInfos) infos;
//
//                updateDateFinExecution(infos, getOlderProcessEndExecution(process.getId().longValue(), false));
//                ttpi.incNbActivitesTotal(countActivitiesByState(process.getId().longValue(), null, false));
//                ttpi.incNbActivitesInitial(countActivitiesByState(process.getId().longValue(), ActivityState.INITIAL, false));
//                ttpi.incNbActivitesExecuting(countActivitiesByState(process.getId().longValue(), ActivityState.EXECUTING, false));
//                ttpi.incNbActivitesRetry(countActivitiesRetry(process.getId().longValue(), false));
//                ttpi.incNbActivitesAborted(countActivitiesByState(process.getId().longValue(), ActivityState.ABORTED, false));
//                ttpi.incNbActivitesFinished(countActivitiesByState(process.getId().longValue(), ActivityState.FINISHED, false));
//                ttpi.incNbActivitesError(countActivitiesByState(process.getId().longValue(), ActivityState.ERROR, false));
//                ttpi.incNbActivitesWaiting(countActivitiesByState(process.getId().longValue(), ActivityState.WAIT_ON_CHILDREN, false));
//                ttpi.incNbActivitesSuspended(countActivitiesByState(process.getId().longValue(), ActivityState.SUSPENDED, false));
//                ttpi.incNbActivitesRequestEndExecution(countActivitiesRequestEndExecution(process.getId().longValue(), false));
//            }
        }
        return infos;
    }

//    public Date getOlderProcessEndExecution(final Long processId, final boolean subProcessOnly) {
//        final Date infos = txTemplate.txWith((s) -> {
//            Date result = null;
//
//            DbActivityCriteria crit = new DbActivityCriteria();
//            EqualsExpr eqExpr = getSubProcessExpr(crit, processId, subProcessOnly);
//
//            crit.addAndExpression(eqExpr);
//
//            crit.addAndExpression(crit.dateEndExecute().isNotNull());
//            List<DbActivity> resultSet = hqlBuilder.getResultList(DbActivity.class, crit, 1);
//            if (resultSet.size() == 1) {
//                result = null;
//            } else {
//                crit = new DbActivityCriteria();
//                crit.addAndExpression(eqExpr);
//                crit.addOrder(DbActivityProperty.DateEndExecute, true);
//
//                resultSet = hqlBuilder.getResultList(DbActivity.class, crit, 1);
//                if (resultSet.size() == 1) {
//                    DbActivity am = resultSet.get(0);
//                    result = am.getDateEndExecute();
//                }
//            }
//            return result;
//        });
//        return infos;
//    }

//    private EqualsExpr getSubProcessExpr(DbActivityCriteria crit, Long processId, boolean subProcessOnly) {
//        EqualsExpr eqExpr;
//        if (subProcessOnly) {
//            eqExpr = crit.parent__Id().eq(processId);
//        } else {
//            eqExpr = crit.process__Id().eq(processId);
//        }
//        return eqExpr;
//    }
//
//    private void updateDateFinExecution(TipiSubProcessInfos aInfos, Date aChildDate) {
//        if (null != aInfos.getDateEndExecute()) {
//            if ((null != aChildDate) && (aInfos.getDateEndExecute().before(aChildDate))) {
//                aInfos.dateEndExecute = aChildDate;
//            }
//        }
//    }

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
//            TipiSubProcessInfos i = getRunningProcessInfos((DbTopProcess) am, false, false);
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
//                    TipiSubProcessInfos i = getRunningProcessInfos((DbTopProcess) am, false, true);
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
//                    TipiSubProcessInfos i = getRunningProcessInfos((DbTopProcess) am, false, true);
//                    list.add((TipiTopProcessInfos) i);
//                }
//            }
//            return new ResultListWithCount<TipiTopProcessInfos>(list, count);
//        });
//        return infos;
//    }

//    public Long countActivitiesByState(final Long processId, final ActivityState state, final boolean subProcessOnly) {
//        final Long infos = txTemplate.txWith((s) -> {
//            final DbActivityCriteria crit = new DbActivityCriteria();
//            EqualsExpr eqExpr = getSubProcessExpr(crit, processId, subProcessOnly);
//
//            if (state == null) {
//                crit.addAndExpression(eqExpr);
//            } else {
//                crit.addAndExpression(eqExpr, crit.state().eq(state));
//            }
//            crit.activateRowCount();
//            final Long result = (Long) hqlBuilder.getSingleResult(crit);
//            return result;
//        });
//        return infos;
//    }

//    public Long countActivitiesRequestEndExecution(final Long processId, final boolean subProcessOnly) {
//        final Long infos = txTemplate.txWith((s) -> {
//            final DbActivityCriteria crit = new DbActivityCriteria();
//
//            EqualsExpr eqExpr = getSubProcessExpr(crit, processId, subProcessOnly);
//            crit.addAndExpression(eqExpr, crit.requestEndExecution().eq(true));
//            crit.activateRowCount();
//            final Long result = (Long) hqlBuilder.getSingleResult(crit);
//            return result;
//        });
//        return infos;
//    }

//    public Long countActivitiesRetry(final Long processId, final boolean subProcessOnly) {
//        final Long infos = txTemplate.txWith((s) -> {
//            final DbActivityCriteria crit = new DbActivityCriteria();
//
//            EqualsExpr eqExpr = getSubProcessExpr(crit, processId, subProcessOnly);
//            crit.addAndExpression(eqExpr, crit.nbRetryDone().gt(0));
//            crit.activateRowCount();
//            final Long result = (Long) hqlBuilder.getSingleResult(crit);
//            return result;
//        });
//        return infos;
//    }
}
