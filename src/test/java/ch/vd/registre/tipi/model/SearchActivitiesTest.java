package ch.vd.registre.tipi.model;

import ch.sharedvd.tipi.engine.AbstractTipiPersistenceTest;
import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.client.TipiActivityInfos;
import ch.sharedvd.tipi.engine.client.TipiCriteria;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import ch.sharedvd.tipi.engine.model.DbTopProcess;
import ch.vd.registre.base.hqlbuilder.srv.ResultListWithCount;
import ch.vd.registre.base.tx.TxCallbackWithoutResult;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.transaction.TransactionStatus;

;

public class SearchActivitiesTest extends AbstractTipiPersistenceTest {

    @Test
    public void searchByVariableName() throws Exception {

        doInTransaction(new TxCallbackWithoutResult() {
            @Override
            public void execute(TransactionStatus status) throws Exception {

                {
                    DbTopProcess model = new DbTopProcess();
                    model.setFqn("ch.vd.registre.tipi.model.SearchActivitiesTest$SearchTopProcess");
                    activityPersistenceService.putVariable(model, "bla", "bli");
                    em.persist(model);
                }
                Thread.sleep(100); // Pour que Creation date soit plus grande
                {
                    DbTopProcess model = new DbTopProcess();
                    model.setFqn("ch.vd.registre.tipi.model.SearchActivitiesTest$SearchTopProcess");
                    activityPersistenceService.putVariable(model, "bla", "blo");
                    em.persist(model);
                }
            }
        });

        {
            TipiCriteria criteria = new TipiCriteria();
            criteria.setVariableName("bla");
            ResultListWithCount<TipiActivityInfos> results = activityPersistenceService.searchActivities(criteria, -1);
            Assert.assertEquals(2L, results.getCount());
            Assert.assertEquals(2, results.getResult().size());
        }
        {
            TipiCriteria criteria = new TipiCriteria();
            criteria.setVariableName("bla");
            ResultListWithCount<TipiActivityInfos> results = activityPersistenceService.searchActivities(criteria, 1);
            Assert.assertEquals(2L, results.getCount());
            Assert.assertEquals(1, results.getResult().size());
            TipiActivityInfos infos = results.getResult().get(0);
            Assert.assertEquals("SearchActivitiesTest$SearchTopProcess", infos.getNameOrProcessName());
        }
    }

    public static class SearchTopProcess extends TopProcess {

        public final static TopProcessMetaModel meta = new TopProcessMetaModel(SearchTopProcess.class, 100, -1, 10, null) {
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
