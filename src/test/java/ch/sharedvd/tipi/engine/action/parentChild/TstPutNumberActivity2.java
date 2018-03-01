package ch.sharedvd.tipi.engine.action.parentChild;

import ch.sharedvd.tipi.engine.action.Activity;
import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.meta.ActivityMetaModel;
import ch.sharedvd.tipi.engine.meta.VariableDescription;
import ch.sharedvd.tipi.engine.meta.VariableType;

import java.util.concurrent.atomic.AtomicInteger;

public class TstPutNumberActivity2 extends Activity {

    public static AtomicInteger beginStep = new AtomicInteger(0);
    public static AtomicInteger endStep = new AtomicInteger(0);

    public static final ActivityMetaModel meta = new ActivityMetaModel(TstPutNumberActivity2.class,
            new VariableDescription[] {
                    new VariableDescription("var", VariableType.Integer),
                    new VariableDescription("result", VariableType.Integer)
            });

    @Override
    public ActivityResultContext execute() throws Exception {
        beginStep.incrementAndGet();

        int nb = this.getIntVariable("var");
        Thread.sleep(300);
        putVariable("result", nb * 2);

        while (TstParentProcess.globalStep.get() < 3) {
            Thread.sleep(10);
        }

        endStep.incrementAndGet();
        return new FinishedActivityResultContext();
    }

}
