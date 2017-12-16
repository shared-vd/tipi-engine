package ch.sharedvd.tipi.engine.command.impl;

import ch.sharedvd.tipi.engine.client.VariableMap;
import ch.sharedvd.tipi.engine.model.DbActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResumeActivityCommand extends ActivityCommand {

	private Logger LOGGER = LoggerFactory.getLogger(ResumeActivityCommand.class);

	private VariableMap vars;

	public ResumeActivityCommand(final long id, final VariableMap vars) {
		super(id);
		this.vars = vars;
	}

	public VariableMap getVars() {
		return vars;
	}

	@Override
	public void execute() {
		final DbActivity aActivity = getModel();

		boolean isRes = aActivity.isResumable();
		if (isRes) {
			ActivityStateChangeService.resuming(aActivity);
			AppLog.info(LOGGER, "Resuming activity " + aActivity.getId() + " / " + aActivity.getFqn());
			runActivity();
		}
		else {
			AppLog.info(LOGGER, "Activity " + aActivity.getId() + " is not in a resumable state (" + aActivity.getState() + ")");
		}
	}

}
