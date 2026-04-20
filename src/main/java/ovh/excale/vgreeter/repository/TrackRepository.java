package ovh.excale.vgreeter.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ovh.excale.vgreeter.entity.MemberEntity;
import ovh.excale.vgreeter.entity.TrackEntity;

import java.util.Optional;

@Repository
public interface TrackRepository extends JpaRepository<TrackEntity, Long> {

	boolean existsByTitleAndOwner(String title, MemberEntity owner);

	@Query("select t from Track t where lower(t.title) like lower(?1)")
	Page<TrackEntity> findAllByTitleQuery(String title, Pageable pageable);

	Page<TrackEntity> findAllByOwnerIdIs(long ownerId, Pageable pageable);

	Optional<TrackEntity> findByIdAndOwnerId(Long trackId, Long ownerId);

}
