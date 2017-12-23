package ch.sharedvd.tipi.engine.engine;

import ch.sharedvd.tipi.engine.action.Activity;
import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import ch.sharedvd.tipi.engine.meta.ActivityMetaModel;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class MasseTest extends TipiEngineTest {

    private static final Logger log = Logger.getLogger(MasseTest.class);

    public static class MasseTestProcess extends TopProcess {

        public static final int NB_TO_START = 200;

        public final static TopProcessMetaModel meta = new TopProcessMetaModel(MasseTestProcess.class, 100, -1, 2 * NB_TO_START, null) {
            @Override
            protected void init() {
                setDeleteWhenFinished(false);
            }
        };

        @Override
        protected ActivityResultContext execute() throws Exception {

            for (int i = 0; i < NB_TO_START; i++) {
                addChildActivity(MasseTestActivity.meta, null);
            }

            return new FinishedActivityResultContext();
        }
    }

    public static class MasseTestActivity extends Activity {

        public static AtomicInteger count = new AtomicInteger(0);

        public static final ActivityMetaModel meta = new ActivityMetaModel(MasseTestActivity.class);

        @Override
        protected ActivityResultContext execute() throws Exception {

            count.incrementAndGet();

            return new FinishedActivityResultContext();
        }
    }

    @Test
    public void startUneMasseActivities() throws Exception {

        log.info("Début du run ...");
        long begin = System.currentTimeMillis();

        final long pid = tipiFacade.launch(MasseTestProcess.meta, null);
        while (tipiFacade.isRunning(pid)) {
            Thread.sleep(2);
        }
        Assert.assertEquals(MasseTestProcess.NB_TO_START, MasseTestActivity.count.get());
        long diff = System.currentTimeMillis() - begin;
        log.info("Temps: " + (int) (diff / 1000.0) + " secondes pour " + MasseTestProcess.NB_TO_START + " activités");
    }

}
