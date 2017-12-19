package ch.vd.registre.tipi.engine.stopStartGroup;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;

import java.util.concurrent.atomic.AtomicInteger;

public class StopStartGroupParentProcess extends TopProcess {

    public static AtomicInteger globalStep = new AtomicInteger(0);
    public static AtomicInteger beginStep = new AtomicInteger(0);
    public static AtomicInteger endStep = new AtomicInteger(0);
    public static AtomicInteger terminatedStep = new AtomicInteger(0);

    public final static TopProcessMetaModel meta = new TopProcessMetaModel(StopStartGroupParentProcess.class, 2, -1, 2, null);

    @Override
    public ActivityResultContext execute() throws Exception {
        beginStep.incrementAndGet();

        addChildActivity(StopStartGroupActivity1.meta, null);
        addChildActivity(StopStartGroupActivity2.meta, null);
        addChildActivity(StopStartGroupActivity3.meta, null);

        while (globalStep.get() < 1) {
            Thread.sleep(10);
        }

        endStep.incrementAndGet();
        return new FinishedActivityResultContext();
    }

    @Override
    protected ActivityResultContext terminate() throws Exception {
        terminatedStep.incrementAndGet();
        return super.terminate();
    }

}
