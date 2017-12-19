package ch.vd.registre.tipi.engine.retryAfterOther;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import org.apache.log4j.Logger;

public class RetryCountProcess extends TopProcess {

    private static final Logger LOGGER = Logger.getLogger(RetryCountProcess.class);

    public static final TopProcessMetaModel meta = new TopProcessMetaModel(RetryCountProcess.class, 1, -1, 1, null);

    @Override
    protected ActivityResultContext execute() throws Exception {

        addChildActivity(RetryCountActivity.meta, null);
        addChildActivity(RetryCountActivity.meta, null);

        LOGGER.info("Passage");

        return new FinishedActivityResultContext();
    }

}
