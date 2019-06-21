package ch.sharedvd.tipi.engine.client;

import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;

public interface TipiFacade {

    /**
     * Démarre le process spécifié.
     *
     * @param meta les meta-données du process à démarrer
     * @param vars les variables de démarrage du process
     * @return l'id du process démarré
     */
    long launch(TopProcessMetaModel meta, VariableMap vars);

    long launch(Class<? extends TopProcess> cls, VariableMap vars);

    void unsuspendActivity(final long id, final VariableMap vars);

    void restartInErrorProcess(final long id);

    void resumeAllError();

    void resumeErrors(String groupName);

    void resumeAllSuspended();

    boolean hasActivityPending();

    boolean hasCommandPending();

    int getPendingCommandCount();

    /**
     * Test si l'activité (our le sub process ou le process) dont l'id est donné est en cours de traitement.
     *
     * @param pid l'id d'une activité
     * @return
     */
    boolean isRunning(long pid);

    /**
     * Test si le processus contenant l'activité (our le sub process ou le process) dont l'id est donné est
     * en cours de traitement.
     *
     * @param id l'id d'une activité du processus
     * @return
     */
    boolean isProcessRunning(long id);

    /**
     * Détermine si l'activité spécifiée est en cours d'exécution dans un thread actuellement (une activité pour être dans l'état EXECUTING, mais néanmoins en
     * attente si tous les threads d'exécution sont occupés, par exemple).
     *
     * @param aid l'id d'une activité
     * @return <b>vrai</b> si l'activité est en cours d'exécution; <b>faux</b> autrement.
     */
    boolean isProcessScheduled(final long aid);

    boolean isResumable(long pid);

    /**
     * Passe le processus spécifié à l'état {@link ch.sharedvd.tipi.engine.model.ActivityState#ABORTED}. <b>Attention !</b> Cette méthode n'a aucun effet sur les sous-processus.
     *
     * @param topPid l'id d'un top-processus.
     * @param delete s'il faut effacer le processus après l'avoir arrêté.
     */
    void abortProcess(long topPid, boolean delete);

    void startTipi() throws Exception;

    void stopTipi() throws Exception;

    boolean isTipiStarted() throws Exception;

    // Groupes
    void startAllGroups() throws Exception;

    void stopAllGroups() throws Exception;

    void startGroup(String fqn) throws Exception;

    void restartGroup(String fqn, int nbMax, int priority) throws Exception;

    void stopGroup(String fqn) throws Exception;

    void setMaxConnections(String aConnectionType, int aNbMaxConnections);

    void setMaxConcurrentActivitiesForGroup(String aGroupName, int aNbMaxConnections);

    void setPriorityForGroup(String aGroupName, int aPrio);

    void coldRestart();

}
