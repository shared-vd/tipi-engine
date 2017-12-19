package ch.vd.registre.tipi.engine;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import org.junit.Assert;
import org.junit.Test;

;

public class ProcessWithoutChildrenTest extends TipiEngineTest {

    public static class ProcessWithoutChildren extends TopProcess {

        public static int value = 0;

        public static final TopProcessMetaModel meta = new TopProcessMetaModel(ProcessWithoutChildren.class, 6, -1, 10, null);


        @Override
        protected ActivityResultContext execute() throws Exception {

            value = 42;

            return new FinishedActivityResultContext();
        }
    }

    @Test
    public void nbRetry() throws Exception {

        final long pid = tipiFacade.launch(ProcessWithoutChildren.meta, null);
        while (tipiFacade.isRunning(pid)) {
            Thread.sleep(100);
        }
        Assert.assertEquals(42, ProcessWithoutChildren.value);
    }

}
