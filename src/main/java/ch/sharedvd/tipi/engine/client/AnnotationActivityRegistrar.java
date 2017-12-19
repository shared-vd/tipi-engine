package ch.sharedvd.tipi.engine.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

import java.util.Collection;
import java.util.Set;

/**
 * Registrer of tipi activities using annocations.
 * <p/>
 * Example use:
 * <pre>{@code
 * <bean class="ch.vd.registre.tipi.client.AnnotationActivityRegistrar" lazy-init="false">
 * <property name="aPackage" value="ch.vd.refinf"/>
 * </bean>
 * }</pre>
 *
 * @author rzurcher
 */
public class AnnotationActivityRegistrar extends AbstractRegistrar implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityRegistrar.class);

    private String aPackage;

    private Collection<? extends TypeFilter> excludeFilters;

    /**
     * Spécifie une liste de filtres pour exclure d'éventuels processus de l'enregistrement automatique dans Tipi. Par défaut, tous les processus annotés avec {@link
     * TipiTopProcess} sont automatiquement enregistrés.
     *
     * @param excludeFilters une liste de filtres ou <b>null</b> pour ne rien exclure.
     */
    public void setExcludeFilters(Collection<? extends TypeFilter> excludeFilters) {
        this.excludeFilters = excludeFilters;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        // on recherche toutes les classes concrètes du package à la recherche de celles qui sont annotées 'TipiTopProcess'
        final ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                return beanDefinition.getMetadata().isConcrete();
            }
        };
        scanner.addIncludeFilter(new AnnotationTypeFilter(TipiTopProcess.class));

        if (excludeFilters != null) {
            for (TypeFilter filter : excludeFilters) {
                scanner.addExcludeFilter(filter);
            }
        }

        Set<BeanDefinition> beans = scanner.findCandidateComponents(aPackage);
        LOGGER.info("Registering " + beans.size() + " Tipi activities");
        for (BeanDefinition bean : beans) {
            Class<?> clazz = Class.forName(bean.getBeanClassName());
            registerClass(clazz);
        }
    }

    public void setaPackage(String aPackage) {
        this.aPackage = aPackage;
    }
}
