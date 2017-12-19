package ch.vd.registre.tipi.engine;

import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.engine.ActivityStateChangeService;
import ch.sharedvd.tipi.engine.model.ActivityState;
import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.sharedvd.tipi.engine.model.DbSubProcess;
import ch.vd.registre.testing.AssertableBaseTest;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

;

public class ActivityStateChangeServiceTest extends AssertableBaseTest {

    @Test
    public void executing() {
        DbActivity model = new DbActivity();
        model.setState(ActivityState.INITIAL);
        model.setRequestEndExecution(false);

        ActivityStateChangeService.executing(model);

        Assert.assertEquals(ActivityState.EXECUTING, model.getState());
        assertFalse(model.isRequestEndExecution());
    }

    @Test
    public void runnerFinished() {
        DbSubProcess model = new DbSubProcess();
        model.setExecuted(false);
        model.setState(ActivityState.EXECUTING);
        model.setRequestEndExecution(false);

        ActivityStateChangeService.runnerFinished(model, new FinishedActivityResultContext("Bla bla"));

        Assert.assertEquals(ActivityState.WAIT_ON_CHILDREN, model.getState());
        Assert.assertTrue(model.isRequestEndExecution());
    }

    @Test
    public void waitingOnChildren() {
        DbSubProcess model = new DbSubProcess();
        model.setState(ActivityState.WAIT_ON_CHILDREN);
        model.setRequestEndExecution(true);

        ActivityStateChangeService.waitingOnChildren(model);

        Assert.assertEquals(ActivityState.WAIT_ON_CHILDREN, model.getState());
        assertFalse(model.isRequestEndExecution());
    }

    @Test
    public void runnerFinished_Acti() {
        DbActivity model = new DbActivity();
        model.setState(ActivityState.EXECUTING);
        model.setRequestEndExecution(false);

        ActivityStateChangeService.runnerFinished(model, new FinishedActivityResultContext("Message"));

        Assert.assertEquals(ActivityState.FINISHED, model.getState());
        Assert.assertTrue(model.isRequestEndExecution());
    }

    @Test
    public void runnerFinished_SubProc() {
        DbSubProcess model = new DbSubProcess();
        model.setExecuted(true);
        model.setState(ActivityState.EXECUTING);
        model.setRequestEndExecution(false);

        ActivityStateChangeService.runnerFinished(model, new FinishedActivityResultContext("Message"));

        Assert.assertEquals(ActivityState.FINISHED, model.getState());
        Assert.assertTrue(model.isRequestEndExecution());
    }

    @Test
    public void executionEnded() {
        DbSubProcess model = new DbSubProcess();
        model.setState(ActivityState.FINISHED);
        model.setRequestEndExecution(true);

        ActivityStateChangeService.executionEnded(model);

        Assert.assertEquals(ActivityState.FINISHED, model.getState());
        assertFalse(model.isRequestEndExecution());
    }

    @Test
    public void executingFirstActivity() {
        DbSubProcess model = new DbSubProcess();
        model.setState(ActivityState.INITIAL);
        model.setRequestEndExecution(false);

        ActivityStateChangeService.executingFirstActivity(model);

        Assert.assertEquals(ActivityState.EXECUTING, model.getState());
        assertFalse(model.isRequestEndExecution());
    }

}
