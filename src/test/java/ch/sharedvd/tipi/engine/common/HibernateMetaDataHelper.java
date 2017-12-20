package ch.sharedvd.tipi.engine.common;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HibernateMetaDataHelper {

    public static MetadataImplementor createMetadataImplementor(String dialect, String[] hibernatePackagesToScan) {
        final MetadataSources metadata = new MetadataSources(
                new StandardServiceRegistryBuilder()
                        .applySetting("hibernate.dialect", dialect)
                        .build());

        for (String packageName : hibernatePackagesToScan) {
            for (Class clazz : HibernateMetaDataHelper.getClasses(packageName)) {
                metadata.addAnnotatedClass(clazz);
            }
        }
        return (MetadataImplementor) metadata.buildMetadata();
    }

    /**
     * Retourne les classes d'une package
     *
     * @param packageName
     * @return
     * @throws Exception
     */
    public static List<Class> getClasses(String packageName) {
        final List<Class> list = new ArrayList<>();

        final ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(true);
        scanner.addIncludeFilter(new AssignableTypeFilter(Object.class));
        final Set<BeanDefinition> bds = scanner.findCandidateComponents(packageName);
        try {
            for (BeanDefinition bd : bds) {
                final Class<?> tc = Class.forName(bd.getBeanClassName());
                if (tc.getAnnotation(Entity.class) != null) {
                    list.add(tc);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return list;
    }
}
