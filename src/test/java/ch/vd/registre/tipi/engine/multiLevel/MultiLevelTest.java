package ch.vd.registre.tipi.engine.multiLevel;

import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import org.junit.Test;

public class MultiLevelTest extends TipiEngineTest {

    @Test
    public void runMultiLevelProcess() throws Exception {

        long pid = tipiFacade.launch(MultiLevelTopProcess.meta, null);
        while (tipiFacade.isRunning(pid)) {
            Thread.sleep(200);
        }
    }

}
