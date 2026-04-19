package ovh.excale.vgreeter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ovh.excale.vgreeter.entity.LogErrorEntity;

import java.util.UUID;

@Repository
public interface LogErrorRepository extends JpaRepository<LogErrorEntity, UUID> {

}
