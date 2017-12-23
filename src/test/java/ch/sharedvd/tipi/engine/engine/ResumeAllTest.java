package ch.sharedvd.tipi.engine.engine;

import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import ch.sharedvd.tipi.engine.engine.resumeall.ResumeAllActivity;
import ch.sharedvd.tipi.engine.engine.resumeall.ResumeAllProcess;
import ch.sharedvd.tipi.engine.model.ActivityState;
import ch.sharedvd.tipi.engine.model.DbActivity;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ResumeAllTest extends TipiEngineTest {

    @Test
    public void resumeAllErrors() throws Exception {
        resumeAll(ActivityState.ERROR);
    }

    @Test
    public void resumeAllSuspendeds() throws Exception {
        resumeAll(ActivityState.SUSPENDED);
    }

    @SuppressWarnings("unchecked")
    private void resumeAll(final ActivityState stateToResume) throws Exception {

        ResumeAllActivity.nbCalled = new AtomicInteger(0);
        ResumeAllActivity.nbFinished = new AtomicInteger(0);
        ResumeAllActivity.mode = (ActivityState.SUSPENDED == stateToResume) ? 1 : 2; // suspended : error

        final long pid = tipiFacade.launch(ResumeAllProcess.meta, null);
        // On attends que le processus soit créé et que les acti soient runnées
        while (ResumeAllActivity.nbCalled.get() < 2) {
            Thread.sleep(20);
        }

        // On attends que tous les child soient suspended/en erreur
        {
            boolean ready = false;
            while (!ready) {
                Thread.sleep(20);
                ready = txTemplate.txWith(s -> {
                    List<DbActivity> actis = activityRepository.findByRequestEndExecutionOrderById(false);
                    boolean wait = false;
                    int suspErr = 0;
                    for (DbActivity acti : actis) {
                        if (ActivityState.WAIT_ON_CHILDREN == acti.getState()) {
                            wait = true; // le top process
                        } else if (stateToResume == acti.getState()) {
                            suspErr++;
                        }
                    }
                    return wait && suspErr == 2;
                }).booleanValue();
            }
        }
        Assert.assertEquals(2, ResumeAllActivity.nbCalled.get());

        ResumeAllActivity.mode = 3; // Finished

        // Resume all
        if (ActivityState.SUSPENDED == stateToResume) {
            tipiFacade.resumeAllSuspended();
        } else {
            tipiFacade.resumeAllError();
        }
        Thread.sleep(50);
        while (tipiFacade.isRunning(pid)) {
            Thread.sleep(10);
        }
        Thread.sleep(50);
        while (ResumeAllActivity.nbFinished.get() < 2) {
            Thread.sleep(10);
        }
        Thread.sleep(50);
        Assert.assertEquals(4, ResumeAllActivity.nbCalled.get());
    }
}
