package ch.sharedvd.tipi.engine.model;

import org.hibernate.annotations.Index;

import javax.persistence.*;

@Entity
@Table(name="TP_VARIABLE")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
	name="DTYPE",
	discriminatorType=DiscriminatorType.STRING
)
@org.hibernate.annotations.Table(appliesTo = "TP_VARIABLE", indexes = {
		@Index(name="TP_VARIABLE_DTYPE_IDX", columnNames="DTYPE")

})
public abstract class DbVariable<T> extends DbBaseEntity {

	private String key; //Le nom de la variable
	private DbActivity owner;

	protected DbVariable() {
		// Seulement utile a Hibernate en introspection
	}
	public DbVariable(String key) {
		this.key = key;
	}

	@Column(name="KEY", nullable=false)
	public String getKey() {
		return key;
	}
	public void setKey(String aKey) {
		this.key = aKey;
	}

	@Transient
	public abstract T getValue();

	@ManyToOne
	@JoinColumn(name="OWNER_FK", nullable = false)
	@Index(name="TP_VAR_OWNER_FK_IDX")
	public DbActivity getOwner() {
		return owner;
	}
	public void setOwner(DbActivity activity) {
		this.owner = activity;
	}

	@Override
	public String toString() {
		String str = key+"='"+getValue()+"'";
		return super.toString(str);
	}

}
