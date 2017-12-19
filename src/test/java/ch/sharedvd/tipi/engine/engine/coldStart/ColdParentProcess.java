package ch.sharedvd.tipi.engine.engine.coldStart;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;

import java.util.concurrent.atomic.AtomicInteger;

public class ColdParentProcess extends TopProcess {

    public static AtomicInteger globalStep = new AtomicInteger(0);
    public static AtomicInteger beginStep = new AtomicInteger(0);
    public static AtomicInteger endStep = new AtomicInteger(0);

    public final static TopProcessMetaModel meta = new TopProcessMetaModel(ColdParentProcess.class, 6, -1, 10, null);

    @Override
    public ActivityResultContext execute() throws Exception {
        beginStep.incrementAndGet();

        long g1a1 = addChildActivity(ColdGroup1Activity1.meta, null);
        long g1a2 = addChildActivity(ColdGroup1Activity2.meta, g1a1, null);
        addChildActivity(ColdGroup1Activity3.meta, null);

        long g2a1 = addChildActivity(ColdGroup2Activity1.meta, g1a2, null);
        addChildActivity(ColdGroup2Activity2.meta, g2a1, null);

        while (globalStep.get() < 1) {
            Thread.sleep(10);
        }

        endStep.incrementAndGet();
        return new FinishedActivityResultContext();
    }

}
