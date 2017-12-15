package ch.sharedvd.tipi.engine.model;

import ch.sharedvd.tipi.engine.AbstractTipiPersistenceTest;
import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import org.junit.Test;
import org.springframework.transaction.TransactionStatus;

public class SearchActivitiesTest extends AbstractTipiPersistenceTest {

	@Test
	public void searchByVariableName() throws Exception {

		doInTransaction(new TxCallbackWithoutResult() {
			@Override
			public void execute(TransactionStatus status) throws Exception {

				{
					TopProcessModel model = new TopProcessModel();
					model.setFqn("ch.vd.registre.tipi.model.SearchActivitiesTest$SearchTopProcess");
					activityPersistenceService.putVariable(model, "bla", "bli");
					persist.save(model);
				}
				Thread.sleep(100); // Pour que Creation date soit plus grande
				{
					TopProcessModel model = new TopProcessModel();
					model.setFqn("ch.vd.registre.tipi.model.SearchActivitiesTest$SearchTopProcess");
					activityPersistenceService.putVariable(model, "bla", "blo");
					persist.save(model);
				}
			}
		});

		{
			TipiCriteria criteria = new TipiCriteria();
			criteria.setVariableName("bla");
			ResultListWithCount<TipiActivityInfos> results = activityPersistenceService.searchActivities(criteria, -1);
			assertEquals(2L, results.getCount());
			assertEquals(2, results.getResult().size());
		}
		{
			TipiCriteria criteria = new TipiCriteria();
			criteria.setVariableName("bla");
			ResultListWithCount<TipiActivityInfos> results = activityPersistenceService.searchActivities(criteria, 1);
			assertEquals(2L, results.getCount());
			assertEquals(1, results.getResult().size());
			TipiActivityInfos infos = results.getResult().get(0);
			assertEquals("SearchActivitiesTest$SearchTopProcess", infos.getNameOrProcessName());
		}
	}

	public static class SearchTopProcess extends TopProcess {

		private final static RetryPolicy retry = new DefaultRetryPolicy(0);
		public final static TopProcessMetaModel meta = new TopProcessMetaModel(SearchTopProcess.class, retry, 100, -1, 10, null) {
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
