package ch.sharedvd.tipi.engine.engine.aborting;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.SubProcess;
import ch.sharedvd.tipi.engine.meta.SubProcessMetaModel;
import ch.sharedvd.tipi.engine.meta.VariableDescription;
import ch.sharedvd.tipi.engine.meta.VariableType;

import java.util.concurrent.atomic.AtomicInteger;

import static ch.sharedvd.tipi.engine.engine.aborting.AbortingActivity.CREATE_CHILDREN;

public class AbortManagerActivity extends SubProcess {

    public static SubProcessMetaModel meta = new SubProcessMetaModel(AbortManagerActivity.class,
            new VariableDescription[]{
                    new VariableDescription(CREATE_CHILDREN, VariableType.Boolean)
            });

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
