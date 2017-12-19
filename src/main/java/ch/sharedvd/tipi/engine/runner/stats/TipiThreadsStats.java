package ch.sharedvd.tipi.engine.runner.stats;

import ch.sharedvd.tipi.engine.utils.Assert;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TipiThreadsStats implements Iterable<TipiThreadStats> {
	
	private Map<Long, TipiThreadStats> threads = new HashMap<Long, TipiThreadStats>();

	public TipiThreadStats get(Thread th) {
		TipiThreadStats ts = threads.get(th.getId());
		if (ts == null) {
			ts = new TipiThreadStats();
			ts.setId(th.getId());
			ts.setName(th.getName());
			threads.put(th.getId(), ts);
		}
		Assert.isEqual(ts.getName(), th.getName());
		return ts;
	}
	
	public void purge() {
		Map<Thread, StackTraceElement[]> stackTraces = Thread.getAllStackTraces();
		
		Map<Long, TipiThreadStats> newList = new HashMap<Long, TipiThreadStats>();
		for (Thread thread : stackTraces.keySet()) {
			TipiThreadStats s = threads.get(thread.getId());
			if (s != null) {
				newList.put(thread.getId(), s);
			}
		}
		threads = newList;
	}
	
	@Override
	public Iterator<TipiThreadStats> iterator() {
		return threads.values().iterator();
	}

}
