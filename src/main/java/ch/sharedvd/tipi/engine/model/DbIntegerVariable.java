package ch.sharedvd.tipi.engine.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue("int")
public class DbIntegerVariable extends DbSimpleVariable<Integer> {

	private static final long serialVersionUID = -3784790510891895361L;

	protected DbIntegerVariable() {
	}

	public DbIntegerVariable(String key, Integer value) {
		super(key, value);
	}

	/**
	 * Nécessaire pour que Hibernate crée une colonne de type LONG et pas INT (mutualisation avec DbLongVariable)
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
