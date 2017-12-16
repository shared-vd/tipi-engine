package ch.sharedvd.tipi.engine.infos;

import ch.sharedvd.tipi.engine.engine.TopProcessGroupLauncher;

import java.io.Serializable;

public class TopProcessGroupInfos implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private long nbActivitesInitial = 0;
	private long nbActivitesExecuting = 0;
	private long nbActivitesFinished = 0;
	private long nbActivitesSuspended = 0;
	private long nbActivitesWaitChildren = 0;
	private long nbActivitesAborted = 0;
	private long nbActivitesError = 0;
	private long nbActivitesWithReqEnd = 0;
	private long nbActivitesWithRetryCount = 0;

	private int nbMaxConcurrent;
	private int nbActivitesCurrentlyExecuting;
	private boolean started;
	private int priority;

	private String name;
	private String simpleName;

	public TopProcessGroupInfos(String aFqn, String simpleName, TopProcessGroupLauncher launcher) {
		this.name = aFqn;
		this.simpleName = simpleName;

		nbMaxConcurrent = (null == launcher) ? -1 : launcher.getNbMaxConcurrentActivities();
		nbActivitesCurrentlyExecuting = (null == launcher) ? -1 : launcher.getRunningCount();
		started = (null == launcher) ? false : launcher.isStarted();
		priority = (null == launcher) ? -1 : launcher.getPriority();
	}
	
	public String getSimpleName() {
		return simpleName;
	}

	// Info statique du groupe MM
	public String getName() {
		return name;
	}

	// Infos dynamiques du launcher
	public int getNbMaxConcurrent() {
		return nbMaxConcurrent;
	}
	public long getNbActivitesCurrentlyExecuting() {
		return nbActivitesCurrentlyExecuting;
	}
	public boolean isStarted() {
		return started;
	}
	public int getPriority() {
		return priority;
	}


	// Infos dynamiques de la DB
	public long getNbActivitesInitial() {
		return nbActivitesInitial;
	}
	public void setNbActivitesInitial(long nbActivitesInitial) {
		this.nbActivitesInitial = nbActivitesInitial;
	}

	public long getNbActivitesExecuting() {
		return nbActivitesExecuting;
	}
	public void setNbActivitesExecuting(long nbActivitesExecuting) {
		this.nbActivitesExecuting = nbActivitesExecuting;
	}
	
	public long getNbActivitesWithRetryCount() {
		return nbActivitesWithRetryCount;
	}
	public void setNbActivitesWithRetryCount(long nbActivitesWithRetryCount) {
		this.nbActivitesWithRetryCount = nbActivitesWithRetryCount;
	}
	
	public long getNbActivitesFinished() {
		return nbActivitesFinished;
	}
	public void setNbActivitesFinished(long nbActivitesFinished) {
		this.nbActivitesFinished = nbActivitesFinished;
	}
	
	public long getNbActivitesWaitChildren() {
		return nbActivitesWaitChildren;
	}
	public void setNbActivitesWaitChildren(long nbActivitesWaitChildren) {
		this.nbActivitesWaitChildren = nbActivitesWaitChildren;
	}
	
	public long getNbActivitesSuspended() {
		return nbActivitesSuspended;
	}
	public void setNbActivitesSuspended(long nbActivitesSuspended) {
		this.nbActivitesSuspended = nbActivitesSuspended;
	}
	
	public long getNbActivitesAborted() {
		return nbActivitesAborted;
	}
	public void setNbActivitesAborted(long nb) {
		this.nbActivitesAborted = nb;
	}
	
	public long getNbActivitesError() {
		return nbActivitesError;
	}
	public void setNbActivitesError(long nbActivitesError) {
		this.nbActivitesError = nbActivitesError;
	}
	
	public long getNbActivitesWithReqEnd() {
		return nbActivitesWithReqEnd;
	}
	public void setNbActivitesWithReqEnd(long aNbActivitesWithReqEnd) {
		nbActivitesWithReqEnd = aNbActivitesWithReqEnd;
	}

	public void incrementWithData(RunningProcessesData process) {
		nbActivitesError = nbActivitesError + process.getErrorCount();
		nbActivitesExecuting = nbActivitesExecuting + process.getExecutingCount();
		nbActivitesFinished = nbActivitesFinished + process.getFinishCount();
		nbActivitesInitial = nbActivitesInitial + process.getInitialCount();
		nbActivitesAborted = nbActivitesAborted + process.getAbortCount();
		nbActivitesSuspended = nbActivitesSuspended + process.getSuspendedCount();
		nbActivitesWaitChildren = nbActivitesWaitChildren + process.getWaitCount();
		nbActivitesWithReqEnd = nbActivitesWithReqEnd + process.getReqEndCount();
		nbActivitesWithRetryCount = nbActivitesWithRetryCount + process.getRetryCount();
	}

}
