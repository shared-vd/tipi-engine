package ch.sharedvd.tipi.engine.model;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
// abstract
public abstract class DbSimpleVariable<T> extends DbVariable<T> {

	private static final long serialVersionUID = -3284346815946097319L;

	protected T value; // La valeur

	protected DbSimpleVariable() {
		// Seulement utile a Hibernate en introspection
	}
	public DbSimpleVariable(String key, T value) {
		super(key);
		this.value = value;
	}

	@Override
	@Transient
	public final T getValue() {
		return value;
	}

}
