package ch.sharedvd.tipi.engine.retry;

import org.apache.commons.lang.exception.ExceptionUtils;

public abstract class AbstractRetryPolicy implements RetryPolicy {
	
	private static final long serialVersionUID = 1L;
	
	@Override
	public final boolean canRetry(RetryContext context) {
		Throwable rootException = ExceptionUtils.getRootCause(context.getThrowable());
		if (rootException instanceof InterruptedException) {
			return false;
		}
		if ((rootException instanceof NullPointerException) || (rootException instanceof IllegalArgumentException)) {
			return context.getRetryCount() < 2;
		}
		return doCanRetry(context);
	}

	public abstract boolean doCanRetry(RetryContext context);

}
