package ch.sharedvd.tipi.engine.engine.retryAfterOther;

import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import org.junit.Test;

;

public class RetryAfterOtherExecutingTest extends TipiEngineTest {

    public static long FIRST_ACTI_ID = -1;

    @Test
    public void retryCount() throws Exception {

        long pid = tipiFacade.launch(RetryCountProcess.meta, null);
        while (tipiFacade.isRunning(pid)) {
            Thread.sleep(100);
        }
    }

}
