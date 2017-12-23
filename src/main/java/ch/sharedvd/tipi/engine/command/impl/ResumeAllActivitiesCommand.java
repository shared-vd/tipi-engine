package ch.sharedvd.tipi.engine.command.impl;

import ch.sharedvd.tipi.engine.command.Command;
import ch.sharedvd.tipi.engine.model.ActivityState;
import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.sharedvd.tipi.engine.runner.ActivityStateChangeService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import java.util.List;

public class ResumeAllActivitiesCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResumeAllActivitiesCommand.class);

    @Autowired
    private EntityManager em;

    private ActivityState state;
    private String groupName;

    public ResumeAllActivitiesCommand(ActivityState state) {
        this.state = state;
    }

    public ResumeAllActivitiesCommand(ActivityState state, String gname) {
        this.state = state;
        this.groupName = gname;
    }

    @SuppressWarnings("unchecked")
    public List<DbActivity> getActivities() {
        final List<DbActivity> models;
        if (StringUtils.isNotBlank(groupName)) {
            models = activityRepository.findByGroupAndState(groupName, state);
        } else {
            models = activityRepository.findByState(state);
        }
        return models;
    }

    @Override
    public void execute() {
        final List<DbActivity> models = getActivities();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Trouvé " + models.size() + " activités a resumer");
        }
        for (DbActivity acti : models) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Resuming activity " + acti.getId() + " / " + acti.getFqn());
            }
            ActivityStateChangeService.resuming(acti);
            runActivity(acti);
        }
    }
}
