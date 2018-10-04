package ch.sharedvd.tipi.engine.engine;

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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadLimitationTest extends TipiEngineTest {

    public static class ThreadLimitationTestProcess extends TopProcess {

        public static final int NB_TO_START = 1;
        public static int begin = 0;

        public final static TopProcessMetaModel meta = new TopProcessMetaModel(ThreadLimitationTestProcess.class, 100, -1, 5, null) {
            @Override
            protected void init() {
                setDeleteWhenFinished(false);
            }
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

        public static final ActivityMetaModel meta = new ActivityMetaModel(TLTActivity1.class, new String[]{TestingConnectionType.MAINFRAME_DB.name()}, null);

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

        public static final ActivityMetaModel meta = new ActivityMetaModel(TLTActivity2.class, new String[]{TestingConnectionType.ESB.name(), TestingConnectionType.WS.name()}, null);

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

        public static final ActivityMetaModel meta = new ActivityMetaModel(TLTActivity3.class, new String[]{TestingConnectionType.MAINFRAME_DB.name(), TestingConnectionType.WS.name()}, null);

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
        tipiFacade.launch(ThreadLimitationTestProcess.meta, null);
        while (TLTActivity1.begin.get() < 1) {    // ok: DB_ORACLE_PROD et MAINFRAME_DB sont libres
            Thread.sleep(10);
        }
        while (TLTActivity2.begin.get() < 1) {    // ok: ESB et UPI_WS sont libres
            Thread.sleep(10);
        }
        Assert.assertEquals(0, TLTActivity3.begin.get());    // bloqué: ni MAINFRAME_DB ni UPI_WS n'est libre
        Assert.assertEquals(0, TLTActivity1.end.get());
        Assert.assertEquals(0, TLTActivity2.end.get());
        Assert.assertEquals(0, TLTActivity3.end.get());

        TLTActivity1.count.incrementAndGet();        // Termine l'activité 1 => libère DB_ORACLE_PROD et MAINFRAME_DB

        Assert.assertEquals(1, TLTActivity1.begin.get());
        Assert.assertEquals(1, TLTActivity2.begin.get());
        Assert.assertEquals(0, TLTActivity3.begin.get());    // bloqué: MAINFRAME_DB est libre, UPI_WS n'est pas libre
        while (TLTActivity1.end.get() < 1) {
            Thread.sleep(10);
        }
        Assert.assertEquals(0, TLTActivity2.end.get());
        Assert.assertEquals(0, TLTActivity3.end.get());

        TLTActivity2.count.incrementAndGet();        // Termine l'activité 2 => libère ESB et UPI_WS

        Assert.assertEquals(1, TLTActivity1.begin.get());
        Assert.assertEquals(1, TLTActivity2.begin.get());
        while (TLTActivity3.begin.get() < 1) {    // ok: MAINFRAME_DB et UPI_WS sont libres
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
                List<DbActivity> acts = activityRepository.findAll();
                end = ((null != acts) && !acts.isEmpty());
                Thread.sleep(10);
            }
        }

        {
            boolean end = false;
            while (!end) {
                // Assert que plus aucune activité n'est potentiellement démarrable.
                List<DbActivity> acts = activityRepository.findByStateOrRequestEndExecution(ActivityState.EXECUTING, true);
                end = ((null == acts) || acts.isEmpty());
                if (!end) {
                    // On attend sur cette activité puis on relance
                    final Long actId = acts.get(0).getId();
                    DbActivity model;
                    do {
                        Thread.sleep(10);
                        model = txTemplate.txWith(s -> {
                            return activityRepository.findById(actId).orElse(null);
                        });
                    } while ((null != model)
                            && ((ActivityState.EXECUTING == model.getState()) || model.isRequestEndExecution()));
                }
            }
        }
    }
}
