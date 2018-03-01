package ch.sharedvd.tipi.engine.meta;

import ch.sharedvd.tipi.engine.action.TopProcess;
import org.springframework.util.Assert;

public class TopProcessMetaModel extends SubProcessMetaModel {

    private static final long serialVersionUID = 1L;

    private Boolean startable;
    private Boolean deleteWhenFinished;
    private Boolean isShownInUI;
    private final int priority;
    private final int nbMaxTopConcurrent;
    private final int nbMaxConcurrent;

    public TopProcessMetaModel(Class<?> clazz, int priority, int nbMaxConcurrent, String descr) {
        this(clazz, priority, Integer.MAX_VALUE, nbMaxConcurrent, descr);
    }

    public TopProcessMetaModel(Class<?> clazz, int priority,
                               int nbMaxTopConcurrent,
                               int nbMaxConcurrent, String descr, boolean showInUI) {
        super(clazz, null, descr);
        this.priority = priority;
        this.nbMaxTopConcurrent = nbMaxTopConcurrent;
        this.nbMaxConcurrent = nbMaxConcurrent;
        this.isShownInUI = showInUI;
        Assert.isTrue(TopProcess.class.isAssignableFrom(clazz));
    }

    public TopProcessMetaModel(Class<?> clazz, int priority,
                               int nbMaxTopConcurrent,
                               int nbMaxConcurrent, String descr) {
        super(clazz, null, descr);
        this.priority = priority;
        this.nbMaxTopConcurrent = nbMaxTopConcurrent;
        this.nbMaxConcurrent = nbMaxConcurrent;
        Assert.isTrue(TopProcess.class.isAssignableFrom(clazz));
    }

    public TopProcessMetaModel(Class<?> clazz, String[] usedConnections, int priority, int nbMaxTopConcurrent, int nbMaxConcurrent, String descr) {
        super(clazz, usedConnections, descr);
        this.priority = priority;
        this.nbMaxTopConcurrent = nbMaxTopConcurrent;
        this.nbMaxConcurrent = nbMaxConcurrent;
        Assert.isTrue(TopProcess.class.isAssignableFrom(clazz));
    }

    public boolean isShownInUI() {
        return isShownInUI == null || isShownInUI;
    }
    void setShownInUI(Boolean shownInUI) {
        isShownInUI = shownInUI;
    }

    /**
     * Défini si le process est démarrable au travers du BatchScheduler
     *
     * @return
     */
    public boolean isStartable() {
        return startable == null || startable;
    }

    void setStartable(boolean startable) {
        this.startable = startable;
    }

    /**
     * Défini si le process est supprimé s'il se termine sans erreur ou conservé en base de données
     * Utile principalement pour les rapports (DiffCdhUpi) les tests (Pour vérifier l'état des variables)
     *
     * @return
     */
    public boolean isDeleteWhenFinished() {
        return deleteWhenFinished == null || deleteWhenFinished;
    }

    public void setDeleteWhenFinished(boolean deleteWhenFinished) {
        this.deleteWhenFinished = deleteWhenFinished;
    }

    public int getPriority() {
        return priority;
    }

    /**
     * @return le nombre maximal de top processus en exécution concurrente.
     */
    public int getNbMaxTopConcurrent() {
        return nbMaxTopConcurrent;
    }

    /**
     * @return le nombre maximal de processus en exécution concurrente dans toute la hiérarchie des processus (top process + sub processes).
     */
    public int getNbMaxConcurrent() {
        return nbMaxConcurrent;
    }

}
