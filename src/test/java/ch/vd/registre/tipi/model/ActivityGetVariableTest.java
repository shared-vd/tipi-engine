package ch.vd.registre.tipi.model;

import ch.sharedvd.tipi.engine.client.VariableMap;
import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import ch.vd.registre.base.tx.TxCallback;
import ch.vd.registre.base.tx.TxCallbackWithoutResult;
import org.junit.Test;
import org.springframework.transaction.TransactionStatus;

public class ActivityGetVariableTest extends TipiEngineTest {

    @Test
    public void getVariable() throws Exception {

        final long aId = doInTransaction(new TxCallback<Long>() {
            @Override
            public Long execute(TransactionStatus status) throws Exception {

                DbActivity model = new DbActivity();
                model.setFqn("act1");
                activityRepository.save(model);
                return model.getId();
            }
        }).longValue();

        VariableMap vars = new VariableMap();
        vars.put("var", 42);
        vars.put("id", aId);
        final long pid = tipiFacade.launch(TstStoreNumberProcess.meta, vars);
        while (tipiFacade.isRunning(pid)) {
            Thread.sleep(10);
        }
        assertEquals(42, TstStoreNumberProcess.number);

        doInTransaction(new TxCallbackWithoutResult() {
            @Override
            public void execute(TransactionStatus status) throws Exception {
                DbTopProcess p = persist.get(DbTopProcess.class, pid);
                assertEquals("TheResult", p.getVariable("result"));
                assertEquals("act1", p.getVariable("name"));
            }
        });
    }

}
