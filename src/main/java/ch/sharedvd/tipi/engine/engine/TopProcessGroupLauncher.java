package ch.sharedvd.tipi.engine.engine;

import ch.vd.registre.base.utils.Assert;
import ch.vd.registre.tipi.client.ActivityThreadInfos;
import ch.vd.registre.tipi.engine.stats.TipiThreadStats;
import ch.vd.registre.tipi.meta.TopProcessMetaModel;
import ch.vd.registre.tipi.model.DbActivity;
import org.apache.log4j.Logger;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TopProcessGroupLauncher {

	private static final Logger LOGGER = Logger.getLogger(TopProcessGroupLauncher.class);

	// public pour que les UNIT TESTS puissent changer cette valeur
	public static int CACHE_SIZE = 1000;

	protected ActivityServiceImpl activityService;
	protected ConnectionCapManager connectionsCup;

	private TransactionManager txManager;

	private List<DbActivity> readyActivities = new ArrayList<>();
	private TopProcessMetaModel topProcess;
	private boolean groupStarted = true;
	private final AtomicBoolean groupShutdown = new AtomicBoolean(false);
	private TipiThreadPoolExecutor executor = new TipiThreadPoolExecutor();
	private final Set<Long> runningTopActivities = new HashSet<>();
	private final Set<Long> runningActivities = new HashSet<>();
	private final int nbMaxTopConcurrentActivities;
	private int nbMaxConcurrentActivities;
	private int priority;

	public TopProcessGroupLauncher(final TopProcessMetaModel topProcess, final TransactionManager txManager,
			final ActivityServiceImpl activityService, final ConnectionCapManager connectionsCup, boolean startGroup) {
		Assert.notNull(topProcess);
		Assert.notNull(txManager);
		Assert.notNull(activityService);
		Assert.notNull(connectionsCup);

		this.topProcess = topProcess;
		this.txManager = txManager;
		this.activityService = activityService;
		this.connectionsCup = connectionsCup;
		this.groupStarted = startGroup;

		final ThreadGroup threadGroup = new ThreadGroup("TG-"+topProcess.getFQN());
		nbMaxTopConcurrentActivities = topProcess.getNbMaxTopConcurrent();
		nbMaxConcurrentActivities = topProcess.getNbMaxConcurrent();
		priority = topProcess.getPriority();
		Assert.isTrue(priority > 0);

		executor.setThreadFactory(new ThreadFactory() {
			private final AtomicInteger threadNumber = new AtomicInteger(1);

			@Override
			public Thread newThread(Runnable r) {
				StringBuilder name = new StringBuilder("AT-");
				name.append(topProcess.getSimpleName()).append("-").append(String.format("%02d", threadNumber.getAndIncrement()));
				Thread t = new Thread(threadGroup, r, name.toString(), 0);
				t.setDaemon(true); // Comme ça, elle se termine toute seule
				t.setPriority(Thread.NORM_PRIORITY);
				return t;
			}
		});
	}

	public void clearCache() {
		readyActivities = new ArrayList<>();
	}

	private int getNbMaxStartableActivities() {
		int nbrMax  = connectionsCup.getAvailableConnections(topProcess);
		if (nbMaxConcurrentActivities < 0){
			return nbrMax;
		}
		return Math.min(nbrMax, nbMaxConcurrentActivities);
	}

	public List<DbActivity> getNextReadyActivities() {

		if (readyActivities.size() == 0) {
			populateCache();
		}

		final int nbActivitiesReady = readyActivities.size();
		int nbMax = getNbMaxStartableActivities();
		final int toIndex = (nbMax < 0) || (readyActivities.size() < nbMax) ? readyActivities.size() : nbMax;
		// On renvoie la première partie de la liste
		final List<DbActivity> toRetList = readyActivities.subList(0, toIndex);
		// Puis on enlève cette première partie de la liste cached
		readyActivities = readyActivities.subList(toIndex, readyActivities.size());
		Assert.isEqual(nbActivitiesReady, readyActivities.size() + toRetList.size(), "Non egal!");

		if (toRetList.size() > 0) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Returning "+toRetList.size()+" activities for group "+topProcess.getFQN()+". The cache still contains "+readyActivities.size()+" activities ready to run");
			}
		}

		if (!readyActivities.isEmpty()) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Le cache du groupe "+topProcess.getFQN()+" contient encore "+readyActivities.size()+" activites ready to run");
			}
		}

		return toRetList;
	}

	private void populateCache() {
		Assert.notNull(runningActivities);
		Assert.isEqual(0, readyActivities.size());

		// On prends au moins autant d'activités qu'il y a d'activités running pour qu'on en ait au moins 2 a runner a la fin
		int cacheSize = CACHE_SIZE;
		if (cacheSize < runningActivities.size()) {
			cacheSize = runningActivities.size() + 2;
		}

		final List<DbActivity> executingActivities = activityService.getExecutingActivities(topProcess.getFQN(), runningActivities, cacheSize);
		if (LOGGER.isDebugEnabled()) {
			//log.trace("HQL: "+crit.buildHqlQuery().getHqlQuery());
			LOGGER.debug("Found "+executingActivities.size()+" activities ready to run for group "+topProcess.getFQN());
		}

		// Enleve celles qui sont en train de runner
		readyActivities = new ArrayList<>();
		for (DbActivity act : executingActivities) {
			if (!runningActivities.contains(act.getId())) {
				readyActivities.add(act);
			}
		}
	}

	public void shutdown() {
		if (!groupShutdown.get()) {
			synchronized (groupShutdown) {
				executor.shutdownNow();
				groupShutdown.set(true);
			}
		}
	}

	public void start() {
		groupStarted = true;
	}
	public void stop() {
		groupStarted = false;
	}
	public final boolean isStarted() {
		return !groupShutdown.get() && groupStarted;
	}

	public boolean removeRunning(long id) {
		synchronized (runningActivities) {
			connectionsCup.remove(id);
			runningTopActivities.remove(id);
			return runningActivities.remove(id);
		}
	}
	public int getRunningCount() {
		synchronized (runningActivities) {
			return runningActivities.size();
		}
	}

	/**
	 * @return <b>vrai</b> s'il est possible de démarrer un nouveau top processus; <b>faux</b> autrement.
	 */
	public boolean hasTopRoom() {
		if (groupShutdown.get()) {
			return false;
		}
		if (nbMaxTopConcurrentActivities == -1) {
			// pas de limite
			return true;
		}
		synchronized (runningTopActivities) {
			if (runningTopActivities.size() < nbMaxTopConcurrentActivities) {
				return true;
			}
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(
					"Pas de place pour démarrer un nouveau top-process: " + topProcess.getFQN() + ". Il y a déjà " + runningTopActivities.size() + " top-processus.");
		}
		return false;
	}

	/**
	 * @return <b>vrai</b> s'il est possible de démarrer un nouveau processus (top ou sub); <b>faux</b> autrement.
	 */
	public boolean hasRoom() {
		if (!groupShutdown.get()) {
			synchronized (runningActivities) {
				if ((nbMaxConcurrentActivities < 0) || (runningActivities.size() < nbMaxConcurrentActivities)) {
					return true;
				}
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Pas de place dans le groupe: "+topProcess.getFQN()+". Il y a deja "+runningActivities.size()+" activités.");
			}
		}
		return false;
	}

	public boolean isRunning(long id) {
		synchronized (runningActivities) {
			return runningActivities.contains(id);
		}
	}

	public boolean startNewThread(ActivityRunner runner) {
		boolean wasStarted = false;
		if (isStarted()) {
			synchronized (runningActivities) {
				Assert.isFalse(runningActivities.contains(runner.getActivityId()));
				runningActivities.add(runner.getActivityId());
				connectionsCup.add(runner.getActivityName(), runner.getActivityId());

				// TX Synchro
				try {
					ThreadStarterSynchronization synchro = new ThreadStarterSynchronization(runner);
					Transaction tx = txManager.getTransaction();
					tx.registerSynchronization(synchro);
					wasStarted = true;
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			if (runner.isTopActivity()) {
				synchronized (runningTopActivities) {
					runningTopActivities.add(runner.getActivityId());
				}
			}
		}
		return wasStarted;
	}

	private void startNewThreadDeffered(ActivityRunner runner) {
		synchronized (groupShutdown) {
			if (!groupShutdown.get()) {
				executor.execute(runner);
			}
		}
	}

	private class ThreadStarterSynchronization implements Synchronization {

		private ActivityRunner runner;

		ThreadStarterSynchronization(ActivityRunner runner) {
			this.runner = runner;
		}

		@Override
		public void beforeCompletion() {
		}
		@Override
		public void afterCompletion(int aStatus) {
			if (Status.STATUS_COMMITTED == aStatus) {
				startNewThreadDeffered(runner);
			}
			else if (Status.STATUS_ROLLEDBACK == aStatus) {
				removeRunning(runner.getActivityId());
			}
			else {
				Assert.fail("Impossible.");
			}
		}
	}

	public TopProcessMetaModel getTopProcessMetaModel() {
		return topProcess;
	}

	public void setStatusForThread(String status) {
		executor.setStatusForThread(status);
	}

	public void initInfosForThread(ActivityRunner activity) {
		executor.initInfosForThread(activity.getActivityId(), activity.getActivityName());
	}

	public List<ActivityThreadInfos> getThreadsInfos() {

		final List<ActivityThreadInfos> threads = new ArrayList<>();

		// Supprime les threads qui n'existent plus des stats
		executor.purgeStats();

		for (TipiThreadStats s : executor.getPoolStats()) {
			threads.add(new ActivityThreadInfos(s));
		}
		return threads;
	}

	public int getNbMaxConcurrentActivities() {
		return nbMaxConcurrentActivities;
	}
	public void setNbMaxConcurrentActivities(int nbMaxConcurrentActivities) {
		this.nbMaxConcurrentActivities = nbMaxConcurrentActivities;
	}
	public void setPriority(int prio) {
		priority = prio;
	}
	public int getPriority() {
		return priority;
	}

}
