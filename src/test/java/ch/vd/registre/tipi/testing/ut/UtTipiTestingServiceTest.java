package ch.vd.registre.tipi.testing.ut;

import ch.sharedvd.tipi.engine.client.VariableMap;
import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import org.junit.Test;

public class UtTipiTestingServiceTest extends TipiEngineTest {

    @Test
    public void waitEndAllActivities_NOT_WaitIfEmpty() throws Exception {
        UtTipiTestingServiceTestProcess.counter.set(0);

        final VariableMap vars = new VariableMap();
        tipiFacade.launch(UtTipiTestingServiceTestProcess.class, vars);

        while (UtTipiTestingServiceTestProcess.counter.get() == 0) {
            Thread.sleep(10);
        }

        UtTipiTestingServiceTestProcess.counter.set(2);
        tts.waitEndAllActivitiesNoAssertIfError();

        assertEquals(3, UtTipiTestingServiceTestProcess.counter.get());
    }

    private class ProcessStarter implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(1000);

                final VariableMap vars = new VariableMap();
                tipiFacade.launch(UtTipiTestingServiceTestProcess.class, vars);

                while (UtTipiTestingServiceTestProcess.counter.get() == 0) {
                    Thread.sleep(10);
                }

                UtTipiTestingServiceTestProcess.counter.set(2);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
