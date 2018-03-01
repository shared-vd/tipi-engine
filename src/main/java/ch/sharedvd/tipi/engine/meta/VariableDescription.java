package ch.sharedvd.tipi.engine.meta;

import java.io.Serializable;

public class VariableDescription implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String description;

	/**
	 * La valeur par défaut de la variable qui sera utilisée si aucune valeur explicite n'est fournie. Cette valeur peut être nulle.
	 */
	private String defaultValue;

	private VariableType variableType;

	public VariableDescription(String name, VariableType variableType, String descr) {
		this.name = name;
		this.description = descr;
		this.variableType = variableType;
	}

	public VariableDescription(String name, VariableType variableType, String description, String defaultValue) {
		this.name = name;
		this.description = description;
		this.defaultValue = defaultValue;
		this.variableType = variableType;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public VariableType getVariableType() {
		return variableType;
	}

	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public String toString() {
		return name+":"+variableType;
	}
}
