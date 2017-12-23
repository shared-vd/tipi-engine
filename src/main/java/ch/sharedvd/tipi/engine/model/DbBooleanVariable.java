package ch.sharedvd.tipi.engine.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue("boolean")
public class DbBooleanVariable extends DbVariable<Boolean> {

    private static final long serialVersionUID = -1L;

    @Column(name = "BOOLEAN_VALUE")
    protected Boolean value; // La valeur

    protected DbBooleanVariable() {
    }

    public DbBooleanVariable(String key, Boolean value) {
        super(key);
        this.value = value;
    }

    public Boolean getBooleanValue() {
        return value;
    }

    public void setBooleanValue(Boolean b) {
        value = b;
    }

    @Transient
    @Override
    public Boolean getValue() {
        return value;
    }
}
