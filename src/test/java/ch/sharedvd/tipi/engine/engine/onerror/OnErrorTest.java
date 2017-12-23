package ch.sharedvd.tipi.engine.engine.onerror;

import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import org.junit.Assert;
import org.junit.Test;

public class OnErrorTest extends TipiEngineTest {

    @Test
    public void onError() throws Exception {

        Assert.assertFalse(OnErrorProcess.errorCalled);

        doWithLoggingBlocked("ch.sharedvd.tipi.engine", new Log4jBlockingCallback<Object>() {
            @Override
            public Object execute() throws Exception {
                long pid = tipiFacade.launch(OnErrorProcess.meta, null);
                waitWhileRunning(pid, 5000);
                return null;
            }
        });
        Assert.assertTrue(OnErrorProcess.errorCalled);
    }

}
