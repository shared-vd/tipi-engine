package ch.sharedvd.tipi.engine.dates;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;

public class DateDebutFinProcess extends TopProcess {

    public static TopProcessMetaModel meta = new TopProcessMetaModel(DateDebutFinProcess.class, 10, -1, 10, null) {
        @Override
        protected void init() {
            setDeleteWhenFinished(false);
        }
    };

    @Override
    protected ActivityResultContext execute() throws Exception {

        addChildActivity(DateDebutFinActivity.meta, null);

        return new FinishedActivityResultContext();
    }

}
