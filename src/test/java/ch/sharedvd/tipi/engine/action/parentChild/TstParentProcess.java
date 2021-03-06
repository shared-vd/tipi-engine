package ch.sharedvd.tipi.engine.action.parentChild;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.client.TipiTopProcess;
import ch.sharedvd.tipi.engine.client.TipiVariable;
import ch.sharedvd.tipi.engine.client.VariableMap;
import ch.sharedvd.tipi.engine.meta.VariableType;

import java.util.concurrent.atomic.AtomicInteger;

@TipiTopProcess(priority = 6, nbMaxTopConcurrent = -1, nbMaxConcurrent = 20, description = "Bla bla", deleteWhenFinished = false,
        variables = {
                @TipiVariable(name = "nbSubAct", type = VariableType.Integer),
                @TipiVariable(name = "listVarsId", type = VariableType.Long),
                @TipiVariable(name = "concat", type = VariableType.String)
        })
public class TstParentProcess extends TopProcess {

    public static AtomicInteger globalStep = new AtomicInteger(0);
    public static AtomicInteger beginStep = new AtomicInteger(0);
    public static AtomicInteger endStep = new AtomicInteger(0);

    @Override
    public ActivityResultContext execute() throws Exception {
        beginStep.incrementAndGet();

        final long actId1;
        {
            final VariableMap vars = new VariableMap();
            vars.put("var", 23);
            actId1 = addChildActivity(TstPutNumberActivity1.meta, vars);
        }
        {
            final VariableMap vars = new VariableMap();
            vars.put("var", 32);
            addChildActivity(TstPutNumberActivity2.meta, actId1, vars);
        }
        {
            final VariableMap vars = new VariableMap();
            final long spId3 = addChildActivity(TstListVarsSubProcess.meta, vars);
            putVariable("listVarsId", spId3);
        }

        while (globalStep.get() < 1) {
            Thread.sleep(10);
        }

        endStep.incrementAndGet();
        return new FinishedActivityResultContext();
    }

    @Override
    public ActivityResultContext terminate() throws Exception {
        super.terminate();

        long id = getLongVariable("listVarsId");
        String concat = getStringVariable(id, "concat");
        putVariable("concat", concat);

        return new FinishedActivityResultContext();
    }

}
