package ch.vd.registre.tipi.engine.multiLevel;

import ch.sharedvd.tipi.engine.action.Activity;
import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.meta.ActivityMetaModel;

public class MultiLevelFirstActivity extends Activity {

    public static final ActivityMetaModel meta =
            new ActivityMetaModel(MultiLevelFirstActivity.class);

    @Override
    protected ActivityResultContext execute() throws Exception {
        return new FinishedActivityResultContext();
    }

}
