package ch.sharedvd.tipi.engine.action.parentChild;

import ch.sharedvd.tipi.engine.action.ActivityFacade;
import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.SubProcess;
import ch.sharedvd.tipi.engine.client.VariableMap;
import ch.sharedvd.tipi.engine.meta.SubProcessMetaModel;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TstListVarsSubProcess extends SubProcess {

    public static AtomicInteger beginStep = new AtomicInteger(0);
    public static AtomicInteger endStep = new AtomicInteger(0);

    public static final SubProcessMetaModel meta = new SubProcessMetaModel(TstListVarsSubProcess.class);

    @Override
    public ActivityResultContext execute() throws Exception {
        beginStep.incrementAndGet();

        putVariable("in", 1);

        {
            final VariableMap vars = new VariableMap();
            vars.put("in", 2);
            addChildActivity(TstAssignVarActivity.meta, vars);
        }
        {
            final VariableMap vars = new VariableMap();
            vars.put("in", 3);
            addChildActivity(TstAssignVarActivity.meta, vars);
        }
        final long id;
        {
            final VariableMap vars = new VariableMap();
            vars.put("in", 4);
            id = addChildActivity(TstAssignVarActivity.meta, vars);
        }
        {
            final VariableMap vars = new VariableMap();
            vars.put("in", 5);
            addChildActivity(TstAssignVarActivity.meta, id, vars);
        }

        while (TstParentProcess.globalStep.get() < 4) {
            Thread.sleep(10);
        }

        endStep.incrementAndGet();
        return new FinishedActivityResultContext();
    }

    @Override
    public ActivityResultContext terminate() throws Exception {
        ActivityResultContext ret = super.terminate();
        if (!(ret instanceof FinishedActivityResultContext)) {
            return ret;
        }

        List<ActivityFacade> activities = getChildren();
        Collections.sort(activities, new Comparator<ActivityFacade>() {
            @Override
            public int compare(ActivityFacade o1, ActivityFacade o2) {
                if (((Integer) o1.getVariable("in")) > ((Integer) o2.getVariable("in"))) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });

        String concat = "";
        for (ActivityFacade act : activities) {
            int out = (Integer) act.getVariable("out");
            if (concat.length() > 0) {
                concat += ",";
            }
            concat += out;
        }
        putVariable("concat", concat);
        return new FinishedActivityResultContext();
    }

}
