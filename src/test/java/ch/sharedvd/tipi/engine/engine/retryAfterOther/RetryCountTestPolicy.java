package ch.sharedvd.tipi.engine.engine.retryAfterOther;

import ch.sharedvd.tipi.engine.retry.RetryContext;
import ch.sharedvd.tipi.engine.retry.RetryPolicy;

public class RetryCountTestPolicy implements RetryPolicy {

    @Override
    public boolean canRetry(RetryContext context) {
        return context.getRetryCount() < 5;
    }

}
