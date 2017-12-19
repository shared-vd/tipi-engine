package ch.vd.registre.tipi.command.annotated;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.SubProcess;
import ch.sharedvd.tipi.engine.client.TipiSubProcess;

@TipiSubProcess(description = "Test SubProcess")
public class AnnotatedSubProcess extends SubProcess {

    @Override
    protected ActivityResultContext execute() throws Exception {
        return new FinishedActivityResultContext();
    }

}
