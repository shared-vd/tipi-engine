package ch.sharedvd.tipi.engine.engine.maxConcurrent;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.client.VariableMap;
import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class MaxConcurrentTopProcessTest extends TipiEngineTest {

    public static class MyProcess extends TopProcess {

        public static AtomicInteger globalStep = new AtomicInteger(0);
        public static AtomicInteger beginStep = new AtomicInteger(0);
        public static AtomicInteger endStep = new AtomicInteger(0);
        public static AtomicInteger terminatedStep = new AtomicInteger(0);

        public final static TopProcessMetaModel meta = new TopProcessMetaModel(MyProcess.class, 1, -1, 1, null);

        @Override
        public ActivityResultContext execute() throws Exception {
            beginStep.incrementAndGet();

            int threshold = getIntVariable("threshold").intValue();

            while (globalStep.get() < threshold) {
                Thread.sleep(10);
            }

            endStep.incrementAndGet();
            return new FinishedActivityResultContext();
        }

        @Override
        protected ActivityResultContext terminate() throws Exception {
            terminatedStep.incrementAndGet();
            return super.terminate();
        }

    }

    @Test
    public void run() throws Exception {

        VariableMap vars = new VariableMap();
        vars.put("threshold", 1);
        long p1 = tipiFacade.launch(MyProcess.meta, vars);

        vars = new VariableMap();
        vars.put("threshold", 2);
        long p2 = tipiFacade.launch(MyProcess.meta, vars);

        {
            // Le premier démarre
            while (MyProcess.beginStep.get() < 1) {
                Thread.sleep(10);
            }
            Assert.assertEquals(1, MyProcess.beginStep.get());

            // On lui permet de continuer...
            MyProcess.globalStep.incrementAndGet(); // -> 1

            // Le premier se termine
            while (MyProcess.endStep.get() < 1) {
                Thread.sleep(10);
            }
            Assert.assertEquals(1, MyProcess.endStep.get());

            // Le deuxième démarre
            while (MyProcess.beginStep.get() < 2) {
                Thread.sleep(10);
            }
            Assert.assertEquals(2, MyProcess.beginStep.get());

            // On lui permet de continuer...
            MyProcess.globalStep.incrementAndGet(); // -> 2

            // Le deuxième se termine
            while (MyProcess.endStep.get() < 2) {
                Thread.sleep(10);
            }
            Assert.assertEquals(2, MyProcess.endStep.get());

            // On attend la fin de la fin
            waitWhileRunning(p1, 5000);
            waitWhileRunning(p2, 5000);
//            while (tipiFacade.isRunning(p1) || tipiFacade.isRunning(p2)) {
//                Thread.sleep(10);
//            }
            Assert.assertEquals(2, MyProcess.terminatedStep.get());
        }
    }
}
