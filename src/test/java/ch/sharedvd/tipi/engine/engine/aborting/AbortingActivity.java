package ch.sharedvd.tipi.engine.engine.aborting;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.SubProcess;
import ch.sharedvd.tipi.engine.client.VariableMap;
import ch.sharedvd.tipi.engine.meta.SubProcessMetaModel;
import ch.sharedvd.tipi.engine.meta.VariableDescription;
import ch.sharedvd.tipi.engine.meta.VariableType;

import java.util.concurrent.atomic.AtomicInteger;

public class AbortingActivity extends SubProcess {

    public static String CREATE_CHILDREN = "create";
    public static SubProcessMetaModel meta = new SubProcessMetaModel(AbortingActivity.class,
            new VariableDescription[]{
                    new VariableDescription(CREATE_CHILDREN, VariableType.Boolean)
            });

    public static AtomicInteger count = new AtomicInteger();

    @Override
    protected ActivityResultContext execute() throws Exception {

        count.incrementAndGet();

        Boolean create = getBooleanVariable(CREATE_CHILDREN);
        if (create) {
            VariableMap vars = new VariableMap();

            vars.put(AbortingActivity.CREATE_CHILDREN, false);
            addChildActivity(meta, vars);
            vars.put(AbortingActivity.CREATE_CHILDREN, false);
            addChildActivity(meta, vars);

            vars.put(AbortingActivity.CREATE_CHILDREN, true);
            addChildActivity(meta, vars);
        }

        return new FinishedActivityResultContext();
    }
}
