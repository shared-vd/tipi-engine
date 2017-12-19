package ch.sharedvd.tipi.engine.command.impl;

import ch.sharedvd.tipi.engine.model.ActivityState;
import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.sharedvd.tipi.engine.model.DbSubProcess;
import ch.sharedvd.tipi.engine.model.DbTopProcess;
import ch.sharedvd.tipi.engine.runner.ActivityStateChangeService;
import ch.sharedvd.tipi.engine.runner.TopProcessGroupLauncher;
import ch.sharedvd.tipi.engine.utils.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class EndActivityCommand extends ActivityCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(EndActivityCommand.class);

    public EndActivityCommand(long aid) {
        super(aid);
    }

    @Override
    public void execute() {


        final DbActivity finishedActivity = getModel();
        // Pas supprimée pour une autre raison?
        if (finishedActivity != null) {
            if (!finishedActivity.isRequestEndExecution()) {
                LOGGER.error("Activité " + finishedActivity + " en reqEnd = false!");
                return;
            }

            // Enlève cette activité de la liste des running
            final TopProcessGroupLauncher launcher = getLauncher();
            launcher.removeRunning(getActivityId());

            // Si le *process* est ABORTED, on met l'*activité* a ABORTED
            if (finishedActivity.getProcess() != null && finishedActivity.getProcess().isAborted()) {
                ActivityStateChangeService.aborted(finishedActivity);
                return;
            }

            if (finishedActivity.getState() == ActivityState.ERROR || finishedActivity.getState() == ActivityState.SUSPENDED) {

                // En erreur ou en suspens => on ne fait rien d'autre que de terminer l'activité.
                ActivityStateChangeService.executionEnded(finishedActivity);
            } else {

                final List<DbActivity> finishedActivityChildren = getChildren(finishedActivity);

                // On regarde s'il y a des enfants à démarrer
                final List<DbActivity> finishedActivityLaunchables = getLaunchableChildren(finishedActivityChildren);
                if (!finishedActivityLaunchables.isEmpty()) {

                    // Si oui on démarre les enfants et on a fini notre job...
                    launchActivities(finishedActivityLaunchables);
                    ActivityStateChangeService.waitingOnChildren((DbSubProcess) finishedActivity);

                }
                // On regarde si il y a au moins un enfant non terminé
                else if (isAChildNotFinished(finishedActivityChildren)) {

                    // Si oui on attend sur les enfants (comme d'hab...)
                    Assert.isEqual(ActivityState.WAIT_ON_CHILDREN, finishedActivity.getState());
                    ActivityStateChangeService.waitingOnChildren((DbSubProcess) finishedActivity);
                } else {
                    // L'activité est réellement terminée

                    if (finishedActivity.getState() == ActivityState.WAIT_ON_CHILDREN) {

                        // Un SubProcess sans enfant n'a pas passé par le waitingOnChildren et, donc, n'est
                        // pas correct.
                        if (finishedActivityChildren.isEmpty()) {
                            ActivityStateChangeService.waitingOnChildren((DbSubProcess) finishedActivity);
                        }

                        ActivityStateChangeService.executingAfter(finishedActivity);
                    } else {
                        Assert.isEqual(ActivityState.FINISHED, finishedActivity.getState());

                        ActivityStateChangeService.executionEnded(finishedActivity);

                        if (finishedActivity instanceof DbTopProcess) {

                            final StringBuilder msg = new StringBuilder("fin du process ");
                            msg.append(getMeta().getFQN()).append(" (").append(finishedActivity.getId()).append(") ");

                            // Récup du temps global
                            if (finishedActivity.getDateEndActivity() != null && finishedActivity.getCreationDate() != null) {
                                final long millis = finishedActivity.getDateEndActivity().getTime()
                                        - finishedActivity.getCreationDate().getTime();
                                msg.append(" (Duree: ").append(String.format("%.3f", (millis / 1000.0)) + " [secs]").append(")");
                            }
                            LOGGER.debug(msg.toString());

                            // C'est un top process => on le supprime s'il est terminé et s'il est configuré pour
                            if (getTopProcMeta().isDeleteWhenFinished()) {
                                activityService.deleteProcess((DbTopProcess) finishedActivity);
                            }
                        } else {
                            final DbSubProcess parent = finishedActivity.getParent();
                            Assert.notNull(parent); // Ce n'est pas un TopProcess
                            Assert.isEqual(ActivityState.FINISHED, finishedActivity.getState());

                            // Termine le parent si il faut
                            if (areAllOtherChildrenFinished(parent, finishedActivity)) {
                                ActivityStateChangeService.executingAfter(parent);
                            } else {
                                // Lance les next si il y en a
                                final List<DbActivity> nexts = getNexts(finishedActivity);
                                if (nexts != null && !nexts.isEmpty()) {
                                    launchActivities(nexts);
                                }
                            }
                        }
                    }
                }
            }
            commandService.sendCommand(new RunExecutingActivitiesCommand());
        }
    }

    private void launchActivities(List<DbActivity> aActis) {
        for (DbActivity act : aActis) {
            ActivityStateChangeService.executing(act);
        }
    }

    @SuppressWarnings("unchecked")
    private List<DbActivity> getChildren(DbActivity aAct) {
        final long begin = System.currentTimeMillis();
        List<DbActivity> ch = new ArrayList<>();
        if (aAct instanceof DbSubProcess) {
            ch.addAll(activityRepository.findChildren(aAct));
        }
        if (!ch.isEmpty()) {
            final long diff = System.currentTimeMillis() - begin;
            if (diff > 100 && LOGGER.isDebugEnabled()) {
                LOGGER.debug("Temps: " + diff + "[ms] pour " + ch.size() + " children de " + aAct.getId() + "/" + aAct.getFqn());
            }
        }
        return ch;
    }

    private List<DbActivity> getLaunchableChildren(List<DbActivity> aChildren) {
        List<DbActivity> lch = new ArrayList<DbActivity>();
        for (DbActivity act : aChildren) {
            if ((act.getState() == ActivityState.INITIAL) && (null == act.getPrevious())) {
                lch.add(act);
            }
        }
        return lch;
    }

    private boolean isAChildNotFinished(List<DbActivity> aChildren) {
        boolean notFinished = false;
        for (DbActivity act : aChildren) {
            if (!act.isTerminated()) {
                notFinished = true;
                break;
            }
        }
        return notFinished;
    }

    private boolean areAllOtherChildrenFinished(DbSubProcess aSub, DbActivity finishedActivity) {
        final long begin = System.currentTimeMillis();
        Assert.fail("");

//        DbActivityCriteria crit = new DbActivityCriteria();
//        // Les activités qui sont enfants de SubProcess
//        crit.addAndExpression(crit.parent().eq(aSub),
//                // Nécessaire parce que l'activité courante
//                // n'a pas encore été flushée/committée
//                crit.id().notEquals(finishedActivity.getId()),
//                // Et qui n'ont pas ...
//                Expr.not(
//                        // ... l'état FINISHED ...
//                        Expr.and(crit.state().eq(ActivityState.FINISHED),
//                                // ... et pas de ReqEnd
//                                crit.requestEndExecution().eq(false)
//                        )
//                )
//        );
//        crit.restrictSelect(crit.id());
//        crit.activateRowCount();

//        if (LOGGER.isTraceEnabled()) {
//            LOGGER.trace("HQL: " + crit.buildHqlQuery().getHqlQuery());
//        }
//        final Long notFinished = hqlBuilder.getSingleResult(Long.class, crit);

        final long diff = System.currentTimeMillis() - begin;
        if (diff > 100 && LOGGER.isDebugEnabled()) {
            LOGGER.debug("Temps: " + diff + "[ms] pour savoir s'il reste des children a !FINISHED de " + aSub.getId() + "/" + aSub.getFqn());
        }

        //return notFinished == 0;
        return true;
    }

//	private boolean areAllChildrenFinished_OLD(List<DbActivity> aChildren) {
//		boolean allFinished = true;
//		for (DbActivity act : aChildren) {
//			if (!act.isTerminated()) {
//				allFinished = false;
//				break;
//			}
//		}
//		return allFinished;
//	}

    @SuppressWarnings("unchecked")
    private List<DbActivity> getNexts(DbActivity aAct) {

//        // On prend ceux dont je suis le previous
//        DbActivityCriteria crit = new DbActivityCriteria();
//        crit.addAndExpression(crit.previous().eq(aAct));
//        crit.addAndExpression(crit.state().eq(ActivityState.INITIAL));
//
//        if (LOGGER.isTraceEnabled()) {
//            LOGGER.trace("HQL: " + crit.buildHqlQuery().getHqlQuery());
//        }
//        List<DbActivity> nexts = hqlBuilder.getResultList(crit);
//        return nexts;
        Assert.fail("");
        return null;
    }

}
