package ch.vd.registre.tipi.batch;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import ch.vd.registre.base.utils.ArrayLong;
import ch.vd.registre.quartz.client.BatchScheduler;
import ch.vd.registre.quartz.client.def.JobDefinition;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


@ContextConfiguration(locations = {
        "classpath:ch/vd/registre/tipi/batch/TipiBatchSchedulerTest-spring.xml"
})
public class TipiBatchSchedulerTest extends TipiEngineTest {

    @Autowired
    private BatchScheduler scheduler;

    @Test
    public void withBlobVariable_Start_Wait_End() throws Exception {

        long[] blob = new long[120000];

        List<Long> list = new ArrayList<Long>();
        list.add(12L);
        list.add(15L);
        list.add(20L);
        list.add(33L);

        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("name", "Bla bla");
        vars.put("list", new ArrayLong(list));
        vars.put("blob", blob);

        // Lance le job
        BatchTopProcess.state.set(0);
        JobDefinition job = scheduler.startJob(BatchTopProcess.meta.getSimpleName(), vars);
        assertNotNull(job);

        assertNotNull(scheduler.getJob(BatchTopProcess.meta.getSimpleName()));

        // Attend que le process ait démarré
        do {
            Thread.sleep(200);
        }
        while (BatchTopProcess.state.get() < 1);

        assertNotNull(scheduler.getJob(BatchTopProcess.meta.getSimpleName()));

        // Permet au processus de se terminer
        BatchTopProcess.state.incrementAndGet();

        // Attends la fin du processus
        scheduler.waitEndJob(BatchTopProcess.meta.getSimpleName());

        // Attend que le process ait démarré
        do {
            Thread.sleep(200);
        }
        while (BatchTopProcess.state.get() != 3);

        assertEquals(3, BatchTopProcess.state.get());
    }

    public static class BatchTopProcess extends TopProcess {

        public final static TopProcessMetaModel meta = new TopProcessMetaModel(BatchTopProcess.class, null, 5, -1, 10, "TopProcess description") {
            @Override
            protected void init() {
                setDeleteWhenFinished(false);
                setStartable(true);
            }
        };

        public static AtomicInteger state = new AtomicInteger(0);

        @Override
        protected ActivityResultContext execute() throws Exception {

            state.incrementAndGet();
            do {
                Thread.sleep(200);
            } while (state.get() < 2);

            state.incrementAndGet();

            return new FinishedActivityResultContext();
        }
    }
}
