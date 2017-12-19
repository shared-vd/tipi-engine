package ch.vd.registre.tipi.engine.onerror;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;

public class OnErrorProcess extends TopProcess {

    public static TopProcessMetaModel meta = new TopProcessMetaModel(OnErrorProcess.class, 10, -1, 10, null);

    public static boolean errorCalled = false;

    @Override
    @SuppressWarnings("all")
    protected ActivityResultContext execute() throws Exception {
        if (1 == 1) {
            throw new RuntimeException("Erreur de testing normale");
        }
        return new FinishedActivityResultContext();
    }

    @Override
    public void onError(Throwable exception) {
        errorCalled = true;
    }

}
