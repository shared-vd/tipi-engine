package ch.sharedvd.tipi.engine.common;

import ch.sharedvd.tipi.engine.TipiTestingApplication;
import ch.sharedvd.tipi.engine.client.TipiFacade;
import ch.sharedvd.tipi.engine.model.*;
import ch.sharedvd.tipi.engine.query.ActivityQueryService;
import ch.sharedvd.tipi.engine.query.TipiQueryFacade;
import ch.sharedvd.tipi.engine.repository.ActivityRepository;
import ch.sharedvd.tipi.engine.repository.TopProcessRepository;
import ch.sharedvd.tipi.engine.runner.ActivityRunningService;
import ch.sharedvd.tipi.engine.svc.ActivityPersisterService;
import ch.sharedvd.tipi.engine.utils.BlobFactory;
import ch.sharedvd.tipi.engine.utils.InputStreamHolder;
import ch.sharedvd.tipi.engine.utils.QuantityFormatter;
import ch.sharedvd.tipi.engine.utils.TixTemplate;
import org.hibernate.Session;
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
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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

    protected void forcePutVariable(DbActivity aDbActivity, String key, Serializable value) {
        ch.sharedvd.tipi.engine.utils.Assert.notNull(key);
        ch.sharedvd.tipi.engine.utils.Assert.notNull(value, "Missing value for key=" + key);

        final DbVariable<?> variable;
        if (value instanceof String) {
            variable = new DbStringVariable(key, (String) value);
        } else if (value instanceof LocalDate) {
            final LocalDate date = (LocalDate) value;
            final String str = DateTimeFormatter.ofPattern("YYYYMMdd").format(date);
            Integer i = Integer.parseInt(str);
            variable = new DbIntegerVariable(key, i);
        } else if (value instanceof Integer) {
            variable = new DbIntegerVariable(key, (Integer) value);
        } else if (value instanceof Long) {
            variable = new DbLongVariable(key, (Long) value);
        } else if (value instanceof Boolean) {
            variable = new DbBooleanVariable(key, (Boolean) value);
        } else if (value instanceof Timestamp) {
            variable = new DbTimestampVariable(key, (Timestamp) value);
        } else if (value instanceof InputStreamHolder) {
            variable = new DbInputStreamVariable(key, (InputStreamHolder) value, new BlobFactory((Session) em.getDelegate()));
        } else {
            variable = new DbSerializableVariable(key, (Serializable) value, new BlobFactory((Session) em.getDelegate()));
        }
        variable.setOwner(aDbActivity);
        aDbActivity.putVariable(variable);
    }
}
