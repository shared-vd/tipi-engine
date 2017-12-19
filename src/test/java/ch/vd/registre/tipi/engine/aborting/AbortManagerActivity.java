package ch.vd.registre.tipi.engine.aborting;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.SubProcess;
import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import ch.sharedvd.tipi.engine.meta.SubProcessMetaModel;

import java.util.concurrent.atomic.AtomicInteger;

public class AbortManagerActivity extends SubProcess {

    public static SubProcessMetaModel meta = new SubProcessMetaModel(AbortManagerActivity.class, TipiEngineTest.defaultRetry);

    public static AtomicInteger count = new AtomicInteger(0);

    @Override
    protected ActivityResultContext execute() throws Exception {

        count.incrementAndGet();

        while (true) {
            testAbort();
            Thread.sleep(10);
        }
    }
}
