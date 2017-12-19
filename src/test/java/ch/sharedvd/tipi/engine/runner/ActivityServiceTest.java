package ch.sharedvd.tipi.engine.runner;

import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import ch.sharedvd.tipi.engine.model.ActivityState;
import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.sharedvd.tipi.engine.model.DbTopProcess;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

;

public class ActivityServiceTest extends TipiEngineTest {

    private static Long ID1;
    private static Long ID2;

    @Test
    public void getExecutingActivities() throws Exception {

        // Charge 2 activités
        txTemplate.txWithout((s) -> {
            DbTopProcess tp = new DbTopProcess();
            tp.setFqn("abc");
            tp.setState(ActivityState.WAIT_ON_CHILDREN);
            tp.setVersion(1);
            em.persist(tp);

            {
                DbActivity am = new DbActivity();
                am.setProcess(tp);
                am.setParent(tp);
                am.setFqn("abcd");
                am.setNbRetryDone(1); // Retry -> 1
                am.setState(ActivityState.EXECUTING);
                am.setVersion(1);
                em.persist(am);
                ID1 = am.getId();
            }

            {
                DbActivity am = new DbActivity();
                am.setProcess(tp);
                am.setParent(tp);
                am.setFqn("abcd");
                am.setNbRetryDone(0);
                am.setState(ActivityState.EXECUTING);
                am.setVersion(1);
                em.persist(am);
                ID2 = am.getId();
            }
        });

        Assert.assertTrue(ID1 < ID2);

        // Vérifie l'ordre
        txTemplate.txWithout((s) -> {
            // On veut que ID2 soit en premier et ID1 en deuxième
            // ID1 a un retry count > ID2 donc passe après

            List<DbActivity> ams = activityRunningService.getExecutingActivities("abc", new ArrayList<>(), 10);
            Assert.assertEquals(ID2, ams.get(0).getId());
            Assert.assertEquals(ID1, ams.get(1).getId());
        });

    }

}
