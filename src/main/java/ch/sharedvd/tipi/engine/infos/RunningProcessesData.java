package ch.sharedvd.tipi.engine.infos;

import ch.sharedvd.tipi.engine.model.ActivityState;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class RunningProcessesData implements Serializable {

	private Long topProcessId;
	private ActivityState state;
	private String time;
	private String fqn;
	private String simpleName;
	
	private int activitiesCount;
	private int retryCount;
	private int waitCount;
	private int errorCount;
	private int abortCount;
	private int initialCount;
	private int finishCount;
	private int executingCount;
	private int suspendedCount;
	private int reqEndCount;
	private Date dateEndActivity;
	
	public void setTopProcessId(Long topProcessId) {
		this.topProcessId = topProcessId;
	}
	
	public void setState(ActivityState state) {
		this.state = state;
	}
	
	public void setTime(String time) {
		this.time = time;
	}
	
	public ActivityState getState() {
		return state;
	}
	public String getTime() {
		return time;
	}
	
	public void setFqn(String fqn) {
		this.fqn = fqn;
	}
	
	public String getFqn() {
		return fqn;
	}
	
	public void setTopProcessName(String simpleName) {
		this.simpleName = simpleName;
	}
	
	public String getSimpleName() {
		return simpleName;
	}
	
	public void incActivitiesCount() {
		activitiesCount++;
	}
	
	public int getActivitiesCount() {
		return activitiesCount;
	}
	
	public void incRetryCount() {
		retryCount++;
	}
	
	public int getRetryCount() {
		return retryCount;
	}
	
	public Long getTopProcessId() {
		return topProcessId;
	}
	public void incWaitCount() {
		waitCount++;
	}
	public int getWaitCount() {
		return waitCount;
	}
	
	public int getAbortCount() {
		return abortCount;
	}
	
	public void incAbortCount() {
		abortCount++;
	}
	
	public int getErrorCount() {
		return errorCount;
	}
	
	public void incErrorCount() {
		errorCount++;
	}
	
	public int getFinishCount() {
		return finishCount;
	}
	
	public void incFinishCount() {
		finishCount++;
	}
	
	public int getInitialCount() {
		return initialCount;
	}
	
	public void incInitialCount() {
		initialCount++;
	}
	
	public int getExecutingCount() {
		return executingCount;
	}
	public void incExecutingCount() {
		executingCount++;
	}
	public int getSuspendedCount() {
		return suspendedCount;
	}
	public void incSuspendedCount() {
		suspendedCount++;
	}
	
	public int getReqEndCount() {
		return reqEndCount;
	}
	
	public void incReqEndCount() {
		reqEndCount++;
	}

	public void setDateEnd(Date dateEndActivity) {
		this.dateEndActivity = dateEndActivity;
	}
	
	public Date getDateEndActivity() {
		return dateEndActivity;
	}
	
}
