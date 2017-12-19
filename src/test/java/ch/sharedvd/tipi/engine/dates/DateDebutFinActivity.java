package ch.sharedvd.tipi.engine.dates;

import ch.sharedvd.tipi.engine.action.Activity;
import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.meta.ActivityMetaModel;

public class DateDebutFinActivity extends Activity {

    public static ActivityMetaModel meta = new ActivityMetaModel(DateDebutFinActivity.class);

    @Override
    protected ActivityResultContext execute() throws Exception {

        // On attends 0.5 seconde
        Thread.sleep(500);

        return new FinishedActivityResultContext();
    }

}
