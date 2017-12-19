package ch.sharedvd.tipi.engine.engine.multiLevel;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;

public class MultiLevelTopProcess extends TopProcess {

    public static final TopProcessMetaModel meta = new TopProcessMetaModel(MultiLevelTopProcess.class,
            10,
            -1,
            10, null) {
    };

    @Override
    protected ActivityResultContext execute() throws Exception {

        long child1 = addChildActivity(MultiLevelFirstSubProcess.meta, null);
        addChildActivity(MultiLevelSecondSubProcess.meta, child1, null);

        return new FinishedActivityResultContext();
    }

}
