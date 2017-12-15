package ch.sharedvd.tipi.engine.repository;

import ch.sharedvd.tipi.engine.model.DbActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<DbActivity, Long> {

    List<DbActivity> findByParentId(long parentId);
}
