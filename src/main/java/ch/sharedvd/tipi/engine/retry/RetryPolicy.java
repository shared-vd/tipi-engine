package ch.sharedvd.tipi.engine.retry;

import java.io.Serializable;

public interface RetryPolicy extends Serializable {

	public boolean canRetry(RetryContext context);

}
