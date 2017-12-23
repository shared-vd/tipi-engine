package ch.sharedvd.tipi.engine.engine.multiLevel;

import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import org.junit.Test;

public class MultiLevelTest extends TipiEngineTest {

    @Test
    public void runMultiLevelProcess() throws Exception {

        long pid = tipiFacade.launch(MultiLevelTopProcess.meta, null);
        waitWhileRunning(pid, 30000);
    }

}
