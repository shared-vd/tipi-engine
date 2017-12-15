package ch.sharedvd.tipi.engine.repository;

import ch.sharedvd.tipi.engine.model.DbTopProcess;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopProcessRepository extends JpaRepository<DbTopProcess, Long> {
}
