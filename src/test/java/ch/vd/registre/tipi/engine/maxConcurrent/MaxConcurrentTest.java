package ch.vd.registre.tipi.engine.maxConcurrent;

import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import org.junit.Test;

public class MaxConcurrentTest extends TipiEngineTest {

    @Test
    public void run() throws Exception {

        final Long pid = doWithLog4jBlocking("ch.vd.registre.tipi", new Log4jBlockingCallback<Long>() {
            @Override
            public Long execute() throws Exception {
                _run();
                return null;
            }
        });
    }

    private void _run() throws Exception {

        final long pid = tipiFacade.launch(MaxConcParentProcess.meta, null);

        {
            while (MaxConcParentProcess.beginStep.get() < 1) {
                Thread.sleep(10);
            }

            MaxConcParentProcess.globalStep.incrementAndGet(); // -> 1

            while (MaxConcParentProcess.endStep.get() < 1) {
                Thread.sleep(10);
            }
            assertEquals(1, MaxConcParentProcess.beginStep.get());
            assertEquals(1, MaxConcParentProcess.endStep.get());
        }

        // Child1
        {
            // Les 2 premiers ont démarré
            while (MaxConcActivity1.beginStep.get() < 1) {
                Thread.sleep(10);
            }
            while (MaxConcActivity2.beginStep.get() < 1) {
                Thread.sleep(10);
            }
            assertEquals(1, MaxConcActivity1.beginStep.get());
            assertEquals(0, MaxConcActivity1.endStep.get());
            assertEquals(1, MaxConcActivity2.beginStep.get());
            assertEquals(0, MaxConcActivity2.endStep.get());
            assertEquals(0, MaxConcActivity3.beginStep.get());
            assertEquals(0, MaxConcActivity3.endStep.get());

            MaxConcParentProcess.globalStep.incrementAndGet(); // -> 2

            while (MaxConcActivity1.endStep.get() < 1) {
                Thread.sleep(10);
            }
            while (MaxConcActivity3.beginStep.get() < 1) {
                Thread.sleep(10);
            }
            assertEquals(1, MaxConcActivity1.endStep.get());
            assertEquals(1, MaxConcActivity2.beginStep.get());
            assertEquals(0, MaxConcActivity2.endStep.get());
            assertEquals(1, MaxConcActivity3.beginStep.get());
            assertEquals(0, MaxConcActivity3.endStep.get());
        }

        // Child2
        {
            MaxConcParentProcess.globalStep.incrementAndGet(); // -> 3

            while (MaxConcActivity2.endStep.get() < 1) {
                Thread.sleep(10);
            }
            assertEquals(1, MaxConcActivity2.beginStep.get());
            assertEquals(1, MaxConcActivity2.endStep.get());
            assertEquals(1, MaxConcActivity3.beginStep.get());
            assertEquals(0, MaxConcActivity3.endStep.get());
        }

        // Child3
        {
            MaxConcParentProcess.globalStep.incrementAndGet(); // -> 4

            while (MaxConcActivity3.endStep.get() < 1) {
                Thread.sleep(10);
            }
            assertEquals(1, MaxConcActivity3.beginStep.get());
            assertEquals(1, MaxConcActivity3.endStep.get());
        }

        // End parent
        {
            while (MaxConcParentProcess.terminatedStep.get() < 1) {
                Thread.sleep(10);
            }
            assertEquals(1, MaxConcParentProcess.terminatedStep.get());
        }

        while (tipiFacade.isRunning(pid)) {
            Thread.sleep(100);
        }
    }

}
