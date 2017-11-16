package ch.vd.shared.tipi.engine.model;

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
public abstract class Variable<T> extends TpBaseEntity {

	private static final long serialVersionUID = 1L;

	private String key; //Le nom de la variable
	private ActivityModel owner;

	protected Variable() {
		// Seulement utile a Hibernate en introspection
	}
	public Variable(String key) {
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
	public ActivityModel getOwner() {
		return owner;
	}
	public void setOwner(ActivityModel activity) {
		this.owner = activity;
	}

	@Override
	public String toString() {
		String str = key+"='"+getValue()+"'";
		return super.toString(str);
	}

}
