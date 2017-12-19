package ch.sharedvd.tipi.engine.repository;

import ch.sharedvd.tipi.engine.model.DbTopProcess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TopProcessRepository extends JpaRepository<DbTopProcess, Long> {

    List<DbTopProcess> findProcessesByFqn(String processName);

}
