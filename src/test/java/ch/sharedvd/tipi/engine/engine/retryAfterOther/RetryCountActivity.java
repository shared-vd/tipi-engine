package ch.sharedvd.tipi.engine.engine.retryAfterOther;

import ch.sharedvd.tipi.engine.action.Activity;
import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.meta.ActivityMetaModel;
import ch.sharedvd.tipi.engine.utils.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetryCountActivity extends Activity {

    private static final Logger log = LoggerFactory.getLogger(RetryCountActivity.class);

    public static final ActivityMetaModel meta = new ActivityMetaModel(RetryCountActivity.class);

    private static int passages = 0;

    @Override
    protected ActivityResultContext execute() throws Exception {
        passages++;

        /**
         * On vérifie que la premiere activité part en erreur
         * Puis c'est la 2eme qui executée (pas la premiere qui est partie en erreur
         * Puis ensuite la 1ere est relancée et passe
         */

        if (RetryAfterOtherExecutingTest.FIRST_ACTI_ID < 0) {
            RetryAfterOtherExecutingTest.FIRST_ACTI_ID = getActivityId();
            // 1er passage -> exception
            Assert.isEqual(1, passages);
            Assert.fail("Assert.fail pour les tests. Pas de problème boy!");
        } else if (RetryAfterOtherExecutingTest.FIRST_ACTI_ID != getActivityId()) {
            // 2eme passage -> OK
            Assert.isEqual(2, passages);
        } else if (RetryAfterOtherExecutingTest.FIRST_ACTI_ID == getActivityId()) {
            // 3eme passage -> OK
            Assert.isEqual(3, passages);
        }

        log.info("Passage");

        return new FinishedActivityResultContext();
    }

}
