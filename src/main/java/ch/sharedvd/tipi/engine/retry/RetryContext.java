package ch.sharedvd.tipi.engine.retry;

public class RetryContext {

	private final long retryCount;
	private final Throwable throwable;
	private long durationBeforeException;

	public RetryContext(long retryCount, Throwable throwable, long durationBeforeException) {
		this.retryCount = retryCount;
		this.throwable = throwable;
		this.durationBeforeException = durationBeforeException;
	}

	public long getRetryCount() {
		return retryCount;
	}

	public Throwable getThrowable() {
		return throwable;
	}
	
	public long getDurationBeforeException() {
		return durationBeforeException;
	}

}
