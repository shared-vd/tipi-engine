package ch.vd.shared.tipi.engine.model;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
// abstract
public abstract class SimpleVariable<T> extends Variable<T> {

	private static final long serialVersionUID = -3284346815946097319L;

	protected T value; // La valeur

	protected SimpleVariable() {
		// Seulement utile a Hibernate en introspection
	}
	public SimpleVariable(String key, T value) {
		super(key);
		this.value = value;
	}

	@Override
	@Transient
	public final T getValue() {
		return value;
	}

}
