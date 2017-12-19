package ch.vd.registre.tipi.engine;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.vd.registre.base.tx.TxCallbackWithoutResult;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;

;

public class SessionClearTest extends TipiEngineTest {

    public static class SessionClearProcess extends TopProcess {

        @Autowired
        private SessionFactory sessionFactory;

        public static final TopProcessMetaModel meta = new TopProcessMetaModel(SessionClearProcess.class, 2, -1, 10, null) {
            @Override
            protected void init() {
                setDeleteWhenFinished(false);
            }

            ;
        };

        @Override
        protected ActivityResultContext execute() throws Exception {

            sessionFactory.getCurrentSession().clear();

            facade.putVariable("bla", "bli");

            return new FinishedActivityResultContext();
        }
    }

    @Test
    public void clear() throws Exception {

        final long pid = tipiFacade.launch(SessionClearProcess.meta, null);
        while (tipiFacade.isRunning(pid)) {
            Thread.sleep(10);
        }

        doInTransaction(new TxCallbackWithoutResult() {
            @Override
            public void execute(TransactionStatus status) throws Exception {
                DbActivity model = persist.get(DbActivity.class, pid);
				Assert.assertEquals("bli", model.getVariable("bla"));
            }
        });
    }

}
