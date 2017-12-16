package ch.sharedvd.tipi.engine.command.impl;

import ch.vd.registre.tipi.AppLog;
import ch.vd.registre.tipi.command.Command;
import ch.vd.registre.tipi.criteria.DbActivityCriteria;
import ch.vd.registre.tipi.criteria.DbActivityProperty;
import ch.vd.registre.tipi.model.DbActivity;
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
			final DbActivityCriteria crit = new DbActivityCriteria();
			crit.addAndExpression(crit.requestEndExecution().eq(Boolean.TRUE));
			crit.addOrder(DbActivityProperty.Id);

			final List<DbActivity> actis = hqlBuilder.getResultList(crit);
			AppLog.info(LOGGER, "Found {} activities to end properly", actis.size());

			for (final DbActivity act : actis) {
				AppLog.info(LOGGER, "Sending EndActivityCommand for activity " + act.getId());
				commandService.sendCommand(new EndActivityCommand(act.getId()));
			}
		}

		// On envoie un RunExecutingActivities pour toutes celles qui sont en EXECUTING et en ReqEnd=false
		commandService.sendCommand(new RunExecutingActivitiesCommand());
	}

}
