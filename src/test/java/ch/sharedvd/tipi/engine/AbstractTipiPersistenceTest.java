package ch.sharedvd.tipi.engine;

import ch.sharedvd.tipi.engine.client.TipiFacade;
import ch.sharedvd.tipi.engine.query.TipiQueryFacade;
import ch.sharedvd.tipi.engine.repository.ActivityRepository;
import ch.sharedvd.tipi.engine.repository.TopProcessRepository;
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
public abstract class AbstractTipiPersistenceTest {

    @Autowired
    protected ActivityPersisterService activityPersistenceService;

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

    @Before
    public void onSetUp() {

    }
}
