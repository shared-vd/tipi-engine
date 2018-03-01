package ch.sharedvd.tipi.engine.model;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.client.TipiTopProcess;
import ch.sharedvd.tipi.engine.client.TipiVariable;
import ch.sharedvd.tipi.engine.client.VariableMap;
import ch.sharedvd.tipi.engine.common.AbstractTipiPersistenceTest;
import ch.sharedvd.tipi.engine.meta.ActivityMetaModel;
import ch.sharedvd.tipi.engine.meta.MetaModelHelper;
import ch.sharedvd.tipi.engine.meta.VariableType;
import ch.sharedvd.tipi.engine.svc.ActivityPersisterService;
import ch.sharedvd.tipi.engine.svc.UndefinedVariableException;
import ch.sharedvd.tipi.engine.svc.WrongTypeVariableException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static junit.framework.TestCase.fail;

public class ActivityPersisterServiceTest extends AbstractTipiPersistenceTest {

    @Autowired
    private ActivityPersisterService activityPersisterService;

    @Test
    public void putVariables_OK() {
        final DbTopProcess a = new DbTopProcess();
        final VariableMap vars = new VariableMap();
        vars.put("message", "allo");
        final ActivityMetaModel meta = MetaModelHelper.createActivityMetaModel(MyTopProcess.class);

        activityPersisterService.putVariables(a, meta, vars);
    }

    @Test
    public void putVariables_Undefined() {
        final DbTopProcess a = new DbTopProcess();
        final VariableMap vars = new VariableMap();
        vars.put("message2", "allo"); // unknown variable
        final ActivityMetaModel meta = MetaModelHelper.createActivityMetaModel(MyTopProcess.class);

        try {
            activityPersisterService.putVariables(a, meta, vars);
            fail();
        }
        catch (UndefinedVariableException e) {
            // ok
        }
    }

    @Test
    public void putVariables_WrongType() {
        final DbTopProcess a = new DbTopProcess();
        final VariableMap vars = new VariableMap();
        vars.put("message", 1L); // wrong type
        final ActivityMetaModel meta = MetaModelHelper.createActivityMetaModel(MyTopProcess.class);

        try {
            activityPersisterService.putVariables(a, meta, vars);
            fail();
        }
        catch (WrongTypeVariableException e) {
            // ok
        }
    }



    @TipiTopProcess(variables = {
            @TipiVariable(name = "message", type = VariableType.String, required = true),
            @TipiVariable(name = "count", type = VariableType.Integer)
    })
    private static class MyTopProcess extends TopProcess {

        @Override
        protected ActivityResultContext execute() throws Exception {
            return null;
        }
    }
}
