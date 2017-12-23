package ch.sharedvd.tipi.engine.model;

import org.springframework.util.Assert;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue("string")
public class DbStringVariable extends DbVariable<String> {

    @Column(name = "STRING_VALUE", length = 2000)
    protected String stringValue; // La valeur

    protected DbStringVariable() {
        super();
    }

    public DbStringVariable(String key, String value) {
        super(key);
        this.stringValue = value;
        Assert.isTrue(value.length() <= 2000);
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String s) {
        stringValue = s;
    }

    @Override
    @Transient
    public final String getValue() {
        return stringValue;
    }

}
