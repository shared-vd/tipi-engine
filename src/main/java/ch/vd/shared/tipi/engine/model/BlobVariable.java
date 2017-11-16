package ch.vd.shared.tipi.engine.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.sql.Blob;

@Entity
public abstract class BlobVariable<T> extends Variable<T> {

	protected BlobVariable() {
	}

	protected BlobVariable(String key) {
		super(key);
	}

	private Blob blob;

	@Column(name = "BLOB_VALUE")
	public Blob getBlob() {
		return blob;
	}

	public void setBlob(Blob b) {
		blob = b;
	}
}
