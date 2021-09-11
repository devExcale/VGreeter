package ovh.excale.vgreeter.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import ovh.excale.vgreeter.models.TrackModel;
import ovh.excale.vgreeter.models.UserModel;

@Repository
public interface TrackRepository extends PagingAndSortingRepository<TrackModel, Long> {

	boolean existsByNameAndUploader(String name, UserModel uploader);

}
