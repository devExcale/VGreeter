package ovh.excale.vgreeter.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ovh.excale.vgreeter.models.UserModel;

@Repository
public interface UserRepository extends CrudRepository<UserModel, Long> {

	boolean existsByAltname(String altname);

}
