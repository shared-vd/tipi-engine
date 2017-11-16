package ch.vd.shared.tipi.engine.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("long")
public class LongVariable extends SimpleVariable<Long> {

	private static final long serialVersionUID = -2831380707734145336L;

	protected LongVariable() {
	}

	public LongVariable(String key, Long value) {
		super(key, value);
	}

	@Column(name="LONG_VALUE")
	public Long getLongValue() {
		return value;
	}
	public void setLongValue(Long v) {
		value = v;
	}

}
