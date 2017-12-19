package ch.sharedvd.tipi.engine.model;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@MappedSuperclass
public class DbBaseEntity implements Serializable {

	private static final long serialVersionUID = 6734035143688829890L;

	private Long id;
	private Integer version;
	private Date creation;

	public DbBaseEntity() {
		creation = new Date();
	}

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tipiEntitySeqGen")
	@SequenceGenerator(name = "tipiEntitySeqGen", sequenceName = "TP_SEQ")
	@Column(name = "ID")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Version
	@Column(name = "OPTLOCK")
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer aVersion) {
		this.version = aVersion;
	}

	@Column(name = "DATE_CREATION")
	@Temporal(TemporalType.TIMESTAMP)
	public Date getCreationDate() {
		return creation;
	}

	public void setCreationDate(Date aCreation) {
		this.creation = aCreation;
	}

	@Override
	public int hashCode() {
		return (null == getId()) ? 0 : getId().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DbBaseEntity other = (DbBaseEntity) obj;
		if (getId() == null) {
			return false;
		}
		else if (other.getId() == null) {
			return false;
		}
		else {
			return getId().equals(other.getId());
		}
	}

	@Override
	public String toString() {
		return toString("");
	}

	protected String toString(String suffix) {
		StringBuilder str = new StringBuilder();
		str.append(getClass().getSimpleName());
		str.append("(").append("id=").append(getId());
		if (StringUtils.isNotBlank(suffix)) {
			str.append(",").append(suffix);
		}
		str.append(")");
		if (getCreationDate() == null) {
			str.append("/new");
		}
		return str.toString();
	}

}
