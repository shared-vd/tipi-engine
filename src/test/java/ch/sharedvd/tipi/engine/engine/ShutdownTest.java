package ch.sharedvd.tipi.engine.engine;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.command.CommandConsumer;
import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicInteger;

;

@Ignore
public class ShutdownTest extends TipiEngineTest {

    @Autowired
    private CommandConsumer consumer;

    public static class ShutProcess extends TopProcess {

        public static boolean blockWithSleep = true;
        public static AtomicInteger globalStep = new AtomicInteger(0);
        public static AtomicInteger beginStep = new AtomicInteger(0);
        public static AtomicInteger endStep = new AtomicInteger(0);
        public static boolean throwException = false;

        public final static TopProcessMetaModel meta = new TopProcessMetaModel(ShutProcess.class, 6, -1, 10, null);

        @Override
        public ActivityResultContext execute() throws Exception {
            beginStep.incrementAndGet();

            // La première fois il bloque, la 2eme fois, il passe
            while (beginStep.get() == 1) {
                if (blockWithSleep) {
                    Thread.sleep(10);
                }
            }
            // La première fois il bloque, la 2eme fois, il passe
            while (globalStep.get() < 1) {
                if (blockWithSleep) {
                    Thread.sleep(10);
                }
            }

            endStep.incrementAndGet();

            if (throwException) {
                throw new RuntimeException("TEST : pour débloquer le thread sans le finir");
            }
            return new FinishedActivityResultContext();
        }
    }

    @Test
    public void shutdownWhileNotSleeping() throws Exception {
        ShutProcess.blockWithSleep = false;
        ShutProcess.globalStep = new AtomicInteger(0);
        ShutProcess.beginStep = new AtomicInteger(0);
        ShutProcess.endStep = new AtomicInteger(0);

        final long pid = tipiFacade.launch(ShutProcess.meta, null);
        while (ShutProcess.beginStep.get() < 1) {
            Thread.sleep(10);
        }
        Assert.assertTrue(tipiFacade.hasActivityPending());
        Assert.assertEquals(1, ShutProcess.beginStep.get());
        Assert.assertEquals(0, ShutProcess.endStep.get());

        starter.stop();
        Assert.assertFalse(tipiFacade.isTipiStarted());

        // On arrete le process qui est en boucle infinie
        ShutProcess.throwException = true;
        ShutProcess.beginStep.incrementAndGet();
        ShutProcess.globalStep.incrementAndGet();
        while (ShutProcess.endStep.get() < 1) {
            Thread.sleep(10);
        }
        Assert.assertFalse(tipiFacade.hasActivityPending());

        ShutProcess.throwException = false;
        ShutProcess.globalStep = new AtomicInteger(0);
        ShutProcess.beginStep = new AtomicInteger(1);
        ShutProcess.endStep = new AtomicInteger(0);
        commandConsumer.setResumeTipiAtBoot(true);
        starter.start(); // Fais un cold restart
        while (ShutProcess.beginStep.get() < 2) {
            Thread.sleep(10);
        }

        Assert.assertEquals(2, ShutProcess.beginStep.get());
        Assert.assertEquals(0, ShutProcess.endStep.get());

        while (tipiFacade.hasCommandPending()) {
            Thread.sleep(10);
        }
        Assert.assertTrue(tipiFacade.hasActivityPending());
        ShutProcess.globalStep.incrementAndGet(); // -> 1
        while (ShutProcess.endStep.get() < 1) {
            Thread.sleep(10);
        }
        Assert.assertEquals(2, ShutProcess.beginStep.get());
        Assert.assertEquals(1, ShutProcess.endStep.get());

        // Fin du process
        while (tipiFacade.isRunning(pid)) {
            Thread.sleep(10);
        }
        while (tipiFacade.hasActivityPending()) {
            Thread.sleep(10);
        }
        while (tipiFacade.hasCommandPending()) {
            Thread.sleep(10);
        }
    }

    @Test
    public void shutdownAndColdRestart() throws Exception {
        ShutProcess.blockWithSleep = true;
        ShutProcess.globalStep = new AtomicInteger(0);
        ShutProcess.beginStep = new AtomicInteger(0);
        ShutProcess.endStep = new AtomicInteger(0);

        final long pid = tipiFacade.launch(ShutProcess.meta, null);
        while (ShutProcess.beginStep.get() < 1) {
            Thread.sleep(10);
        }
        Assert.assertTrue(tipiFacade.hasActivityPending());
        Assert.assertEquals(1, ShutProcess.beginStep.get());
        Assert.assertEquals(0, ShutProcess.endStep.get());

        starter.stop();
        // On attend que l'activité se termine.
        while (tipiFacade.hasActivityPending()) {
            Thread.sleep(10);
        }
        starter.start(); // Fais un cold restart
        while (ShutProcess.beginStep.get() < 2) {
            Thread.sleep(10);
        }
        Assert.assertTrue(tipiFacade.hasActivityPending());
        ShutProcess.globalStep.incrementAndGet(); // -> 1
        while (ShutProcess.endStep.get() < 1) {
            Thread.sleep(10);
        }
        Assert.assertEquals(2, ShutProcess.beginStep.get());
        Assert.assertEquals(1, ShutProcess.endStep.get());

        // Fin du process
        while (tipiFacade.isRunning(pid)) {
            Thread.sleep(10);
        }
        while (tipiFacade.hasActivityPending()) {
            Thread.sleep(10);
        }
    }

    @Test
    public void stopStartThenRun() throws Exception {
        ShutProcess.blockWithSleep = true;
        ShutProcess.globalStep = new AtomicInteger(1);
        ShutProcess.beginStep = new AtomicInteger(1);
        ShutProcess.endStep = new AtomicInteger(0);

        {
            final long pid = tipiFacade.launch(ShutProcess.meta, null);
            while (tipiFacade.isRunning(pid)) {
                Thread.sleep(10);
            }
        }

        starter.stop();
        starter.start();

        {
            ShutProcess.blockWithSleep = true;
            ShutProcess.globalStep = new AtomicInteger(0);
            ShutProcess.beginStep = new AtomicInteger(1);
            ShutProcess.endStep = new AtomicInteger(0);
            final long pid = tipiFacade.launch(ShutProcess.meta, null);
            while (ShutProcess.beginStep.get() < 2) {
                Thread.sleep(10);
            }
            Assert.assertEquals(2, ShutProcess.beginStep.get());

            ShutProcess.globalStep.incrementAndGet();

            while (ShutProcess.endStep.get() < 1) {
                Thread.sleep(10);
            }
            Assert.assertEquals(1, ShutProcess.endStep.get());

            // Fin du process
            while (tipiFacade.isRunning(pid)) {
                Thread.sleep(10);
            }
        }
    }

}
