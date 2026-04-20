package ovh.excale.vgreeter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ovh.excale.vgreeter.entity.GuildEntity;

import java.util.function.Supplier;

@Repository
public interface GuildRepository extends JpaRepository<GuildEntity, Long> {

	default GuildEntity findByIdOrSave(long id, Supplier<GuildEntity> creator) {
		return findById(id).orElseGet(() -> save(creator.get()));
	}

}
