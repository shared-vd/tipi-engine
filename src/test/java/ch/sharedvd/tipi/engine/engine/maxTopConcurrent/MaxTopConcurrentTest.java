package ch.sharedvd.tipi.engine.engine.maxTopConcurrent;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.client.TipiTopProcess;
import ch.sharedvd.tipi.engine.client.VariableMap;
import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

;

/**
 * Cette classe teste le comportement du paramètre <i>nbMaxTopConcurrent</i> des top-process Tipi.
 */
public class MaxTopConcurrentTest extends TipiEngineTest {

    private static final String RESULTS_KEY = "RESULTS";
    private static final String WAIT_LOCK_KEY = "WAIT_LOCK";

    private static final Map<String, Object> datastore = new HashMap<>();

    @TipiTopProcess(description = "Job avec nbMaxTopConcurrent=1", nbMaxTopConcurrent = 1)
    public static class MaxTopOneJob extends TopProcess {

        @Override
        protected ActivityResultContext execute() throws Exception {

            // on attend sur un lock, si nécessaire
            final String lockKey = getStringVariable(WAIT_LOCK_KEY);
            if (lockKey != null) {
                final MutableBoolean lock = (MutableBoolean) datastore.get(lockKey);
                if (lock != null) {
                    //noinspection SynchronizationOnLocalVariableOrMethodParameter
                    synchronized (lock) {
                        while (lock.booleanValue()) {
                            lock.wait();
                        }
                    }
                }
            }

            // on stocke un pseudo-résultat dans le datastore global (pour simuler la database dans le cas ordinaire)
            final String key = getStringVariable(RESULTS_KEY);
            datastore.put(key, true);

            return new FinishedActivityResultContext();
        }
    }

    /**
     * Ce test vérifie qu'il est possible de démarrer un top-process avec maxTopConcurrent=1 lorsqu'il n'y aucun autre top-process qui tourne.
     */
    @Test
    public void testLaunchOneTopProcess() throws Exception {

        // on démarre le process
        final long id = tipiFacade.launch(MaxTopOneJob.class, new VariableMap(
                RESULTS_KEY, "testLaunchOneTopProcess"));

        // on attend le fin d'exécution
        while (tipiFacade.isRunning(id)) {
            Thread.sleep(100);
        }

        // on vérifie que tout s'est bien passé
        final Boolean runned = (Boolean) datastore.get("testLaunchOneTopProcess");
        Assert.assertTrue(runned != null && runned);
    }

    /**
     * Ce test vérifie qu'il est possible de démarrer deux top-process avec maxTopConcurrent=1, l'un à la suite de l'autre.
     */
    @Test
    public void testLaunchTwoTopProcessesSequentially() throws Exception {

        // démarrage du premier top process
        {
            // on démarre le process
            final long id = tipiFacade.launch(MaxTopOneJob.class, new VariableMap(
                    RESULTS_KEY, "testLaunchTwoTopProcessesSequentially1"));

            // on attend le fin d'exécution
            while (tipiFacade.isRunning(id)) {
                Thread.sleep(100);
            }

            // on vérifie que tout s'est bien passé
            final Boolean runned = (Boolean) datastore.get("testLaunchTwoTopProcessesSequentially1");
            Assert.assertTrue(runned != null && runned);
        }

        // démarrage du second top process
        {
            // on démarre le process
            final long id = tipiFacade.launch(MaxTopOneJob.class, new VariableMap(
                    RESULTS_KEY, "testLaunchTwoTopProcessesSequentially2"));

            // on attend le fin d'exécution
            while (tipiFacade.isRunning(id)) {
                Thread.sleep(100);
            }

            // on vérifie que tout s'est bien passé
            final Boolean runned = (Boolean) datastore.get("testLaunchTwoTopProcessesSequentially2");
            Assert.assertTrue(runned);
        }
    }

    /**
     * Ce test vérifie qu'il n'est pas possible de démarrer deux top-process avec maxTopConcurrent=1 de manière concurrente (= tipi va sérializer leurs exécutions)
     */
    @Test
    public void testLaunchTwoTopProcessesConcurrently() throws Exception {

        final MutableBoolean process1Lock = new MutableBoolean(true);
        datastore.put("process1Lock", process1Lock);

        // on démarre le premier process
        final long id1 = tipiFacade.launch(MaxTopOneJob.class, new VariableMap(
                RESULTS_KEY, "testLaunchTwoTopProcessesSequentially1",
                WAIT_LOCK_KEY, "process1Lock"));

        // on démarre le second process
        final long id2 = tipiFacade.launch(MaxTopOneJob.class, new VariableMap(
                RESULTS_KEY, "testLaunchTwoTopProcessesSequentially2"));

        // on laisse un peu de temps à Tipi pour tenter de démarrer le second process
        Thread.sleep(2000);
        Assert.assertTrue(tipiFacade.isRunning(id1)); // le premier process doit être démarré
        Assert.assertTrue(tipiFacade.isRunning(id2)); // le second process doit aussi être démarré (dans le sens : une demande de démarrage a été faite)
        Assert.assertTrue(tipiFacade.isProcessScheduled(id1)); // le premier process doit être en cours d'exécution
        Assert.assertFalse(tipiFacade.isProcessScheduled(id2)); // le second process ne doit pas être en cours d'exécution

        // on libère le lock du premier process
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (process1Lock) {
            process1Lock.setValue(false);
            process1Lock.notifyAll();
        }

        // on attend le fin d'exécution du premier process
        while (tipiFacade.isRunning(id1)) {
            Thread.sleep(100);
        }

        // on attend le fin d'exécution du second process
        while (tipiFacade.isRunning(id2)) {
            Thread.sleep(100);
        }

        // on vérifie que tout s'est bien passé pour le premier process
        final Boolean runned1 = (Boolean) datastore.get("testLaunchTwoTopProcessesSequentially1");
        Assert.assertTrue(runned1);

        // on vérifie que tout s'est bien passé pour le second process
        final Boolean runned2 = (Boolean) datastore.get("testLaunchTwoTopProcessesSequentially2");
        Assert.assertTrue(runned2);
    }
}
