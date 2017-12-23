package ch.sharedvd.tipi.engine.runner;

import ch.sharedvd.tipi.engine.common.AbstractTipiPersistenceTest;
import ch.sharedvd.tipi.engine.model.ActivityState;
import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.sharedvd.tipi.engine.model.DbSubProcess;
import ch.sharedvd.tipi.engine.model.DbTopProcess;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ActivityRunningServiceTest extends AbstractTipiPersistenceTest {

    @Test
    public void getTopProcessNamesWithExecutingActivities() throws Exception {
        txTemplate.txWithout(s -> {
            // Le TopProcess qui n'a pas de EXECUTING
            {
                final DbTopProcess tp = new DbTopProcess();
                tp.setFqn("xyz");
                tp.setState(ActivityState.WAIT_ON_CHILDREN);
                tp.setVersion(1);
                em.persist(tp);

                {
                    final DbActivity am = new DbActivity();
                    am.setProcess(tp);
                    am.setParent(tp);
                    am.setFqn("xyza");
                    am.setState(ActivityState.SUSPENDED);
                    am.setVersion(1);
                    em.persist(am);
                }
            }
            // Le TopProcess qui s'execute mais en ReqEnd -> pas pris
            {
                final DbTopProcess tp = new DbTopProcess();
                tp.setFqn("ghj");
                tp.setState(ActivityState.EXECUTING);
                tp.setRequestEndExecution(true);
                tp.setVersion(1);
                em.persist(tp);

                {
                    final DbActivity am = new DbActivity();
                    am.setProcess(tp);
                    am.setParent(tp);
                    am.setFqn("ghjj");
                    am.setState(ActivityState.INITIAL);
                    am.setVersion(1);
                    em.persist(am);
                }
            }
            // Le TopProcess qui s'execute
            {
                final DbTopProcess tp = new DbTopProcess();
                tp.setFqn("def");
                tp.setState(ActivityState.EXECUTING);
                tp.setVersion(1);
                em.persist(tp);
                {
                    final DbActivity am = new DbActivity();
                    am.setProcess(tp);
                    am.setParent(tp);
                    am.setFqn("defg");
                    am.setState(ActivityState.INITIAL);
                    am.setVersion(1);
                    em.persist(am);
                }
            }
            // Le TopProcess qui a des activités a executer
            {
                final DbTopProcess tp = new DbTopProcess();
                tp.setFqn("abc");
                tp.setState(ActivityState.WAIT_ON_CHILDREN);
                tp.setVersion(1);
                em.persist(tp);
                {
                    final DbActivity am = new DbActivity();
                    am.setProcess(tp);
                    am.setParent(tp);
                    am.setFqn("abcd");
                    am.setState(ActivityState.EXECUTING);
                    am.setVersion(1);
                    em.persist(am);
                }
                {
                    final DbActivity am = new DbActivity();
                    am.setProcess(tp);
                    am.setParent(tp);
                    am.setFqn("abcd");
                    am.setState(ActivityState.EXECUTING);
                    am.setVersion(1);
                    em.persist(am);
                }
            }
            // Le SubProcess qui a des SUB activités a executer
            {
                final DbTopProcess tp = new DbTopProcess();
                tp.setFqn("tp3");
                tp.setState(ActivityState.WAIT_ON_CHILDREN);
                tp.setVersion(1);
                em.persist(tp);
                {
                    final DbSubProcess am = new DbSubProcess();
                    am.setProcess(tp);
                    am.setParent(tp);
                    am.setFqn("tp3.1");
                    am.setState(ActivityState.WAIT_ON_CHILDREN);
                    am.setVersion(1);
                    em.persist(am);
                    {
                        final DbActivity sub = new DbActivity();
                        sub.setProcess(tp);
                        sub.setParent(am);
                        sub.setFqn("tp3.1.1");
                        sub.setState(ActivityState.EXECUTING);
                        sub.setVersion(1);
                        em.persist(sub);
                    }
                }
            }
            // Le TopProcess qui a des activités a executer mais en ReqEnd -> pas pris
            {
                final DbTopProcess tp = new DbTopProcess();
                tp.setFqn("mno");
                tp.setState(ActivityState.WAIT_ON_CHILDREN);
                tp.setVersion(1);
                em.persist(tp);

                {
                    final DbActivity am = new DbActivity();
                    am.setProcess(tp);
                    am.setParent(tp);
                    am.setFqn("mnop");
                    am.setState(ActivityState.EXECUTING);
                    am.setRequestEndExecution(true);
                    am.setVersion(1);
                    em.persist(am);
                }
            }
        });

        // Vérifie l'ordre
        txTemplate.txWithout(s -> {
            List<String> names = activityRunningService.getTopProcessNamesWithExecutingActivities();
            Assert.assertEquals(3, names.size());
            Assert.assertTrue(names.contains("abc"));
            Assert.assertTrue(names.contains("def"));
            Assert.assertTrue(names.contains("tp3"));
        });
    }

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
            Assert.assertEquals(2, ams.size());
            Assert.assertEquals(ID2, ams.get(0).getId());
            Assert.assertEquals(ID1, ams.get(1).getId());
        });

    }
}
