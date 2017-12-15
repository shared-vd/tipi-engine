package ch.sharedvd.tipi.engine.model;

import org.springframework.util.Assert;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("string")
public class DbStringVariable extends DbSimpleVariable<String> {

	protected DbStringVariable() {
		super();
	}

	public DbStringVariable(String key, String value) {
		super(key, value);
		Assert.isTrue(value.length() <= 2000);
	}

	@Column(name="STRING_VALUE", length=2000)
	public String getStringValue() {
		return value;
	}
	public void setStringValue(String s) {
		value = s;
	}

}
