package ch.sharedvd.tipi.engine.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue("long")
public class DbLongVariable extends DbVariable<Long> {

    private static final long serialVersionUID = -2831380707734145336L;

    @Column(name = "LONG_VALUE")
    protected Long longValue; // La valeur

    protected DbLongVariable() {
    }

    public DbLongVariable(String key, Long value) {
        super(key);
        this.longValue = value;
    }

    public Long getLongValue() {
        return longValue;
    }

    public void setLongValue(Long v) {
        longValue = v;
    }

    @Transient
    @Override
    public Long getValue() {
        return longValue;
    }
}
