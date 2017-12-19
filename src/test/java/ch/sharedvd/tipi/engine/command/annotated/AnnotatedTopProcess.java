package ch.vd.registre.tipi.command.annotated;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.client.TipiTopProcess;

@TipiTopProcess(description = "Test TopProcess")
public class AnnotatedTopProcess extends TopProcess {

    @Override
    protected ActivityResultContext execute() throws Exception {
        return new FinishedActivityResultContext();
    }

}
