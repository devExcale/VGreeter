package ovh.excale.vgreeter.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ovh.excale.vgreeter.models.GuildModel;

@Repository
public interface GuildRepository extends CrudRepository<GuildModel, Long> {

}
