package ch.sharedvd.tipi.engine.runner;

import ch.sharedvd.tipi.engine.runner.stats.TipiThreadStats;
import ch.sharedvd.tipi.engine.runner.stats.TipiThreadsStats;

import java.util.Date;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TipiThreadPoolExecutor extends ThreadPoolExecutor {
	
	private TipiThreadsStats stats = new TipiThreadsStats();

	public TipiThreadPoolExecutor() {
		super(0, Integer.MAX_VALUE, 1, TimeUnit.HOURS, new SynchronousQueue<Runnable>());
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		final TipiThreadStats s = stats.get(t);
		s.setRunning(true);
		s.setStatus(TipiThreadStats.STATUS_RUNNING);
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		final TipiThreadStats s = stats.get(Thread.currentThread());
		s.setRunning(false);
		if (t != null) {
			s.setStatus(TipiThreadStats.STATUS_NOT_TRAP);
		}
		else {
			s.setStatus(TipiThreadStats.STATUS_FINISHED);
		}
	}

	public void purgeStats() {
		stats.purge();
	}

	public TipiThreadsStats getPoolStats() {
		return stats;
	}

	public void setStatusForThread(String status) {
		final TipiThreadStats s = stats.get(Thread.currentThread());
		s.setStatus(status);
	}
	
	public void initInfosForThread(long id, String name) {
		final TipiThreadStats s = stats.get(Thread.currentThread());
		s.setActivityId(id);
		s.setActivityName(name);
		s.setStartTime(new Date());
	}
	
}
