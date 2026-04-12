package ovh.excale.vgreeter.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ovh.excale.vgreeter.models.UserModel;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {

	boolean existsByAltname(String altname);

}
