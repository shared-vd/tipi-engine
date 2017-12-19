package ch.sharedvd.tipi.engine.engine.aborting;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.client.VariableMap;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;

public class AbortManagerProcess extends TopProcess {

    public static TopProcessMetaModel meta = new TopProcessMetaModel(AbortManagerProcess.class, 1, -1, 10, null);

    @Override
    protected ActivityResultContext execute() throws Exception {

        VariableMap vars = new VariableMap();
        vars.put(AbortingActivity.CREATE_CHILDREN, true);
        addChildActivity(AbortManagerActivity.meta, vars);

        return new FinishedActivityResultContext();
    }

}
