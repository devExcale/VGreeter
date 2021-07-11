package ovh.excale.fainabot.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ovh.excale.fainabot.models.GuildModel;

@Repository
public interface GuildRepository extends CrudRepository<GuildModel, Long> {

}
