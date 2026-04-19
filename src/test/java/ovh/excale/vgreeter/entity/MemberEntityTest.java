package ovh.excale.vgreeter.entity;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

import static org.junit.jupiter.api.Assertions.*;
import static ovh.excale.vgreeter.entity.EntityUtils.*;

@DataJpaTest(
	excludeAutoConfiguration = DataJpaRepositoriesAutoConfiguration.class,
	properties = {
		"spring.datasource.url=jdbc:h2:mem:vgreeter;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE;NON_KEYWORDS=member",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=validate",
		"spring.sql.init.mode=always",
		"spring.sql.init.schema-locations=classpath:h2/schema.sql"
	})
@EntityScan(basePackageClasses = MemberEntity.class)
class MemberEntityTest {

	@Autowired
	private TestEntityManager entityManager;

	/*=== Positive Tests ===*/

	@Test
	void givenValidMember_whenSave_thenSuccess() {

		// Prepare test data
		long discordId = 3001L;
		String discordUsername = "member-one";

		// Save member
		entityManager.persistAndFlush(validMember(discordId, discordUsername));
		entityManager.clear();

		// Verify saved member
		MemberEntity reloaded = entityManager.find(MemberEntity.class, discordId);
		assertNotNull(reloaded);
		assertEquals(discordId, reloaded.getDiscordId());
		assertEquals(discordUsername, reloaded.getDiscordUsername());
		assertEquals(DEFAULT_TRACK_MAX_SIZE, reloaded.getTrackMaxSize());
		assertTrue(reloaded.getTracks().isEmpty());

	}

	@Test
	void givenSavedMember_whenUpdateDiscordUsername_thenSuccess() {

		// Prepare test data
		long discordId = 3002L;
		String updatedUsername = "member-two-updated";

		// Save initial member
		MemberEntity member = entityManager.persistAndFlush(validMember(discordId, "member-two"));

		// Update member username
		member.setDiscordUsername(updatedUsername);
		entityManager.flush();
		entityManager.clear();

		// Verify update
		MemberEntity reloaded = entityManager.find(MemberEntity.class, discordId);
		assertNotNull(reloaded);
		assertEquals(updatedUsername, reloaded.getDiscordUsername());

	}

	@Test
	void givenMissingTrackMaxSize_whenSave_thenDefaultApplied() {

		// Prepare test data
		long discordId = 3003L;

		// Insert raw row without track_max_size
		EntityManager em = entityManager.getEntityManager();
		em.createNativeQuery("insert into member (discord_id, discord_username) values (?, ?)")
			.setParameter(1, discordId)
			.setParameter(2, "member-three")
			.executeUpdate();
		entityManager.clear();

		// Verify default track max size
		MemberEntity reloaded = entityManager.find(MemberEntity.class, discordId);
		assertNotNull(reloaded);
		assertEquals(DEFAULT_TRACK_MAX_SIZE, reloaded.getTrackMaxSize());

	}

	@Test
	void givenSavedMember_whenUpdateTrackMaxSize_thenSuccess() {

		// Prepare test data
		long discordId = 3004L;
		long updatedTrackMaxSize = 128 * 1024L;

		// Save initial member
		MemberEntity member = entityManager.persistAndFlush(validMember(discordId, "member-four"));

		// Update track max size
		member.setTrackMaxSize(updatedTrackMaxSize);
		entityManager.flush();
		entityManager.clear();

		// Verify update
		MemberEntity reloaded = entityManager.find(MemberEntity.class, discordId);
		assertNotNull(reloaded);
		assertEquals(updatedTrackMaxSize, reloaded.getTrackMaxSize());

	}

	/*=== Negative Tests ===*/

	@Test
	void givenNullDiscordId_whenSave_thenFail() {

		// Prepare invalid test data
		MemberEntity member = MemberEntity.builder()
			.discordUsername("missing-id")
			.trackMaxSize(DEFAULT_TRACK_MAX_SIZE)
			.build();

		// Verify failure
		assertThrows(RuntimeException.class, () -> entityManager.persistAndFlush(member));

	}

	@Test
	void givenNullDiscordUsername_whenSave_thenFail() {

		// Prepare invalid test data
		MemberEntity member = MemberEntity.builder()
			.discordId(3006L)
			.trackMaxSize(DEFAULT_TRACK_MAX_SIZE)
			.build();

		// Verify failure
		assertThrows(RuntimeException.class, () -> entityManager.persistAndFlush(member));

	}

	/*=== Relational and Aggregation Tests ===*/

	@Test
	void givenValidTrack_whenAddToOwnedTracks_thenSuccess() {

		// Prepare test data
		long ownerDiscordId = 3007L;

		// Save owner and track
		MemberEntity owner = entityManager.persistAndFlush(validMember(ownerDiscordId, "member-seven"));
		TrackEntity track = entityManager.persistAndFlush(validTrack(owner, "Owned Track A", EMPTY_OPUS));
		entityManager.clear();

		// Verify owned tracks relation
		MemberEntity reloaded = entityManager.find(MemberEntity.class, ownerDiscordId);
		Long trackId = track.getId();
		assertNotNull(reloaded);
		assertEquals(1, reloaded.getTracks().size());
		assertTrue(
			reloaded.getTracks()
				.stream()
				.anyMatch(t -> t.getId().equals(trackId))
		);

	}

	@Test
	void givenTrackInOwnedTracks_whenRemove_thenSuccess() {

		// Prepare test data
		long ownerDiscordId = 3008L;

		// Save owner and track
		MemberEntity owner = entityManager.persistAndFlush(validMember(ownerDiscordId, "member-eight"));
		TrackEntity track = entityManager.persistAndFlush(validTrack(owner, "Owned Track B", EMPTY_OPUS));
		entityManager.clear();

		// Verify track exists and then remove it
		TrackEntity reloadedTrack = entityManager.find(TrackEntity.class, track.getId());
		assertNotNull(reloadedTrack);
		entityManager.remove(reloadedTrack);
		entityManager.flush();
		entityManager.clear();

		// Verify track removed from owner's tracks
		MemberEntity reloadedOwner = entityManager.find(MemberEntity.class, ownerDiscordId);
		assertNotNull(reloadedOwner);
		assertTrue(reloadedOwner.getTracks().isEmpty());

	}

	@Test
	void givenTrackAddedToMember_whenSave_thenBidirectionalRelationshipMaintained() {

		// Prepare test data
		long ownerDiscordId = 3009L;
		Long trackId;

		// Save owner and track
		MemberEntity owner = entityManager.persistAndFlush(validMember(ownerDiscordId, "member-nine"));
		TrackEntity track = validTrack(owner, "Owned Track C", EMPTY_OPUS);
		owner.addTrack(track);
		track = entityManager.persistAndFlush(track);
		trackId = track.getId();
		entityManager.clear();

		// Verify both sides of the relationship
		MemberEntity reloadedOwner = entityManager.find(MemberEntity.class, ownerDiscordId);
		TrackEntity reloadedTrack = entityManager.find(TrackEntity.class, trackId);
		assertNotNull(reloadedOwner);
		assertNotNull(reloadedTrack);
		assertEquals(ownerDiscordId, reloadedTrack.getOwnerId());
		assertTrue(
			reloadedOwner.getTracks()
				.stream()
				.anyMatch(t -> t.getId().equals(trackId))
		);

	}

	@Test
	void givenMemberWithTracks_whenDeleteMember_thenTracksDeleted() {

		// Prepare test data
		long ownerDiscordId = 3010L;

		// Save owner and tracks
		MemberEntity owner = entityManager.persistAndFlush(validMember(ownerDiscordId, "member-ten"));
		TrackEntity first = entityManager.persistAndFlush(validTrack(owner, "Owned Track D1", EMPTY_OPUS));
		TrackEntity second = entityManager.persistAndFlush(validTrack(owner, "Owned Track D2", EMPTY_OPUS));
		entityManager.clear();

		// Delete owner
		MemberEntity reloadedOwner = entityManager.find(MemberEntity.class, ownerDiscordId);
		assertNotNull(reloadedOwner);
		entityManager.remove(reloadedOwner);
		entityManager.flush();
		entityManager.clear();

		// Verify owned tracks are removed
		assertNull(entityManager.find(MemberEntity.class, ownerDiscordId));
		assertNull(entityManager.find(TrackEntity.class, first.getId()));
		assertNull(entityManager.find(TrackEntity.class, second.getId()));

	}

}



