package ch.sharedvd.tipi.engine.engine;

import ch.vd.registre.tipi.client.ActivityThreadInfos;
import ch.vd.registre.tipi.command.MetaModelHelper;
import ch.vd.registre.tipi.meta.TopProcessMetaModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.Assert;

import javax.transaction.TransactionManager;
import java.util.*;


public class TopProcessGroupManager implements Startable {

	public static enum RunReason {
		OK,
		NO_ROOM,
		EXCLUSIVE,
		/**
		 * Indique que le nombre maximal de top-processus pour le job est atteint.
		 */
		NO_TOP_ROOM
	}

	@Autowired
	protected ActivityServiceImpl activityService;
	@Autowired
	protected ConnectionCapManager connectionsCup;

	@Autowired
	@Qualifier("tipiTransactionManager")
	private TransactionManager txManager;

	private boolean stopped = false;

	private final Map<String, TopProcessGroupLauncher> launchers = new HashMap<String, TopProcessGroupLauncher>();

	@Override
	public void start() throws Exception {
		start(false);
	}

	public void start(boolean stopGroups) throws Exception {
		synchronized (launchers) {
			stopped = false;
			launchers.clear();
			
		}
	}
	
	@Override
	public void stop() throws Exception {
		synchronized (launchers) {
			for (TopProcessGroupLauncher l : launchers.values()) {
				l.shutdown();
			}
			stopped = true;
		}
	}

	@Override
	public void destroy() throws Exception {
		stop();
	}

	private Map<String, TopProcessGroupLauncher> getLaunchers() {
		return Collections.unmodifiableMap(launchers);
	}

	/**
	 * Flush tous les caches des launchers
	 */
	public void clearCaches() {
		for (TopProcessGroupLauncher launcher : getLaunchers().values()) {
			launcher.clearCache();
		}
	}

	public int getExecutingThreadsCount() {
		int count = 0;
		for (TopProcessGroupLauncher l : getLaunchers().values()) {
			count += l.getRunningCount();
		}
		return count;
	}

	public void startAllGroups() throws Exception {
		for (TopProcessGroupLauncher launcher : getLaunchers().values()) {
			Assert.notNull(launcher);
			launcher.start();
		}
	}

	public void stopAllGroups() throws Exception {
		for (TopProcessGroupLauncher launcher : getLaunchers().values()) {
			Assert.notNull(launcher);
			launcher.stop();
		}
	}

	public void restart(TopProcessMetaModel topProcess, int nbMax, int priority) throws Exception {
		TopProcessGroupLauncher launcher = getLauncher(topProcess);
		Assert.notNull(launcher);
		launcher.setNbMaxConcurrentActivities(nbMax);
		launcher.setPriority(priority);
		launcher.start();
	}

	public void start(TopProcessMetaModel topProcess) throws Exception {
		TopProcessGroupLauncher launcher = getLauncher(topProcess);
		Assert.notNull(launcher);
		launcher.start();
	}

	public void stop(TopProcessMetaModel topProcess) throws Exception {
		TopProcessGroupLauncher launcher = getLauncher(topProcess);
		Assert.notNull(launcher);
		launcher.stop();
	}

	public TopProcessGroupLauncher getLauncher(String fqn) {
		Assert.notNull(fqn);
		TopProcessGroupLauncher launcher = getLaunchers().get(fqn);
		if (launcher == null) {
			TopProcessMetaModel meta = MetaModelHelper.getTopProcessMeta(fqn);
			launcher = new TopProcessGroupLauncher(meta, txManager, activityService, connectionsCup, true);
			launchers.put(fqn, launcher);
		}
		Assert.notNull(launcher, "Name: "+fqn);
		return launcher;
	}

	public TopProcessGroupLauncher getLauncher(TopProcessMetaModel topProcess) {
		Assert.notNull(topProcess);
		return getLauncher(topProcess.getFQN());
	}

	public List<TopProcessGroupLauncher> getAllGroupLaunchers() {
		final List<TopProcessGroupLauncher> list = new ArrayList<TopProcessGroupLauncher>();
		for (TopProcessGroupLauncher l : getLaunchers().values()) {
			list.add(l);
		}
		return list;
	}

	public boolean hasActivityPending() {
		boolean has = false;
		for (TopProcessGroupLauncher l : getLaunchers().values()) {
			has = has || (l.getRunningCount() > 0);
		}
		return has;
	}

	/**
	 * Vérifie si il y a encore de la place dans le launcher du groupe
	 * @param topProcess
	 * @return
	 */
	public RunReason hasRoom(final TopProcessMetaModel topProcess) {
		Assert.notNull(topProcess);
		// Room dans le groupe
		TopProcessGroupLauncher launcher = getLauncher(topProcess);
		Assert.notNull(launcher);
		if (!launcher.hasTopRoom()) {
			return RunReason.NO_TOP_ROOM;
		}
		if (!launcher.hasRoom()) {
			return RunReason.NO_ROOM;
		}
		return RunReason.OK;
	}

	public List<ActivityThreadInfos> getThreadsInfos() {
		final List<ActivityThreadInfos> threads = new ArrayList<ActivityThreadInfos>();
		for (TopProcessGroupLauncher l : getLaunchers().values()) {
			List<ActivityThreadInfos> ths = l.getThreadsInfos();
			threads.addAll(ths);
		}
		return threads;
	}

	public void setMaxConcurrentActivitiesForGroup(String aGroupName, int nb) {
		getLauncher(aGroupName).setNbMaxConcurrentActivities(nb);
	}

	public void setPriorityForGroup(String aGroupName, int aPrio) {
		getLauncher(aGroupName).setPriority(aPrio);
	}

}
