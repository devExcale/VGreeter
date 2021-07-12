package ovh.excale.fainabot.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ovh.excale.fainabot.models.TrackModel;

@Repository
public interface TrackRepository extends CrudRepository<TrackModel, Integer> {

	Page<TrackModel> findAll(Pageable pageable);

}
