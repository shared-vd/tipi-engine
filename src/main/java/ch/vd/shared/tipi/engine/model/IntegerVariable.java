package ch.vd.shared.tipi.engine.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue("int")
public class IntegerVariable extends SimpleVariable<Integer> {

	private static final long serialVersionUID = -3784790510891895361L;

	protected IntegerVariable() {
	}

	public IntegerVariable(String key, Integer value) {
		super(key, value);
	}

	/**
	 * Nécessaire pour que Hibernate crée une colonne de type LONG et pas INT (mutualisation avec LongVariable)
	 */
	@Column(name="LONG_VALUE")
	public Long getLongValue() {
		return (long)value;
	}
	public void setLongValue(Long v) {
		if (v != null) {
			value = v.intValue();
		}
		else {
			value = null;
		}
	}
	
	@Transient
	public Integer getIntValue() {
		return value;
	}
	@Transient
	public void setIntValue(Integer i) {
		value = i;
	}

}
