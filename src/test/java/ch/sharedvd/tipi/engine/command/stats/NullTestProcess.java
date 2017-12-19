package ch.sharedvd.tipi.engine.command.stats;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;

public class NullTestProcess extends TopProcess {

    public static final TopProcessMetaModel meta = new TopProcessMetaModel(NullTestProcess.class, 20, -1, 10, null) {
        @Override
        protected void init() {
            setDeleteWhenFinished(false);
        }

        ;
    };

    @Override
    protected ActivityResultContext execute() throws Exception {

        Thread.sleep(55); // Au moins le temps entre 2 calculs de stats

        return new FinishedActivityResultContext();
    }

}
