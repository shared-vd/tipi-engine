package ch.vd.registre.tipi.mem;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.client.VariableMap;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;

public class MemTopProcess extends TopProcess {

    public static TopProcessMetaModel meta = new TopProcessMetaModel(MemTopProcess.class, 1, -1, 10, null);

    @Override
    protected ActivityResultContext execute() throws Exception {

        VariableMap vars = new VariableMap();
        addChildActivity(MemActivity.meta, vars);

        return new FinishedActivityResultContext();
    }


}
