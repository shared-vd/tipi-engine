package ch.sharedvd.tipi.engine.engine;

import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import ch.sharedvd.tipi.engine.engine.aborting.AbortManagerActivity;
import ch.sharedvd.tipi.engine.engine.aborting.AbortManagerProcess;
import ch.sharedvd.tipi.engine.engine.aborting.AbortingActivity;
import ch.sharedvd.tipi.engine.engine.aborting.AbortingProcess;
import ch.sharedvd.tipi.engine.model.ActivityState;
import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.sharedvd.tipi.engine.model.DbTopProcess;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class AbortingTest extends TipiEngineTest {

    @Test
    @SuppressWarnings("unchecked")
    public void abortProcessThroughManager() throws Exception {

        // Démarrage et attente
        final long pid = tipiFacade.launch(AbortManagerProcess.meta, null);
        // On attends que la sub activity ait démarré et attende
        int steps = 0;
        while (AbortManagerActivity.count.get() == 0 && steps < 500) {
            Thread.sleep(10);
            steps++;
        }
        Assert.assertTrue(steps < 500);

        // On abort le process
        tipiFacade.abortProcess(pid, false);
        // On attends qu'il se finisse
        waitWhileRunning(pid, 5000);

        // On attends que toutes les activités se finissent
        boolean end = false;
        while (!end) {
            end = txTemplate.txWith(s -> {
                List<DbActivity> actis = activityRepository.findAll();
                boolean hasNotFinished = false;
                for (DbActivity a : actis) {
                    if (a.getState() != ActivityState.ABORTED) {
                        hasNotFinished = true;
                    }
                }
                return !hasNotFinished;
            }).booleanValue();
        }
    }

    @Test
    public void abortBigProcess() throws Exception {

        // Démarrage et attente
        final long pid = tipiFacade.launch(AbortingProcess.meta, null);

        // On attends qu'il y ait au moins 10 activités créées
        int steps = 0;
        while (AbortingActivity.count.intValue() < 10 && steps < 500) {
            Thread.sleep(10);
            steps++;
        }
        Assert.assertTrue(steps < 500);

        // Abort
        tipiFacade.abortProcess(pid, false);
        waitWhileRunning(pid, 5000);

        // Vérification
        txTemplate.txWithout(s -> {
            DbTopProcess tp = topProcessRepository.findById(pid).orElse(null);
            Assert.assertEquals(ActivityState.ABORTED, tp.getState());

        });

    }

}
