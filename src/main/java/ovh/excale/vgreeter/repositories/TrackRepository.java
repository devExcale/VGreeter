package ovh.excale.vgreeter.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import ovh.excale.vgreeter.models.TrackModel;
import ovh.excale.vgreeter.models.UserModel;

@Repository
public interface TrackRepository extends PagingAndSortingRepository<TrackModel, Long> {

	boolean existsByNameAndUploader(String name, UserModel uploader);

	@Query("select t from TrackModel t where lower(t.name) like lower(?1)")
	Page<TrackModel> findAllByNameQuery(String name, Pageable pageable);

	Page<TrackModel> findAllByUploaderIdIs(long userId, Pageable pageable);

}
