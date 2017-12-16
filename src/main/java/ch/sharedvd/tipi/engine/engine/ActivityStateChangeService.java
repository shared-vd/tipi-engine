package ch.sharedvd.tipi.engine.engine;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.ErrorActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.SuspendedActivityResultContext;
import ch.sharedvd.tipi.engine.model.ActivityState;
import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.sharedvd.tipi.engine.model.DbSubProcess;
import ch.sharedvd.tipi.engine.utils.Assert;
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
			LOGGER.error("Activity in error: " + aActivity + " / " + ((ErrorActivityResultContext) aResultContext).getErrorMessage());

			aActivity.setState(ActivityState.ERROR);
		}
		else {
			String msg = "Contexte du résultat de l'activité inconnu: " + aResultContext.getClass().getName();
			LOGGER.error(msg);
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
