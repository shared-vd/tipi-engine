package ch.sharedvd.tipi.engine.model;

import ch.sharedvd.tipi.engine.AbstractTipiPersistenceTest;
import ch.vd.registre.base.tx.TxCallback;
import ch.vd.registre.base.tx.TxCallbackWithoutResult;
import ch.vd.registre.tipi.client.VariableMap;
import org.junit.Test;
import org.springframework.transaction.TransactionStatus;

public class ActivityGetVariableTest extends AbstractTipiPersistenceTest {

	@Test
	public void getVariable() throws Exception {

		final long aId = doInTransaction(new TxCallback<Long>() {
			@Override
			public Long execute(TransactionStatus status) throws Exception {

				ActivityModel model = new ActivityModel();
				model.setFqn("act1");
				persist.attachWithPersist(model);
				return model.getId();
			}
		}).longValue();

		VariableMap vars = new VariableMap();
		vars.put("var", 42);
		vars.put("id", aId);
		final long pid = tipiFacade.launch(TstStoreNumberProcess.meta, vars);
		while (tipiFacade.isRunning(pid))  {
			Thread.sleep(10);
		}
		assertEquals(42, TstStoreNumberProcess.number);

		doInTransaction(new TxCallbackWithoutResult() {
			@Override
			public void execute(TransactionStatus status) throws Exception {
				TopProcessModel p = persist.get(TopProcessModel.class, pid);
				assertEquals("TheResult", p.getVariable("result"));
				assertEquals("act1", p.getVariable("name"));
			}
		});
	}

}
