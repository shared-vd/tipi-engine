package ch.sharedvd.tipi.engine.model;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import ch.sharedvd.tipi.engine.meta.VariableDescription;
import ch.sharedvd.tipi.engine.meta.VariableType;
import ch.sharedvd.tipi.engine.repository.ActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class TstStoreNumberProcess extends TopProcess {

    public static final TopProcessMetaModel meta = new TopProcessMetaModel(TstStoreNumberProcess.class,
            new VariableDescription[]{
                    new VariableDescription("var", VariableType.Integer),
                    new VariableDescription("id", VariableType.Long),
                    new VariableDescription("result", VariableType.String),
                    new VariableDescription("name", VariableType.String)
            }, null,
            2, -1, 20, null, true) {
        @Override
        protected void init() {
            setDeleteWhenFinished(false);
        }
    };

    @Autowired
    private ActivityRepository activityRepository;

    public static int number = 0;

    public TstStoreNumberProcess() {
        super();
    }

    @Override
    protected ActivityResultContext execute() throws Exception {

        Integer var = getIntVariable("var");
        number = var;
        putVariable("result", "TheResult");

        Long pid = getLongVariable("id");
        DbActivity model = activityRepository.findOne(pid);
        putVariable("name", model.getFqn());

        return new FinishedActivityResultContext();
    }

}
