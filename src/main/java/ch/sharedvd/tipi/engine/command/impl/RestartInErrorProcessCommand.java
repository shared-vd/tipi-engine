package ch.sharedvd.tipi.engine.command.impl;

import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.sharedvd.tipi.engine.model.DbSubProcess;
import ch.sharedvd.tipi.engine.runner.ActivityStateChangeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RestartInErrorProcessCommand extends ActivityCommand {

    private Logger LOGGER = LoggerFactory.getLogger(RestartInErrorProcessCommand.class);

    public RestartInErrorProcessCommand(final long id) {
        super(id);
    }

    @Override
    public void execute() {
        final DbActivity aActivity = getModel();
        resume(aActivity);

        if (aActivity instanceof DbSubProcess) {
            final DbSubProcess sub = (DbSubProcess) aActivity;
            List<DbActivity> children = activityRepository.findChildren(sub);
            for (DbActivity child : children) {
                resume(child);
            }
        }
    }

    private boolean resume(DbActivity aActivity) {
        boolean isRes = aActivity.isResumable();
        if (isRes) {
            ActivityStateChangeService.resuming(aActivity);
            LOGGER.info("Resuming activity " + aActivity.getId() + " / " + aActivity.getFqn());
            runActivity(aActivity);
        }
        return isRes;
    }
}
