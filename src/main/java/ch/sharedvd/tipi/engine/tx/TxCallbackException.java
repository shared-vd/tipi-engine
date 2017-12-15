package ch.sharedvd.tipi.engine.tx;

public class TxCallbackException extends RuntimeException {

	private static final long serialVersionUID = -186013776810807208L;

	public TxCallbackException(Throwable e) {
		super(e);
	}

}
