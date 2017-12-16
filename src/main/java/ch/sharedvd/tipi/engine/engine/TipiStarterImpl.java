package ch.sharedvd.tipi.engine.engine;

import ch.sharedvd.tipi.engine.command.CommandConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.Assert;

public class TipiStarterImpl implements TipiStarter, Startable, InitializingBean, DisposableBean, ApplicationContextAware, ApplicationListener<ApplicationEvent> {

	private static final Logger LOGGER = LoggerFactory.getLogger(TipiStarterImpl.class);

	@Autowired
	private TopProcessGroupManager activityGroupManager;
	@Autowired
	private CommandConsumer commandConsumer;
	@Autowired
	protected ConnectionCapManager connectionCapManager;

	private String tipiContext = "TBD";

	private ApplicationContext applicationContext;

	private Boolean autostart;

	private boolean alreadyStartedBySpring = false;

	@Override
	public void onApplicationEvent(ApplicationEvent applicationEvent) {
		if (!alreadyStartedBySpring && applicationEvent instanceof ContextRefreshedEvent &&
				// il arrive qu'on reçoive des événements de 'refresh context' qui correspondent à des contextes Spring différents de celui sur lequel le starter
				// est défini (par exemple, un sous-contexte tel que défini dans la classe BrokerFactoryBean de ActiveMQ). Dans le cas où le context est différent,
				// il ne faut pas démarrer tipi.
				((ContextRefreshedEvent) applicationEvent).getApplicationContext() == applicationContext) {

			if (autostart == null || autostart) {
				boolean pauseGroups = false;

				String value = System.getProperty("tipi.pause");
				if (value != null && "true".equals(value)) {
					pauseGroups = true;

					LOGGER.warn("TiPi démarré en mode pause (-Dtipi.pause=true)");
				}
				else {
					LOGGER.debug("TiPi démarré normalement (-Dtipi.pause=false)");
				}
				try {
					start(pauseGroups);
					alreadyStartedBySpring = true;
				}
				catch (RuntimeException e) {
					throw e;
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		checkJvmVersion();
	}

	private void checkJvmVersion() {
		// Il y a un bug dans la JVM avant la version 1.6.0_30 qui empêche TiPi de bien fonctionner
		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7014263
		// On vérifie donc que la JVM soit plus récente que cette version
		//
		// Problème : Tipi génére des exceptions NegativeArraySizeException dans la ArrayBlockingQueue

		String version = System.getProperty("java.version");
		//version = "1.6.0_20";
		if (version.startsWith("1.6")) {
			int cmp = version.compareTo("1.6.0_30");
			if (cmp < 0) {
				LOGGER.error("Mauvaise version de la JVM " + version + " : incompatible avec TiPi (version minimale: 1.6.0_30)");
				Assert.isTrue(cmp >= 0, "Mauvaise version de la JVM: " + version);
			}
		}

		LOGGER.info("Version de la JVM " + version + " compatible avec TiPi (version minimale: 1.6.0_30)");
	}

	@Override
	public void destroy() throws Exception {
		LOGGER.info("Destroy de TiPi - " + tipiContext);
		commandConsumer.destroy();
		activityGroupManager.destroy();
	}

	@Override
	public boolean isStarted() throws Exception {
		return !commandConsumer.isStopped();
	}

	@Override
	public void start() throws Exception {
		start(false);
	}

	public void start(boolean pauseGroups) throws Exception {
		LOGGER.info("Démarrage de TiPi - " + tipiContext);
		activityGroupManager.start(pauseGroups);
		commandConsumer.start();
	}

	@Override
	public void stop() throws Exception {
		LOGGER.info("Arret de TiPi - " + tipiContext);
		activityGroupManager.stop();
		commandConsumer.destroy();
	}

	public void setAutostart(Boolean autostart) {
		this.autostart = autostart;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
