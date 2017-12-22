package ch.sharedvd.tipi.engine.model;

import javax.persistence.*;

@Entity
@Table(name = "TP_VARIABLE", indexes = {
        @Index(name = "TP_VARIABLE_DTYPE_IDX", columnList = "DTYPE"),
        @Index(name = "TP_VAR_OWNER_FK_IDX", columnList = "OWNER_FK")
})
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING)
public abstract class DbVariable<T> extends DbBaseEntity {

    @Column(name = "KEY", nullable = false)
    private String key; //Le nom de la variable

    @ManyToOne
    @JoinColumn(name = "OWNER_FK", nullable = false)
    private DbActivity owner;

    protected DbVariable() {
        // Seulement utile a Hibernate en introspection
    }

    public DbVariable(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String aKey) {
        this.key = aKey;
    }

    @Transient
    public abstract T getValue();

    public DbActivity getOwner() {
        return owner;
    }
    public void setOwner(DbActivity activity) {
        this.owner = activity;
    }

    @Override
    public String toString() {
        String str = key + "='" + getValue() + "'";
        return super.toString(str);
    }

}
