package ch.sharedvd.tipi.engine.engine.stats;

import java.util.Date;


public class TipiThreadStats {

	public static final String STATUS_RUNNING = "Running";
	public static final String STATUS_RETRY = "Retrying";
	public static final String STATUS_WAITING = "Waiting";
	public static final String STATUS_COMMIT = "Commit";
	public static final String STATUS_FINISHED = "Finished";

	public static final String STATUS_EXCEPTION = "Exception";
	public static final String STATUS_NOT_TRAP = "Not trap";
	public static final String STATUS_ABORTED = "Aborted";
	public static final String STATUS_INTERRUPTED = "Interrupted";

	private long id;
	private String name = "NOT SET";
	private boolean running = false;
	private String status = "NOT SET";

	private Long activityId = null;
	private String activityName = "NOT SET";
	private Date startTime;

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public Long getActivityId() {
		return activityId;
	}
	public void setActivityId(Long activityId) {
		this.activityId = activityId;
	}

	public String getActivityName() {
		return activityName;
	}
	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
}
