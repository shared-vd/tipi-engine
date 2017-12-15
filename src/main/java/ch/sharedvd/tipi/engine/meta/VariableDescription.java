package ch.sharedvd.tipi.engine.meta;

import java.io.Serializable;

public class VariableDescription implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String description;

	/**
	 * Si <b>vrai</b>, cette variable ne doit être utilisée que dans un environnement de test (et en tout cas pas en production).
	 */
	private boolean testingOnly;

	/**
	 * La valeur par défaut de la variable qui sera utilisée si aucune valeur explicite n'est fournie. Cette valeur peut être nulle.
	 */
	private String defaultValue;

	private VariableType variableType;
	
	/**
	 * @deprecated Utiliser le constructeur ou l'on choisi manuellement le type de variable.
	 * Ce constructeur n'a pas été supprimé afin d'assurer une retrocompatibilité.
	 * @param name
	 * @param descr
	 */
	@Deprecated
	public VariableDescription(String name, String descr) {
		this(name, VariableType.String, descr);
	}
	
	public VariableDescription(String name, VariableType variableType, String descr) {
		this.name = name;
		this.description = descr;
		this.variableType = variableType;
	}

	public VariableDescription(String name, VariableType variableType, String description, boolean testingOnly, String defaultValue) {
		this.name = name;
		this.description = description;
		this.testingOnly = testingOnly;
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

	/**
	 * @return <b>vrai</b> si cette variable ne doit être utilisée que dans un environnement de test (et en tout cas pas en production). Note : le respect de cette limitation est laissé aux
	 *         implémentations des processus Tipi.
	 */
	public boolean isTestingOnly() {
		return testingOnly;
	}

	public void setTestingOnly(boolean testingOnly) {
		this.testingOnly = testingOnly;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
}
