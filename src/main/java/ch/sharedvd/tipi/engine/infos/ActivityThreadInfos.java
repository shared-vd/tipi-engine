package ch.sharedvd.tipi.engine.infos;

import ch.sharedvd.tipi.engine.engine.stats.TipiThreadStats;

import java.io.Serializable;
import java.util.Date;


public class ActivityThreadInfos implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private long threadId;
	private String threadName;

	private Long activityId;
	private String activityName;
	
	private boolean running;
	private String status;
	private Date startTime;
	
	public ActivityThreadInfos(TipiThreadStats s) {
		threadId = s.getId();
		threadName = s.getName();
		activityId = s.getActivityId();
		activityName = s.getActivityName();
		running = s.isRunning();
		status = s.getStatus();
		startTime = s.getStartTime();
	}
	
	public Long getThreadId() {
		return threadId;
	}

	public String getThreadName() {
		return threadName;
	}

	public Long getActivityId() {
		return activityId;
	}

	public String getActivityName() {
		return activityName;
	}

	public boolean isRunning() {
		return running;
	}

	public String getStatus() {
		if (isRunning()) {
			Date now = new Date();
			long diff = now.getTime() - startTime.getTime();
			return String.format("%s : %d[s]", status, (int)(diff/1000.0));
		}
		return status;
	}

}
