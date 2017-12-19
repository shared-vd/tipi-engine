package ch.vd.registre.tipi.engine;

import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import ch.sharedvd.tipi.engine.criteria.DbActivityCriteria;
import ch.sharedvd.tipi.engine.engine.aborting.AbortManagerActivity;
import ch.sharedvd.tipi.engine.engine.aborting.AbortManagerProcess;
import ch.sharedvd.tipi.engine.engine.aborting.AbortingActivity;
import ch.sharedvd.tipi.engine.engine.aborting.AbortingProcess;
import ch.sharedvd.tipi.engine.model.ActivityState;
import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.sharedvd.tipi.engine.model.DbTopProcess;
import ch.vd.registre.base.tx.TxCallback;
import ch.vd.registre.base.tx.TxCallbackWithoutResult;
import org.junit.Test;
import org.springframework.transaction.TransactionStatus;

import java.util.List;

public class AbortingTest extends TipiEngineTest {

    @Test
    @SuppressWarnings("unchecked")
    public void abortProcessThroughManager() throws Exception {

        // Démarrage et attente
        final long pid = tipiFacade.launch(AbortManagerProcess.meta, null);
        // On attends que la sub activity ait démarré et attende
        while (AbortManagerActivity.count.get() == 0) {
            Thread.sleep(10);
        }

        // On abort le process
        tipiFacade.abortProcess(pid, false);
        // On attends qu'il se finisse
        while (tipiFacade.isRunning(pid)) {
            Thread.sleep(10);
        }

        // On attends que toutes les activités se finissent
        boolean end = false;
        while (!end) {
            end = doInTransaction(new TxCallback<Boolean>() {
                @Override
                public Boolean execute(TransactionStatus status) throws Exception {
                    DbActivityCriteria crit = new DbActivityCriteria();
                    List<DbActivity> actis = hqlBuilder.getResultList(crit);
                    boolean hasNotFinished = false;
                    for (DbActivity a : actis) {
                        if (a.getState() != ActivityState.ABORTED) {
                            hasNotFinished = true;
                        }
                    }
                    return !hasNotFinished;
                }
            }).booleanValue();
        }
    }

    @Test
    public void abortBigProcess() throws Exception {

        // Démarrage et attente
        final long pid = tipiFacade.launch(AbortingProcess.meta, null);

        // On attends qu'il y ait au moins 10 activités créées
        while (AbortingActivity.count.intValue() < 10) {
            Thread.sleep(10);
        }

        // Abort
        tipiFacade.abortProcess(pid, false);
        while (tipiFacade.isRunning(pid)) {
            Thread.sleep(10);
        }

        // Vérification
        doInTransaction(new TxCallbackWithoutResult() {
            @Override
            public void execute(TransactionStatus status) throws Exception {

                DbTopProcess tp = persist.get(DbTopProcess.class, pid);
                assertEquals(ActivityState.ABORTED, tp.getState());
            }
        });

    }

}
