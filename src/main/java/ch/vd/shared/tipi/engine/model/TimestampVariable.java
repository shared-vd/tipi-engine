package ch.vd.shared.tipi.engine.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Date;


@Entity
@DiscriminatorValue("timestamp")
public class TimestampVariable extends SimpleVariable<Date> {
	private static final long serialVersionUID = 3264744898823904986L;

	protected TimestampVariable() {
		super();
	}

	public TimestampVariable(String key, Date value) {
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
