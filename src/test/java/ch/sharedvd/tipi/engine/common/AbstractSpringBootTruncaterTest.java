package ch.sharedvd.tipi.engine.common;

import ch.sharedvd.tipi.engine.utils.TxTemplate;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import static junit.framework.TestCase.assertTrue;

public abstract class AbstractSpringBootTruncaterTest implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(AbstractSpringBootTruncaterTest.class);

    private boolean onSetUpWasRun = false;

    private HibernateMetaDataTruncater truncater;

    protected abstract TxTemplate getTxTemplate();

    private ApplicationContext applicationContext;

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
    }

    /**
     * S'assure que le onSetUp() n'est appelé qu'une fois, qu'on soit dans une
     * méthode transactionnelle ou non
     *
     * @throws Exception
     */
    protected final void runOnSetUpOnlyOnce() throws Exception {
        if (!onSetUpWasRun) {
            assertTrue("Pour être sûr que mvn test exécute les tests, il faut que le nom de la classe se termine par Test, ou IT ou UT.",
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
}
