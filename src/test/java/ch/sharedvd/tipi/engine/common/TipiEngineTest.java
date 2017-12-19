package ch.sharedvd.tipi.engine.common;

import ch.sharedvd.tipi.engine.client.TipiFacade;
import ch.sharedvd.tipi.engine.command.CommandConsumer;
import ch.sharedvd.tipi.engine.command.CommandHelperService;
import ch.sharedvd.tipi.engine.command.CommandService;
import ch.sharedvd.tipi.engine.runner.TipiStarter;
import ch.sharedvd.tipi.engine.runner.TopProcessGroupLauncher;
import ch.sharedvd.tipi.engine.runner.TopProcessGroupManager;
import ch.vd.registre.tipi.testing.TipiTestingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;


@ContextConfiguration(locations = {
        TipiTestingConstants.ENGINE,
        TipiTestingConstants.UT_ENGINE,
        TipiTestingConstants.TESTING
})
public abstract class TipiEngineTest extends AbstractTipiPersistenceTest {

    @Autowired
    protected TipiTestingService tts;

    @Autowired
    protected TipiStarter starter;

    @Autowired
    protected CommandConsumer commandConsumer;

    @Autowired
    protected CommandHelperService commandHelperService;

    @Autowired
    protected TopProcessGroupManager groupManager;

    @Autowired
    protected CommandService commandService;

    @Autowired
    protected TipiFacade tipiFacade;

    @Override
    public void onSetUp() throws Exception {
        super.onSetUp();

        // Pour tester un peu le cache
        TopProcessGroupLauncher.CACHE_SIZE = 3;
    }

    @Override
    protected void afterOnSetUp() throws Exception {
        super.afterOnSetUp();

        // Start apres que la DB a été truncated
        starter.start();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onTearDown() throws Exception {
        super.onTearDown();

        starter.stop();
    }
}
