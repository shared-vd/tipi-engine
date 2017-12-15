package ch.sharedvd.tipi.engine.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation utilisée pour décrire un processus Tipi de type Unknown Process
 *
 */
@Target(ElementType.TYPE)
// Applies to classes, interfaces
@Retention(RetentionPolicy.RUNTIME)
// Load all annotations at runtime
public @interface TipiUnknownActivity {

	String description() default "Unknown activity";

}
