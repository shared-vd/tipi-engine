package ch.vd.registre.tipi.dates;

import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import ch.sharedvd.tipi.engine.model.DbTopProcess;
import ch.vd.registre.base.tx.TxCallbackWithoutResult;
import org.junit.Test;
import org.springframework.transaction.TransactionStatus;

public class DateDebutFinExecutionTest extends TipiEngineTest {

    @Test
    public void diffDebutFin() throws Exception {

        final long pid = tipiFacade.launch(DateDebutFinProcess.meta, null);
        while (tipiFacade.isRunning(pid)) {
            Thread.sleep(2);
        }

        doInTransaction(new TxCallbackWithoutResult() {
            @Override
            public void execute(TransactionStatus status) throws Exception {
                DbTopProcess process = persist.get(DbTopProcess.class, pid);
                long diff = process.getDateEndActivity().getTime() - process.getCreationDate().getTime();
                assertTrue("Diff trop petit: " + diff, diff > 500);
            }
        });
    }

}
