package ovh.excale.vgreeter.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ovh.excale.vgreeter.entity.GuildEntity;
import ovh.excale.vgreeter.entity.TrackEntity;
import ovh.excale.vgreeter.repository.GuildRepository;
import ovh.excale.vgreeter.track.TrackPlayer;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import static java.lang.String.format;

@RequiredArgsConstructor
@Log4j2
@Service
public class VoiceGreeterHandler extends ListenerAdapter {

	private final GuildRepository guildRepo;

	private final TrackService trackService;

	private final LogErrorService logErrorService;

	private final Random random = new Random();

	/**
	 * Handle voice state updates to greet users when they join a voice channel.
	 *
	 * @param event the voice state update event
	 */
	@Transactional
	@Override
	public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {

		Guild guild = event.getGuild();
		User user = event.getMember().getUser();
		Set<Long> guildLocks = DiscordService.getGuildVoiceLocks();

		// If the bot left a channel, remove the lock for that guild
		if(event.getChannelLeft() != null) {
			SelfUser selfUser = event.getJDA().getSelfUser();
			if(user.getIdLong() == selfUser.getIdLong())
				guildLocks.remove(guild.getIdLong());
		}

		// Continue only if a user joined a channel
		if(event.getChannelJoined() == null)
			return;

		// Ignore bots and locked guilds
		if(user.isBot() || guildLocks.contains(guild.getIdLong()))
			return;

		// Fetch guild settings
		Optional<GuildEntity> guildEntityOpt = guildRepo.findById(guild.getIdLong());
		GuildEntity guildEntity;
		if(guildEntityOpt.isEmpty()) {
			// Create new guild settings if not found
			guildEntity = GuildEntity.builder()
				.discordId(guild.getIdLong())
				.name(guild.getName())
				.build();
			guildRepo.save(guildEntity);
		} else {
			guildEntity = guildEntityOpt.get();
		}

		// Apply probability of greeting the user
		float greetProbab = guildEntity.getGreetProbab();
		if(random.nextFloat() > greetProbab)
			return;

		// Get a random track
		TrackEntity track = trackService.randomTrack();
		if(track == null)
			// No tracks available, fail silently
			return;

		AudioChannelUnion channel = event.getChannelJoined();
		AudioManager audioManager = guild.getAudioManager();
		TrackPlayer trackPlayer;

		// Create a TrackPlayer to play the track,
		// with a callback to close the audio connection when the track finishes
		try {

			trackPlayer = new TrackPlayer(track.getOpusPacketReader(), audioManager::closeAudioConnection);

		} catch(SQLException | IOException e) {

			String exceptionMsg = format(
				"Failed to create TrackPlayer for track #%s in guild `%s` (%s)",
				track.getId(),
				guild.getName(),
				guild.getId()
			);

			log.error(exceptionMsg, e);
			logErrorService.error(exceptionMsg, e, user.getIdLong(), guild.getIdLong());

			return;

		}

		// Connect to voice channel and play the track
		try {

			audioManager.setSendingHandler(trackPlayer);
			audioManager.openAudioConnection(channel);
			guildLocks.add(guild.getIdLong());

		} catch(InsufficientPermissionException _) {
			// fail silently
		}

	}


}
