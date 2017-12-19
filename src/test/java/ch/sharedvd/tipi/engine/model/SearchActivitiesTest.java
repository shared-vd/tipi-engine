package ch.sharedvd.tipi.engine.model;


import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.common.AbstractTipiPersistenceTest;
import ch.sharedvd.tipi.engine.infos.TipiActivityInfos;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import ch.sharedvd.tipi.engine.query.TipiCriteria;
import ch.sharedvd.tipi.engine.utils.ResultListWithCount;
import org.junit.Assert;
import org.junit.Test;

public class SearchActivitiesTest extends AbstractTipiPersistenceTest {

    @Test
    public void searchByVariableName() throws Exception {

        txTemplate.txWithout(s -> {

                {
                    DbTopProcess model = new DbTopProcess();
                    model.setFqn("ch.vd.registre.tipi.model.SearchActivitiesTest$SearchTopProcess");
                    activityPersisterService.putVariable(model, "bla", "bli");
                    em.persist(model);
                }
                Thread.sleep(100); // Pour que Creation date soit plus grande
                {
                    DbTopProcess model = new DbTopProcess();
                    model.setFqn("ch.vd.registre.tipi.model.SearchActivitiesTest$SearchTopProcess");
                    activityPersisterService.putVariable(model, "bla", "blo");
                    em.persist(model);
                }

        });

        {
            TipiCriteria criteria = new TipiCriteria();
            criteria.setVariableName("bla");
            ResultListWithCount<TipiActivityInfos> results = activityQueryService.searchActivities(criteria, -1);
            Assert.assertEquals(2L, results.getCount());
            Assert.assertEquals(2, results.getResult().size());
        }
        {
            TipiCriteria criteria = new TipiCriteria();
            criteria.setVariableName("bla");
            ResultListWithCount<TipiActivityInfos> results = activityQueryService.searchActivities(criteria, 1);
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
