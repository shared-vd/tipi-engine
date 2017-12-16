package ch.sharedvd.tipi.engine.retry;

public class DefaultRetryPolicy extends AbstractRetryPolicy {

	private static final long serialVersionUID = 1L;
	
	private int maxRetry = 5;

	public DefaultRetryPolicy() {
	}
	
	public DefaultRetryPolicy(int max) {
		maxRetry = max;
	}

	public int getMaxRetry(){
		return maxRetry;
	}
	
	@Override
	public boolean doCanRetry(RetryContext context) {
		return context.getRetryCount() <= maxRetry;
	}

	public void setMaxRetry(int maxRetry) {
		this.maxRetry = maxRetry;
	}

}
