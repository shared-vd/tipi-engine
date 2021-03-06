package ch.sharedvd.tipi.engine.engine;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.SuspendedActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.client.VariableMap;
import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import ch.sharedvd.tipi.engine.meta.VariableDescription;
import ch.sharedvd.tipi.engine.meta.VariableType;
import ch.sharedvd.tipi.engine.model.ActivityState;
import ch.sharedvd.tipi.engine.model.DbActivity;
import org.junit.Assert;
import org.junit.Test;

public class ResumingTest extends TipiEngineTest {

    public static class ResumingProcess extends TopProcess {

        public static int value = 0;

        public static final TopProcessMetaModel meta = new TopProcessMetaModel(ResumingProcess.class,
                new VariableDescription[]{
                        new VariableDescription("correl", VariableType.Integer)
                }, null,
                6, -1, 10, null, true) {
            @Override
            protected void init() {
                setDeleteWhenFinished(false);
            }
        };

        @Override
        protected ActivityResultContext execute() throws Exception {
            value++;

            if (getIntVariable("correl") == null) {
                return new SuspendedActivityResultContext("blabla");
            }

            while (value < 3) {
                Thread.sleep(10);
            }

            return new FinishedActivityResultContext();
        }
    }

    @Test
    public void unsuspendActivity() throws Exception {
        final long pid = tipiFacade.launch(ResumingProcess.meta, null);
        waitWhileRunning(pid, 5000);
        Assert.assertEquals(1, ResumingProcess.value);

        txTemplate.txWithout(s -> {
            DbActivity model = activityRepository.findById(pid).orElse(null);
            Assert.assertEquals(ActivityState.SUSPENDED, model.getState());
            Assert.assertEquals("blabla", model.getCorrelationId());
        });

        final VariableMap vars = new VariableMap();
        vars.put("correl", 42);
        tipiFacade.unsuspendActivity(pid, vars);
        while (!tipiFacade.isRunning(pid)) {
            Thread.sleep(10);
        }
        while (ResumingProcess.value < 2) {
            Thread.sleep(10);
        }
        Assert.assertEquals(2, ResumingProcess.value);
        ResumingProcess.value = 3;
        waitWhileRunning(pid, 5000);

        txTemplate.txWithout(s -> {
            DbActivity model = activityRepository.findById(pid).orElse(null);
            Assert.assertEquals(ActivityState.FINISHED, model.getState());
            Assert.assertEquals("blabla", model.getCorrelationId());
            Assert.assertEquals(42, model.getVariable("correl"));
        });
    }
}
