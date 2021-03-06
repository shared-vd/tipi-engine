package ch.sharedvd.tipi.engine.action.parentChild;

import ch.sharedvd.tipi.engine.action.Activity;
import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.meta.ActivityMetaModel;
import ch.sharedvd.tipi.engine.meta.VariableDescription;
import ch.sharedvd.tipi.engine.meta.VariableType;

public class TstAssignVarActivity extends Activity {

    public static final ActivityMetaModel meta = new ActivityMetaModel(TstAssignVarActivity.class,
            new VariableDescription[]{
                    new VariableDescription("in", VariableType.Integer),
                    new VariableDescription("out", VariableType.Integer)
            });

    @Override
    public ActivityResultContext execute() throws Exception {

        int value = getIntVariable("in");
        putVariable("out", value * 2);

        return new FinishedActivityResultContext();
    }
}
