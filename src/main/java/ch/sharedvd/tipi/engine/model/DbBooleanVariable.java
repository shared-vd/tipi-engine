package ch.sharedvd.tipi.engine.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;


@Entity
@DiscriminatorValue("boolean")
public class DbBooleanVariable extends DbSimpleVariable<Boolean> {

	private static final long serialVersionUID = -1L;

	protected DbBooleanVariable() {
	}

	public DbBooleanVariable(String key, Boolean value) {
		super(key, value);
	}

	@Column(name="BOOLEAN_VALUE")
	public Boolean getBooleanValue() {
		return value;
	}
	public void setBooleanValue(Boolean b) {
		value = b;
	}

}
