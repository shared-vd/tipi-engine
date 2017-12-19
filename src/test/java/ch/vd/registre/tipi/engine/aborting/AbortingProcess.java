package ch.vd.registre.tipi.engine.aborting;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.client.TipiTopProcess;
import ch.sharedvd.tipi.engine.client.VariableMap;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;

@TipiTopProcess(description = "Un process qui dit Bonjour")
public class AbortingProcess extends TopProcess {

    public static TopProcessMetaModel meta = new TopProcessMetaModel(AbortingProcess.class, 1, -1, 10, "The description");

    @Override
    protected ActivityResultContext execute() throws Exception {

        VariableMap vars = new VariableMap();
        vars.put(AbortingActivity.CREATE_CHILDREN, true);
        addChildActivity(AbortingActivity.meta, vars);

        return new FinishedActivityResultContext();
    }

}
