package ch.vd.registre.tipi.engine;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import ch.sharedvd.tipi.engine.model.ActivityState;
import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.vd.registre.base.tx.TxCallbackWithoutResult;
import org.junit.Test;
import org.springframework.transaction.TransactionStatus;

public class NbRetryTest extends TipiEngineTest {

    public static class NbRetryProcess extends TopProcess {

        public final static TopProcessMetaModel meta = new TopProcessMetaModel(NbRetryProcess.class, 6, -1, 10, null);

        @Override
        public ActivityResultContext execute() throws Exception {
            throw new RuntimeException("TEST: une erreur");
        }
    }

    @Test
    public void nbRetry() throws Exception {

        final Long pid = doWithLog4jBlocking("ch.vd.registre.tipi", new Log4jBlockingCallback<Long>() {
            @Override
            public Long execute() throws Exception {
                final long pid = tipiFacade.launch(NbRetryProcess.meta, null);
                while (tipiFacade.isRunning(pid)) {
                    Thread.sleep(100);
                }
                return pid;
            }
        });

        doInTransaction(new TxCallbackWithoutResult() {
            @Override
            public void execute(TransactionStatus status) throws Exception {

                DbActivity model = persist.get(DbActivity.class, pid);
                assertEquals(2, model.getNbRetryDone());
                assertEquals(ActivityState.ERROR, model.getState());

                // Call stack
                assertContains("TEST: une erreur", model.getCallstack());
                assertContains("NbRetryTest$NbRetryProcess.execute", model.getCallstack());
                assertContains("ActivityRunner.executeActivity", model.getCallstack());
            }
        });
    }

}
