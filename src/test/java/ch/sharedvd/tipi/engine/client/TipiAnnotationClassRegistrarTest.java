package ch.vd.registre.tipi.client;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.client.AnnotationActivityRegistrar;
import ch.sharedvd.tipi.engine.client.TipiTopProcess;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import ch.sharedvd.tipi.engine.registry.TipiRegistry;
import ch.sharedvd.tipi.engine.registry.TipiRegistryImpl;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TipiAnnotationClassRegistrarTest {

    @TipiTopProcess(description = "AnnotedTopProcess1", showInUi = false, deleteWhenFinished = false, priority = 5,
            nbMaxTopConcurrent = 1, nbMaxConcurrent = 4, startable = false)
    public class AnnotedTopProcess1 extends TopProcess {

        @Override
        protected ActivityResultContext execute() throws Exception {
            return new FinishedActivityResultContext();
        }
    }

    @TipiTopProcess(description = "AnnotedTopProcess2", showInUi = false, deleteWhenFinished = false, priority = 5,
            nbMaxTopConcurrent = 1, nbMaxConcurrent = 4, startable = false)
    public class AnnotedTopProcess2 extends TopProcess {

        @Override
        protected ActivityResultContext execute() throws Exception {
            return new FinishedActivityResultContext();
        }
    }

    @Test
    public void testAnnotationRegistration() throws Exception {

        TipiRegistry registry = new TipiRegistryImpl();
        AnnotationActivityRegistrar registrar = new AnnotationActivityRegistrar();
        registrar.setaPackage("ch.vd.registre.tipi.client");
        registrar.setRegistry(registry);
        registrar.afterPropertiesSet();

        List<TopProcessMetaModel> topProcessesMeta = registry.getAllTopProcesses();
        Assert.assertEquals(2, topProcessesMeta.size());

        Collections.sort(topProcessesMeta, new Comparator<TopProcessMetaModel>() {
            @Override
            public int compare(TopProcessMetaModel o1, TopProcessMetaModel o2) {
                return o1.getSimpleName().compareTo(o2.getSimpleName());
            }
        });

        TopProcessMetaModel meta0 = topProcessesMeta.get(0);
        Assert.assertEquals("AnnotedTopProcess1", meta0.getDescription());
        Assert.assertEquals(AnnotedTopProcess1.class.getSimpleName(), meta0.getSimpleName());
        Assert.assertEquals(false, meta0.isShownInUI());
        Assert.assertEquals(false, meta0.isDeleteWhenFinished());
        Assert.assertEquals(5, meta0.getPriority());
        Assert.assertEquals(1, meta0.getNbMaxTopConcurrent());
        Assert.assertEquals(4, meta0.getNbMaxConcurrent());
        Assert.assertEquals(false, meta0.isStartable());

        TopProcessMetaModel meta1 = topProcessesMeta.get(1);
        Assert.assertEquals("AnnotedTopProcess2", meta1.getDescription());
    }

    /**
     * Ce test vérifie que la propriété <i>excludeFilters</i> fonctionne bien et que les filtres spécifiés permettent d'exclure certain process de l'enregistrement
     * automatique.
     */
    @Test
    public void testExcludeFilters() throws Exception {

        TipiRegistry registry = new TipiRegistryImpl();
        AnnotationActivityRegistrar registrar = new AnnotationActivityRegistrar();
        registrar.setaPackage("ch.vd.registre.tipi.client");
        registrar.setRegistry(registry);
        registrar.setExcludeFilters(Arrays.asList(new AssignableTypeFilter(AnnotedTopProcess2.class)));
        registrar.afterPropertiesSet();

        // le AnnotedTopProcess2 devrait être exclu
        List<TopProcessMetaModel> topProcessesMeta = registry.getAllTopProcesses();
        Assert.assertEquals(1, topProcessesMeta.size());
        TopProcessMetaModel meta = topProcessesMeta.get(0);
        Assert.assertEquals("AnnotedTopProcess1", meta.getDescription());
        Assert.assertEquals(AnnotedTopProcess1.class.getSimpleName(), meta.getSimpleName());
        Assert.assertEquals(false, meta.isShownInUI());
        Assert.assertEquals(false, meta.isDeleteWhenFinished());
        Assert.assertEquals(5, meta.getPriority());
        Assert.assertEquals(1, meta.getNbMaxTopConcurrent());
        Assert.assertEquals(4, meta.getNbMaxConcurrent());
        Assert.assertEquals(false, meta.isStartable());
    }

}
