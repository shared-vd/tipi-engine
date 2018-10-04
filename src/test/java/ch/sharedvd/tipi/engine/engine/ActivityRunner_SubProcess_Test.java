package ch.sharedvd.tipi.engine.engine;

import ch.sharedvd.tipi.engine.action.*;
import ch.sharedvd.tipi.engine.client.VariableMap;
import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import ch.sharedvd.tipi.engine.meta.VariableDescription;
import ch.sharedvd.tipi.engine.meta.VariableType;
import ch.sharedvd.tipi.engine.model.ActivityState;
import ch.sharedvd.tipi.engine.model.DbTopProcess;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertFalse;

public class ActivityRunner_SubProcess_Test extends TipiEngineTest {

    public static class ProcTstSubProcess extends TopProcess {
        public static final TopProcessMetaModel meta = new TopProcessMetaModel(ProcTstSubProcess.class,
                new VariableDescription[]{
                        new VariableDescription("mode", VariableType.Integer)
                }, null,
                2, -1, 10, null, true) {
            @Override
            protected void init() {
                setDeleteWhenFinished(false);
            }
        };

        public static AtomicInteger nb = new AtomicInteger(0);

        @Override
        protected ActivityResultContext execute() throws Exception {
            int mode = getIntVariable("mode");
            if (mode == 1) {
                // OK
                return new FinishedActivityResultContext();
            } else if (mode == 2) {
                throw new ActivityException("Error");
            } else if (mode == 3) {
                return new ErrorActivityResultContext("Error");
            } else if (mode == 4) {
                nb.incrementAndGet();
                if (nb.get() == 1) {
                    // Exception
                    throw new RuntimeException("Erreur de test");
                }
                return new FinishedActivityResultContext();
            } else if (mode == 5) {
                throw new RuntimeException("Erreur de test");
            }
            return new FinishedActivityResultContext();
        }
    }

    @Test
    public void executeException_Mode4() throws Exception {
        ProcTstSubProcess.nb = new AtomicInteger(0);

        final Long pid = doWithLoggingBlocked("ch.sharedvd.tipi.engine", new Log4jBlockingCallback<Long>() {
            @Override
            public Long execute() throws Exception {
                VariableMap vars = new VariableMap();
                vars.put("mode", 4);
                final long pid = tipiFacade.launch(ProcTstSubProcess.meta, vars);
                while (ProcTstSubProcess.nb.get() < 2) {
                    Thread.sleep(10);
                }
                return pid;
            }
        });
        Assert.assertEquals(2, ProcTstSubProcess.nb.get());

        waitWhileRunning(pid, 5000);

        txTemplate.txWithout(s -> {
            DbTopProcess model = topProcessRepository.findById(pid).orElse(null);
            Assert.assertEquals(ActivityState.FINISHED, model.getState());
            assertFalse(model.isRequestEndExecution());
            Assert.assertEquals(1, model.getNbRetryDone());
        });
    }

    @Test
    public void executeError_Mode2() throws Exception {
        ProcTstSubProcess.nb = new AtomicInteger(0);

        VariableMap vars = new VariableMap();
        vars.put("mode", 2);
        final long pid = tipiFacade.launch(ProcTstSubProcess.meta, vars);
        waitWhileRunning(pid, 5000);

        txTemplate.txWithout(s -> {
            DbTopProcess model = topProcessRepository.findById(pid).orElse(null);
            Assert.assertEquals(ActivityState.ERROR, model.getState());
            assertFalse(model.isRequestEndExecution());
            Assert.assertEquals(0, model.getNbRetryDone());

        });
    }

    @Test
    public void executeActivityException_Mode3() throws Exception {
        ProcTstSubProcess.nb = new AtomicInteger(0);

        VariableMap vars = new VariableMap();
        vars.put("mode", 3);
        final long pid = tipiFacade.launch(ProcTstSubProcess.meta, vars);
        waitWhileRunning(pid, 5000);

        txTemplate.txWithout(s -> {
            DbTopProcess model = topProcessRepository.findById(pid).orElse(null);
            Assert.assertEquals(ActivityState.ERROR, model.getState());
            assertFalse(model.isRequestEndExecution());
            Assert.assertEquals(0, model.getNbRetryDone());

        });
    }

    @Test
    public void executeNormal_Mode_1() throws Exception {
        ProcTstSubProcess.nb = new AtomicInteger(0);

        final Long pid = doWithLoggingBlocked("ch.sharedvd.tipi.engine", new Log4jBlockingCallback<Long>() {
            @Override
            public Long execute() throws Exception {
                VariableMap vars = new VariableMap();
                vars.put("mode", 1);
                final long pid = tipiFacade.launch(ProcTstSubProcess.meta, vars);
                waitWhileRunning(pid, 5000);
                return pid;
            }
        });

        txTemplate.txWithout(s -> {
            DbTopProcess model = topProcessRepository.findById(pid).orElse(null);
            Assert.assertEquals(ActivityState.FINISHED, model.getState());
            assertFalse(model.isRequestEndExecution());
            Assert.assertEquals(0, model.getNbRetryDone());

        });
    }

}
