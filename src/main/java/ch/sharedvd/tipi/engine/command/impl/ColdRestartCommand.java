package ch.sharedvd.tipi.engine.command.impl;

import ch.sharedvd.tipi.engine.command.Command;
import ch.sharedvd.tipi.engine.model.DbActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ColdRestartCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(ColdRestartCommand.class);

    @Override
    @SuppressWarnings("unchecked")
    public void execute() {
        LOGGER.info("Cold restart TiPi ...");
        // On récupère toutes les activités qui sont en ReqEnd=true pour les terminer correctement
        {
            final List<DbActivity> actis = activityRepository.findByRequestEndExecutionOrderById(true);
            LOGGER.info("Found {} activities to end properly", actis.size());

            for (final DbActivity act : actis) {
                LOGGER.info("Sending EndActivityCommand for activity " + act.getId());
                commandService.sendCommand(new EndActivityCommand(act.getId()));
            }
        }

        // On envoie un RunExecutingActivities pour toutes celles qui sont en EXECUTING et en ReqEnd=false
        commandService.sendCommand(new RunExecutingActivitiesCommand());
    }

}
