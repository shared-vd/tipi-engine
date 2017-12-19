package ch.sharedvd.tipi.engine.engine.multiLevel;

import ch.sharedvd.tipi.engine.action.Activity;
import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.meta.ActivityMetaModel;

public class MultiLevelSecondActivity extends Activity {

    public static final ActivityMetaModel meta =
            new ActivityMetaModel(MultiLevelSecondActivity.class);

    @Override
    protected ActivityResultContext execute() throws Exception {
        return new FinishedActivityResultContext();
    }

}
