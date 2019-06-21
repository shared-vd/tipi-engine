package ch.sharedvd.tipi.engine.command.impl;

import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.sharedvd.tipi.engine.runner.ActivityStateChangeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnsuspendActivityCommand extends ActivityCommand {

    private Logger LOGGER = LoggerFactory.getLogger(UnsuspendActivityCommand.class);

    public UnsuspendActivityCommand(final long id) {
        super(id);
    }

    @Override
    public void execute() {
        final DbActivity aActivity = getModel();

        boolean isRes = aActivity.isResumable();
        if (isRes) {
            ActivityStateChangeService.resuming(aActivity);
            LOGGER.info("Resuming activity " + aActivity.getId() + " / " + aActivity.getFqn());
            runActivity();
        } else {
            LOGGER.info("Activity " + aActivity.getId() + " is not in a resumable state (" + aActivity.getState() + ")");
        }
    }
}
