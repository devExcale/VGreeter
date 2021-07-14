package ovh.excale.fainabot.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ovh.excale.fainabot.models.UserModel;

@Repository
public interface UserRepository extends CrudRepository<UserModel, Long> {

	boolean existsByAltname(String altname);

}
