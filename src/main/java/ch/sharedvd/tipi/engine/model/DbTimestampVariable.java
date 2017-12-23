package ch.sharedvd.tipi.engine.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.Date;


@Entity
@DiscriminatorValue("timestamp")
public class DbTimestampVariable extends DbVariable<Date> {

    @Column(name = "TIMESTAMP_VALUE")
    protected Date timeValue; // La valeur

    protected DbTimestampVariable() {
        super();
    }

    public DbTimestampVariable(String key, Date value) {
        super(key);
        this.timeValue = value;
    }

    public Date getTimeValue() {
        return timeValue;
    }

    public void setTimeValue(Date t) {
        timeValue = t;
    }

    @Transient
    @Override
    public Date getValue() {
        return null;
    }
}
