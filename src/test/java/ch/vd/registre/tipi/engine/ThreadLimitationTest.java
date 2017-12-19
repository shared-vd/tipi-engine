package ch.vd.registre.tipi.engine;

import ch.sharedvd.tipi.engine.action.Activity;
import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import ch.sharedvd.tipi.engine.meta.ActivityMetaModel;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import ch.sharedvd.tipi.engine.model.ActivityState;
import ch.sharedvd.tipi.engine.model.DbActivity;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.transaction.TransactionStatus;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

;

public class ThreadLimitationTest extends TipiEngineTest {

    public static class ThreadLimitationTestProcess extends TopProcess {

        public static final int NB_TO_START = 1;
        public static int begin = 0;

        public final static TopProcessMetaModel meta = new TopProcessMetaModel(ThreadLimitationTestProcess.class, 100, -1, 5, null) {
            @Override
            protected void init() {
                setDeleteWhenFinished(false);
            }

            ;
        };

        @Override
        protected ActivityResultContext execute() throws Exception {
            begin++;
            for (int i = 0; i < NB_TO_START; i++) {
                addChildActivity(TLTActivity1.meta, null);
                addChildActivity(TLTActivity2.meta, null);
                addChildActivity(TLTActivity3.meta, null);
            }
            return new FinishedActivityResultContext();
        }
    }

    public static class TLTActivity1 extends Activity {

        public static AtomicInteger count = new AtomicInteger();
        public static AtomicInteger begin = new AtomicInteger();
        public static AtomicInteger end = new AtomicInteger();

        public static final ActivityMetaModel meta = new ActivityMetaModel(TLTActivity1.class, TipiEngineTest.defaultRetry,
                new String[]{"DB_ORACLE_PROD", "DB_HOST"}, null);

        @Override
        protected ActivityResultContext execute() throws Exception {

            begin.incrementAndGet();

            int value = count.get();

            while (count.get() <= value) {
                Thread.sleep(10);
            }

            end.incrementAndGet();

            return new FinishedActivityResultContext();
        }
    }

    public static class TLTActivity2 extends Activity {

        public static AtomicInteger count = new AtomicInteger();
        public static AtomicInteger begin = new AtomicInteger();
        public static AtomicInteger end = new AtomicInteger();

        public static final ActivityMetaModel meta = new ActivityMetaModel(TLTActivity2.class, TipiEngineTest.defaultRetry,
                new String[]{TestingConnectionType.ESB.name(), TestingConnectionType.UPI_WS.name()}, null);

        @Override
        protected ActivityResultContext execute() throws Exception {

            begin.incrementAndGet();

            int value = count.get();

            while (count.get() <= value) {
                Thread.sleep(10);
            }

            end.incrementAndGet();

            return new FinishedActivityResultContext();
        }
    }

    public static class TLTActivity3 extends Activity {

        public static AtomicInteger count = new AtomicInteger();
        public static AtomicInteger begin = new AtomicInteger();
        public static AtomicInteger end = new AtomicInteger();

        public static final ActivityMetaModel meta = new ActivityMetaModel(TLTActivity3.class, TipiEngineTest.defaultRetry,
                new String[]{TestingConnectionType.DB_HOST.name(), TestingConnectionType.UPI_WS.name()}, null);

        @Override
        protected ActivityResultContext execute() throws Exception {

            begin.incrementAndGet();

            int value = count.get();

            while (count.get() <= value) {
                Thread.sleep(10);
            }

            end.incrementAndGet();

            return new FinishedActivityResultContext();
        }
    }

    @Test
    public void limitedByConnectionTest() throws Exception {

        tipiFacade.setMaxConnections(TestingConnectionType.DB_ORACLE_PROD.name(), 1);
        tipiFacade.setMaxConnections(TestingConnectionType.DB_HOST.name(), 1);
        tipiFacade.setMaxConnections(TestingConnectionType.ESB.name(), 1);
        tipiFacade.setMaxConnections(TestingConnectionType.UPI_WS.name(), 1);

        tipiFacade.launch(ThreadLimitationTestProcess.meta, null);
        while (TLTActivity1.begin.get() < 1) {    // ok: DB_ORACLE_PROD et DB_HOST sont libres
            Thread.sleep(10);
        }
        while (TLTActivity2.begin.get() < 1) {    // ok: ESB et UPI_WS sont libres
            Thread.sleep(10);
        }
		Assert.assertEquals(0, TLTActivity3.begin.get());    // bloqué: ni DB_HOST ni UPI_WS n'est libre
		Assert.assertEquals(0, TLTActivity1.end.get());
		Assert.assertEquals(0, TLTActivity2.end.get());
		Assert.assertEquals(0, TLTActivity3.end.get());

        TLTActivity1.count.incrementAndGet();        // Termine l'activité 1 => libère DB_ORACLE_PROD et DB_HOST

		Assert.assertEquals(1, TLTActivity1.begin.get());
		Assert.assertEquals(1, TLTActivity2.begin.get());
		Assert.assertEquals(0, TLTActivity3.begin.get());    // bloqué: DB_HOST est libre, UPI_WS n'est pas libre
        while (TLTActivity1.end.get() < 1) {
            Thread.sleep(10);
        }
		Assert.assertEquals(0, TLTActivity2.end.get());
		Assert.assertEquals(0, TLTActivity3.end.get());

        TLTActivity2.count.incrementAndGet();        // Termine l'activité 2 => libère ESB et UPI_WS

		Assert.assertEquals(1, TLTActivity1.begin.get());
		Assert.assertEquals(1, TLTActivity2.begin.get());
        while (TLTActivity3.begin.get() < 1) {    // ok: DB_HOST et UPI_WS sont libres
            Thread.sleep(10);
        }
		Assert.assertEquals(1, TLTActivity1.end.get());
        while (TLTActivity2.end.get() < 1) {
            Thread.sleep(10);
        }
		Assert.assertEquals(0, TLTActivity3.end.get());

        TLTActivity3.count.incrementAndGet();        // Termine l'activité 3
        Thread.sleep(10);

		Assert.assertEquals(1, TLTActivity1.begin.get());
		Assert.assertEquals(1, TLTActivity2.begin.get());
		Assert.assertEquals(1, TLTActivity3.begin.get());
		Assert.assertEquals(1, TLTActivity1.end.get());
		Assert.assertEquals(1, TLTActivity2.end.get());
        while (TLTActivity3.end.get() < 1) {
            Thread.sleep(10);
        }

        waitEndAllActivities(true);
    }

    @SuppressWarnings("unchecked")
    protected void waitEndAllActivities(boolean waitIfEmpty) throws Exception {

        if (waitIfEmpty) {

            boolean end = false;
            while (!end) {
                DbActivityCriteria actCrit = new DbActivityCriteria();
                List<DbActivity> acts = hqlBuilder.getResultList(actCrit);
                end = ((null != acts) && !acts.isEmpty());
                Thread.sleep(10);
            }
        }

        {
            boolean end = false;
            while (!end) {
                // Assert que plus aucune activité n'est potentiellement démarrable.
                DbActivityCriteria actCrit = new DbActivityCriteria();
                actCrit.addAndExpression(Expr.or(actCrit.state().eq(ActivityState.EXECUTING), actCrit.requestEndExecution().eq(Boolean.TRUE)));
                List<DbActivity> acts = hqlBuilder.getResultList(actCrit);
                end = ((null == acts) || acts.isEmpty());
                if (!end) {
                    // On attend sur cette activité puis on relance
                    final Long actId = acts.get(0).getId();
                    DbActivity model;
                    do {
                        Thread.sleep(10);
                        model = doInTransaction(new TxCallback<DbActivity>() {
                            @Override
                            public DbActivity execute(TransactionStatus aArg0) throws Exception {
                                return activityRepository.findOne(actId);
                            }
                        });
                    } while ((null != model)
                            && ((ActivityState.EXECUTING == model.getState()) || model.isRequestEndExecution()));
                }
            }
        }
    }
}
