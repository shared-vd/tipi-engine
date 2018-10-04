package ch.sharedvd.tipi.engine.action;

import ch.sharedvd.tipi.engine.action.parentChild.*;
import ch.sharedvd.tipi.engine.client.VariableMap;
import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.sharedvd.tipi.engine.model.DbSubProcess;
import ch.sharedvd.tipi.engine.model.DbTopProcess;
import org.junit.Assert;
import org.junit.Test;

import javax.persistence.Query;
import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;

public class ParentChildTest extends TipiEngineTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testRunInSequence() throws Exception {
        // Vérifie la sequence d'execution du process

        VariableMap vars = new VariableMap();
        vars.put("nbSubAct", 3);
        final long pid = tipiFacade.launch(TstParentProcess.class, vars);

        // Attends sur le parent
        {
            while (TstParentProcess.beginStep.get() < 1) {
                Thread.sleep(10);
            }
            Assert.assertEquals(1, TstParentProcess.beginStep.get());

            TstParentProcess.globalStep.incrementAndGet(); // -> 1

            while (TstParentProcess.endStep.get() < 1) {
                Thread.sleep(10);
            }
            Assert.assertEquals(1, TstParentProcess.endStep.get());
        }

        // Attends sur le 1er fils et le 3eme fils
        {
            while (TstPutNumberActivity1.beginStep.get() < 1) {
                Thread.sleep(10);
            }
            Assert.assertEquals(1, TstPutNumberActivity1.beginStep.get());

            while (TstListVarsSubProcess.beginStep.get() < 1) {
                Thread.sleep(10);
            }
            Assert.assertEquals(1, TstListVarsSubProcess.beginStep.get());

            TstParentProcess.globalStep.incrementAndGet(); // -> 2

            while (TstPutNumberActivity1.endStep.get() < 1) {
                Thread.sleep(10);
            }
            Assert.assertEquals(1, TstPutNumberActivity1.endStep.get());
            // List vars a pas terminé
            Assert.assertEquals(1, TstListVarsSubProcess.beginStep.get());
            Assert.assertEquals(0, TstListVarsSubProcess.endStep.get());
        }

        // Attends sur le 2eme fils
        {
            while (TstPutNumberActivity2.beginStep.get() < 1) {
                Thread.sleep(10);
            }
            Assert.assertEquals(1, TstPutNumberActivity2.beginStep.get());

            TstParentProcess.globalStep.incrementAndGet(); // -> 3

            while (TstPutNumberActivity2.endStep.get() < 1) {
                Thread.sleep(10);
            }
            Assert.assertEquals(1, TstPutNumberActivity2.endStep.get());
            // List vars a pas terminé
            Assert.assertEquals(1, TstListVarsSubProcess.beginStep.get());
            Assert.assertEquals(0, TstListVarsSubProcess.endStep.get());
        }

        // Attends sur le 3eme fils
        {
            while (TstListVarsSubProcess.beginStep.get() < 1) {
                Thread.sleep(10);
            }
            Assert.assertEquals(1, TstListVarsSubProcess.beginStep.get());

            TstParentProcess.globalStep.incrementAndGet(); // -> 4

            while (TstListVarsSubProcess.endStep.get() < 1) {
                Thread.sleep(10);
            }
            Assert.assertEquals(1, TstListVarsSubProcess.endStep.get());
        }

        waitWhileRunning(pid, 5000);

        // Vérifie les pointeurs parent/process
        txTemplate.txWithout(s -> {
            // Process
            DbTopProcess process = topProcessRepository.findById(pid).orElse(null);
            assertNotNull(process);
            assertNull(process.getProcess());
            assertNull(process.getParent());
            assertNull(process.getPrevious());

            final String concat = (String)process.getVariable("concat");
            Assert.assertEquals("10,8,6,4", concat);

            // Put number 1
            final Long id_nb1;
            {
                final List<DbActivity> acts = activityRepository.findByFqn(TstPutNumberActivity1.meta.getFQN());
                assertNotNull(acts);
                Assert.assertEquals(1, acts.size());
                final DbActivity act = acts.get(0);
                Assert.assertEquals(process, act.getProcess());
                Assert.assertEquals(process, act.getParent());
                assertNull(act.getPrevious());
                id_nb1 = act.getId();
            }
            // Put number 2
            {
                final List<DbActivity> acts = activityRepository.findByFqn(TstPutNumberActivity2.meta.getFQN());
                assertNotNull(acts);
                Assert.assertEquals(1, acts.size());
                final DbActivity act = acts.get(0);
                Assert.assertEquals(process, act.getProcess());
                Assert.assertEquals(process, act.getParent());
                Assert.assertEquals(id_nb1, act.getPrevious().getId());
            }

            // Sub-Proc
            final DbSubProcess sub;
            {
                final List<DbActivity> acts = activityRepository.findByFqn(TstListVarsSubProcess.meta.getFQN());
                assertNotNull(acts);
                Assert.assertEquals(1, acts.size());
                sub = (DbSubProcess) acts.get(0);
                assertNotNull(sub);
                Assert.assertEquals(process, sub.getProcess());
                Assert.assertEquals(process, sub.getParent());
                assertNull(sub.getPrevious());
            }

            // Assign vars sans previous
            {
                final Query q = em.createQuery("from DbActivity a where a.fqn = :fqn and a.previous is null");
                q.setParameter("fqn", TstAssignVarActivity.meta.getFQN());
                final List<DbActivity> acts = q.getResultList();
                for (DbActivity act : acts) {
                    assertNotNull(act);
                    Assert.assertEquals(process, act.getProcess());
                    Assert.assertEquals(sub, act.getParent());
                    assertNull(act.getPrevious());
                }
            }
            // Assign vars sans previous
            {
                final Query q = em.createQuery("from DbActivity a where a.fqn = :fqn and a.previous is NOT null");
                q.setParameter("fqn", TstAssignVarActivity.meta.getFQN());
                final DbActivity act = (DbActivity) q.getSingleResult();
                assertNotNull(act);
                Assert.assertEquals(process, act.getProcess());
                Assert.assertEquals(sub, act.getParent());
                assertNotNull(act.getPrevious());
            }

        });
    }

}
