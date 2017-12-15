package ch.sharedvd.tipi.engine.action;

import ch.sharedvd.tipi.engine.client.TipiUnknownActivity;

/**
 * A process representing some process defined in the database, but whom matching Java class has not been found.
 * This could be used to delete old batches after MeP.
 *
 * @author xsicdt
 */
@TipiUnknownActivity
public class UnknownProcess extends TopProcess {


    @Override
    protected ActivityResultContext execute() throws Exception {
        return new ErrorActivityResultContext();
    }

}
