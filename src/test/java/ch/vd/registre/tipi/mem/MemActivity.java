package ch.vd.registre.tipi.mem;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.SubProcess;
import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import ch.sharedvd.tipi.engine.meta.SubProcessMetaModel;

import java.util.concurrent.atomic.AtomicInteger;

public class MemActivity extends SubProcess {
    public static SubProcessMetaModel meta = new SubProcessMetaModel(MemActivity.class, TipiEngineTest.defaultRetry);

    public static AtomicInteger count = new AtomicInteger();

    public static boolean started = true;

    @Override
    protected ActivityResultContext execute() throws Exception {

        count.incrementAndGet();

        while (started) {
            Thread.sleep(100);
        }

        return new FinishedActivityResultContext();
    }

}
