package ch.vd.registre.tipi.testing.ut;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.client.TipiTopProcess;
import ch.vd.registre.base.utils.Assert;

import java.util.concurrent.atomic.AtomicInteger;

@TipiTopProcess(description = "UT : Process de test du service UtTipiTestingServiceTest")
public class UtTipiTestingServiceTestProcess extends TopProcess {

    public static AtomicInteger counter = new AtomicInteger(0);

    @Override
    protected ActivityResultContext execute() throws Exception {
        counter.set(1);

        while (counter.get() == 1) {
            Thread.sleep(10);
        }

        Assert.isEqual(2, counter.get());

        // Attends un peu pour que la méthode waitEndAll fasse son boulot
        Thread.sleep(1000);

        counter.set(3);
        return new FinishedActivityResultContext();
    }
}
