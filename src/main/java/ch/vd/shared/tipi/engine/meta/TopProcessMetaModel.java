package ch.vd.shared.tipi.engine.meta;

import ch.vd.shared.tipi.engine.action.TopProcess;
import org.springframework.util.Assert;

import java.util.List;

public class TopProcessMetaModel extends SubProcessMetaModel {

    private static final long serialVersionUID = 1L;

    private Boolean startable;
    private Boolean deleteWhenFinished;
    private Boolean isShownInUI;
    private final int priority;
    private final int nbMaxTopConcurrent;
    private final int nbMaxConcurrent;

    private List<VariableDescription> variablesDescription = null;

    public TopProcessMetaModel(Class<?> clazz, int priority, int nbMaxConcurrent, String descr) {
        this(clazz, priority, Integer.MAX_VALUE, nbMaxConcurrent, descr);
    }

    public TopProcessMetaModel(Class<?> clazz, int priority, int nbMaxTopConcurrent, int nbMaxConcurrent, String descr) {
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

    public List<VariableDescription> getVariablesDescription() {
        return variablesDescription;
    }

    public void setVariablesDescription(List<VariableDescription> vars) {
        variablesDescription = vars;
    }

    public boolean isShownInUI() {
        return isShownInUI == null || isShownInUI;
    }

    public void setShownInUI(boolean show) {
        this.isShownInUI = show;
    }

    /**
     * Défini si le process est démarrable au travers du BatchScheduler
     *
     * @return
     */
    public boolean isStartable() {
        return startable == null || startable;
    }

    public void setStartable(boolean startable) {
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
