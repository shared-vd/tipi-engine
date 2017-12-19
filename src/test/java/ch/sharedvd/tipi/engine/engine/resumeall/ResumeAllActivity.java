package ch.sharedvd.tipi.engine.engine.resumeall;

import ch.sharedvd.tipi.engine.action.*;
import ch.sharedvd.tipi.engine.meta.ActivityMetaModel;

import java.util.concurrent.atomic.AtomicInteger;

public class ResumeAllActivity extends Activity {

    public static final ActivityMetaModel meta = new ActivityMetaModel(ResumeAllActivity.class);

    // mode = 1 -> Suspended
    // mode = 2 -> Errors
    // mode = 3 -> Finished
    public static int mode = 0;
    public static AtomicInteger nbCalled = new AtomicInteger(0);
    public static AtomicInteger nbFinished = new AtomicInteger(0);

    @Override
    protected ActivityResultContext execute() throws Exception {
        nbCalled.incrementAndGet();

        Thread.sleep(50);

        if (mode == 1) {
            return new SuspendedActivityResultContext("blabla");
        } else if (mode == 2) {
            return new ErrorActivityResultContext("Error");
        }

        nbFinished.incrementAndGet();
        return new FinishedActivityResultContext();
    }

}
