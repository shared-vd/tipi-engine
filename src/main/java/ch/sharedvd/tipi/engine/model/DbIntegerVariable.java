package ch.sharedvd.tipi.engine.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue("int")
public class DbIntegerVariable extends DbVariable<Integer> {

    private static final long serialVersionUID = -3784790510891895361L;

    @Column(name = "INT_VALUE")
    protected Integer intValue; // La valeur

    protected DbIntegerVariable() {
    }

    public DbIntegerVariable(String key, Integer value) {
        super(key);
        this.intValue = value;
    }

    @Transient
    @Override
    public Integer getValue() {
        return intValue;
    }

    public Integer getIntValue() {
        return intValue;
    }

    public void setIntValue(Integer i) {
        intValue = i;
    }

}
