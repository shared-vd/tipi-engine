package ch.vd.shared.tipi.engine.action;

import ch.vd.shared.tipi.engine.client.AbortManager;
import ch.vd.shared.tipi.engine.utils.ArrayLong;
import ch.vd.shared.tipi.engine.utils.Assert;
import ch.vd.shared.tipi.engine.utils.InputStreamHolder;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public abstract class Activity {

    private static final Logger LOGGER = Logger.getLogger(Activity.class);

    protected ActivityFacade facade;

    protected Activity() {
    }

    protected Activity(ActivityFacade facade) {
        this.facade = facade;
    }

    protected abstract ActivityResultContext execute() throws Exception;

    /**
     * Méthode appelée avant l'execute et le terminate
     *
     * @throws Exception
     */
    protected void begin() throws Exception {
    }

    /**
     * Méthode appelée après l'execute et le terminate
     *
     * @throws Exception
     */
    protected void end() throws Exception {
    }

    public final ActivityResultContext doExecute() throws Exception {
        //On démarre l'activité
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Executing activity: " + facade.getId() + ". Nom: " + facade.getName() + ". ");
        }

        ActivityResultContext resultContext;
        try {
            begin();
            resultContext = execute();
        } catch (ActivityException e) {
            resultContext = new ErrorActivityResultContext("Type: " + e.getClass().getName() + ". Message: " + e.getMessage());
        } finally {
            try {
                end();
            } catch (Throwable t) {
                LOGGER.error("La méthode end() a renvoyé une exception!", t);
            }
        }

        //On envoie un message pour indiquer qu'il faut terminer l'activité
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Finishing activity: " + facade.getId() + ". Nom: " + facade.getName() + ". ");
        }
        return resultContext;
    }

    /**
     * Appelé si l'activité est mise en erreur (typiquement après 5 retry)
     *
     * @param exception
     */
    // A overrider
    public void onError(Throwable exception) {

    }

    /**
     * Appelé après le commit, typiquement pour logger le temps y compris le temps de commit
     */
    // A overrider
    public void onAfterCommit() {

    }

    public void testAbort() {
        getAbortManager().testAbort();
    }

    public long getActivityId() {
        return facade.getId();
    }

    public List<ActivityFacade> getChildren() {
        return facade.getChildren();
    }

    protected Object getVariable(String name) {
        return facade.getVariable(name);
    }

    protected LocalDate getRegDateVariable(String name) {
        Integer i = (Integer) facade.getVariable(name);
        if (i != null) {
            Assert.fail("Bla");
            return null;//LocalDate.fromIndex(i, true);
        }
        return null;
    }

    protected InputStream getInputStreamVariable(String name) {
        InputStreamHolder variable = (InputStreamHolder) facade.getVariable(name);
        return variable.getInputStream();
    }

    protected Integer getIntVariable(String name) {
        return (Integer) facade.getVariable(name);
    }

    protected String getStringVariable(String name) {
        return (String) facade.getVariable(name);
    }

    protected Boolean getBooleanVariable(String name) {
        return (Boolean) facade.getVariable(name);
    }

    protected Long getLongVariable(String name) {
        return (Long) facade.getVariable(name);
    }

    protected ArrayLong getArrayLongVariable(String name) {
        return (ArrayLong) facade.getVariable(name);
    }

    protected Integer getIntVariable(long id, String name) {
        return (Integer) facade.getVariable(id, name);
    }

    protected String getStringVariable(long id, String name) {
        return (String) facade.getVariable(id, name);
    }

    protected Long getLongVariable(long id, String name) {
        return (Long) facade.getVariable(id, name);
    }

    protected void putVariable(String key, Serializable value) {
        facade.putVariable(key, value);
    }

    public void setFacade(ActivityFacade facade) {
        this.facade = facade;
    }

    protected AbortManager getAbortManager() {
        AbortManager abort = new AbortManagerImpl(facade);
        return abort;
    }

}
