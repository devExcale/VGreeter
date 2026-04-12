package ovh.excale.vgreeter.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ovh.excale.vgreeter.models.GuildModel;

@Repository
public interface GuildRepository extends JpaRepository<GuildModel, Long> {

}
