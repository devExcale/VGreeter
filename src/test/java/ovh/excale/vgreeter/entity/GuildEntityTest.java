package ovh.excale.vgreeter.entity;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

import java.util.Set;

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
		"spring.sql.init.schema-locations=classpath:db/h2_source.sql"
	})
@EntityScan(basePackageClasses = GuildEntity.class)
class GuildEntityTest {

	private static final float FLOAT_COMPARE_DELTA = 0.0001f;

	@Autowired
	private TestEntityManager entityManager;

	/*=== Positive Tests ===*/

	@Test
	void givenValidGuild_whenSave_thenSuccess() {

		// Prepare test data
		long discordId = 1001L;
		String name = "Test Guild";
		float greetProbab = 0.5f;

		// Save new guild
		entityManager.persistAndFlush(
			GuildEntity.builder()
				.discordId(discordId)
				.name(name)
				.greetProbab(greetProbab)
				.build()
		);
		entityManager.clear();

		// Verify saved guild
		GuildEntity reloaded = entityManager.find(GuildEntity.class, discordId);
		assertNotNull(reloaded);
		assertEquals(discordId, reloaded.getDiscordId());
		assertEquals(name, reloaded.getName());
		assertEquals(greetProbab, reloaded.getGreetProbab(), FLOAT_COMPARE_DELTA);
		assertTrue(reloaded.getSharedTracks().isEmpty());

	}

	@Test
	void givenSavedGuild_whenUpdateName_thenSuccess() {

		// Prepare test data
		long discordId = 1002L;
		String initialName = "Guild Alpha";
		String updatedName = "Guild Beta";

		// Save initial guild
		GuildEntity guild = entityManager.persistAndFlush(
			GuildEntity.builder()
				.discordId(discordId)
				.name(initialName)
				.greetProbab(0.25f)
				.build()
		);

		// Update name
		guild.setName(updatedName);
		entityManager.flush();
		entityManager.clear();

		// Verify update
		GuildEntity reloaded = entityManager.find(GuildEntity.class, discordId);
		assertNotNull(reloaded);
		assertEquals(updatedName, reloaded.getName());

	}

	@Test
	void givenSavedGuild_whenUpdateGreetProbab_thenSuccess() {

		// Prepare test data
		long discordId = 1003L;
		float initialGreetProbab = 0.10f;
		float updatedGreetProbab = 0.85f;

		// Save initial guild
		GuildEntity guild = entityManager.persistAndFlush(
			GuildEntity.builder()
				.discordId(discordId)
				.name("Guild Gamma")
				.greetProbab(initialGreetProbab)
				.build()
		);

		// Update greetProbab
		guild.setGreetProbab(updatedGreetProbab);
		entityManager.flush();
		entityManager.clear();

		// Verify update
		GuildEntity reloaded = entityManager.find(GuildEntity.class, discordId);
		assertNotNull(reloaded);
		assertEquals(updatedGreetProbab, reloaded.getGreetProbab(), FLOAT_COMPARE_DELTA);

	}

	@Test
	void givenGreetProbabAtBoundaries_whenSave_thenSuccess() {

		// Prepare test data
		float lowerProbabBoundary = 0f;
		float upperProbabBoundary = 1f;

		// Save initial guilds
		GuildEntity lower = entityManager.persistAndFlush(
			GuildEntity.builder()
				.discordId(1004L)
				.name("Lower")
				.greetProbab(lowerProbabBoundary)
				.build()
		);
		GuildEntity upper = entityManager.persistAndFlush(
			GuildEntity.builder()
				.discordId(1005L)
				.name("Upper")
				.greetProbab(upperProbabBoundary)
				.build()
		);

		// Verify saved guilds
		assertEquals(lowerProbabBoundary, lower.getGreetProbab(), FLOAT_COMPARE_DELTA);
		assertEquals(upperProbabBoundary, upper.getGreetProbab(), FLOAT_COMPARE_DELTA);

	}

	@Test
	void givenMissingGreetProbab_whenSave_thenDefaultApplied() {

		// Prepare test data
		long discordId = 1006L;

		// Insert raw row without greet_probab
		EntityManager em = entityManager.getEntityManager();
		em.createNativeQuery("insert into guild (discord_id, name) values (?, ?)")
			.setParameter(1, discordId)
			.setParameter(2, "Default Guild")
			.executeUpdate();
		em.flush();
		em.clear();

		// Verify default greet probability
		GuildEntity reloaded = entityManager.find(GuildEntity.class, discordId);
		assertNotNull(reloaded);
		assertEquals(GuildEntity.DEFAULT_GREET_PROBAB, reloaded.getGreetProbab(), FLOAT_COMPARE_DELTA);

	}

	/*=== Negative Tests ===*/

	@Test
	void givenNullDiscordId_whenSave_thenFail() {

		// Prepare invalid test data
		GuildEntity guild = GuildEntity.builder()
			.name("Broken Guild")
			.greetProbab(0.2f)
			.build();

		// Verify failure on null discordId
		assertThrows(RuntimeException.class, () -> entityManager.persistAndFlush(guild));

	}

	@Test
	void givenNullName_whenSave_thenFail() {

		// Prepare invalid test data
		GuildEntity guild = GuildEntity.builder()
			.discordId(1007L)
			.greetProbab(0.2f)
			.build();

		// Verify failure on null name
		assertThrows(RuntimeException.class, () -> entityManager.persistAndFlush(guild));

	}

	@Test
	void givenGreetProbabExceedingOne_whenSave_thenFail() {

		// Prepare invalid test data
		GuildEntity guild = GuildEntity.builder()
			.discordId(1008L)
			.name("Too High")
			.greetProbab(1.1f)
			.build();

		// Verify failure on greetProbab > 1
		assertThrows(RuntimeException.class, () -> entityManager.persistAndFlush(guild));

	}

	@Test
	void givenNegativeGreetProbab_whenSave_thenFail() {

		// Prepare invalid test data
		GuildEntity guild = GuildEntity.builder()
			.discordId(1009L)
			.name("Too Low")
			.greetProbab(-0.01f)
			.build();

		// Verify failure
		assertThrows(RuntimeException.class, () -> entityManager.persistAndFlush(guild));

	}

	/*=== Relational and Aggregation Tests ===*/

	@Test
	void givenValidTrack_whenAddToSharedTracks_thenSuccess() {

		// Prepare test data
		long guildId = 2002L;

		// Save owner, track, and guild
		MemberEntity owner = entityManager.persistAndFlush(
			MemberEntity.builder()
				.discordId(2001L)
				.discordUsername("owner")
				.trackMaxSize(64 * 1024L)
				.build()
		);
		TrackEntity track = entityManager.persistAndFlush(
			TrackEntity.builder()
				.title("Track 1")
				.owner(owner)
				.opusBytes(new byte[] { 1, 2, 3 })
				.build()
		);
		GuildEntity guild = entityManager.persistAndFlush(
			GuildEntity.builder()
				.discordId(guildId)
				.name("Shared Guild")
				.greetProbab(0.2f)
				.build()
		);

		// Add shared track
		guild.getSharedTracks()
			.add(track);
		entityManager.flush();
		entityManager.clear();

		// Verify track is in shared collection
		GuildEntity reloaded = entityManager.find(GuildEntity.class, guildId);
		assertNotNull(reloaded);
		assertEquals(1, reloaded.getSharedTracks().size());
		assertTrue(
			reloaded.getSharedTracks()
				.stream()
				.anyMatch(shared -> shared.getId().equals(track.getId()))
		);

	}

	@Test
	void givenTrackInSharedTracks_whenRemove_thenSuccess() {

		// Prepare test data
		long guildDiscordId = 2004L;

		// Save owner, track, and guild
		MemberEntity owner = entityManager.persistAndFlush(
			MemberEntity.builder()
				.discordId(2003L)
				.discordUsername("owner-2")
				.build()
		);
		TrackEntity track = entityManager.persistAndFlush(
			TrackEntity.builder()
				.title("Track 2")
				.owner(owner)
				.opusBytes(new byte[] { 4, 5, 6 })
				.build()
		);
		GuildEntity guild = entityManager.persistAndFlush(
			GuildEntity.builder()
				.discordId(guildDiscordId)
				.name("Removable Guild")
				.greetProbab(0.3f)
				.build()
		);

		// Add track to shared collection
		guild.getSharedTracks()
			.add(track);
		entityManager.flush();

		// Remove track from shared collection
		guild.getSharedTracks()
			.remove(track);
		entityManager.flush();
		entityManager.clear();

		// Verify track is removed from shared collection
		GuildEntity reloaded = entityManager.find(GuildEntity.class, guildDiscordId);
		assertNotNull(reloaded);
		assertTrue(reloaded.getSharedTracks().isEmpty());

	}

	@Test
	void givenSharedTracks_whenSave_thenReflectedInJoinTable() {

		// Prepare test data
		long guildDiscordId = 2006L;

		// Save owner, tracks, and guild
		MemberEntity owner = entityManager.persistAndFlush(
			MemberEntity.builder()
				.discordId(2005L)
				.discordUsername("owner-3")
				.build()
		);
		TrackEntity first = entityManager.persistAndFlush(
			TrackEntity.builder()
				.title("Track 3")
				.owner(owner)
				.opusBytes(new byte[] { 7, 8, 9 })
				.build()
		);
		TrackEntity second = entityManager.persistAndFlush(
			TrackEntity.builder()
				.title("Track 4")
				.owner(owner)
				.opusBytes(new byte[] { 10, 11, 12 })
				.build()
		);
		GuildEntity guild = entityManager.persistAndFlush(
			GuildEntity.builder()
				.discordId(guildDiscordId)
				.name("Join Table Guild")
				.greetProbab(0.4f)
				.build()
		);

		// Add tracks to shared collection
		Set<TrackEntity> sharedTracks = guild.getSharedTracks();
		sharedTracks.add(first);
		sharedTracks.add(second);
		entityManager.flush();

		// Verify join table entries
		Number rows = (Number) entityManager.getEntityManager()
			.createNativeQuery("select count(*) from guild_track where guild_id = ?")
			.setParameter(1, guildDiscordId)
			.getSingleResult();

		assertEquals(2L, rows.longValue());

	}

	@Test
	void givenDuplicateTracks_whenAddToSharedTracks_thenOnlyOneAdded() {

		// Prepare test data
		long guildDiscordId = 2008L;

		// Save owner, track, and guild
		MemberEntity owner = entityManager.persistAndFlush(
			MemberEntity.builder()
			.discordId(2007L)
			.discordUsername("owner-4")
			.build()
		);
		TrackEntity track = entityManager.persistAndFlush(
			TrackEntity.builder()
			.title("Track 5")
			.owner(owner)
			.opusBytes(new byte[] { 13, 14, 15 })
			.build()
		);
		GuildEntity guild = entityManager.persistAndFlush(
			GuildEntity.builder()
			.discordId(guildDiscordId)
			.name("Dedup Guild")
			.greetProbab(0.5f)
			.build()
		);

		// Add the same track twice
		Set<TrackEntity> sharedTracks = guild.getSharedTracks();
		//noinspection OverwrittenKey
		sharedTracks.add(track);
		//noinspection OverwrittenKey
		sharedTracks.add(track);
		entityManager.flush();

		// Verify deduplication in shared collection and join table
		Number rows = (Number) entityManager.getEntityManager()
			.createNativeQuery("select count(*) from guild_track where guild_id = ?")
			.setParameter(1, guildDiscordId)
			.getSingleResult();

		assertEquals(1, guild.getSharedTracks().size());
		assertEquals(1L, rows.longValue());

	}

	@Test
	void givenGuildWithSharedTracks_whenDeleteGuild_thenTracksRemain() {

		// Prepare test data
		long guildDiscordId = 2010L;

		// Save owner, track, and guild
		MemberEntity owner = entityManager.persistAndFlush(
			MemberEntity.builder()
				.discordId(2009L)
				.discordUsername("owner-5")
				.build()
		);
		TrackEntity track = entityManager.persistAndFlush(
			TrackEntity.builder()
				.title("Track 6")
				.owner(owner)
				.opusBytes(new byte[] { 16, 17, 18 })
				.build()
		);
		GuildEntity guild = entityManager.persistAndFlush(
			GuildEntity.builder()
				.discordId(guildDiscordId)
				.name("Deletable Guild")
				.greetProbab(0.6f)
				.build()
		);

		// Add track to shared collection
		guild.getSharedTracks()
			.add(track);
		entityManager.flush();

		// Delete guild
		GuildEntity reloadedGuild = entityManager.find(GuildEntity.class, guildDiscordId);
		assertNotNull(reloadedGuild);
		entityManager.remove(reloadedGuild);
		entityManager.flush();
		entityManager.clear();

		// Verify track still exists and join table entries are removed
		Number rows = (Number) entityManager.getEntityManager()
			.createNativeQuery("select count(*) from guild_track where track_id = ?")
			.setParameter(1, track.getId())
			.getSingleResult();
		assertNotNull(entityManager.find(TrackEntity.class, track.getId()));
		assertEquals(0L, rows.longValue());

	}

}
