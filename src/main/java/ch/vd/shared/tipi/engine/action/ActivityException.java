package ch.vd.shared.tipi.engine.action;

public class ActivityException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 4783249387415463296L;

	public ActivityException() {
		super();
	}

	public ActivityException(String message, Throwable cause) {
		super(message, cause);
	}

	public ActivityException(String message) {
		super(message);
	}

	public ActivityException(Throwable cause) {
		super(cause);
	}

}
