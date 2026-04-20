package ovh.excale.vgreeter.commands.slash;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.jspecify.annotations.NonNull;
import ovh.excale.vgreeter.commands.core.annotation.CommandController;
import ovh.excale.vgreeter.commands.core.annotation.Option;
import ovh.excale.vgreeter.commands.core.annotation.SlashMapping;
import ovh.excale.vgreeter.entity.LogErrorEntity;
import ovh.excale.vgreeter.entity.TrackEntity;
import ovh.excale.vgreeter.message.ErrorMessages;
import ovh.excale.vgreeter.message.TrackMessages;
import ovh.excale.vgreeter.repository.TrackRepository;
import ovh.excale.vgreeter.services.DiscordService;
import ovh.excale.vgreeter.services.LogErrorService;
import ovh.excale.vgreeter.track.TrackPlayer;

import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static ovh.excale.vgreeter.utilities.DiscordUtil.replyEphemeralWith;

@RequiredArgsConstructor
@Log4j2
@CommandController
public class PlaytestCommand {

	private final TrackRepository trackRepo;

	private final LogErrorService logErrorService;

	private final ErrorMessages msgError;

	private final TrackMessages msgTrack;

	@Transactional
	@SlashMapping(
		name = "playtest",
		description = "Test a track"
	)
	public @NonNull ReplyCallbackAction playtest(
		SlashCommandInteractionEvent event,
		@Option(name = "trackid", description = "The track to play") Long trackId
	) {

		Guild guild = event.getGuild();
		Member member = event.getMember();

		if(guild == null)
			return replyEphemeralWith(msgError.getCmdGuildOnly(), event);

		Set<Long> guildLocks = DiscordService.getGuildVoiceLocks();
		if(guildLocks.contains(guild.getIdLong()))
			// TODO: channel mention
			return replyEphemeralWith(msgError.getBotConnectedToVc("TODO"), event);

		//noinspection ConstantConditions
		AudioChannelUnion channel = member.getVoiceState()
				.getChannel();

		if(channel == null)
			return replyEphemeralWith(msgError.getMemberMustConnectVc(), event);

		Optional<TrackEntity> opt = trackRepo.findById(trackId);
		if(opt.isEmpty())
			// TODO: check guild-sharing and track ownership
			return replyEphemeralWith("A track with that id doesn't exist", event);

		TrackEntity track = opt.get();
		TrackPlayer trackPlayer;
		AudioManager audioManager;
		try {

			audioManager = guild.getAudioManager();
			trackPlayer = new TrackPlayer(track.getOpusPacketReader(), audioManager::closeAudioConnection);

		} catch(Exception e) {

			String message = format("Failed to create TrackPlayer for track #%s", trackId);

			log.error(message, e);
			LogErrorEntity error = logErrorService.error(message, e);

			return replyEphemeralWith(msgError.getInternalErrorUuid(error.getId()), event);
		}

		try {

			audioManager.setSendingHandler(trackPlayer);
			audioManager.openAudioConnection(channel);
			guildLocks.add(guild.getIdLong());

		} catch(InsufficientPermissionException _) {
			return replyEphemeralWith(msgError.getBotNoVcPerms(channel.getAsMention()), event);
		}

		return event.reply(msgTrack.getPlayingIdTitle(track.getId(), track.getTitle()));
	}

}
