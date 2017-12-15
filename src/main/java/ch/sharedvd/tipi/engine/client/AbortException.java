package ch.sharedvd.tipi.engine.client;

import ch.sharedvd.tipi.engine.utils.Assert;

public class AbortException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private AbortType abortType;

	public AbortException(AbortType type) {
		Assert.notNull(type);
		abortType = type;
	}

	public AbortType getAbortType() {
		return abortType;
	}

	public enum AbortType {
		INTERRUPTED,
		ABORTED
	}
}
