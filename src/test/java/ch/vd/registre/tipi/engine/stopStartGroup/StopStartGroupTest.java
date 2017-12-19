package ch.vd.registre.tipi.engine.stopStartGroup;

import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import org.junit.Test;

public class StopStartGroupTest extends TipiEngineTest {

    @Test
    public void run() throws Exception {

        final long pid = tipiFacade.launch(StopStartGroupParentProcess.meta, null);

        {
            while (StopStartGroupParentProcess.beginStep.get() < 1) {
                Thread.sleep(10);
            }

            StopStartGroupParentProcess.globalStep.incrementAndGet(); // -> 1

            while (StopStartGroupParentProcess.endStep.get() < 1) {
                Thread.sleep(10);
            }
            assertEquals(1, StopStartGroupParentProcess.beginStep.get());
            assertEquals(1, StopStartGroupParentProcess.endStep.get());
        }

        // Child1
        {
            // Les 2 premiers ont démarré
            while (StopStartGroupActivity1.beginStep.get() < 1) {
                Thread.sleep(10);
            }
            while (StopStartGroupActivity2.beginStep.get() < 1) {
                Thread.sleep(10);
            }
            assertEquals(1, StopStartGroupActivity1.beginStep.get());
            assertEquals(0, StopStartGroupActivity1.endStep.get());
            assertEquals(1, StopStartGroupActivity2.beginStep.get());
            assertEquals(0, StopStartGroupActivity2.endStep.get());
            assertEquals(0, StopStartGroupActivity3.beginStep.get());
            assertEquals(0, StopStartGroupActivity3.endStep.get());

            tipiFacade.stopGroup(StopStartGroupParentProcess.meta.getFQN());

            StopStartGroupParentProcess.globalStep.incrementAndGet(); // -> 2

            // On attends la fin de 1
            while (StopStartGroupActivity1.endStep.get() < 1) {
                Thread.sleep(10);
            }
            assertEquals(1, StopStartGroupActivity1.endStep.get());
            assertEquals(1, StopStartGroupActivity2.beginStep.get());
            assertEquals(0, StopStartGroupActivity2.endStep.get());
            assertEquals(0, StopStartGroupActivity3.beginStep.get());
            assertEquals(0, StopStartGroupActivity3.endStep.get());
        }

        // Child2
        {
            StopStartGroupParentProcess.globalStep.incrementAndGet(); // -> 3

            while (StopStartGroupActivity2.endStep.get() < 1) {
                Thread.sleep(10);
            }
            assertEquals(1, StopStartGroupActivity2.beginStep.get());
            assertEquals(1, StopStartGroupActivity2.endStep.get());
            assertEquals(0, StopStartGroupActivity3.beginStep.get());
            assertEquals(0, StopStartGroupActivity3.endStep.get());
        }

        // Child3
        {
            // Le 3eme est pas démarré parce que le groupe est stopped
            assertEquals(0, StopStartGroupActivity3.beginStep.get());
            assertEquals(0, StopStartGroupActivity3.endStep.get());

            // On restart le groupe 3
            tipiFacade.startGroup(StopStartGroupParentProcess.meta.getFQN());

            while (StopStartGroupActivity3.beginStep.get() < 1) {
                Thread.sleep(10);
            }
            assertEquals(1, StopStartGroupActivity3.beginStep.get());
            assertEquals(0, StopStartGroupActivity3.endStep.get());

            StopStartGroupParentProcess.globalStep.incrementAndGet(); // -> 4

            while (StopStartGroupActivity3.endStep.get() < 1) {
                Thread.sleep(10);
            }
            assertEquals(1, StopStartGroupActivity3.beginStep.get());
            assertEquals(1, StopStartGroupActivity3.endStep.get());
        }

        // End parent
        {
            while (StopStartGroupParentProcess.terminatedStep.get() < 1) {
                Thread.sleep(10);
            }
            assertEquals(1, StopStartGroupParentProcess.terminatedStep.get());
        }

        while (tipiFacade.isRunning(pid)) {
            Thread.sleep(100);
        }
    }

}
