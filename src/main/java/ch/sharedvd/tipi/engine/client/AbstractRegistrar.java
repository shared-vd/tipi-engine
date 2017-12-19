package ch.sharedvd.tipi.engine.client;

import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.command.MetaModelHelper;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import ch.sharedvd.tipi.engine.registry.TipiRegistry;
import ch.sharedvd.tipi.engine.utils.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Classe abstraite pour l'enregistrement des TopProcess soit au moyen des annotations, soit au moyen d'un membre static (meta ou META)
 *
 * @author rzurcher
 */
public abstract class AbstractRegistrar {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRegistrar.class);

    @Autowired
    private TipiRegistry registry;

    protected void registerClass(Class<?> c) throws Exception {

        boolean registered = false;
        // Meta ?
        try {
            TopProcessMetaModel meta = null;
            if (c.getAnnotation(TipiTopProcess.class) != null) {
                if (!TopProcess.class.isAssignableFrom(c)) {
                    throw new IllegalArgumentException("La classe " + c.getSimpleName()
                            + " est annotée avec @TipiTopProcess mais n'hérite pas de TopProcess");
                }
                meta = MetaModelHelper.getTopProcessMetaModel(c);
            } else {
                meta = (TopProcessMetaModel) MetaModelHelper.getMetaFromStaticField(c);
            }

            if (meta != null) {
                registry.register(meta);

                registered = true;
            }
        } catch (Exception e) {
            LOGGER.error("Impossible d'enregistrer: " + c.getSimpleName(), e);
            throw e;
        }
        Assert.isTrue(registered, "Impossible d'enregistrer: " + c.getSimpleName());
    }

    public TipiRegistry getRegistry() {
        return registry;
    }

    public void setRegistry(TipiRegistry registry) {
        this.registry = registry;
    }

}
