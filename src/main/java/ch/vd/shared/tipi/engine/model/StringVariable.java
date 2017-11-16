package ch.vd.shared.tipi.engine.model;

import org.springframework.util.Assert;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("string")
public class StringVariable extends SimpleVariable<String> {
	private static final long serialVersionUID = -4531940641684224000L;

	protected StringVariable() {
		super();
	}

	public StringVariable(String key, String value) {
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
