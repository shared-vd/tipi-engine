package ch.sharedvd.tipi.engine.engine.coldStart;

import ch.sharedvd.tipi.engine.command.impl.ColdRestartCommand;
import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import ch.sharedvd.tipi.engine.model.ActivityState;
import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.sharedvd.tipi.engine.runner.TopProcessGroupLauncher;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class TipiColdStarterTest extends TipiEngineTest {

    private static final Logger log = Logger.getLogger(TipiColdStarterTest.class);

    @Test
    public void testRunThenRestart() throws Exception {
        doWithLog4jBlocking("ch.vd.registre.tipi", new Log4jBlockingCallback<Object>() {
            @Override
            public Object execute() throws Exception {
                doJob();
                return null;
            }
        });
    }

    private void doJob() throws Exception {
        ColdParentProcess.globalStep = new AtomicInteger(3);
        final long pid = tipiFacade.launch(ColdParentProcess.meta, null);

        // Attente que l'activité G1A1 soit finie
        {
            boolean end = false;
            while (!end) {
                Thread.sleep(100);
                end = txTemplate.txWith(s -> {
                    DbActivity g1a1 = activityRepository.findOne(ColdGroup1Activity1.id);
                    if ((g1a1 != null)
                            &&
                            g1a1.getState() == ActivityState.FINISHED
                            &&
                            !g1a1.isRequestEndExecution()) {

                        log.info(g1a1);
                        return Boolean.TRUE;
                    }
                    return Boolean.FALSE;
                });
            }
        }

        // Attente que l'activité G1A2 ait envoyé une exception
        {
            boolean end = false;
            while (!end) {
                Thread.sleep(100);
                end = txTemplate.txWith(s -> {
                    DbActivity g1a2 = activityRepository.findOne(ColdGroup1Activity2.id);
                        if ((g1a2 != null)
                                &&
                                g1a2.getState() == ActivityState.ERROR
                                &&
                                !g1a2.isRequestEndExecution()) {

                            log.info(g1a2);
                            return Boolean.TRUE;
                        }
                        return Boolean.FALSE;

                });
            }
        }

        TopProcessGroupLauncher l = groupManager.getLauncher(ColdParentProcess.meta);
        @SuppressWarnings("unused")
        int nb1 = l.getRunningCount();

        while (groupManager.hasActivityPending()
                &&
                commandConsumer.hasCommandPending()) {
            Thread.sleep(10);
        }

        // On remet les activités a pas terminées
        txTemplate.txWithout(s -> {
            final DbActivity g1a1 = activityRepository.findOne(ColdGroup1Activity1.id);
            g1a1.setState(ActivityState.FINISHED);
            g1a1.setRequestEndExecution(true);

            final DbActivity g1a2 = activityRepository.findOne(ColdGroup1Activity2.id);
            g1a2.setState(ActivityState.EXECUTING);
            g1a2.setRequestEndExecution(false);

        });
        ColdGroup1Activity2.sendException = false;

        // Cold restart
        @SuppressWarnings("unused")
        int nb2 = l.getRunningCount();
        log.info("Envoi du ColdRestartCommand");
        commandService.sendCommand(new ColdRestartCommand());
        @SuppressWarnings("unused")
        int nb3 = l.getRunningCount();
        Thread.sleep(100);
        @SuppressWarnings("unused")
        int nb4 = l.getRunningCount();

        group1();

        // L'activité G1A1 doit être reqEnd = false
        txTemplate.txWithout(s -> {
            boolean end = false;
            while (!end) {
                em.clear();
                final DbActivity g1a1 = activityRepository.findOne(ColdGroup1Activity1.id);
                end = !g1a1.isRequestEndExecution();

            }
        });

        group2();

        // Attente de la fin du process
        while (tipiFacade.isRunning(pid))

        {
            Thread.sleep(10);
        }
        while (tipiFacade.hasActivityPending())

        {
            Thread.sleep(10);
        }

    }

    private void group2() throws Exception {

        // G2-A1
        {
            while (ColdGroup2Activity1.beginStep.get() < 1) {
                Thread.sleep(10);
            }
            Assert.assertEquals(1, ColdGroup2Activity1.beginStep.get());
            Assert.assertEquals(0, ColdGroup2Activity1.endStep.get());
            Assert.assertEquals(0, ColdGroup2Activity2.beginStep.get());
            Assert.assertEquals(0, ColdGroup2Activity2.endStep.get());

            ColdParentProcess.globalStep.incrementAndGet(); // -> 5

            while (ColdGroup2Activity1.endStep.get() < 1) {
                Thread.sleep(10);
            }
            while (ColdGroup2Activity2.beginStep.get() < 1) {
                Thread.sleep(10);
            }
            Assert.assertEquals(1, ColdGroup2Activity1.beginStep.get());
            Assert.assertEquals(1, ColdGroup2Activity1.endStep.get());
            // Vérifie les autres
            Assert.assertEquals(1, ColdGroup2Activity2.beginStep.get());
            Assert.assertEquals(0, ColdGroup2Activity2.endStep.get());
        }

        // G2-A2
        {
            while (ColdGroup2Activity2.beginStep.get() < 1) {
                Thread.sleep(10);
            }

            ColdParentProcess.globalStep.incrementAndGet(); // -> 6

            while (ColdGroup2Activity2.endStep.get() < 1) {
                Thread.sleep(10);
            }
            while (ColdGroup2Activity2.beginStep.get() < 1) {
                Thread.sleep(10);
            }
            Assert.assertEquals(1, ColdGroup2Activity1.beginStep.get());
            Assert.assertEquals(1, ColdGroup2Activity1.endStep.get());
            // Vérifie les autres
            Assert.assertEquals(1, ColdGroup2Activity2.beginStep.get());
            Assert.assertEquals(1, ColdGroup2Activity2.endStep.get());
        }
    }

    private void group1() throws Exception {

        // Parent
        {
            Assert.assertEquals(1, ColdParentProcess.beginStep.get());
            Assert.assertEquals(1, ColdParentProcess.endStep.get());
        }

        // G1-A1 + G1-A3
        {
            Assert.assertEquals(1, ColdGroup1Activity1.beginStep.get());
            Assert.assertEquals(1, ColdGroup1Activity1.endStep.get());
            Assert.assertEquals(1, ColdGroup1Activity3.beginStep.get());
            Assert.assertEquals(1, ColdGroup1Activity3.endStep.get());
        }

        // G1-A2
        {
            Assert.assertEquals(0, ColdGroup1Activity2.endStep.get());

            // On vérifie que les groupe2 n'ont pas commencé
            {
                Assert.assertEquals(0, ColdGroup2Activity1.beginStep.get());
                Assert.assertEquals(0, ColdGroup2Activity1.endStep.get());
                Assert.assertEquals(0, ColdGroup2Activity2.beginStep.get());
                Assert.assertEquals(0, ColdGroup2Activity2.endStep.get());
            }

            ColdParentProcess.globalStep.incrementAndGet();

            while (ColdGroup1Activity2.endStep.get() < 1) {
                Thread.sleep(10);
            }
            Assert.assertEquals(2, ColdGroup1Activity2.beginStep.get());
            Assert.assertEquals(1, ColdGroup1Activity2.endStep.get());
        }
    }

}
