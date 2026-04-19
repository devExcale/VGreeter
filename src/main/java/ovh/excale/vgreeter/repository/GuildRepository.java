package ovh.excale.vgreeter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ovh.excale.vgreeter.entity.GuildEntity;

@Repository
public interface GuildRepository extends JpaRepository<GuildEntity, Long> {

}
