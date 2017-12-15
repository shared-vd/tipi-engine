package ch.sharedvd.tipi.engine.action;

import ch.sharedvd.tipi.engine.client.VariableMap;
import ch.sharedvd.tipi.engine.meta.ActivityMetaModel;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

public abstract class SubProcess extends Activity {

    private static final Logger LOGGER = Logger.getLogger(SubProcess.class);

    // On ne peut ajouter des children que dans le execute()
    private boolean canAddChilren = true;

    public SubProcess() {
    }

    public SubProcess(ActivityFacade facade) {
        super(facade);
    }

    public final ActivityResultContext doTerminate() throws Exception {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Executing activity: " + facade.getId() + ". Nom: " + facade.getName() + ". ");
        }

        ActivityResultContext resultContext;
        try {
            canAddChilren = false;
            begin();
            resultContext = terminate();
        } catch (ActivityException e) {
            resultContext = new ErrorActivityResultContext("Type: " + e.getClass().getName() + ". Message: " + e.getMessage());
        } finally {
            try {
                end();
            } catch (Throwable t) {
                LOGGER.error("La méthode end() a renvoyé une exception!", t);
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Executing activity: " + facade.getId() + ". Nom: " + facade.getName() + ". ");
        }
        return resultContext;
    }

    protected ActivityResultContext terminate() throws Exception {
        return new FinishedActivityResultContext();
    }

    protected long addChildActivity(Class<? extends Activity> clazz, VariableMap vars) {
        return addChildActivity(clazz, null, vars);
    }

    protected long addChildActivity(Class<? extends Activity> clazz, Long previousId, VariableMap vars) {
        Assert.isTrue(canAddChilren);
        return facade.addChildActivity(clazz, previousId, vars);
    }

    protected long addChildActivity(Class<? extends Activity> clazz, Long previousId, VariableMap vars, String correlationId) {
        Assert.isTrue(canAddChilren);
        return facade.addChildActivity(clazz, previousId, vars, correlationId);
    }

    protected long addChildActivity(ActivityMetaModel meta, VariableMap vars) {
        return addChildActivity(meta, null, vars);
    }

    protected long addChildActivity(ActivityMetaModel meta, Long previousId, VariableMap vars) {
        Assert.isTrue(canAddChilren);
        return facade.addChildActivity(meta, previousId, vars);
    }


}
