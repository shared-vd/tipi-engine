package ch.vd.registre.tipi.engine;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.SuspendedActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.client.VariableMap;
import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import ch.sharedvd.tipi.engine.model.ActivityState;
import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.vd.registre.base.tx.TxCallbackWithoutResult;
import org.junit.Test;
import org.springframework.transaction.TransactionStatus;

public class ResumingTest extends TipiEngineTest {

    public static class ResumingProcess extends TopProcess {

        public static int value = 0;

        public static final TopProcessMetaModel meta = new TopProcessMetaModel(ResumingProcess.class, 6, -1, 10, null) {
            @Override
            protected void init() {
                setDeleteWhenFinished(false);
            }
        };

        @Override
        protected ActivityResultContext execute() throws Exception {
            value++;

            if (getIntVariable("correl") == null) {
                return new SuspendedActivityResultContext("blabla");
            }

            while (value < 3) {
                Thread.sleep(10);
            }

            return new FinishedActivityResultContext();
        }
    }

    @Test
    public void resume() throws Exception {

        final long pid = tipiFacade.launch(ResumingProcess.meta, null);
        while (tipiFacade.isRunning(pid)) {
            Thread.sleep(10);
        }
        assertEquals(1, ResumingProcess.value);

        doInTransaction(new TxCallbackWithoutResult() {
            @Override
            public void execute(TransactionStatus status) throws Exception {

                DbActivity model = persist.get(DbActivity.class, pid);
                assertEquals(ActivityState.SUSPENDED, model.getState());
                assertEquals("blabla", model.getCorrelationId());
            }
        });

        final VariableMap vars = new VariableMap();
        vars.put("correl", 42);
        tipiFacade.resume(pid, vars);
        while (!tipiFacade.isRunning(pid)) {
            Thread.sleep(10);
        }
        while (ResumingProcess.value < 2) {
            Thread.sleep(10);
        }
        assertEquals(2, ResumingProcess.value);
        ResumingProcess.value = 3;
        while (tipiFacade.isRunning(pid)) {
            Thread.sleep(10);
        }

        doInTransaction(new TxCallbackWithoutResult() {
            @Override
            public void execute(TransactionStatus status) throws Exception {

                DbActivity model = persist.get(DbActivity.class, pid);
                assertEquals(ActivityState.FINISHED, model.getState());
                assertEquals("blabla", model.getCorrelationId());
                assertEquals(42, model.getVariable("correl"));
            }
        });
    }

}