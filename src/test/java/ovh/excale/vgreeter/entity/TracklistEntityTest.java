package ovh.excale.vgreeter.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

import java.util.Set;
import java.util.UUID;

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
		"spring.sql.init.schema-locations=classpath:db/h2_source.sql"
	})
@EntityScan(basePackageClasses = TracklistEntity.class)
class TracklistEntityTest {

	@Autowired
	private TestEntityManager entityManager;

	/*=== Positive Tests ===*/

	@Test
	void givenValidTracklist_whenSave_thenSuccess() {

		// Prepare test data
		long memberId = 5001L;
		long guildId = 5002L;
		String name = "Tracklist One";

		// Save prerequisites and tracklist
		entityManager.persistAndFlush(validMember(memberId, "tracklist-owner"));
		entityManager.persistAndFlush(validGuild(guildId, "Tracklist Guild", 0.25f));
		entityManager.persistAndFlush(validTracklist(memberId, guildId, name));
		entityManager.clear();

		// Verify saved tracklist
		TracklistEntity reloaded = entityManager.find(TracklistEntity.class,
			entityManager.getEntityManager()
				.createQuery(
					"SELECT t.id " +
						"FROM Tracklist t " +
						"WHERE t.memberId = :memberId " +
						"AND t.guildId = :guildId " +
						"AND t.name = :name",
					UUID.class
				)
				.setParameter("memberId", memberId)
				.setParameter("guildId", guildId)
				.setParameter("name", name)
				.getSingleResult()
		);
		assertNotNull(reloaded);
		assertEquals(name, reloaded.getName());
		assertEquals(memberId, reloaded.getMemberId());
		assertEquals(guildId, reloaded.getGuildId());
		assertTrue(reloaded.getTracks().isEmpty());

	}

	@Test
	void givenNewTracklist_whenSave_thenUUIDv7Generated() {

		// Prepare test data
		long memberId = 5003L;
		long guildId = 5004L;

		// Save tracklist
		entityManager.persistAndFlush(validMember(memberId, "tracklist-owner-two"));
		entityManager.persistAndFlush(validGuild(guildId, "Tracklist Guild Two", 0.3f));
		TracklistEntity tracklist = entityManager.persistAndFlush(validTracklist(memberId, guildId, "Tracklist Two"));
		entityManager.clear();

		// Verify generated UUIDv7
		TracklistEntity reloaded = entityManager.find(TracklistEntity.class, tracklist.getId());
		assertNotNull(reloaded);
		assertNotNull(reloaded.getId());
		assertEquals(7, reloaded.getId().version());

	}

	@Test
	void givenSavedTracklist_whenUpdateName_thenSuccess() {

		// Prepare test data
		long memberId = 5005L;
		long guildId = 5006L;
		String initialName = "Tracklist Alpha";
		String updatedName = "Tracklist Beta";

		// Save initial tracklist
		entityManager.persistAndFlush(validMember(memberId, "tracklist-owner-three"));
		entityManager.persistAndFlush(validGuild(guildId, "Tracklist Guild Three", 0.35f));
		TracklistEntity tracklist = entityManager.persistAndFlush(validTracklist(memberId, guildId, initialName));

		// Update name
		tracklist.setName(updatedName);
		entityManager.flush();
		entityManager.clear();

		// Verify update
		TracklistEntity reloaded = entityManager.find(TracklistEntity.class, tracklist.getId());
		assertNotNull(reloaded);
		assertEquals(updatedName, reloaded.getName());

	}

	/*=== Negative Tests ===*/

	@Test
	void givenNullName_whenSave_thenFail() {

		// Prepare invalid test data
		TracklistEntity tracklist = TracklistEntity.builder()
			.memberId(5007L)
			.guildId(5008L)
			.build();

		// Verify failure
		assertThrows(RuntimeException.class, () -> entityManager.persistAndFlush(tracklist));

	}

	@Test
	void givenNullMemberId_whenSave_thenFail() {

		// Prepare invalid test data
		TracklistEntity tracklist = TracklistEntity.builder()
			.guildId(5009L)
			.name("Missing Member")
			.build();

		// Verify failure
		assertThrows(RuntimeException.class, () -> entityManager.persistAndFlush(tracklist));

	}

	@Test
	void givenNullGuildId_whenSave_thenFail() {

		// Prepare invalid test data
		TracklistEntity tracklist = TracklistEntity.builder()
			.memberId(5010L)
			.name("Missing Guild")
			.build();

		// Verify failure
		assertThrows(RuntimeException.class, () -> entityManager.persistAndFlush(tracklist));

	}

	@Test
	void givenNonExistentMemberId_whenSave_thenFail() {

		// Prepare invalid test data
		TracklistEntity tracklist = validTracklist(6001L, 6002L, "Broken Member Ref");

		// Verify failure
		assertThrows(RuntimeException.class, () -> entityManager.persistAndFlush(tracklist));

	}

	@Test
	void givenNonExistentGuildId_whenSave_thenFail() {

		// Prepare invalid test data
		TracklistEntity tracklist = validTracklist(6003L, 6004L, "Broken Guild Ref");

		// Verify failure
		assertThrows(RuntimeException.class, () -> entityManager.persistAndFlush(tracklist));

	}

	/*=== Relational and Aggregation Tests ===*/

	@Test
	void givenValidTrack_whenAddToTracklist_thenSuccess() {

		// Prepare test data
		long memberId = 5011L;
		long guildId = 5012L;
		String trackTitle = "Tracklist Track One";
		byte[] opusBytes = new byte[] { 1, 2, 3 };

		// Save prerequisites, track, and tracklist
		MemberEntity owner = entityManager.persistAndFlush(validMember(memberId, "tracklist-owner-four"));
		entityManager.persistAndFlush(validGuild(guildId, "Tracklist Guild Four", 0.4f));
		TrackEntity track = entityManager.persistAndFlush(validTrack(owner, trackTitle, opusBytes));
		TracklistEntity tracklist = entityManager.persistAndFlush(validTracklist(memberId, guildId, "Tracklist Add One"));

		// Add track
		tracklist.getTracks()
			.add(track);
		entityManager.flush();
		entityManager.clear();

		// Verify relation
		TracklistEntity reloaded = entityManager.find(TracklistEntity.class, tracklist.getId());
		assertNotNull(reloaded);
		assertEquals(1, reloaded.getTracks().size());
		assertTrue(
			reloaded.getTracks()
				.stream()
				.anyMatch(t -> t.getId().equals(track.getId()))
		);

	}

	@Test
	void givenTrackInTracklist_whenRemove_thenSuccess() {

		// Prepare test data
		long memberId = 5013L;
		long guildId = 5014L;
		String trackTitle = "Tracklist Track Two";
		byte[] opusBytes = new byte[] { 4, 5, 6 };

		// Save prerequisites, track, and tracklist
		MemberEntity owner = entityManager.persistAndFlush(validMember(memberId, "tracklist-owner-five"));
		entityManager.persistAndFlush(validGuild(guildId, "Tracklist Guild Five", 0.45f));
		TrackEntity track = entityManager.persistAndFlush(validTrack(owner, trackTitle, opusBytes));
		TracklistEntity tracklist = entityManager.persistAndFlush(validTracklist(memberId, guildId, "Tracklist Remove One"));
		tracklist.getTracks()
			.add(track);
		entityManager.flush();
		entityManager.clear();

		// Remove track
		TracklistEntity reloadedTracklist = entityManager.find(TracklistEntity.class, tracklist.getId());
		TrackEntity reloadedTrack = entityManager.find(TrackEntity.class, track.getId());
		assertNotNull(reloadedTracklist);
		assertNotNull(reloadedTrack);
		reloadedTracklist.getTracks()
			.remove(reloadedTrack);
		entityManager.flush();
		entityManager.clear();

		// Verify removal
		TracklistEntity reloaded = entityManager.find(TracklistEntity.class, tracklist.getId());
		assertNotNull(reloaded);
		assertTrue(reloaded.getTracks().isEmpty());

	}

	@Test
	void givenTracksInTracklist_whenSave_thenReflectedInJoinTable() {

		// Prepare test data
		long memberId = 5015L;
		long guildId = 5016L;
		byte[] firstOpusBytes = new byte[] { 7, 8, 9 };
		byte[] secondOpusBytes = new byte[] { 10, 11, 12 };

		// Save prerequisites, tracks, and tracklist
		MemberEntity owner = entityManager.persistAndFlush(validMember(memberId, "tracklist-owner-six"));
		entityManager.persistAndFlush(validGuild(guildId, "Tracklist Guild Six", 0.5f));
		TrackEntity first = entityManager.persistAndFlush(validTrack(owner, "Tracklist Track Three", firstOpusBytes));
		TrackEntity second = entityManager.persistAndFlush(validTrack(owner, "Tracklist Track Four", secondOpusBytes));
		TracklistEntity tracklist = entityManager.persistAndFlush(validTracklist(memberId, guildId, "Tracklist Rows"));

		// Add tracks
		Set<TrackEntity> tracks = tracklist.getTracks();
		tracks.add(first);
		tracks.add(second);
		entityManager.flush();
		entityManager.clear();

		// Verify join table reflects relations
		Number rows = (Number) entityManager.getEntityManager()
			.createNativeQuery(
				"SELECT count(*) " +
					"FROM tracklist_track " +
					"WHERE tracklist_id = ?"
			)
			.setParameter(1, tracklist.getId())
			.getSingleResult();
		TracklistEntity reloaded = entityManager.find(TracklistEntity.class, tracklist.getId());
		assertNotNull(reloaded);
		assertEquals(2, reloaded.getTracks().size());
		assertEquals(2L, rows.longValue());

	}

	@Test
	void givenDuplicateTracks_whenAddToTracklist_thenOnlyOneAdded() {

		// Prepare test data
		long memberId = 5017L;
		long guildId = 5018L;
		byte[] opusBytes = new byte[] { 13, 14, 15 };

		// Save prerequisites, track, and tracklist
		MemberEntity owner = entityManager.persistAndFlush(validMember(memberId, "tracklist-owner-seven"));
		entityManager.persistAndFlush(validGuild(guildId, "Tracklist Guild Seven", 0.55f));
		TrackEntity track = entityManager.persistAndFlush(validTrack(owner, "Tracklist Duplicate", opusBytes));
		TracklistEntity tracklist = entityManager.persistAndFlush(validTracklist(memberId, guildId, "Tracklist Dedup"));

		// Add same track twice
		Set<TrackEntity> tracks = tracklist.getTracks();
		//noinspection OverwrittenKey
		tracks.add(track);
		//noinspection OverwrittenKey
		tracks.add(track);
		entityManager.flush();

		// Verify only one relation exists
		Number rows = (Number) entityManager.getEntityManager()
			.createNativeQuery(
				"SELECT count(*) " +
					"FROM tracklist_track " +
					"WHERE tracklist_id = ?"
			)
			.setParameter(1, tracklist.getId())
			.getSingleResult();
		assertEquals(1, tracklist.getTracks().size());
		assertEquals(1L, rows.longValue());

	}

	@Test
	void givenTracklistWithTracks_whenDeleteTracklist_thenTracksRemain() {

		// Prepare test data
		long memberId = 5019L;
		long guildId = 5020L;
		byte[] opusBytes = new byte[] { 16, 17, 18 };

		// Save prerequisites, track, and tracklist
		MemberEntity owner = entityManager.persistAndFlush(validMember(memberId, "tracklist-owner-eight"));
		entityManager.persistAndFlush(validGuild(guildId, "Tracklist Guild Eight", 0.6f));
		TrackEntity track = entityManager.persistAndFlush(validTrack(owner, "Tracklist Delete", opusBytes));
		TracklistEntity tracklist = entityManager.persistAndFlush(validTracklist(memberId, guildId, "Tracklist Delete Me"));
		tracklist.getTracks()
			.add(track);
		entityManager.flush();
		entityManager.clear();

		// Delete tracklist
		TracklistEntity reloadedTracklist = entityManager.find(TracklistEntity.class, tracklist.getId());
		assertNotNull(reloadedTracklist);
		entityManager.remove(reloadedTracklist);
		entityManager.flush();
		entityManager.clear();

		// Verify track remains and join table entries removed
		assertNotNull(entityManager.find(TrackEntity.class, track.getId()));
		assertNull(entityManager.find(TracklistEntity.class, tracklist.getId()));
		Number rows = (Number) entityManager.getEntityManager()
			.createNativeQuery(
				"SELECT count(*) " +
					"FROM tracklist_track " +
					"WHERE tracklist_id = ?"
			)
			.setParameter(1, tracklist.getId())
			.getSingleResult();
		assertEquals(0L, rows.longValue());

	}

}






