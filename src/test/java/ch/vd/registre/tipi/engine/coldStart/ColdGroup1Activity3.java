package ch.vd.registre.tipi.engine.coldStart;

import ch.sharedvd.tipi.engine.action.Activity;
import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.meta.ActivityMetaModel;

import java.util.concurrent.atomic.AtomicInteger;

public class ColdGroup1Activity3 extends Activity {

    public static AtomicInteger beginStep = new AtomicInteger(0);
    public static AtomicInteger endStep = new AtomicInteger(0);

    public static final ActivityMetaModel meta = new ActivityMetaModel(ColdGroup1Activity3.class);

    @Override
    public ActivityResultContext execute() throws Exception {
        beginStep.incrementAndGet();

        while (ColdParentProcess.globalStep.get() < 3) {
            Thread.sleep(10);
        }

        endStep.incrementAndGet();
        return new FinishedActivityResultContext();
    }

}
