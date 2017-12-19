package ch.vd.registre.tipi.engine.multiLevel;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.SubProcess;
import ch.sharedvd.tipi.engine.meta.SubProcessMetaModel;

public class MultiLevelSecondSubProcess extends SubProcess {

    public static final SubProcessMetaModel meta =
            new SubProcessMetaModel(MultiLevelSecondSubProcess.class);

    @Override
    protected ActivityResultContext execute() throws Exception {

        for (int i = 0; i < 100; i++) {
            addChildActivity(MultiLevelSecondActivity.meta, null);
        }

        return new FinishedActivityResultContext();
    }

}
