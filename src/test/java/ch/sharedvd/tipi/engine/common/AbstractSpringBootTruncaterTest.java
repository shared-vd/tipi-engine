package ch.sharedvd.tipi.engine.common;

import ch.qos.logback.classic.Level;
import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.sharedvd.tipi.engine.utils.TxTemplate;
import org.hibernate.boot.spi.MetadataImplementor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

public abstract class AbstractSpringBootTruncaterTest implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(AbstractSpringBootTruncaterTest.class);

    private boolean onSetUpWasRun = false;
    private HibernateMetaDataTruncater truncater;
    private ApplicationContext applicationContext;

    private DataSource dataSource;
    protected abstract TxTemplate getTxTemplate();

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * Set the {@link ApplicationContext} to be used by this test instance,
     * provided via {@link ApplicationContextAware} semantics.
     */
    @Override
    public final void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        afterContextInitialization();
    }

    // To be overriden by sub-classes
    protected void afterContextInitialization() {
        this.dataSource = applicationContext.getBean(DataSource.class);
        final Environment env = applicationContext.getBean(Environment.class);
        final String dialect = env.getRequiredProperty("spring.jpa.database-platform");
        final String[] hibernatePackagesToScan = new String[]{DbActivity.class.getPackage().getName()};
        final MetadataImplementor metadataImplementor = HibernateMetaDataHelper.createMetadataImplementor(dialect, hibernatePackagesToScan);
        truncater = new HibernateMetaDataTruncater(dataSource, metadataImplementor);
    }

    /**
     * S'assure que le onSetUp() n'est appelé qu'une fois, qu'on soit dans une
     * méthode transactionnelle ou non
     *
     * @throws Exception
     */
    protected final void runOnSetUpOnlyOnce() throws Exception {
        if (!onSetUpWasRun) {
            Assert.assertTrue("Pour être sûr que mvn test exécute les tests, il faut que le nom de la classe se termine par Test, ou IT ou UT.",
                    getClass().getSimpleName().endsWith("Test") ||
                            getClass().getSimpleName().endsWith("IT") ||
                            getClass().getSimpleName().endsWith("UT")
            );

            onSetUpWasRun = true;
            onSetUp();
            afterOnSetUp();
        }
    }

    protected void onSetUp() throws Exception {
    }

    protected void onTearDown() throws Exception {
    }

    @Before
    public final void beforeMethod() throws Exception {
        runOnSetUpOnlyOnce();
    }

    @After
    public final void afterMethod() throws Exception {
        onTearDown();
    }

    // Can be overriden by sub-classes
    protected void afterOnSetUp() throws Exception {
        try {
            getTxTemplate().txWithout((s) -> {
                truncateDatabase();
            });
            log.info("Database truncated");

            getTxTemplate().txWithout((s) -> {
                beforeDoLoadDatabase();
            });

            // Load database
            getTxTemplate().txWithout((s) -> {
                doLoadDatabase();
            });
            log.debug("Database loaded");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Can be overriden
    protected void doLoadDatabase() throws Exception {
    }

    // Can be overriden
    protected void beforeDoLoadDatabase() throws Exception {
    }

    // Can be overriden
    protected void truncateDatabase() throws Exception {
        truncater.truncate();
    }

    public interface Log4jBlockingCallback<T> {
        T execute() throws Exception;
    }

    protected <T> T doWithLog4jBlocking(String log4jLoggerStr, Log4jBlockingCallback<T> cb) throws Exception {

        ch.qos.logback.classic.Logger logger = null;
        Level loggerLevel = null;
        ch.qos.logback.classic.Logger root = null;
        Level rootLevel = null;
        try {
            logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(log4jLoggerStr);
            loggerLevel = disableLogger(logger);

            final T t = cb.execute();
            return t;
        } finally {
            enableLogger(logger, loggerLevel);
            enableLogger(root, rootLevel);
        }
    }

    private void enableLogger(ch.qos.logback.classic.Logger logger, Level level) {
        if (logger != null && level != null) {
            logger.setLevel(level);
        }
    }

    private Level disableLogger(ch.qos.logback.classic.Logger logger) {
        if (logger != null) {
            Level level = null;
            level = logger.getLevel();
            logger.setLevel(Level.OFF);
            return level;
        }
        return null;
    }
}
