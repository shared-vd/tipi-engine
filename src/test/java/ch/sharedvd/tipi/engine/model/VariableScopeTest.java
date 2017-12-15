package ch.sharedvd.tipi.engine.model;

import ch.sharedvd.tipi.engine.AbstractTipiPersistenceTest;
import ch.sharedvd.tipi.engine.action.Activity;
import ch.sharedvd.tipi.engine.action.ActivityFacade;
import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import org.junit.Test;
import org.springframework.transaction.TransactionStatus;

public class VariableScopeTest extends AbstractTipiPersistenceTest {

	@Test
	public void scopeVariable() throws Exception {

		final ObjectHolder<Long> P1_ID = new ObjectHolder<Long>(0L);
		final ObjectHolder<Long> P1_C1_ID = new ObjectHolder<Long>(0L);

		final ObjectHolder<Long> P2_ID = new ObjectHolder<Long>(0L);
		final ObjectHolder<Long> P2_C1_ID = new ObjectHolder<Long>(0L);
		final ObjectHolder<Long> P2_C2_ID = new ObjectHolder<Long>(0L);
		final ObjectHolder<Long> P2_C3_ID = new ObjectHolder<Long>(0L);


		doInTransaction(new TxCallbackWithoutResult() {
			@Override
			public void execute(TransactionStatus status) throws Exception {

				// Parent 1
				TopProcessModel parent1;
				{
					TopProcessModel parent = new TopProcessModel();
					parent.setFqn("parent1");
					persist.attachWithPersist(parent);
					activityPersistenceService.putVariable(parent, "parent", parent.getFqn());
					activityPersistenceService.putVariable(parent, parent.getFqn(), parent.getFqn());
					activityPersistenceService.putVariable(parent, "name", parent.getFqn());
					P1_ID.set(parent.getId());

					parent1 = parent;
					{
						ActivityModel child = new ActivityModel();
						child.setFqn("p1_child1");
						child.setParent(parent);
						persist.attachWithPersist(child);
						activityPersistenceService.putVariable(child, child.getFqn(), child.getFqn());
						activityPersistenceService.putVariable(child, "name", child.getFqn());
						P1_C1_ID.set(child.getId());
					}
				}

				// Parent 2
				{
					TopProcessModel parent = new TopProcessModel();
					parent.setFqn("parent2");
					parent.setParent(parent1);
					persist.attachWithPersist(parent);
					activityPersistenceService.putVariable(parent, "parent", parent.getFqn());
					activityPersistenceService.putVariable(parent, parent.getFqn(), parent.getFqn());
					activityPersistenceService.putVariable(parent, "name", parent.getFqn());
					P2_ID.set(parent.getId());

					{
						ActivityModel child = new ActivityModel();
						child.setFqn("p2_child1");
						child.setParent(parent);
						persist.attachWithPersist(child);
						activityPersistenceService.putVariable(child, child.getFqn(), child.getFqn());
						activityPersistenceService.putVariable(child, "name", child.getFqn());
						P2_C1_ID.set(child.getId());
					}
					ActivityModel child2;
					{
						ActivityModel child = new ActivityModel();
						child.setFqn("p2_child2");
						child.setParent(parent);
						persist.attachWithPersist(child);
						activityPersistenceService.putVariable(child, child.getFqn(), child.getFqn());
						activityPersistenceService.putVariable(child, "name", child.getFqn());
						P2_C2_ID.set(child.getId());
						child2 = child;
					}
					{
						ActivityModel child = new ActivityModel();
						child.setFqn("p2_child3");
						child.setParent(parent);
						child.setPrevious(child2);
						persist.attachWithPersist(child);
						activityPersistenceService.putVariable(child, child.getFqn(), child.getFqn());
						activityPersistenceService.putVariable(child, "name", child.getFqn());
						P2_C3_ID.set(child.getId());
					}
				}
			}
		});

		doInTransaction(new TxCallbackWithoutResult() {
			@Override
			public void execute(TransactionStatus status) throws Exception {

				class VariableGetter extends Activity {
					public VariableGetter(ActivityFacade facade) {
						super(facade);
					}
					@Override
					public ActivityResultContext execute() throws Exception {
						// Us
						assertEquals("p2_child3", this.getStringVariable("name"));
						assertEquals("p2_child3", this.getStringVariable("p2_child3"));
						// Previous
						assertEquals("p2_child2", this.getStringVariable("p2_child2"));
						// Parent
						assertEquals("parent2", this.getStringVariable("parent"));
						// Parent 1+2
						assertEquals("parent2", this.getStringVariable("parent2"));
						assertEquals("parent1", this.getStringVariable("parent1"));
						// Siblings
						assertNull(this.getStringVariable("p2_child1"));
						assertNull(this.getStringVariable("p1_child1"));

						return new FinishedActivityResultContext();
					}
				}

				ActivityModel child3 = persist.get(ActivityModel.class, P2_C3_ID.get());

				VariableGetter getter = new VariableGetter(new ActivityFacade(child3.getId(), activityPersistenceService));
				getter.execute();
			}
		});
	}

}
