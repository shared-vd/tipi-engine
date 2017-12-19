package ch.vd.registre.tipi.action;

import ch.sharedvd.tipi.engine.client.VariableMap;
import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.sharedvd.tipi.engine.model.DbSubProcess;
import ch.sharedvd.tipi.engine.model.DbTopProcess;
import ch.vd.registre.tipi.action.parentChild.*;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.transaction.TransactionStatus;

import java.util.List;

;


public class ParentChildTest extends TipiEngineTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testRunInSequence() throws Exception {
        // Vérifie la sequence d'execution du process

        VariableMap vars = new VariableMap();
        vars.put("nbSubAct", 3);
        final long pid = tipiFacade.launch(TstParentProcess.meta, vars);

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

        while (tipiFacade.isRunning(pid)) {
            Thread.sleep(10);
        }

        String concat = tipiFacade.getStringVariable(pid, "concat");
        Assert.assertEquals("10,8,6,4", concat);

        // Vérifie les pointeurs parent/process
        doInTransaction(new TxCallbackWithoutResult() {
            @Override
            public void execute(TransactionStatus status) throws Exception {
                // Process
                DbTopProcess process = persist.get(DbTopProcess.class, pid);
                assertNotNull(process);
                assertNull(process.getProcess());
                assertNull(process.getParent());
                assertNull(process.getPrevious());

                // Put number 1
                final long id_nb1;
                {
                    DbActivityCriteria criteria = new DbActivityCriteria();
                    criteria.addAndExpression(criteria.fqn().eq(TstPutNumberActivity1.meta.getFQN()));
                    DbActivity act = hqlBuilder.getSingleResult(DbActivity.class, criteria);
                    assertNotNull(act);
                    Assert.assertEquals(process, act.getProcess());
                    Assert.assertEquals(process, act.getParent());
                    assertNull(act.getPrevious());
                    id_nb1 = act.getId();
                }
                // Put number 2
                {
                    DbActivityCriteria criteria = new DbActivityCriteria();
                    criteria.addAndExpression(criteria.fqn().eq(TstPutNumberActivity2.meta.getFQN()));
                    DbActivity act = hqlBuilder.getSingleResult(DbActivity.class, criteria);
                    assertNotNull(act);
                    Assert.assertEquals(process, act.getProcess());
                    Assert.assertEquals(process, act.getParent());
                    Assert.assertEquals(id_nb1, act.getPrevious().getId());
                }

                // Sub-Proc
                final DbSubProcess sub;
                {
                    DbActivityCriteria criteria = new DbActivityCriteria();
                    criteria.addAndExpression(criteria.fqn().eq(TstListVarsSubProcess.meta.getFQN()));
                    sub = hqlBuilder.getSingleResult(DbSubProcess.class, criteria);
                    assertNotNull(sub);
                    Assert.assertEquals(process, sub.getProcess());
                    Assert.assertEquals(process, sub.getParent());
                    assertNull(sub.getPrevious());
                }

                // Assign vars sans previous
                {
                    DbActivityCriteria criteria = new DbActivityCriteria();
                    criteria.addAndExpression(criteria.fqn().eq(TstAssignVarActivity.meta.getFQN()));
                    criteria.addAndExpression(criteria.previous__Id().isNull());
                    List<DbActivity> acts = hqlBuilder.getResultList(criteria);
                    for (DbActivity act : acts) {
                        assertNotNull(act);
                        Assert.assertEquals(process, act.getProcess());
                        Assert.assertEquals(sub, act.getParent());
                        assertNull(act.getPrevious());
                    }
                }
                // Assign vars sans previous
                {
                    DbActivityCriteria criteria = new DbActivityCriteria();
                    criteria.addAndExpression(criteria.fqn().eq(TstAssignVarActivity.meta.getFQN()));
                    criteria.addAndExpression(criteria.previous__Id().isNotNull());
                    DbActivity act = hqlBuilder.getSingleResult(DbActivity.class, criteria);
                    assertNotNull(act);
                    Assert.assertEquals(process, act.getProcess());
                    Assert.assertEquals(sub, act.getParent());
                    assertNotNull(act.getPrevious());
                }
            }
        });
    }

}
