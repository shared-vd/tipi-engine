package ch.sharedvd.tipi.engine.client;

import org.springframework.beans.factory.InitializingBean;

public class ActivityRegistrar extends AbstractRegistrar implements InitializingBean {

    //private static final Logger LOGGER = LoggerFactory.getLogger(ActivityRegistrar.class);

    private Class<?> clazz;
    private Class<?>[] classes;

    @Override
    public void afterPropertiesSet() throws Exception {

        if (clazz != null) {
            registerClass(clazz);
        }
        if (classes != null) {
            for (Class<?> c : classes) {
                registerClass(c);
            }
        }
    }

    public void setClasses(Class<?>[] classes) {
        this.classes = classes;
    }

    public void setClass(Class<?> clazz) {
        this.clazz = clazz;
    }

}
