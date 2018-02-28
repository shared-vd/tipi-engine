package ch.sharedvd.tipi.engine.query;

import ch.sharedvd.tipi.engine.infos.ActivityThreadInfos;
import ch.sharedvd.tipi.engine.infos.ConnectionCapInfos;
import ch.sharedvd.tipi.engine.infos.TipiActivityInfos;
import ch.sharedvd.tipi.engine.infos.TipiTopProcessInfos;
import ch.sharedvd.tipi.engine.utils.ResultListWithCount;

import java.util.List;

public interface TipiQueryFacade {

    /**
     * Returns one Activity
     *
     * @param id
     * @return
     */
    TipiActivityInfos getActivityInfos(long id);

    /**
     * Returns one Activity
     *
     * @param id
     * @param loadVariables returns also its variables
     * @return
     */
    TipiActivityInfos getActivityInfos(long id, boolean loadVariables);

    /**
     * Retuens the running processes
     *
     * @param maxHits
     * @return the running processes (those that are not FINISHED)
     */
    ResultListWithCount<TipiTopProcessInfos> getRunningProcesses(final int maxHits);

    /**
     * Recherche les activités selon le criteria passé (recherche en HQL)
     *
     * @param criteria
     * @param maxHits
     * @return le liste des activités
     */
    ResultListWithCount<TipiActivityInfos> searchActivities(TipiCriteria criteria, int maxHits);

    /**
     * Renvoie la liste des activités dont le correlationId est spécifié
     *
     * @param aCorrelationId
     * @return
     */
    List<Long> getActivitiesForCorrelationId(String aCorrelationId);

    /**
     * Renvoie la liste des Threads actives pour le moteur TiPi
     *
     * @return
     */
    List<ActivityThreadInfos> getThreadsInfos();

    // Connections cup
    List<ConnectionCapInfos> getAllConnectionCupInfos();

}
