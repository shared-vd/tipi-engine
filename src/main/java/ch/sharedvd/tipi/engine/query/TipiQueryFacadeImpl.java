package ch.sharedvd.tipi.engine.query;

import ch.sharedvd.tipi.engine.infos.ActivityThreadInfos;
import ch.sharedvd.tipi.engine.infos.ConnectionCapInfos;
import ch.sharedvd.tipi.engine.infos.TipiActivityInfos;
import ch.sharedvd.tipi.engine.infos.TipiTopProcessInfos;
import ch.sharedvd.tipi.engine.utils.ResultListWithCount;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class TipiQueryFacadeImpl implements TipiQueryFacade {

    @Autowired
    private ActivityQueryService activityQueryService;

    @Override
    public TipiActivityInfos getActivityInfos(long id) {
        return activityQueryService.getActivityInfos(id, true);
    }

    @Override
    public TipiActivityInfos getActivityInfos(long id, boolean loadVariables) {
        return activityQueryService.getActivityInfos(id, loadVariables);
    }

    @Override
    public ResultListWithCount<TipiTopProcessInfos> getRunningProcesses(int maxHits) {
        return activityQueryService.getRunningProcesses(maxHits);
    }

    @Override
    public ResultListWithCount<TipiTopProcessInfos> getAllProcesses(int maxHits) {
        return activityQueryService.getAllProcesses(maxHits);
    }

    @Override
    public ResultListWithCount<TipiActivityInfos> searchActivities(final TipiCriteria criteria, final int maxHits) {
        return activityQueryService.searchActivities(criteria, maxHits);
    }

    @Override
    public List<Long> getActivitiesForCorrelationId(String aCorrelationId) {
        return activityQueryService.getActivitiesForCorrelationId(aCorrelationId);
    }

    @Override
    public List<ActivityThreadInfos> getThreadsInfos() {
        return activityQueryService.getThreadsInfos();
    }

    @Override
    public List<ConnectionCapInfos> getAllConnectionCupInfos() {
        return activityQueryService.getAllConnectionCupInfos();
    }

}
