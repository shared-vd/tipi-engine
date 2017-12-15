package ch.sharedvd.tipi.engine.model;

import ch.sharedvd.tipi.engine.AbstractTipiPersistenceTest;
import org.junit.Test;
import org.springframework.transaction.TransactionStatus;

import java.util.List;

public class ProcessDeleterTest extends AbstractTipiPersistenceTest {

	private long PID = -1L;

	@Test
	@SuppressWarnings("unchecked")
	public void deleteExecuting() throws Exception {

		doInTransaction(new TxCallbackWithoutResult() {
			@Override
			public void execute(TransactionStatus status) throws Exception {
				loadProcess(true);
			}
		});

		// On vérifie qu'on a 1 process
		doInTransaction(new TxCallbackWithoutResult() {
			@Override
			public void execute(TransactionStatus aStatus) throws Exception {
				ActivityModelCriteria crit = new ActivityModelCriteria();
				List<ActivityModel> actis = hqlBuilder.getResultList(crit);
				assertEquals(11, actis.size());
			}
		});

		doInTransaction(new TxCallbackWithoutResult() {
			@Override
			public void execute(TransactionStatus status) throws Exception {

				final TopProcessModel process = persist.get(TopProcessModel.class, PID);
				final ProcessDeleter deleter = new ProcessDeleter(process, hqlBuilder, persist);
				deleter.delete();
			}
		});

		// On vérifie qu'on n'a tjrs 1 activité
		doInTransaction(new TxCallbackWithoutResult() {
			@Override
			public void execute(TransactionStatus status) throws Exception {
				ActivityModelCriteria crit = new ActivityModelCriteria();
				List<ActivityModel> actis = hqlBuilder.getResultList(crit);
				assertEquals(11, actis.size());
			}
		});

	}

	@Test
	@SuppressWarnings("unchecked")
	public void deleteFinished() throws Exception {

		doInTransaction(new TxCallbackWithoutResult() {
			@Override
			public void execute(TransactionStatus status) throws Exception {
				loadProcess(false);
			}
		});

		// On vérifie qu'on a 11 activités
		doInTransaction(new TxCallbackWithoutResult() {
			@Override
			public void execute(TransactionStatus status) throws Exception {
				ActivityModelCriteria crit = new ActivityModelCriteria();
				List<ActivityModel> actis = hqlBuilder.getResultList(crit);
				assertEquals(11, actis.size());
			}
		});

		doInTransaction(new TxCallbackWithoutResult() {
			@Override
			public void execute(TransactionStatus status) throws Exception {

				final TopProcessModel process = persist.get(TopProcessModel.class, PID);
				final ProcessDeleter deleter = new ProcessDeleter(process, hqlBuilder, persist);
				deleter.delete();
			}
		});

		// On vérifie qu'on n'a plus d'activités
		doInTransaction(new TxCallbackWithoutResult() {
			@Override
			public void execute(TransactionStatus status) throws Exception {
				ActivityModelCriteria crit = new ActivityModelCriteria();
				List<ActivityModel> actis = hqlBuilder.getResultList(crit);
				assertEquals(0, actis.size());
			}
		});
	}

	private void loadProcess(boolean exec) throws Exception {

		TopProcessModel process = new TopProcessModel();
		{
			process.setFqn("Proc");
			addVars(process);
			persist.attachWithPersist(process);
			PID = process.getId();
		}

		ActivityModel procAct1;
		{
			procAct1 = new ActivityModel();
			procAct1.setFqn("ProcAct1");
			procAct1.setProcess(process);
			procAct1.setParent(process);
			addVars(procAct1);
			persist.attachWithPersist(procAct1);
		}
		{
			ActivityModel procAct2 = new ActivityModel();
			procAct2.setFqn("ProcAct2");
			procAct2.setProcess(process);
			procAct2.setParent(process);
			procAct2.setPrevious(procAct1);
			addVars(procAct2);
			persist.attachWithPersist(procAct2);
		}

		// Sub-proc 1
		{
			SubProcessModel sub = new SubProcessModel();
			{
				sub.setFqn("SubProc");
				sub.setProcess(process);
				sub.setParent(process);
				addVars(sub);
				persist.attachWithPersist(sub);
			}

			// S1-Acti1
			ActivityModel subAct1;
			{
				subAct1 = new ActivityModel();
				subAct1.setFqn("SubAct1");
				subAct1.setProcess(process);
				subAct1.setParent(sub);
				if (exec) {
					subAct1.setState(ActivityState.EXECUTING);
				}
				addVars(subAct1);
				persist.attachWithPersist(subAct1);
			}
			// S1-Acti2
			{
				ActivityModel subAct2 = new ActivityModel();
				subAct2.setFqn("SubAct2");
				subAct2.setProcess(process);
				subAct2.setParent(sub);
				subAct2.setPrevious(subAct1);
				addVars(subAct2);
				persist.attachWithPersist(subAct2);
			}
			// S1-Acti3
			{
				ActivityModel subAct3 = new ActivityModel();
				subAct3.setFqn("SubAct3");
				subAct3.setProcess(process);
				subAct3.setParent(sub);
				addVars(subAct3);
				persist.attachWithPersist(subAct3);
			}
		} // Sub proc 1

		// Sub-proc 2
		{
			SubProcessModel sub = new SubProcessModel();
			{
				sub.setFqn("SubProc");
				sub.setProcess(process);
				sub.setParent(process);
				addVars(sub);
				persist.attachWithPersist(sub);
			}

			// S2-Acti1
			ActivityModel subAct1;
			{
				subAct1 = new ActivityModel();
				subAct1.setFqn("SubAct1");
				subAct1.setProcess(process);
				subAct1.setParent(sub);
				addVars(subAct1);
				persist.attachWithPersist(subAct1);
			}
			// S2-Acti2
			{
				ActivityModel subAct2 = new ActivityModel();
				subAct2.setFqn("SubAct2");
				subAct2.setProcess(process);
				subAct2.setParent(sub);
				subAct2.setPrevious(subAct1);
				if (exec) {
					subAct2.setState(ActivityState.EXECUTING);
				}
				addVars(subAct2);
				persist.attachWithPersist(subAct2);
			}
			// S2-Acti3
			{
				ActivityModel subAct3 = new ActivityModel();
				subAct3.setFqn("SubAct3");
				subAct3.setProcess(process);
				subAct3.setParent(sub);
				addVars(subAct3);
				persist.attachWithPersist(subAct3);
			}
		} // Sub proc 2
	}

	private void addVars(ActivityModel acti) {
		final StringVariable var1 = new StringVariable("str", "value");
		var1.setOwner(acti);
		acti.putVariable(var1);
		final BooleanVariable var2 = new BooleanVariable("bool", true);
		var2.setOwner(acti);
		acti.putVariable(var2);
	}

}
