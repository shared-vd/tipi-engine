package ch.sharedvd.tipi.engine.engine;

import ch.sharedvd.tipi.engine.action.*;
import ch.sharedvd.tipi.engine.client.VariableMap;
import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import ch.sharedvd.tipi.engine.meta.ActivityMetaModel;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import ch.sharedvd.tipi.engine.model.ActivityState;
import ch.sharedvd.tipi.engine.model.DbActivity;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ActivityRunner_Activity_Test extends TipiEngineTest {

    public static class ActTstSubProcess extends TopProcess {

        public static final TopProcessMetaModel meta = new TopProcessMetaModel(ActTstSubProcess.class, 2, -1, 10, null) {
            @Override
            protected void init() {
                setDeleteWhenFinished(false);
            }
        };

        public static AtomicInteger nb = new AtomicInteger(0);

        @Override
        protected ActivityResultContext execute() throws Exception {
            int mode = getIntVariable("mode");

            VariableMap vars = new VariableMap();
            //vars.put("mode", mode);
            addChildActivity(ActTstActivity.meta, vars);

            if (mode == 1) {
                // OK
                return new FinishedActivityResultContext();
            } else if (mode == 2) {
                throw new ActivityException("Error");
            } else if (mode == 3) {
                return new ErrorActivityResultContext("Error");
            } else if (mode == 4) {
                nb.incrementAndGet();
                if (nb.get() == 1) {
                    // Exception
                    throw new RuntimeException("Erreur de test");
                }
                return new FinishedActivityResultContext();
            } else if (mode == 5) {
                throw new RuntimeException("Erreur de test");
            }
            return new FinishedActivityResultContext();
        }
    }

    public static class ActTstActivity extends Activity {

        public static final ActivityMetaModel meta = new ActivityMetaModel(ActTstActivity.class);

        public static AtomicInteger nb = new AtomicInteger(0);

        @Override
        protected ActivityResultContext execute() throws Exception {
            int mode = getIntVariable("mode");
            if (mode == 1) {
                // OK
                return new FinishedActivityResultContext();
            } else if (mode == 2) {
                throw new ActivityException("Error");
            } else if (mode == 3) {
                return new ErrorActivityResultContext("Error");
            } else if (mode == 4) {
                nb.incrementAndGet();
                if (nb.get() == 1) {
                    // Exception
                    throw new RuntimeException("Erreur de test");
                }
                return new FinishedActivityResultContext();
            } else if (mode == 5) {
                throw new RuntimeException("Erreur de test");
            }
            return new FinishedActivityResultContext();
        }
    }

    @Test
    public void executeException_Mode4() throws Exception {
        final Long pid = doWithLog4jBlocking("ch.vd.registre.tipi", new Log4jBlockingCallback<Long>() {
            @Override
            public Long execute() throws Exception {
                ActTstSubProcess.nb = new AtomicInteger(0);
                ActTstActivity.nb = new AtomicInteger(0);

                VariableMap vars = new VariableMap();
                vars.put("mode", 4);
                final long pid = tipiFacade.launch(ActTstSubProcess.meta, vars);
                while (tipiFacade.isRunning(pid)) {
                    Thread.sleep(10);
                }
                return pid;
            }
        });

        txTemplate.txWithout(s -> {
            final List<DbActivity> nexts = activityRepository.findByParentId(pid);
            Assert.assertEquals(1, nexts.size());
            final DbActivity act = nexts.get(0);
            act.setState(ActivityState.EXECUTING);
        });
    }

}
