package ch.sharedvd.tipi.engine;

import ch.sharedvd.tipi.engine.client.TipiFacade;
import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.sharedvd.tipi.engine.repository.ActivityRepository;
import ch.sharedvd.tipi.engine.repository.TopProcessRepository;
import ch.sharedvd.tipi.engine.svc.ActivityPersistenceService;
import ch.sharedvd.tipi.engine.tx.TxTemplate;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit4.SpringRunner;
import tipi.config.TipiModelConfig;
import tipiut.config.TipiUtDatabaseConfig;

import javax.persistence.EntityManager;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        TipiModelConfig.class,
        TipiUtDatabaseConfig.class
}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ComponentScan("ch.sharedvd.tipi.engine")
@EntityScan(basePackageClasses = DbActivity.class)
@EnableJpaRepositories(basePackageClasses = ActivityRepository.class)
public abstract class AbstractTipiPersistenceTest {

    @Autowired
    protected ActivityPersistenceService activityPersistenceService;

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
}
