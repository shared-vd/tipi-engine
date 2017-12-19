package ch.sharedvd.tipi.engine.repository;

import ch.sharedvd.tipi.engine.model.ActivityState;
import ch.sharedvd.tipi.engine.model.DbActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<DbActivity, Long> {

    List<DbActivity> findChildren(DbActivity parent);

    List<DbActivity> findByState(ActivityState state);

    List<DbActivity> findByStateOrRequestEndExecution(ActivityState state, boolean reqEnd);

    List<DbActivity> findByGroupAndState(String groupName, ActivityState state);

    List<DbActivity> findExecutingActivities(String topProcessName);

    List<String> findTopProcessNamesByStateAndReqEnd(ActivityState state, boolean reqEnd);

    List<DbActivity> findByParentId(long parentId);

    List<DbActivity> findByRequestEndExecutionOrderById(boolean state);

}
