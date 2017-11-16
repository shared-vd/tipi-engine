package ch.vd.shared.tipi.engine.repository;

import ch.vd.shared.tipi.engine.model.ActivityModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityModelRepository extends JpaRepository<ActivityModel, Long> {

    List<ActivityModel>  findByParentId(long parentId);
}
