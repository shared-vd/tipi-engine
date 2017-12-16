package ch.sharedvd.tipi.engine.command.impl;

import ch.sharedvd.tipi.engine.command.Command;
import ch.sharedvd.tipi.engine.command.MetaModelHelper;
import ch.sharedvd.tipi.engine.engine.TopProcessGroupLauncher;
import ch.sharedvd.tipi.engine.engine.TopProcessGroupManager;
import ch.sharedvd.tipi.engine.meta.ActivityMetaModel;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import ch.sharedvd.tipi.engine.model.DbActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RunExecutingActivitiesCommand extends Command {

	private static final Logger LOGGER = LoggerFactory.getLogger(RunExecutingActivitiesCommand.class);

	@Override
	public void execute() {

		final long begin = System.currentTimeMillis();

		removeOtherSameCommands();

		int nbActivitiesStarted = 0;
		int nbGroups = 0;

		// Seulement les groupes qui ont au moins une activité à exécuter
		final List<String> tpNames = activityService.getTopProcessNamesWithExecutingActivities();
		sortTopProcessesByPriority(tpNames);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Groupes triés: " + tpNames);
		}

		for (String grpName : tpNames) {
			final TopProcessMetaModel tp = MetaModelHelper.getTopProcessMeta(grpName);
			Assert.notNull(tp, "Le process " + grpName + " n'a pas été trouvé");
			// Seulement les groupes qui ont de la place
			TopProcessGroupManager.RunReason reason = groupManager.hasRoom(tp);
			if (reason == TopProcessGroupManager.RunReason.OK) {
				nbActivitiesStarted += launchForTopProcess(tp);
				nbGroups++;
			}
		}

		final long diff = System.currentTimeMillis() - begin;

		if (LOGGER.isDebugEnabled()) {
			//TODO DGO registry existe plus	LOGGER.debug("Places dans " + nbGroups + " / " + registry.getAllTopProcesses().size() + " groupes. Started: " + nbActivitiesStarted + String.format(" (Duree: %.3f [secs])", (diff / 1000.0)));
		}
	}

	private void sortTopProcessesByPriority(List<String> names) {
		Collections.sort(names, new Comparator<String>() {
			@Override
			public int compare(String name1, String name2) {
				TopProcessGroupLauncher tp1 = groupManager.getLauncher(name1);
				TopProcessGroupLauncher tp2 = groupManager.getLauncher(name2);
				if (tp1 == null) {
					return -1;
				}
				if (tp2 == null) {
					return 1;
				}
				if (tp1.getPriority() == tp2.getPriority()) {
					return tp1.getTopProcessMetaModel().getFQN().compareTo(tp2.getTopProcessMetaModel().getFQN());
				}
				return tp1.getPriority() - tp2.getPriority();
			}
		});
	}

	private int launchForTopProcess(final TopProcessMetaModel topProcess) {

		final TopProcessGroupLauncher launcher = groupManager.getLauncher(topProcess.getFQN());
		Assert.notNull(launcher);

		final List<DbActivity> nexts = launcher.getNextReadyActivities();
		if (LOGGER.isDebugEnabled()) {
			StringBuilder str = new StringBuilder("Ready activities returned: [");
			for (DbActivity a : nexts) {
				str.append("{").append(a.getId()).append(",").append(a.getFqn()).append("},");
			}
			str.append("]");
			LOGGER.debug(str.toString());
		}

		int nbStarted = 0;
		if (nexts.size() > 0) {
			for (DbActivity act : nexts) {
				final ActivityMetaModel meta = MetaModelHelper.getMeta(act.getFqn());
				if (meta != null) {
					// Ce cas peut arriver si on des activités en base alors qu'on a supprimé
					// les process/activités dans le code
					if (runActivity(act, meta, topProcess)) {
						nbStarted++;
					}
				}
				else {
					LOGGER.error("Impossible de trouver le meta pour l'activité : " + act.getFqn());
				}
			}
		}

		return nbStarted;
	}

	/**
	 * Supprime les RunExecutingCommand qui sont dans la queue
	 */
	private void removeOtherSameCommands() {
		commandService.removeCommandOfClass(getClass());
	}

}
