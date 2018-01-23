package ch.sharedvd.tipi.engine.common;

import ch.sharedvd.tipi.engine.TipiTestingApplication;
import ch.sharedvd.tipi.engine.client.TipiFacade;
import ch.sharedvd.tipi.engine.query.ActivityQueryService;
import ch.sharedvd.tipi.engine.query.TipiQueryFacade;
import ch.sharedvd.tipi.engine.repository.ActivityRepository;
import ch.sharedvd.tipi.engine.repository.TopProcessRepository;
import ch.sharedvd.tipi.engine.runner.ActivityRunningService;
import ch.sharedvd.tipi.engine.svc.ActivityPersisterService;
import ch.sharedvd.tipi.engine.utils.QuantityFormatter;
import ch.sharedvd.tipi.engine.utils.TixTemplate;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import tipiut.config.TipiUtTestingConfig;

import javax.persistence.EntityManager;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        TipiTestingApplication.class,
        TipiUtTestingConfig.class
}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:tipi-ut.properties")
public abstract class AbstractTipiPersistenceTest extends AbstractSpringBootTruncaterTest {

    private static final Logger log = LoggerFactory.getLogger(AbstractTipiPersistenceTest.class);

    @Autowired
    protected ActivityPersisterService activityPersisterService;
    @Autowired
    protected ActivityRunningService activityRunningService;
    @Autowired
    protected ActivityQueryService activityQueryService;

    @Autowired
    protected TopProcessRepository topProcessRepository;
    @Autowired
    protected ActivityRepository activityRepository;

    @Autowired
    protected TixTemplate txTemplate;

    @Autowired
    protected EntityManager em;

    @Autowired
    protected TipiFacade tipiFacade;
    @Autowired
    protected TipiQueryFacade tipiQueryFacade;

    @Override
    public TixTemplate getTxTemplate() {
        return txTemplate;
    }

    protected void waitWhileRunning(long pid, int maxWait) throws InterruptedException {
        final int loopWait = 10;
        final int maxLoop = maxWait / loopWait;
        final int ONE_SECOND_COUNT = 1000 / loopWait;

        int count = 0;
        while (tipiFacade.isRunning(pid) && count < maxLoop) {
            Thread.sleep(loopWait);
            count++;
            if (count % ONE_SECOND_COUNT == 0) {
                log.info("Waiting on process=" + pid + " ...");
            }
        }
        Assert.assertFalse("Aborting wait on process=" + pid + " after " + QuantityFormatter.formatMillis(maxWait), tipiFacade.isRunning(pid));
    }
}
