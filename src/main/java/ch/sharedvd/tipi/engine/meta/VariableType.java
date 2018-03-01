package ch.sharedvd.tipi.engine.meta;

import ch.sharedvd.tipi.engine.utils.ArrayLong;
import org.springframework.util.Assert;

import java.io.Serializable;

public enum VariableType {

    String(java.lang.String.class),
    ArrayLong(ArrayLong.class),
    Integer(Integer.class),
    Long(Long.class),
    Boolean(Boolean.class),
    LocalDate(java.time.LocalDate.class),
    Date(java.util.Date.class),
    Serializable(java.io.Serializable.class);

    private Class<? extends Serializable> clazz;

    private VariableType(Class<? extends Serializable> clazz) {
        Assert.notNull(clazz, "Null class found");
        this.clazz = clazz;
    }

    public Class<? extends Serializable> getClazz() {
        return clazz;
    }

    public boolean isCompatible(Serializable value) {
        return clazz.isAssignableFrom(value.getClass());
    }
}
