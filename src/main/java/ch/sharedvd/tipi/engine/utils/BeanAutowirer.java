package ch.sharedvd.tipi.engine.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * Permet d'injecter un bean sur l'annotation @Autowired d'une classe
 * Utile quand on doit autowire un Bean en dehors du contexte spring
 *
 * Peut-être utilisé soit directement (new BeanAutowirer() puis setAppContext()
 * Ou alors comme bean spring avec injection auto du contexte spring
 *
 * Utilisation:
 * ba = new BeanAutowirer();
 * ba.setAppContext(...);
 * ba.autowire(myObject);
 * myObject.xyz();
 *
 * @author jec
 *
 */
public class BeanAutowirer implements ApplicationContextAware {

	private AbstractApplicationContext applicationContext;

	public BeanAutowirer() {
	}
	public BeanAutowirer(AbstractApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = (AbstractApplicationContext)applicationContext;
	}

	public void autowire(Object act) {

		final AutowiredAnnotationBeanPostProcessor proc = new AutowiredAnnotationBeanPostProcessor();
		proc.setBeanFactory(applicationContext.getBeanFactory());
		proc.processInjection(act);

		if (act instanceof ApplicationContextAware) {
			((ApplicationContextAware)act).setApplicationContext(applicationContext);
		}

		if (act instanceof InitializingBean) {
			try {
				((InitializingBean) act).afterPropertiesSet();
			}
			catch (RuntimeException e) {
				throw e;
			}
			catch (Exception e) {
				throw new BeanCreationException("Impossible d'autowirer "+act.getClass(), e);
			}
		}
	}

}
