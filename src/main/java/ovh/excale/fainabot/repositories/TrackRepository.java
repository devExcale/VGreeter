package ovh.excale.fainabot.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import ovh.excale.fainabot.models.TrackModel;
import ovh.excale.fainabot.models.UserModel;

@Repository
public interface TrackRepository extends PagingAndSortingRepository<TrackModel, Long> {

	boolean existsByNameAndUploader(String name, UserModel uploader);

}
