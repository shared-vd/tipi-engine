package ch.sharedvd.tipi.engine.client;

import ch.sharedvd.tipi.engine.meta.VariableType;

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
     * La valeur par défaut de la variable qui sera utilisée si aucune valeur explicite n'est fournie. Une valeur vide signifie "pas de valeur par défaut".
     */
    String defaultValue() default "";

    boolean required() default false;
}
