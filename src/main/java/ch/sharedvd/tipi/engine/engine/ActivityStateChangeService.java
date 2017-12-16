package ch.sharedvd.tipi.engine.engine;

import ch.vd.registre.base.utils.Assert;
import ch.vd.registre.tipi.AppLog;
import ch.vd.registre.tipi.action.ActivityResultContext;
import ch.vd.registre.tipi.action.ErrorActivityResultContext;
import ch.vd.registre.tipi.action.FinishedActivityResultContext;
import ch.vd.registre.tipi.action.SuspendedActivityResultContext;
import ch.vd.registre.tipi.model.ActivityState;
import ch.vd.registre.tipi.model.DbActivity;
import ch.vd.registre.tipi.model.DbSubProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class ActivityStateChangeService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ActivityStateChangeService.class);

	public static void aborted(DbActivity aActivity) {
		aActivity.setState(ActivityState.ABORTED);
		aActivity.setRequestEndExecution(false);
	}

	public static void executingFirstActivity(DbActivity aActivity) {
		executing(aActivity);
	}

	public static void executing(DbActivity aActivity) {
		Assert.isTrue(aActivity.getState() == ActivityState.INITIAL, "State impossible: " + aActivity.getState() + " Activity: "
				+ aActivity.getId());
		Assert.isFalse(aActivity.isRequestEndExecution(), "Erreur : ReqEnd="+aActivity.isRequestEndExecution()+" Id=" + aActivity.getId());

		aActivity.setState(ActivityState.EXECUTING);
		aActivity.setDateStartExecute(null);
		aActivity.setDateEndExecute(null);
		aActivity.setDateEndActivity(null);
		if (aActivity instanceof DbSubProcess) {
			((DbSubProcess) aActivity).setDateStartTerminate(null);
			((DbSubProcess) aActivity).setDateEndTerminate(null);
		}

		aActivity.setCallstack(null);
	}

	public static void executingAfter(DbActivity aActivity) {
		Assert.isTrue(aActivity.getState() == ActivityState.WAIT_ON_CHILDREN, "State impossible: " + aActivity.getState() + " Activity: "
				+ aActivity.getId());
		Assert.isTrue(((DbSubProcess) aActivity).isExecuted(), "Id: " + aActivity.getId());
		Assert.isFalse(aActivity.isRequestEndExecution(), "Erreur : ReqEnd="+aActivity.isRequestEndExecution()+" Id=" + aActivity.getId());

		aActivity.setState(ActivityState.EXECUTING);
		aActivity.setCallstack(null);
	}

	public static void resuming(DbActivity aActivity) {
		Assert.isFalse(aActivity.isRequestEndExecution(), "Erreur : ReqEnd="+aActivity.isRequestEndExecution()+" Id=" + aActivity.getId());
		aActivity.setState(ActivityState.EXECUTING);
		aActivity.setCallstack(null);
		aActivity.setNbRetryDone(0);
	}

	public static void waitingOnChildren(DbSubProcess aSubProc) {
		Assert.isTrue(aSubProc.getState() == ActivityState.WAIT_ON_CHILDREN || aSubProc.getState() == ActivityState.ERROR,
				"State impossible: " + aSubProc.getState() + " Activity: " + aSubProc.getId());
		Assert.isTrue(aSubProc.isRequestEndExecution(), "Erreur : ReqEnd="+aSubProc.isRequestEndExecution()+" Id=" + aSubProc.getId());

		aSubProc.setRequestEndExecution(false);
	}

	public static void runnerFinished(DbActivity aActivity, ActivityResultContext aResultContext) {
		Assert.isTrue(aActivity.getState() == ActivityState.EXECUTING
				, "State impossible: " + aActivity.getState() + " Activity: " + aActivity.getId());
		Assert.isFalse(aActivity.isRequestEndExecution(), "Erreur : ReqEnd="+aActivity.isRequestEndExecution()+" Id=" + aActivity.getId());

		aActivity.setRequestEndExecution(true);

		if (aResultContext instanceof FinishedActivityResultContext) {
			if (aActivity instanceof DbSubProcess) {
				if (((DbSubProcess) aActivity).isExecuted()) {
					aActivity.setState(ActivityState.FINISHED);
					aActivity.setDateEndActivity(new Date());
				} else {
					((DbSubProcess) aActivity).setExecuted(true);
					aActivity.setState(ActivityState.WAIT_ON_CHILDREN);
				}
			} else {
				aActivity.setState(ActivityState.FINISHED);
				aActivity.setDateEndActivity(new Date());
			}
		}
		else if (aResultContext instanceof SuspendedActivityResultContext) {
			aActivity.setCorrelationId(((SuspendedActivityResultContext) aResultContext).getCorrelationId());
			aActivity.setState(ActivityState.SUSPENDED);
		}
		else if (aResultContext instanceof ErrorActivityResultContext) {
			AppLog.error(LOGGER, "Activity in error: " + aActivity + " / " + ((ErrorActivityResultContext) aResultContext).getErrorMessage());

			aActivity.setState(ActivityState.ERROR);
		}
		else {
			String msg = "Contexte du résultat de l'activité inconnu: " + aResultContext.getClass().getName();
			AppLog.error(LOGGER, msg);
			throw new RuntimeException(msg);
		}
	}

	public static void executionEnded(DbActivity aActivity) {
		Assert.isTrue(	aActivity.getState() == ActivityState.FINISHED
						||
						aActivity.getState() == ActivityState.SUSPENDED
						||
						aActivity.getState() == ActivityState.ERROR
				, "State impossible: " + aActivity.getState() + " Activity: " + aActivity.getId());
		Assert.isTrue(aActivity.isRequestEndExecution(), "Erreur: ReqEnd="+aActivity.isRequestEndExecution()+" Id=" + aActivity.getId());

		aActivity.setRequestEndExecution(false);
	}
}
