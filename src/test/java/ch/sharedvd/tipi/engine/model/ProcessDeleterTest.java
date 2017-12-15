package ch.sharedvd.tipi.engine.model;

import ch.sharedvd.tipi.engine.AbstractTipiPersistenceTest;
import ch.sharedvd.tipi.engine.engine.ProcessDeleter;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ProcessDeleterTest extends AbstractTipiPersistenceTest {

    private long PID = -1L;

    @Test
    @SuppressWarnings("unchecked")
    public void deleteExecuting() throws Exception {
        txTemplate.txWithout((s) -> {
            loadProcess(true);
        });

        // On vérifie qu'on a 1 process
        txTemplate.txWithout((s) -> {
            List<DbActivity> actis = activityRepository.findAll();
            assertEquals(11, actis.size());
        });

        txTemplate.txWithout((s) -> {
            final DbTopProcess process = topProcessRepository.findOne(PID);
            final ProcessDeleter deleter = new ProcessDeleter(process, hqlBuilder, persist);
            deleter.delete();
        });

        // On vérifie qu'on n'a tjrs 1 activité
        txTemplate.txWithout((s) -> {
            List<DbActivity> actis = activityRepository.findAll();
            assertEquals(11, actis.size());
        });

    }

    @Test
    @SuppressWarnings("unchecked")
    public void deleteFinished() throws Exception {
        txTemplate.txWithout((s) -> {
            loadProcess(false);
        });

        // On vérifie qu'on a 11 activités
        txTemplate.txWithout((s) -> {
            List<DbActivity> actis = activityRepository.findAll();
            assertEquals(11, actis.size());
        });

        txTemplate.txWithout((s) -> {
            final DbTopProcess process = topProcessRepository.findOne(PID);
            final ProcessDeleter deleter = new ProcessDeleter(process, hqlBuilder, persist);
            deleter.delete();
        });

        // On vérifie qu'on n'a plus d'activités
        txTemplate.txWithout((s) -> {
            List<DbActivity> actis = activityRepository.findAll();
            assertEquals(0, actis.size());
        });
    }

    private void loadProcess(boolean exec) throws Exception {
        DbTopProcess process = new DbTopProcess();
        {
            process.setFqn("Proc");
            addVars(process);
            activityRepository.save(process);
            PID = process.getId();
        }

        DbActivity procAct1;
        {
            procAct1 = new DbActivity();
            procAct1.setFqn("ProcAct1");
            procAct1.setProcess(process);
            procAct1.setParent(process);
            addVars(procAct1);
            activityRepository.save(procAct1);
        }
        {
            DbActivity procAct2 = new DbActivity();
            procAct2.setFqn("ProcAct2");
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
                sub.setFqn("SubProc");
                sub.setProcess(process);
                sub.setParent(process);
                addVars(sub);
                activityRepository.save(sub);
            }

            // S1-Acti1
            DbActivity subAct1;
            {
                subAct1 = new DbActivity();
                subAct1.setFqn("SubAct1");
                subAct1.setProcess(process);
                subAct1.setParent(sub);
                if (exec) {
                    subAct1.setState(ActivityState.EXECUTING);
                }
                addVars(subAct1);
                activityRepository.save(subAct1);
            }
            // S1-Acti2
            {
                DbActivity subAct2 = new DbActivity();
                subAct2.setFqn("SubAct2");
                subAct2.setProcess(process);
                subAct2.setParent(sub);
                subAct2.setPrevious(subAct1);
                addVars(subAct2);
                activityRepository.save(subAct2);
            }
            // S1-Acti3
            {
                DbActivity subAct3 = new DbActivity();
                subAct3.setFqn("SubAct3");
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
                sub.setFqn("SubProc");
                sub.setProcess(process);
                sub.setParent(process);
                addVars(sub);
                activityRepository.save(sub);
            }

            // S2-Acti1
            DbActivity subAct1;
            {
                subAct1 = new DbActivity();
                subAct1.setFqn("SubAct1");
                subAct1.setProcess(process);
                subAct1.setParent(sub);
                addVars(subAct1);
                activityRepository.save(subAct1);
            }
            // S2-Acti2
            {
                DbActivity subAct2 = new DbActivity();
                subAct2.setFqn("SubAct2");
                subAct2.setProcess(process);
                subAct2.setParent(sub);
                subAct2.setPrevious(subAct1);
                if (exec) {
                    subAct2.setState(ActivityState.EXECUTING);
                }
                addVars(subAct2);
                activityRepository.save(subAct2);
            }
            // S2-Acti3
            {
                DbActivity subAct3 = new DbActivity();
                subAct3.setFqn("SubAct3");
                subAct3.setProcess(process);
                subAct3.setParent(sub);
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

}
