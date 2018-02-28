package ch.sharedvd.tipi.engine.query;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.common.AbstractTipiPersistenceTest;
import ch.sharedvd.tipi.engine.infos.TipiActivityInfos;
import ch.sharedvd.tipi.engine.infos.TipiTopProcessInfos;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import ch.sharedvd.tipi.engine.meta.VariableDescription;
import ch.sharedvd.tipi.engine.meta.VariableType;
import ch.sharedvd.tipi.engine.model.*;
import ch.sharedvd.tipi.engine.utils.ResultListWithCount;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ActivityQueryServiceTest extends AbstractTipiPersistenceTest {

    @Test
    public void getActivitiesForCorrelationId() {
        txTemplate.txWithout(s -> {
            loadProcess();
        });

        final List<Long> list = activityQueryService.getActivitiesForCorrelationId("TheCorelId");
        assertEquals(1, list.size());
    }

    @Test
    public void getRunningProcesses() {
        txTemplate.txWithout(s -> {
            loadProcess();
        });

        final ResultListWithCount<TipiTopProcessInfos> results = activityQueryService.getRunningProcesses(-1);
        assertEquals(1, results.getResult().size());
        {
            final TipiTopProcessInfos tpi = results.getResult().get(0);
            assertEquals("ActivityQueryServiceTest$ActivityPersisterServiceTopProcess", tpi.getProcessName());
            assertEquals("ActivityQueryServiceTest$ActivityPersisterServiceTopProcess", tpi.getSimpleName());
            assertEquals("Process", tpi.getType());
            assertEquals(15, tpi.getNbActivitesTotal());
            assertEquals(1, tpi.getNbActivitesError());
            assertEquals(3, tpi.getNbActivitesExecuting());
            assertEquals(0, tpi.getNbActivitesAborted());
            assertEquals(2, tpi.getNbActivitesFinished());
            assertEquals(8, tpi.getNbActivitesInitial());
            assertEquals(0, tpi.getNbActivitesRetry());
            assertEquals(0, tpi.getNbActivitesSuspended());
            assertEquals(1, tpi.getNbActivitesWaiting());
        }
    }

    @Test
    public void countProcesses() throws Exception {
        txTemplate.txWithout(s -> {
            loadProcess();
        });

        // On vérifie qu'on a 1 process
        txTemplate.txWithout(s -> {
            List<DbActivity> actis = activityRepository.findAll();
            Assert.assertEquals(11, actis.size());

        });

        ResultListWithCount<TipiTopProcessInfos> infos = activityQueryService.getAllProcesses(50);
        for (TipiTopProcessInfos ttpi : infos) {
            Assert.assertEquals(11L, ttpi.getNbActivitesTotal());
            Assert.assertEquals(1L, ttpi.getNbActivitesError());
            Assert.assertEquals(2L, ttpi.getNbActivitesExecuting());
        }
    }

    @Test
    public void searchByVariableName() throws Exception {

        txTemplate.txWithout(s -> {

            {
                final DbTopProcess model = new DbTopProcess();
                model.setFqn("ch.sharedvd.tipi.engine.query.ActivityQueryServiceTest$SearchTopProcess");
                model.setProcessName("ch.sharedvd.tipi.engine.query.ActivityQueryServiceTest$SearchTopProcess");
                activityPersisterService.putVariable(model, "bla", "bli");
                em.persist(model);
            }
            Thread.sleep(100); // Pour que Creation date soit plus grande
            {
                final DbTopProcess model = new DbTopProcess();
                model.setFqn("ch.sharedvd.tipi.engine.query.ActivityQueryServiceTest$SearchTopProcess");
                model.setProcessName("ch.sharedvd.tipi.engine.query.ActivityQueryServiceTest$SearchTopProcess");
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
            Assert.assertEquals("ActivityQueryServiceTest$SearchTopProcess", infos.getSimpleName());
        }
    }

    public static class SearchTopProcess extends TopProcess {

        public final static TopProcessMetaModel meta = new TopProcessMetaModel(SearchTopProcess.class,
                new VariableDescription[]{
                        new VariableDescription("bla", VariableType.String)
                }, null,
                100, -1, 10, null, true) {
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

    @Test
    public void searchActivities_inexistantClassShouldNotCrash() throws Exception {
        txTemplate.txWithout(s -> {
            loadProcessWithInexistantClassname();
        });

        final TipiCriteria criteria = new TipiCriteria();
        final ResultListWithCount<TipiActivityInfos> results = activityQueryService.searchActivities(criteria, -1);
        Assert.assertEquals(11l, results.getCount());
        final TipiActivityInfos infos = results.getResult().get(0);
        Assert.assertEquals("Unknown: ActivityQueryServiceTest$ActivityPersisterServiceTopProcess", infos.getSimpleName());
    }

    private void loadProcess() throws Exception {

        final DbTopProcess process = new DbTopProcess();
        {
            process.setState(ActivityState.WAIT_ON_CHILDREN);
            process.setFqn("ch.sharedvd.tipi.engine.query.ActivityQueryServiceTest$ActivityPersisterServiceTopProcess");
            process.setProcessName("ch.sharedvd.tipi.engine.query.ActivityQueryServiceTest$ActivityPersisterServiceTopProcess");
            addVars(process);
            activityRepository.save(process);
        }

        DbActivity procAct1;
        {
            procAct1 = new DbActivity();
            procAct1.setState(ActivityState.FINISHED);
            procAct1.setFqn("ch.sharedvd.tipi.engine.query.ActivityQueryServiceTest$ActivityPersisterServiceTopProcess");
            procAct1.setProcess(process);
            procAct1.setParent(process);
            procAct1.setCorrelationId("TheCorelId");
            addVars(procAct1);
            activityRepository.save(procAct1);
        }
        {
            DbActivity procAct2 = new DbActivity();
            procAct2.setState(ActivityState.EXECUTING);
            procAct2.setFqn("ch.sharedvd.tipi.engine.query.ActivityQueryServiceTest$ActivityPersisterServiceTopProcess");
            procAct2.setProcess(process);
            procAct2.setParent(process);
            procAct2.setPrevious(procAct1);
            addVars(procAct2);
            activityRepository.save(procAct2);
        }

        // Sub-proc 1
        {
            DbSubProcess sub = new DbSubProcess();
            {
                sub.setState(ActivityState.EXECUTING);
                sub.setFqn("ch.sharedvd.tipi.engine.query.ActivityQueryServiceTest$ActivityPersisterServiceTopProcess");
                sub.setProcess(process);
                sub.setParent(process);
                addVars(sub);
                activityRepository.save(sub);
            }

            // S1-Acti1
            DbActivity subAct1;
            {
                subAct1 = new DbActivity();
                subAct1.setFqn("ch.sharedvd.tipi.engine.query.ActivityQueryServiceTest$ActivityPersisterServiceTopProcess");
                subAct1.setProcess(process);
                subAct1.setParent(sub);
                subAct1.setState(ActivityState.INITIAL);
                addVars(subAct1);
                activityRepository.save(subAct1);
            }
            // S1-Acti2
            {
                DbActivity subAct2 = new DbActivity();
                sub.setState(ActivityState.INITIAL);
                subAct2.setFqn("ch.sharedvd.tipi.engine.query.ActivityQueryServiceTest$ActivityPersisterServiceTopProcess");
                subAct2.setProcess(process);
                subAct2.setParent(sub);
                subAct2.setPrevious(subAct1);
                addVars(subAct2);
                activityRepository.save(subAct2);
            }
            // S1-Acti3
            {
                DbActivity subAct3 = new DbActivity();
                sub.setState(ActivityState.INITIAL);
                subAct3.setFqn("ch.sharedvd.tipi.engine.query.ActivityQueryServiceTest$ActivityPersisterServiceTopProcess");
                subAct3.setProcess(process);
                subAct3.setParent(sub);
                addVars(subAct3);
                activityRepository.save(subAct3);
            }
        } // Sub proc 1

        // Sub-proc 2
        {
            DbSubProcess sub = new DbSubProcess();
            {
                sub.setFqn("ch.sharedvd.tipi.engine.query.ActivityQueryServiceTest$ActivityPersisterServiceTopProcess");
                sub.setProcess(process);
                sub.setParent(process);
                addVars(sub);
                activityRepository.save(sub);
            }

            // S2-Acti1
            DbActivity subAct1;
            {
                subAct1 = new DbActivity();
                subAct1.setFqn("ch.sharedvd.tipi.engine.query.ActivityQueryServiceTest$ActivityPersisterServiceTopProcess");
                subAct1.setProcess(process);
                subAct1.setParent(sub);
                addVars(subAct1);
                activityRepository.save(subAct1);
            }
            // S2-Acti2
            {
                DbActivity subAct2 = new DbActivity();
                subAct2.setFqn("ch.sharedvd.tipi.engine.query.ActivityQueryServiceTest$ActivityPersisterServiceTopProcess");
                subAct2.setProcess(process);
                subAct2.setParent(sub);
                subAct2.setPrevious(subAct1);
                subAct2.setState(ActivityState.EXECUTING);
                addVars(subAct2);
                activityRepository.save(subAct2);
            }
            // S2-Acti3
            {
                DbActivity subAct3 = new DbActivity();
                subAct3.setFqn("ch.sharedvd.tipi.engine.query.ActivityQueryServiceTest$ActivityPersisterServiceTopProcess");
                subAct3.setProcess(process);
                subAct3.setParent(sub);
                subAct3.setState(ActivityState.ERROR);
                addVars(subAct3);
                activityRepository.save(subAct3);
            }
        } // Sub proc 2
    }

    private void loadProcessWithInexistantClassname() throws Exception {

        DbTopProcess process = new DbTopProcess();
        {
            process.setState(ActivityState.WAIT_ON_CHILDREN);
            process.setFqn("inexistent.ActivityQueryServiceTest$ActivityPersisterServiceTopProcess");
            process.setProcessName("inexistent.ActivityQueryServiceTest$ActivityPersisterServiceTopProcess");
            addVars(process);
            activityRepository.save(process);
        }

        DbActivity procAct1;
        {
            procAct1 = new DbActivity();
            procAct1.setState(ActivityState.FINISHED);
            procAct1.setFqn("inexistent.ActivityQueryServiceTest$ActivityPersisterServiceTopProcess");
            procAct1.setProcess(process);
            procAct1.setParent(process);
            addVars(procAct1);
            activityRepository.save(procAct1);
        }
        {
            DbActivity procAct2 = new DbActivity();
            procAct2.setState(ActivityState.EXECUTING);
            procAct2.setFqn("inexistent.ActivityQueryServiceTest$ActivityPersisterServiceTopProcess");
            procAct2.setProcess(process);
            procAct2.setParent(process);
            procAct2.setPrevious(procAct1);
            addVars(procAct2);
            activityRepository.save(procAct2);
        }

        // Sub-proc 1
        {
            DbSubProcess sub = new DbSubProcess();
            {
                sub.setState(ActivityState.EXECUTING);
                sub.setFqn("inexistent.ActivityQueryServiceTest$ActivityPersisterServiceTopProcess");
                sub.setProcess(process);
                sub.setParent(process);
                addVars(sub);
                activityRepository.save(sub);
            }

            // S1-Acti1
            DbActivity subAct1;
            {
                subAct1 = new DbActivity();
                subAct1.setFqn("inexistent.ActivityQueryServiceTest$ActivityPersisterServiceTopProcess");
                subAct1.setProcess(process);
                subAct1.setParent(sub);
                subAct1.setState(ActivityState.INITIAL);
                addVars(subAct1);
                activityRepository.save(subAct1);
            }
            // S1-Acti2
            {
                DbActivity subAct2 = new DbActivity();
                sub.setState(ActivityState.INITIAL);
                subAct2.setFqn("inexistent.ActivityQueryServiceTest$ActivityPersisterServiceTopProcess");
                subAct2.setProcess(process);
                subAct2.setParent(sub);
                subAct2.setPrevious(subAct1);
                addVars(subAct2);
                activityRepository.save(subAct2);
            }
            // S1-Acti3
            {
                DbActivity subAct3 = new DbActivity();
                sub.setState(ActivityState.INITIAL);
                subAct3.setFqn("inexistent.ActivityQueryServiceTest$ActivityPersisterServiceTopProcess");
                subAct3.setProcess(process);
                subAct3.setParent(sub);
                addVars(subAct3);
                activityRepository.save(subAct3);
            }
        } // Sub proc 1

        // Sub-proc 2
        {
            DbSubProcess sub = new DbSubProcess();
            {
                sub.setFqn("inexistent.ActivityQueryServiceTest$ActivityPersisterServiceTopProcess");
                sub.setProcess(process);
                sub.setParent(process);
                addVars(sub);
                activityRepository.save(sub);
            }

            // S2-Acti1
            DbActivity subAct1;
            {
                subAct1 = new DbActivity();
                subAct1.setFqn("inexistent.ActivityQueryServiceTest$ActivityPersisterServiceTopProcess");
                subAct1.setProcess(process);
                subAct1.setParent(sub);
                addVars(subAct1);
                activityRepository.save(subAct1);
            }
            // S2-Acti2
            {
                DbActivity subAct2 = new DbActivity();
                subAct2.setFqn("inexistent.ActivityQueryServiceTest$ActivityPersisterServiceTopProcess");
                subAct2.setProcess(process);
                subAct2.setParent(sub);
                subAct2.setPrevious(subAct1);
                subAct2.setState(ActivityState.EXECUTING);
                addVars(subAct2);
                activityRepository.save(subAct2);
            }
            // S2-Acti3
            {
                DbActivity subAct3 = new DbActivity();
                subAct3.setFqn("inexistent.ActivityQueryServiceTest$ActivityPersisterServiceTopProcess");
                subAct3.setProcess(process);
                subAct3.setParent(sub);
                subAct3.setState(ActivityState.ERROR);
                addVars(subAct3);
                activityRepository.save(subAct3);
            }
        } // Sub proc 2
    }

    private void addVars(DbActivity acti) {
        final DbStringVariable var1 = new DbStringVariable("str", "value");
        var1.setOwner(acti);
        acti.putVariable(var1);
        final DbBooleanVariable var2 = new DbBooleanVariable("bool", true);
        var2.setOwner(acti);
        acti.putVariable(var2);
    }

    public static class ActivityPersisterServiceTopProcess extends TopProcess {

        @SuppressWarnings("serial")
        public final static TopProcessMetaModel meta = new TopProcessMetaModel(ActivityPersisterServiceTopProcess.class, 100, -1, 10, null) {
            @Override
            protected void init() {
                setDeleteWhenFinished(false);
            }
        };

        @Override
        protected ActivityResultContext execute() throws Exception {
            fail("ne sera jamais appelé");
            return null;
        }
    }

    @Test
    @Ignore("TODO(JEC) ce test peut pas passer et je sais pas comment corriger")
    public void searchActivities_Waiting() throws Exception {
        txTemplate.txWithout(s -> {
            loadProcess();
        });

        final TipiCriteria criteria = new TipiCriteria();
        //	criteria.setGroupe("Proc");
        ResultListWithCount<TipiActivityInfos> results = activityQueryService.searchActivities(criteria, -1);
        Assert.assertEquals(1, results.getCount());
    }

    @Test
    @Ignore("TODO(JEC) ce test peut pas passer et je sais pas comment corriger")
    public void searchActivities_Groupe() throws Exception {

        txTemplate.txWithout(s -> {
            loadProcess();
        });

        final TipiCriteria criteria = new TipiCriteria();
        //	criteria.setGroupe("Proc");
        ResultListWithCount<TipiActivityInfos> results = activityQueryService.searchActivities(criteria, -1);
        Assert.assertEquals(10, results.getCount());
    }
}
