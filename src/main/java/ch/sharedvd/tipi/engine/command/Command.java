package ch.sharedvd.tipi.engine.command;

import ch.sharedvd.tipi.engine.engine.*;
import ch.sharedvd.tipi.engine.meta.ActivityMetaModel;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import ch.sharedvd.tipi.engine.model.ActivityState;
import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.sharedvd.tipi.engine.repository.ActivityRepository;
import ch.sharedvd.tipi.engine.utils.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

public abstract class Command {

	private static final Logger LOGGER = LoggerFactory.getLogger(Command.class);

	@Autowired
	protected ActivityRepository activityRepository;

	@Autowired
	protected PlatformTransactionManager txManager;

	@Autowired
	protected TopProcessGroupManager groupManager;
	@Autowired
	protected ConnectionCapManager connectionsCup;
	@Autowired
	protected CommandService commandService;
	@Autowired
	protected CommandHelperService commandHelperService;

	@Autowired
	protected ActivityServiceImpl activityService;

	public abstract void execute();

	protected TopProcessGroupLauncher getLauncher(final TopProcessMetaModel topProcess) {
		TopProcessGroupLauncher launcher = groupManager.getLauncher(topProcess);
		return launcher;
	}

	protected boolean runActivity(DbActivity acti) {
		ActivityMetaModel meta = MetaModelHelper.getMeta(acti.getFqn());
		TopProcessMetaModel group = MetaModelHelper.getTopProcessMeta(acti.getProcessOrThis().getFqn());
		return runActivity(acti, meta, group);
	}

	protected boolean runActivity(final DbActivity aActivity, final ActivityMetaModel meta, final TopProcessMetaModel topProcess) {
		boolean wasRun = false;
		final long id = aActivity.getId();
		final boolean debugEnabled = LOGGER.isDebugEnabled();
        final boolean traceEnabled = LOGGER.isTraceEnabled();

		final TopProcessGroupManager.RunReason reason = groupManager.hasRoom(topProcess);
		if (reason == TopProcessGroupManager.RunReason.OK) {
			if (connectionsCup.hasConnections(meta)) {
				final TopProcessGroupLauncher launcher = getLauncher(topProcess);
				if (!launcher.isRunning(id)) {
					Assert.isEqual(ActivityState.EXECUTING, aActivity.getState());
					ActivityRunner runner = new ActivityRunner(getContext(topProcess), id, meta);
					launcher.startNewThread(runner);
					wasRun = true;

					if (debugEnabled) {
						LOGGER.debug("Activity " + id + " started");
					}
				}
				else {
					if (debugEnabled) {
						LOGGER.debug("Activity " + id + " already running");
					}
				}
			}
			else {
				if (traceEnabled) {
					LOGGER.trace("L'activité " + id + " ne peut pas être démarrée parce que toutes les connections sont occupées");
				}
			}
		}
		else if (reason == TopProcessGroupManager.RunReason.NO_TOP_ROOM) {
			if (debugEnabled) {
				final String message = "Le top-process " + id +
						" ne peut pas être démarré car le nombre maximal de top-process est atteint dans le groupe " + topProcess.getFQN();
				LOGGER.debug(message);
			}
		}
		else if (reason == TopProcessGroupManager.RunReason.NO_ROOM) {
			if (debugEnabled) {
				LOGGER.debug("L'activité " + id + " ne peut pas être démarrée parce que plus de place dans le groupe " + topProcess.getFQN());
			}
		}
		else if (reason == TopProcessGroupManager.RunReason.EXCLUSIVE) {
			if (debugEnabled) {
				LOGGER.debug("L'activité " + id + " ne peut pas être démarrée parce qu'un groupe exclusif tourne deja");
			}
		}
		return wasRun;
	}

	/**
	 * Determine si le système doit ouvrir une TX ou non avant de démarrer la commande
	 * @return
	 */
	public boolean needTransaction() {
		return true;
	}

	@Override
	public String toString() {
		return "Cmd: "+getClass().getSimpleName();
	}

	private ActivityRunnerContext getContext(TopProcessMetaModel topProcess) {
		return new ActivityRunnerContext(activityRepository, commandHelperService, commandService, groupManager, txManager, getLauncher(topProcess));
	}

	public void setActivityRepository(ActivityRepository activityRepository) {
		this.activityRepository = activityRepository;
	}

	public int getPriority() {
		return 100; // Moyen
	}

	public boolean isRemoveSameCommands() {
		return false;
	}
}
