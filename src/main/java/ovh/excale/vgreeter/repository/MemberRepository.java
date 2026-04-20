package ovh.excale.vgreeter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ovh.excale.vgreeter.entity.MemberEntity;

import java.util.function.Supplier;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

	default MemberEntity findByIdOrSave(long id, Supplier<MemberEntity> creator) {
		return findById(id).orElseGet(() -> save(creator.get()));
	}

}
