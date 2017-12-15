package ch.sharedvd.tipi.engine.model;

import ch.vd.registre.base.hqlbuilder.srv.ResultListWithCount;
import ch.vd.registre.base.tx.TxCallbackWithoutResult;
import ch.vd.registre.base.utils.Assert;
import ch.vd.registre.tipi.action.ActivityResultContext;
import ch.vd.registre.tipi.action.TopProcess;
import ch.vd.registre.tipi.client.TipiActivityInfos;
import ch.vd.registre.tipi.client.TipiCriteria;
import ch.vd.registre.tipi.client.TipiTopProcessInfos;
import ch.vd.registre.tipi.common.TipiPersistenceTest;
import ch.vd.registre.tipi.criteria.ActivityModelCriteria;
import ch.vd.registre.tipi.meta.TopProcessMetaModel;
import ch.vd.registre.tipi.model.svc.ActivityPersistenceService;
import ch.vd.registre.tipi.retry.DefaultRetryPolicy;
import ch.vd.registre.tipi.retry.RetryPolicy;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;

import java.util.List;

public class ActivityPersistenceServiceTest extends TipiPersistenceTest {
	
	@Autowired
	private ActivityPersistenceService service;

	@Test
	@Ignore("TODO(JEC) ce test peut pas passer et je sais pas comment corriger")
	public void searchActivities_Waiting() throws Exception {

		doInTransaction(new TxCallbackWithoutResult() {
			@Override
			public void execute(TransactionStatus status) throws Exception {
				loadProcess();
			}
		});

		final TipiCriteria criteria = new TipiCriteria();
	//	criteria.setGroupe("Proc");
		ResultListWithCount<TipiActivityInfos> results = service.searchActivities(criteria, -1);
		assertEquals(1, results.getCount());
	}

	@Test
	@Ignore("TODO(JEC) ce test peut pas passer et je sais pas comment corriger")
	public void searchActivities_Groupe() throws Exception {

		doInTransaction(new TxCallbackWithoutResult() {
			@Override
			public void execute(TransactionStatus status) throws Exception {
				loadProcess();
			}
		});

		final TipiCriteria criteria = new TipiCriteria();
	//	criteria.setGroupe("Proc");
		ResultListWithCount<TipiActivityInfos> results = service.searchActivities(criteria, -1);
		assertEquals(10, results.getCount());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void countProcess() throws Exception {

		doInTransaction(new TxCallbackWithoutResult() {
			@Override
			public void execute(TransactionStatus status) throws Exception {
				loadProcess();
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
		
		ResultListWithCount<TipiTopProcessInfos> infos = service.getAllProcesses(50);

		for (TipiTopProcessInfos ttpi : infos) {
			assertEquals(11L, ttpi.getNbActivitesTotal());
			assertEquals(1L, ttpi.getNbActivitesError());
			assertEquals(2L, ttpi.getNbActivitesExecuting());
		}
	}

	@Test
	public void searchActivities_inexistantClassShouldNotCrash() throws Exception {

		doInTransaction(new TxCallbackWithoutResult() {
			@Override
			public void execute(TransactionStatus status) throws Exception {
				loadProcessWithInexistantClassname();

			}
		});

		final TipiCriteria criteria = new TipiCriteria();
		ResultListWithCount<TipiActivityInfos> results = service.searchActivities(criteria, -1);
		assertEquals(11l, results.getCount());
		TipiActivityInfos infos = results.getResult().get(0);
		assertEquals("Unknown: ActivityPersistenceServiceTest$ActivityPersistenceServiceTopProcess", infos.getNameOrProcessName());
	}
	
	private void loadProcess() throws Exception {

		TopProcessModel process = new TopProcessModel();
		{
			process.setState(ActivityState.WAIT_ON_CHILDREN);
			process.setFqn("ch.vd.registre.tipi.model.ActivityPersistenceServiceTest$ActivityPersistenceServiceTopProcess");
			addVars(process);
			persist.attachWithPersist(process);
		}

		ActivityModel procAct1;
		{
			procAct1 = new ActivityModel();
			procAct1.setState(ActivityState.FINISHED);
			procAct1.setFqn("ch.vd.registre.tipi.model.ActivityPersistenceServiceTest$ActivityPersistenceServiceTopProcess");
			procAct1.setProcess(process);
			procAct1.setParent(process);
			addVars(procAct1);
			persist.attachWithPersist(procAct1);
		}
		{
			ActivityModel procAct2 = new ActivityModel();
			procAct2.setState(ActivityState.EXECUTING);
			procAct2.setFqn("ch.vd.registre.tipi.model.ActivityPersistenceServiceTest$ActivityPersistenceServiceTopProcess");
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
				sub.setState(ActivityState.EXECUTING);
				sub.setFqn("ch.vd.registre.tipi.model.ActivityPersistenceServiceTest$ActivityPersistenceServiceTopProcess");
				sub.setProcess(process);
				sub.setParent(process);
				addVars(sub);
				persist.attachWithPersist(sub);
			}

			// S1-Acti1
			ActivityModel subAct1;
			{
				subAct1 = new ActivityModel();
				subAct1.setFqn("ch.vd.registre.tipi.model.ActivityPersistenceServiceTest$ActivityPersistenceServiceTopProcess");
				subAct1.setProcess(process);
				subAct1.setParent(sub);
				subAct1.setState(ActivityState.INITIAL);
				addVars(subAct1);
				persist.attachWithPersist(subAct1);
			}
			// S1-Acti2
			{
				ActivityModel subAct2 = new ActivityModel();
				sub.setState(ActivityState.INITIAL);
				subAct2.setFqn("ch.vd.registre.tipi.model.ActivityPersistenceServiceTest$ActivityPersistenceServiceTopProcess");
				subAct2.setProcess(process);
				subAct2.setParent(sub);
				subAct2.setPrevious(subAct1);
				addVars(subAct2);
				persist.attachWithPersist(subAct2);
			}
			// S1-Acti3
			{
				ActivityModel subAct3 = new ActivityModel();
				sub.setState(ActivityState.INITIAL);
				subAct3.setFqn("ch.vd.registre.tipi.model.ActivityPersistenceServiceTest$ActivityPersistenceServiceTopProcess");
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
				sub.setFqn("ch.vd.registre.tipi.model.ActivityPersistenceServiceTest$ActivityPersistenceServiceTopProcess");
				sub.setProcess(process);
				sub.setParent(process);
				addVars(sub);
				persist.attachWithPersist(sub);
			}

			// S2-Acti1
			ActivityModel subAct1;
			{
				subAct1 = new ActivityModel();
				subAct1.setFqn("ch.vd.registre.tipi.model.ActivityPersistenceServiceTest$ActivityPersistenceServiceTopProcess");
				subAct1.setProcess(process);
				subAct1.setParent(sub);
				addVars(subAct1);
				persist.attachWithPersist(subAct1);
			}
			// S2-Acti2
			{
				ActivityModel subAct2 = new ActivityModel();
				subAct2.setFqn("ch.vd.registre.tipi.model.ActivityPersistenceServiceTest$ActivityPersistenceServiceTopProcess");
				subAct2.setProcess(process);
				subAct2.setParent(sub);
				subAct2.setPrevious(subAct1);
				subAct2.setState(ActivityState.EXECUTING);
				addVars(subAct2);
				persist.attachWithPersist(subAct2);
			}
			// S2-Acti3
			{
				ActivityModel subAct3 = new ActivityModel();
				subAct3.setFqn("ch.vd.registre.tipi.model.ActivityPersistenceServiceTest$ActivityPersistenceServiceTopProcess");
				subAct3.setProcess(process);
				subAct3.setParent(sub);
				subAct3.setState(ActivityState.ERROR);
				addVars(subAct3);
				persist.attachWithPersist(subAct3);
			}
		} // Sub proc 2
	}

	private void loadProcessWithInexistantClassname() throws Exception {

		TopProcessModel process = new TopProcessModel();
		{
			process.setState(ActivityState.WAIT_ON_CHILDREN);
			process.setFqn("ch.vd.registre.tipi.inexistent.ActivityPersistenceServiceTest$ActivityPersistenceServiceTopProcess");
			addVars(process);
			persist.attachWithPersist(process);
		}

		ActivityModel procAct1;
		{
			procAct1 = new ActivityModel();
			procAct1.setState(ActivityState.FINISHED);
			procAct1.setFqn("ch.vd.registre.tipi.inexistent.ActivityPersistenceServiceTest$ActivityPersistenceServiceTopProcess");
			procAct1.setProcess(process);
			procAct1.setParent(process);
			addVars(procAct1);
			persist.attachWithPersist(procAct1);
		}
		{
			ActivityModel procAct2 = new ActivityModel();
			procAct2.setState(ActivityState.EXECUTING);
			procAct2.setFqn("ch.vd.registre.tipi.inexistent.ActivityPersistenceServiceTest$ActivityPersistenceServiceTopProcess");
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
				sub.setState(ActivityState.EXECUTING);
				sub.setFqn("ch.vd.registre.tipi.inexistent.ActivityPersistenceServiceTest$ActivityPersistenceServiceTopProcess");
				sub.setProcess(process);
				sub.setParent(process);
				addVars(sub);
				persist.attachWithPersist(sub);
			}

			// S1-Acti1
			ActivityModel subAct1;
			{
				subAct1 = new ActivityModel();
				subAct1.setFqn("ch.vd.registre.tipi.inexistent.ActivityPersistenceServiceTest$ActivityPersistenceServiceTopProcess");
				subAct1.setProcess(process);
				subAct1.setParent(sub);
				subAct1.setState(ActivityState.INITIAL);
				addVars(subAct1);
				persist.attachWithPersist(subAct1);
			}
			// S1-Acti2
			{
				ActivityModel subAct2 = new ActivityModel();
				sub.setState(ActivityState.INITIAL);
				subAct2.setFqn("ch.vd.registre.tipi.inexistent.ActivityPersistenceServiceTest$ActivityPersistenceServiceTopProcess");
				subAct2.setProcess(process);
				subAct2.setParent(sub);
				subAct2.setPrevious(subAct1);
				addVars(subAct2);
				persist.attachWithPersist(subAct2);
			}
			// S1-Acti3
			{
				ActivityModel subAct3 = new ActivityModel();
				sub.setState(ActivityState.INITIAL);
				subAct3.setFqn("ch.vd.registre.tipi.inexistent.ActivityPersistenceServiceTest$ActivityPersistenceServiceTopProcess");
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
				sub.setFqn("ch.vd.registre.tipi.inexistent.ActivityPersistenceServiceTest$ActivityPersistenceServiceTopProcess");
				sub.setProcess(process);
				sub.setParent(process);
				addVars(sub);
				persist.attachWithPersist(sub);
			}

			// S2-Acti1
			ActivityModel subAct1;
			{
				subAct1 = new ActivityModel();
				subAct1.setFqn("ch.vd.registre.tipi.inexistent.ActivityPersistenceServiceTest$ActivityPersistenceServiceTopProcess");
				subAct1.setProcess(process);
				subAct1.setParent(sub);
				addVars(subAct1);
				persist.attachWithPersist(subAct1);
			}
			// S2-Acti2
			{
				ActivityModel subAct2 = new ActivityModel();
				subAct2.setFqn("ch.vd.registre.tipi.inexistent.ActivityPersistenceServiceTest$ActivityPersistenceServiceTopProcess");
				subAct2.setProcess(process);
				subAct2.setParent(sub);
				subAct2.setPrevious(subAct1);
				subAct2.setState(ActivityState.EXECUTING);
				addVars(subAct2);
				persist.attachWithPersist(subAct2);
			}
			// S2-Acti3
			{
				ActivityModel subAct3 = new ActivityModel();
				subAct3.setFqn("ch.vd.registre.tipi.inexistent.ActivityPersistenceServiceTest$ActivityPersistenceServiceTopProcess");
				subAct3.setProcess(process);
				subAct3.setParent(sub);
				subAct3.setState(ActivityState.ERROR);
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

	public static class ActivityPersistenceServiceTopProcess extends TopProcess {

		private final static RetryPolicy retry = new DefaultRetryPolicy(0);
		@SuppressWarnings("serial")
		public final static TopProcessMetaModel meta = new TopProcessMetaModel(ActivityPersistenceServiceTopProcess.class, retry, 100, -1, 10, null) {
			
			
			@Override
			protected void init() {
				setDeleteWhenFinished(false);
			}
		};

		@Override
		protected ActivityResultContext execute() throws Exception {
			Assert.fail("ne sera jamais appelé");
			return null;
		}
	}

}
