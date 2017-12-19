package ch.vd.registre.tipi.engine.onerror;

import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import org.junit.Test;

public class OnErrorTest extends TipiEngineTest {

    @Test
    public void onError() throws Exception {

        assertFalse(OnErrorProcess.errorCalled);

        doWithLog4jBlocking("ch.vd.registre.tipi", new Log4jBlockingCallback<Object>() {
            @Override
            public Object execute() throws Exception {
                long pid = tipiFacade.launch(OnErrorProcess.meta, null);
                while (tipiFacade.isRunning(pid)) {
                    Thread.sleep(10);
                }
                return null;
            }
        });
        assertTrue(OnErrorProcess.errorCalled);
    }

}
