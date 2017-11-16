package ch.vd.shared.tipi.engine.client;

import ch.vd.shared.tipi.engine.meta.VariableType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation utilisée pour définire une variable Tipi
 */
@Target(ElementType.TYPE)
// Applies to classes, interfaces
@Retention(RetentionPolicy.RUNTIME)
// Load all annotations at runtime
public @interface TipiVariable {

	String name();

	String description() default "";

	VariableType type();

	/**
	 * @return <b>vrai</b> si cette variable ne doit être utilisée que dans un environnement de test (et en tout cas pas en production). Note : le respect de cette limitation est laissé aux
	 *         implémentations des processus Tipi.
	 */
	boolean testingOnly() default false;

	/**
	 * La valeur par défaut de la variable qui sera utilisée si aucune valeur explicite n'est fournie. Une valeur vide signifie "pas de valeur par défaut".
	 */
	String defaultValue() default "";
}
