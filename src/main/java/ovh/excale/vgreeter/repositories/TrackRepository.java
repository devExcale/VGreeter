package ovh.excale.vgreeter.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import ovh.excale.vgreeter.models.TrackModel;
import ovh.excale.vgreeter.models.UserModel;

@Repository
public interface TrackRepository extends PagingAndSortingRepository<TrackModel, Long> {

	boolean existsByNameAndUploader(String name, UserModel uploader);

	Page<TrackModel> findAllByNameLike(String name, Pageable pageable);

	Page<TrackModel> findAllByUploaderIdIs(long userId, Pageable pageable);

}
