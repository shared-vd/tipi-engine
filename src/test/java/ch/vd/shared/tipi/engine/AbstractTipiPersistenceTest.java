package ch.vd.shared.tipi.engine;

import ch.vd.shared.tipi.engine.repository.ActivityModelRepository;
import ch.vd.shared.tipi.engine.repository.TopProcessModelRepository;
import ch.vd.shared.tipi.engine.svc.ActivityPersistenceService;
import ch.vd.shared.tipi.engine.tx.TxTemplate;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import tipi.config.TipiModelConfig;
import tipiut.config.TipiUtDatabaseConfig;

import javax.persistence.EntityManager;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        DataSourceAutoConfiguration.class,
        TipiModelConfig.class,
        TipiUtDatabaseConfig.class
}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public abstract class AbstractTipiPersistenceTest {

    @Autowired
    protected ActivityPersistenceService activityPersistenceService;

    @Autowired
    protected TopProcessModelRepository topProcessModelRepository;

    @Autowired
    protected ActivityModelRepository activityModelRepository;

    @Autowired
    protected TxTemplate txTemplate;

    @Autowired
    protected EntityManager em;
}
