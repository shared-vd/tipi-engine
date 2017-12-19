package ch.vd.registre.tipi.batch;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.client.TipiTopProcess;
import ch.sharedvd.tipi.engine.client.TipiVariable;
import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import ch.sharedvd.tipi.engine.meta.VariableType;
import ch.vd.registre.base.utils.ArrayLong;
import ch.vd.registre.quartz.client.BatchScheduler;
import ch.vd.registre.quartz.client.def.JobDefinition;
import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


@ContextConfiguration(locations = {
        "classpath:ch/vd/registre/tipi/batch/TipiBatchSchedulerWithAnnotationsTest-spring.xml"
})
public class TipiBatchSchedulerWithAnnotationsTest extends TipiEngineTest {

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
        JobDefinition job = scheduler.startJob("BatchTopProcess", vars);
        assertNotNull(job);

        assertNotNull(scheduler.getJob("BatchTopProcess"));

        // Attend que le process ait démarré
        do {
            Thread.sleep(200);
        }
        while (BatchTopProcess.state.get() < 1);

        assertNotNull(scheduler.getJob("BatchTopProcess"));

        // Permet au processus de se terminer
        BatchTopProcess.state.incrementAndGet();

        // Attends la fin du processus
        scheduler.waitEndJob("BatchTopProcess");

        // Attend que le process ait démarré
        do {
            Thread.sleep(200);
        }
        while (BatchTopProcess.state.get() != 3);

        assertEquals(3, BatchTopProcess.state.get());
    }

    @Test(timeout = 30000)
    public void startJobWithDefaultParam() throws Exception {
        JobWithParams.data.setValue(null);

        scheduler.startJobWithDefaultParams("JobWithParams");

        // Attends la fin du processus
        scheduler.waitEndJob("JobWithParams");

        assertEquals("mega", JobWithParams.data.getValue());
    }

    @Test(timeout = 30000)
    public void startJobWithExplicitParam() throws Exception {
        JobWithParams.data.setValue(null);

        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("param", "radada");

        scheduler.startJob("JobWithParams", params);

        // Attends la fin du processus
        scheduler.waitEndJob("JobWithParams");

        assertEquals("radada", JobWithParams.data.getValue());
    }

    @TipiTopProcess(description = "TopProcess description", maxRetryForDefaultPolicy = 0, priority = 5, nbMaxConcurrent = 10, deleteWhenFinished = false, startable = true)
    public static class BatchTopProcess extends TopProcess {

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

    @TipiTopProcess(description = "Avec des variables par défaut", maxRetryForDefaultPolicy = 0, priority = 5, nbMaxConcurrent = 10, deleteWhenFinished = false, startable = true,
            variables = {@TipiVariable(name = "param", type = VariableType.String, defaultValue = "mega")})
    public static class JobWithParams extends TopProcess {

        public static final MutableObject<String> data = new MutableObject<String>();

        @Override
        protected ActivityResultContext execute() throws Exception {

            final String val = getStringVariable("param");
            data.setValue(val);

            return new FinishedActivityResultContext();
        }
    }
}
