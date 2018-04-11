package ch.sharedvd.tipi.engine.repository;

import ch.sharedvd.tipi.engine.common.AbstractTipiPersistenceTest;
import ch.sharedvd.tipi.engine.model.ActivityState;
import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.sharedvd.tipi.engine.model.DbTopProcess;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class ActivityRepositoryTest extends AbstractTipiPersistenceTest {

    @Test
    public void findByGroupAndState_ActivityError() {
        final String fqn = "ch.vd.TheTopProcess";
        final AtomicLong ACT_ID = new AtomicLong();

        txTemplate.txWithout((s) -> {
            final DbTopProcess p;
            {
                p = new DbTopProcess();
                p.setFqn(fqn);
                p.setProcessName(fqn);
                p.setState(ActivityState.WAIT_ON_CHILDREN);
                p.setRequestEndExecution(false);
                activityRepository.save(p);
            }
            {
                final DbActivity a = new DbActivity();
                a.setFqn(fqn);
                a.setProcessName(fqn);
                a.setState(ActivityState.ERROR); // Pas trouvé -> Process only
                a.setRequestEndExecution(false);
                a.setProcess(p);
                a.setParent(p);
                activityRepository.save(a);
                ACT_ID.set(a.getId());
            }
            {
                DbTopProcess p2 = new DbTopProcess();
                p2.setFqn(fqn);
                p2.setProcessName(fqn);
                p2.setState(ActivityState.FINISHED); // Pas trouvé -> ERROR only
                p2.setRequestEndExecution(false);
                activityRepository.save(p2);
            }
        });
        final List<DbActivity> actis = activityRepository.findByGroupAndState(fqn, ActivityState.ERROR);
        Assert.assertEquals(1, actis.size());
        Assert.assertEquals((Long)ACT_ID.get(), actis.get(0).getId());
    }

    @Test
    public void findByGroupAndState_TopProcessError() {
        final String fqn = "ch.vd.TheTopProcess";
        final AtomicLong ACT_ID = new AtomicLong();

        txTemplate.txWithout((s) -> {
            final DbTopProcess p;
            {
                p = new DbTopProcess();
                p.setFqn(fqn);
                p.setProcessName(fqn);
                p.setState(ActivityState.ERROR);
                p.setRequestEndExecution(false);
                activityRepository.save(p);
                ACT_ID.set(p.getId());
            }
        });
        final List<DbActivity> actis = activityRepository.findByGroupAndState(fqn, ActivityState.ERROR);
        Assert.assertEquals(1, actis.size());
        Assert.assertEquals((Long)ACT_ID.get(), actis.get(0).getId());
    }
}
