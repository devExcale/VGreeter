package ovh.excale.vgreeter.entity;

import lombok.SneakyThrows;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

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
@EntityScan(basePackageClasses = TrackEntity.class)
class TrackEntityTest {

	private static final long DEFAULT_TRACK_MAX_SIZE = 64 * 1024L;
	@Autowired
	private TestEntityManager entityManager;

	private static MemberEntity validMember(Long discordId, String discordUsername) {
		return MemberEntity.builder()
			.discordId(discordId)
			.discordUsername(discordUsername)
			.trackMaxSize(DEFAULT_TRACK_MAX_SIZE)
			.build();
	}

	private static TrackEntity validTrack(MemberEntity owner, String title, byte[] opusBytes) {
		return TrackEntity.builder()
			.owner(owner)
			.title(title)
			.opusBytes(opusBytes)
			.build();
	}

	private static GuildEntity validGuild(Long discordId, String name, float greetProbab) {
		return GuildEntity.builder()
			.discordId(discordId)
			.name(name)
			.greetProbab(greetProbab)
			.build();
	}

	private static TracklistEntity validTracklist(Long memberId, Long guildId, String name) {
		return TracklistEntity.builder()
			.memberId(memberId)
			.guildId(guildId)
			.name(name)
			.build();
	}

	/*=== Positive Tests ===*/

	@SneakyThrows
	@Test
	void givenValidTrack_whenSave_thenSuccess() {

		// Prepare test data
		long ownerDiscordId = 4001L;
		String ownerUsername = "owner-one";
		String title = "Track One";
		byte[] opusBytes = new byte[] { 1, 2, 3 };

		// Save track
		MemberEntity owner = entityManager.persistAndFlush(validMember(ownerDiscordId, ownerUsername));
		TrackEntity savedTrack = entityManager.persistAndFlush(validTrack(owner, title, opusBytes));
		entityManager.clear();

		// Verify saved track
		TrackEntity reloaded = entityManager.find(TrackEntity.class, savedTrack.getId());
		assertNotNull(reloaded);
		assertEquals(savedTrack.getId(), reloaded.getId());
		assertEquals(title, reloaded.getTitle());
		assertEquals(ownerDiscordId, reloaded.getOwnerId());
		assertEquals(opusBytes.length, reloaded.getOpusSize());
		assertArrayEquals(opusBytes, reloaded.getOpusBytes());
		assertNotNull(reloaded.getCreatedAt());
	}

	@Test
	void givenNewTrack_whenSave_thenIdGeneratedSequentially() {

		// Prepare test data
		long ownerDiscordId = 4002L;
		String ownerUsername = "owner-two";

		// Save sequential tracks
		MemberEntity owner = entityManager.persistAndFlush(validMember(ownerDiscordId, ownerUsername));
		TrackEntity first = entityManager.persistAndFlush(validTrack(owner, "Track Two A", new byte[] { 4, 5, 6 }));
		TrackEntity second = entityManager.persistAndFlush(validTrack(owner, "Track Two B", new byte[] { 7, 8, 9 }));

		// Verify sequential ids
		assertEquals(first.getId() + 1, second.getId());
	}

	@Test
	void givenNewTrack_whenSave_thenCreatedAtGenerated() {

		// Prepare test data
		long ownerDiscordId = 4003L;
		String ownerUsername = "owner-three";
		String title = "Track Three";

		// Save track
		MemberEntity owner = entityManager.persistAndFlush(validMember(ownerDiscordId, ownerUsername));
		TrackEntity track = entityManager.persistAndFlush(validTrack(owner, title, new byte[] { 10, 11, 12 }));
		entityManager.clear();

		// Verify generated created at
		TrackEntity reloaded = entityManager.find(TrackEntity.class, track.getId());
		assertNotNull(reloaded);
		assertNotNull(reloaded.getCreatedAt());
	}

	@Test
	void givenTrackWithOpusBytes_whenSave_thenOpusSizeGenerated() {

		// Prepare test data
		long ownerDiscordId = 4004L;
		String ownerUsername = "owner-four";
		String title = "Track Four";
		byte[] opusBytes = new byte[] { 13, 14, 15, 16 };

		// Save track
		MemberEntity owner = entityManager.persistAndFlush(validMember(ownerDiscordId, ownerUsername));
		TrackEntity track = entityManager.persistAndFlush(validTrack(owner, title, opusBytes));
		entityManager.clear();

		// Verify generated opus size
		TrackEntity reloaded = entityManager.find(TrackEntity.class, track.getId());
		assertNotNull(reloaded);
		assertEquals(opusBytes.length, reloaded.getOpusSize());
	}

	@Test
	void givenSavedTrack_whenUpdateTitle_thenSuccess() {

		// Prepare test data
		long ownerDiscordId = 4005L;
		String ownerUsername = "owner-five";
		String initialTitle = "Track Five";
		String updatedTitle = "Track Five Updated";

		// Save initial track
		MemberEntity owner = entityManager.persistAndFlush(validMember(ownerDiscordId, ownerUsername));
		TrackEntity track = entityManager.persistAndFlush(validTrack(owner, initialTitle, new byte[] { 17, 18, 19 }));

		// Update title
		track.setTitle(updatedTitle);
		entityManager.flush();
		entityManager.clear();

		// Verify update
		TrackEntity reloaded = entityManager.find(TrackEntity.class, track.getId());
		assertNotNull(reloaded);
		assertEquals(updatedTitle, reloaded.getTitle());
	}

	/*=== Negative Tests ===*/

	@Test
	void givenNullTitle_whenSave_thenFail() {

		// Prepare invalid test data
		MemberEntity owner = validMember(4006L, "owner-six");
		TrackEntity track = validTrack(owner, null, new byte[] { 20, 21, 22 });

		// Verify failure
		assertThrows(RuntimeException.class, () -> entityManager.persistAndFlush(track));
	}

	@Test
	void givenNullOwner_whenSave_thenFail() {

		// Prepare invalid test data
		TrackEntity track = TrackEntity.builder()
			.title("No Owner Track")
			.opusBytes(new byte[] { 23, 24, 25 })
			.build();

		// Verify failure
		assertThrows(RuntimeException.class, () -> entityManager.persistAndFlush(track));
	}

	@Test
	void givenNullOpusBytes_whenSave_thenFail() {

		// Prepare invalid test data
		MemberEntity owner = validMember(4007L, "owner-seven");
		TrackEntity track = TrackEntity.builder()
			.title("No Bytes Track")
			.owner(owner)
			.build();

		// Verify failure
		assertThrows(RuntimeException.class, () -> entityManager.persistAndFlush(track));
	}

	@Test
	void givenSavedTrack_whenUpdateOwnerId_thenIgnored() {

		// Prepare test data
		long originalOwnerId = 4008L;
		long otherOwnerId = 4009L;

		// Save members and track
		MemberEntity originalOwner = entityManager.persistAndFlush(validMember(originalOwnerId, "owner-eight"));
		MemberEntity otherOwner = entityManager.persistAndFlush(validMember(otherOwnerId, "owner-nine"));
		TrackEntity track = entityManager.persistAndFlush(validTrack(originalOwner, "Owner Id Track", new byte[] { 26, 27, 28 }));

		// Attempt to change owner id
		track.setOwnerId(otherOwner.getDiscordId());
		entityManager.flush();
		entityManager.clear();

		// Verify owner id remains unchanged
		TrackEntity reloaded = entityManager.find(TrackEntity.class, track.getId());
		assertNotNull(reloaded);
		assertEquals(originalOwnerId, reloaded.getOwnerId());
	}

	@Test
	void givenSavedTrack_whenUpdateCreatedAt_thenIgnored() {

		// Prepare test data
		long ownerDiscordId = 4010L;

		// Save initial track
		MemberEntity owner = entityManager.persistAndFlush(validMember(ownerDiscordId, "owner-ten"));
		TrackEntity track = entityManager.persistAndFlush(validTrack(owner, "Created At Track", new byte[] { 29, 30, 31 }));
		Timestamp originalCreatedAt = track.getCreatedAt();

		// Attempt to change created at
		track.setCreatedAt(new Timestamp(originalCreatedAt.getTime() + 60_000L));
		entityManager.flush();
		entityManager.clear();

		// Verify created at remains unchanged
		TrackEntity reloaded = entityManager.find(TrackEntity.class, track.getId());
		assertNotNull(reloaded);
		assertEquals(originalCreatedAt, reloaded.getCreatedAt());
	}

	@Test
	void givenSavedTrack_whenUpdateOpusSize_thenIgnored() {

		// Prepare test data
		long ownerDiscordId = 4011L;

		// Save initial track
		MemberEntity owner = entityManager.persistAndFlush(validMember(ownerDiscordId, "owner-eleven"));
		TrackEntity track = entityManager.persistAndFlush(validTrack(owner, "Opus Size Track", new byte[] { 32, 33, 34 }));
		Long originalOpusSize = track.getOpusSize();

		// Attempt to change opus size
		track.setOpusSize(originalOpusSize + 100L);
		entityManager.flush();
		entityManager.clear();

		// Verify opus size remains unchanged
		TrackEntity reloaded = entityManager.find(TrackEntity.class, track.getId());
		assertNotNull(reloaded);
		assertEquals(originalOpusSize, reloaded.getOpusSize());
	}

	/*=== Relational and Aggregation Tests ===*/

	@Test
	void givenValidMember_whenSetOwner_thenAssociatedCorrectly() {

		// Prepare test data
		long ownerDiscordId = 4012L;
		String ownerUsername = "owner-twelve";
		String title = "Associated Track";

		// Save track with owner
		MemberEntity owner = entityManager.persistAndFlush(validMember(ownerDiscordId, ownerUsername));
		TrackEntity track = entityManager.persistAndFlush(validTrack(owner, title, new byte[] { 35, 36, 37 }));
		entityManager.clear();

		// Verify association
		MemberEntity reloadedOwner = entityManager.find(MemberEntity.class, ownerDiscordId);
		assertNotNull(reloadedOwner);
		assertEquals(1, reloadedOwner.getTracks().size());
		assertTrue(reloadedOwner.getTracks().stream().anyMatch(t -> t.getId().equals(track.getId())));
	}

	@Test
	void givenSavedTrack_whenFetchOwner_thenFetchedLazily() {

		// Prepare test data
		long ownerDiscordId = 4013L;
		String ownerUsername = "owner-thirteen";

		// Save track
		MemberEntity owner = entityManager.persistAndFlush(validMember(ownerDiscordId, ownerUsername));
		TrackEntity track = entityManager.persistAndFlush(validTrack(owner, "Lazy Owner Track", new byte[] { 38, 39, 40 }));
		entityManager.clear();

		// Verify owner is fetched lazily
		TrackEntity reloaded = entityManager.find(TrackEntity.class, track.getId());
		assertNotNull(reloaded);
		Object ownerProxy = reloaded.getOwner();
		assertFalse(Hibernate.isInitialized(ownerProxy));
		assertEquals(ownerDiscordId, reloaded.getOwner().getDiscordId());
	}

	@Test
	void givenTrackWithOwner_whenDeleteTrack_thenOwnerRemains() {

		// Prepare test data
		long ownerDiscordId = 4015L;
		String ownerUsername = "owner-fifteen";

		// Save owner and track
		MemberEntity owner = entityManager.persistAndFlush(validMember(ownerDiscordId, ownerUsername));
		TrackEntity track = entityManager.persistAndFlush(validTrack(owner, "Owner Survives Track", new byte[] { 45, 46, 47 }));
		entityManager.clear();

		// Delete track and verify owner remains
		TrackEntity reloaded = entityManager.find(TrackEntity.class, track.getId());
		assertNotNull(reloaded);
		entityManager.remove(reloaded);
		entityManager.flush();
		entityManager.clear();

		assertNotNull(entityManager.find(MemberEntity.class, ownerDiscordId));
		assertNull(entityManager.find(TrackEntity.class, track.getId()));
	}

	@Test
	void givenTrackInTracklist_whenDeleteTrack_thenRemovedFromTracklist() {

		// Prepare test data
		long ownerDiscordId = 4016L;
		long guildDiscordId = 4017L;
		String ownerUsername = "owner-sixteen";
		String guildName = "Tracklist Guild";
		String tracklistName = "Tracklist One";

		// Save prerequisites, track, and tracklist
		MemberEntity owner = entityManager.persistAndFlush(validMember(ownerDiscordId, ownerUsername));
		entityManager.persistAndFlush(validGuild(guildDiscordId, guildName, 0.5f));
		TrackEntity track = entityManager.persistAndFlush(validTrack(owner, "Tracklist Track", new byte[] { 48, 49, 50 }));
		TracklistEntity tracklist = entityManager.persistAndFlush(validTracklist(ownerDiscordId, guildDiscordId, tracklistName));
		tracklist.getTracks().add(track);
		entityManager.flush();
		entityManager.clear();

		// Delete track and verify tracklist no longer references it
		TrackEntity reloadedTrack = entityManager.find(TrackEntity.class, track.getId());
		assertNotNull(reloadedTrack);
		entityManager.remove(reloadedTrack);
		entityManager.flush();
		entityManager.clear();

		TracklistEntity reloadedTracklist = entityManager.find(TracklistEntity.class, tracklist.getId());
		assertNotNull(reloadedTracklist);
		assertTrue(reloadedTracklist.getTracks().isEmpty());
		assertEquals(0L, ((Number) entityManager.getEntityManager()
			.createNativeQuery("select count(*) from tracklist_track where track_id = ?")
			.setParameter(1, track.getId())
			.getSingleResult()).longValue());
	}

	@Test
	void givenTrackInGuild_whenDeleteTrack_thenRemovedFromGuild() {

		// Prepare test data
		long ownerDiscordId = 4018L;
		long guildDiscordId = 4019L;
		String ownerUsername = "owner-eighteen";
		String guildName = "Track Guild";

		// Save prerequisites, track, and guild
		MemberEntity owner = entityManager.persistAndFlush(validMember(ownerDiscordId, ownerUsername));
		GuildEntity guild = entityManager.persistAndFlush(validGuild(guildDiscordId, guildName, 0.6f));
		TrackEntity track = entityManager.persistAndFlush(validTrack(owner, "Guild Track", new byte[] { 51, 52, 53 }));
		guild.getSharedTracks().add(track);
		entityManager.flush();
		entityManager.clear();

		// Delete track and verify guild no longer references it
		TrackEntity reloadedTrack = entityManager.find(TrackEntity.class, track.getId());
		assertNotNull(reloadedTrack);
		entityManager.remove(reloadedTrack);
		entityManager.flush();
		entityManager.clear();

		GuildEntity reloadedGuild = entityManager.find(GuildEntity.class, guildDiscordId);
		assertNotNull(reloadedGuild);
		assertTrue(reloadedGuild.getSharedTracks().isEmpty());
		assertEquals(0L, ((Number) entityManager.getEntityManager()
			.createNativeQuery("select count(*) from guild_track where track_id = ?")
			.setParameter(1, track.getId())
			.getSingleResult()).longValue());
	}

}
