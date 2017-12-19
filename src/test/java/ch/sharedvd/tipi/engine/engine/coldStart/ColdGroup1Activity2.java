package ch.vd.registre.tipi.engine.coldStart;

import ch.sharedvd.tipi.engine.action.Activity;
import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.meta.ActivityMetaModel;

import java.util.concurrent.atomic.AtomicInteger;

public class ColdGroup1Activity2 extends Activity {

    public static long id = 0;
    public static boolean sendException = true;
    public static AtomicInteger beginStep = new AtomicInteger(0);
    public static AtomicInteger endStep = new AtomicInteger(0);

    public static final ActivityMetaModel meta = new ActivityMetaModel(ColdGroup1Activity2.class);

    @Override
    public ActivityResultContext execute() throws Exception {
        id = getActivityId();
        beginStep.incrementAndGet();

        if (sendException) {
            throw new RuntimeException("Exception de TESTING: Le process doit pas finir");
        }

        while (ColdParentProcess.globalStep.get() < 4) {
            Thread.sleep(10);
        }

        endStep.incrementAndGet();
        return new FinishedActivityResultContext();
    }

}
