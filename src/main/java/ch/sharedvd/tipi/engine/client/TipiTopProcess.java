package ch.sharedvd.tipi.engine.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation utilisée pour décrire un TopProcess Tipi
 */
@Target(ElementType.TYPE)
// Applies to classes, interfaces
@Retention(RetentionPolicy.RUNTIME)
// Load all annotations at runtime
public @interface TipiTopProcess {

    public String description() default "";

    public boolean showInUi() default true;

    boolean startable() default true;

    boolean deleteWhenFinished() default true;

    int priority() default 100;

    /**
     * @return le nombre maximal de top processus en exécution concurrente.
     */
    int nbMaxTopConcurrent() default -1; // no limit

    /**
     * @return le nombre maximal de processus en exécution concurrente dans toute la hiérarchie des processus (top process + sub processes).
     */
    int nbMaxConcurrent() default -1; // no limit

    TipiVariable[] variables() default {};
}
