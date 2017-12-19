package ch.vd.registre.tipi.command.impl;

import ch.sharedvd.tipi.engine.command.impl.ResumeAllActivitiesCommand;
import ch.sharedvd.tipi.engine.common.AbstractTipiPersistenceTest;
import ch.sharedvd.tipi.engine.model.ActivityState;
import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.sharedvd.tipi.engine.model.DbTopProcess;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

public class ResumeAllActivitiesCommandTest extends AbstractTipiPersistenceTest {

    @Autowired
    @Qualifier("hibernateDialect")
    private String hibernateDialect;

    @Override
    protected void doLoadDatabase() throws Exception {
        super.doLoadDatabase();

        {
            // Process
            DbTopProcess tp = new DbTopProcess();
            tp.setFqn("bla");
            tp.setState(ActivityState.WAIT_ON_CHILDREN);
            em.persist(tp);

            // Acti 1 - tp1
            {
                DbActivity acti = new DbActivity();
                acti.setFqn("blaa");
                acti.setNbRetryDone(1);
                acti.setParent(tp);
                acti.setProcess(tp);
                acti.setState(ActivityState.ERROR);
                em.persist(acti);
            }
            // Acti 2 - tp1
            {
                DbActivity acti = new DbActivity();
                acti.setFqn("blaa");
                acti.setNbRetryDone(0);
                acti.setParent(tp);
                acti.setProcess(tp);
                acti.setState(ActivityState.EXECUTING);
                em.persist(acti);
            }
        }
        {
            // Process 2
            DbTopProcess tp = new DbTopProcess();
            tp.setFqn("bli");
            tp.setState(ActivityState.WAIT_ON_CHILDREN);
            em.persist(tp);

            // Acti 1 - tp2
            {
                DbActivity acti = new DbActivity();
                acti.setFqn("blip");
                acti.setNbRetryDone(1);
                acti.setParent(tp);
                acti.setProcess(tp);
                acti.setState(ActivityState.ERROR);
                em.persist(acti);
            }
            // Acti 2 - tp2
            {
                DbActivity acti = new DbActivity();
                acti.setFqn("blip");
                acti.setNbRetryDone(0);
                acti.setParent(tp);
                acti.setProcess(tp);
                acti.setState(ActivityState.ERROR);
                em.persist(acti);
            }
        }
    }

    @Test
    public void createCriteria_All() throws Exception {
        txTemplate.txWithout(s -> {
            ResumeAllActivitiesCommand cmd = new ResumeAllActivitiesCommand(ActivityState.ERROR);
            cmd.setPersist(persist);
            cmd.setDialect(hibernateDialect);

            List<DbActivity> actis = cmd.getActivities();
            Assert.assertEquals(3, actis.size());
            Assert.assertTrue(actis.get(0) instanceof DbActivity);
            Assert.assertEquals(ActivityState.ERROR, actis.get(0).getState());
            Assert.assertTrue("bla".equals(actis.get(0).getProcess().getFqn()) || "bli".equals(actis.get(0).getProcess().getFqn()));

        });
    }

    @Test
    public void createCriteria_Bla() throws Exception {

        txTemplate.txWithout(s -> {
            ResumeAllActivitiesCommand cmd = new ResumeAllActivitiesCommand(ActivityState.ERROR, "bla");
            //cmd.set(persist);
            cmd.setDialect(hibernateDialect);

            List<DbActivity> actis = cmd.getActivities();
            Assert.assertEquals(1, actis.size());
            Assert.assertTrue(actis.get(0) instanceof DbActivity);
            Assert.assertEquals(ActivityState.ERROR, actis.get(0).getState());
            Assert.assertEquals("bla", actis.get(0).getProcess().getFqn());

        });
    }

    @Test
    public void createCriteria_Bli() throws Exception {

        txTemplate.txWithout(s -> {
            ResumeAllActivitiesCommand cmd = new ResumeAllActivitiesCommand(ActivityState.ERROR, "bli");
            cmd.setPersist(persist);
            cmd.setDialect(hibernateDialect);

            List<DbActivity> actis = cmd.getActivities();
            Assert.assertEquals(2, actis.size());
            Assert.assertTrue(actis.get(0) instanceof DbActivity);
            Assert.assertEquals(ActivityState.ERROR, actis.get(0).getState());
            Assert.assertEquals("bli", actis.get(0).getProcess().getFqn());

        });
    }

}
