package ch.sharedvd.tipi.engine.model;

import ch.sharedvd.tipi.engine.action.Activity;
import ch.sharedvd.tipi.engine.action.ActivityFacade;
import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.common.AbstractTipiPersistenceTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

public class VariableScopeTest extends AbstractTipiPersistenceTest {

    @Test
    public void scopeVariable() {

        final AtomicReference<Long> P1_ID = new AtomicReference<Long>(0L);
        final AtomicReference<Long> P1_C1_ID = new AtomicReference<Long>(0L);

        final AtomicReference<Long> P2_ID = new AtomicReference<Long>(0L);
        final AtomicReference<Long> P2_C1_ID = new AtomicReference<Long>(0L);
        final AtomicReference<Long> P2_C2_ID = new AtomicReference<Long>(0L);
        final AtomicReference<Long> P2_C3_ID = new AtomicReference<Long>(0L);

        txTemplate.txWithout((s) -> {
            // Parent 1
            DbTopProcess parent1;
            {
                DbTopProcess parent = new DbTopProcess();
                parent.setFqn("parent1");
                parent.setProcessName("parent1");
                parent = activityRepository.save(parent);
                forcePutVariable(parent, "parent", parent.getFqn());
                forcePutVariable(parent, parent.getFqn(), parent.getFqn());
                forcePutVariable(parent, "name", parent.getFqn());
                P1_ID.set(parent.getId());

                parent1 = parent;
                {
                    DbActivity child = new DbActivity();
                    child.setFqn("p1_child1");
                    child.setProcessName("p1_child1");
                    child.setParent(parent);
                    child = activityRepository.save(child);
                    forcePutVariable(child, child.getFqn(), child.getFqn());
                    forcePutVariable(child, "name", child.getFqn());
                    P1_C1_ID.set(child.getId());
                }
            }

            // Parent 2
            {
                DbTopProcess parent = new DbTopProcess();
                parent.setFqn("parent2");
                parent.setProcessName("parent2");
                parent.setParent(parent1);
                parent = activityRepository.save(parent);
                forcePutVariable(parent, "parent", parent.getFqn());
                forcePutVariable(parent, parent.getFqn(), parent.getFqn());
                forcePutVariable(parent, "name", parent.getFqn());
                P2_ID.set(parent.getId());

                {
                    DbActivity child = new DbActivity();
                    child.setFqn("p2_child1");
                    child.setProcessName("p2_child1");
                    child.setParent(parent);
                    child = activityRepository.save(child);
                    forcePutVariable(child, child.getFqn(), child.getFqn());
                    forcePutVariable(child, "name", child.getFqn());
                    P2_C1_ID.set(child.getId());
                }
                DbActivity child2;
                {
                    DbActivity child = new DbActivity();
                    child.setFqn("p2_child2");
                    child.setProcessName("p2_child2");
                    child.setParent(parent);
                    child = activityRepository.save(child);
                    forcePutVariable(child, child.getFqn(), child.getFqn());
                    forcePutVariable(child, "name", child.getFqn());
                    P2_C2_ID.set(child.getId());
                    child2 = child;
                }
                {
                    DbActivity child = new DbActivity();
                    child.setFqn("p2_child3");
                    child.setProcessName("p2_child3");
                    child.setParent(parent);
                    child.setPrevious(child2);
                    child = activityRepository.save(child);
                    forcePutVariable(child, child.getFqn(), child.getFqn());
                    forcePutVariable(child, "name", child.getFqn());
                    P2_C3_ID.set(child.getId());
                }
            }
        });

        txTemplate.txWithout((s) -> {
            class VariableGetter extends Activity {
                public VariableGetter(ActivityFacade facade) {
                    super(facade);
                }

                @Override
                public ActivityResultContext execute() throws Exception {
                    // Us
                    Assert.assertEquals("p2_child3", this.getStringVariable("name"));
                    Assert.assertEquals("p2_child3", this.getStringVariable("p2_child3"));
                    // Previous
                    Assert.assertEquals("p2_child2", this.getStringVariable("p2_child2"));
                    // Parent
                    Assert.assertEquals("parent2", this.getStringVariable("parent"));
                    // Parent 1+2
                    Assert.assertEquals("parent2", this.getStringVariable("parent2"));
                    Assert.assertEquals("parent1", this.getStringVariable("parent1"));
                    // Siblings
                    Assert.assertNull(this.getStringVariable("p2_child1"));
                    Assert.assertNull(this.getStringVariable("p1_child1"));

                    return new FinishedActivityResultContext();
                }
            }

            DbActivity child3 = activityRepository.findById(P2_C3_ID.get()).orElse(null);

            VariableGetter getter = new VariableGetter(new ActivityFacade(child3.getId(), activityPersisterService, activityRepository));
            getter.execute();
        });
    }
}
