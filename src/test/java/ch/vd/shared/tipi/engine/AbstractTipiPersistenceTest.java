package ch.vd.shared.tipi.engine;

import ch.vd.shared.tipi.engine.repository.ActivityModelRepository;
import ch.vd.shared.tipi.engine.repository.TopProcessModelRepository;
import ch.vd.shared.tipi.engine.svc.ActivityPersistenceService;
import ch.vd.shared.tipi.engine.tx.TxTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import tipi.config.TipiModelConfig;

import javax.persistence.EntityManager;

@ContextConfiguration(classes = {
        TipiModelConfig.class
})
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
