package ch.sharedvd.tipi.engine.engine.stopStartGroup;

import ch.sharedvd.tipi.engine.action.Activity;
import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.meta.ActivityMetaModel;

import java.util.concurrent.atomic.AtomicInteger;

public class StopStartGroupActivity2 extends Activity {

    public static AtomicInteger beginStep = new AtomicInteger(0);
    public static AtomicInteger endStep = new AtomicInteger(0);

    public static final ActivityMetaModel meta = new ActivityMetaModel(StopStartGroupActivity2.class);

    @Override
    public ActivityResultContext execute() throws Exception {
        beginStep.incrementAndGet();

        while (StopStartGroupParentProcess.globalStep.get() < 3) {
            Thread.sleep(10);
        }

        endStep.incrementAndGet();
        return new FinishedActivityResultContext();
    }

}
