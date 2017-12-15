package ch.sharedvd.tipi.engine.utils;

import java.io.InputStream;
import java.io.Serializable;

public class InputStreamHolder implements Serializable {

	private static final long serialVersionUID = 6633210850174717740L;

	transient final private InputStream inputStream;

	public InputStreamHolder(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public InputStream getInputStream() {
		return inputStream;
	}
}
