package ch.sharedvd.tipi.engine.engine;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import ch.sharedvd.tipi.engine.model.ActivityState;
import ch.sharedvd.tipi.engine.model.DbActivity;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public class NbRetryTest extends TipiEngineTest {

    public static class NbRetryProcess extends TopProcess {

        public final static TopProcessMetaModel meta = new TopProcessMetaModel(NbRetryProcess.class, 6, -1, 10, null);

        @Override
        public ActivityResultContext execute() throws Exception {
            throw new RuntimeException("TEST: une erreur");
        }
    }

    @Test
    public void nbRetry() throws Exception {
        final Long pid = doWithLog4jBlocking("ch.vd.registre.tipi", new Log4jBlockingCallback<Long>() {
            @Override
            public Long execute() throws Exception {
                final long pid = tipiFacade.launch(NbRetryProcess.meta, null);
                while (tipiFacade.isRunning(pid)) {
                    Thread.sleep(100);
                }
                return pid;
            }
        });

        txTemplate.txWithout(s -> {
            DbActivity model = activityRepository.findOne(pid);
            Assert.assertEquals(6, model.getNbRetryDone());
            Assert.assertEquals(ActivityState.ERROR, model.getState());

            // Call stack
            assertContains("TEST: une erreur", model.getCallstack());
            assertContains("NbRetryTest$NbRetryProcess.execute", model.getCallstack());
            assertContains("ActivityRunner.executeActivity", model.getCallstack());

        });
    }

    public static void assertContains(String containee, String container) {
        assertContains(containee, container, "'" + container + "' does not contain '" + containee + "'");
    }

    public static void assertContains(String containee, String container, String msg) {
        if (StringUtils.isNotBlank(containee) && StringUtils.isNotBlank(container)) {
            Assert.assertTrue(msg, container.contains(containee));
        } else {
            Assert.fail("Les 2 valeurs à comparer ne peuvent pas être nulles");
        }
    }

}
