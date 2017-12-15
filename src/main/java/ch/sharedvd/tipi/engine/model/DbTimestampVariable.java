package ch.sharedvd.tipi.engine.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Date;


@Entity
@DiscriminatorValue("timestamp")
public class DbTimestampVariable extends DbSimpleVariable<Date> {

	protected DbTimestampVariable() {
		super();
	}

	public DbTimestampVariable(String key, Date value) {
		super(key, value);
	}

	@Column(name="TIMESTAMP_VALUE")
	public Date getTimeValue() {
		return value;
	}
	public void setTimeValue(Date t) {
		value = t;
	}

}
