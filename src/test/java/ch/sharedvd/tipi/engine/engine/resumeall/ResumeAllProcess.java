package ch.sharedvd.tipi.engine.engine.resumeall;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;

public class ResumeAllProcess extends TopProcess {

    public static final TopProcessMetaModel meta = new TopProcessMetaModel(ResumeAllProcess.class, 10, -1, 10, null) {
        @Override
        protected void init() {
            setDeleteWhenFinished(false);
        }
    };

    @Override
    protected ActivityResultContext execute() throws Exception {

        addChildActivity(ResumeAllActivity.meta, null);
        addChildActivity(ResumeAllActivity.meta, null);

        return new FinishedActivityResultContext();
    }

}
