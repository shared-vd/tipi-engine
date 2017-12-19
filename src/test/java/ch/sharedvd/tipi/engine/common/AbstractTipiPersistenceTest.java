package ch.sharedvd.tipi.engine.common;

import ch.sharedvd.tipi.engine.TipiTestingApplication;
import ch.sharedvd.tipi.engine.client.TipiFacade;
import ch.sharedvd.tipi.engine.query.ActivityQueryService;
import ch.sharedvd.tipi.engine.query.TipiQueryFacade;
import ch.sharedvd.tipi.engine.repository.ActivityRepository;
import ch.sharedvd.tipi.engine.repository.TopProcessRepository;
import ch.sharedvd.tipi.engine.runner.ActivityRunningService;
import ch.sharedvd.tipi.engine.svc.ActivityPersisterService;
import ch.sharedvd.tipi.engine.utils.TxTemplate;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import tipiut.config.TipiUtDatabaseConfig;

import javax.persistence.EntityManager;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        TipiTestingApplication.class,
        TipiUtDatabaseConfig.class
}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public abstract class AbstractTipiPersistenceTest extends AbstractSpringBootTruncaterTest {

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
    protected TxTemplate txTemplate;

    @Autowired
    protected EntityManager em;

    @Autowired
    protected TipiFacade tipiFacade;
    @Autowired
    protected TipiQueryFacade tipiQueryFacade;

    @Override
    public TxTemplate getTxTemplate() {
        return txTemplate;
    }

    @Before
    public void onSetUp() throws Exception {
    }
}
