package ovh.excale.vgreeter.commands.slash;

import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.RestAction;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import ovh.excale.vgreeter.commands.core.AbstractSlashCommand;
import ovh.excale.vgreeter.entity.TrackEntity;
import ovh.excale.vgreeter.repository.TrackRepository;
import ovh.excale.vgreeter.services.DiscordService;
import ovh.excale.vgreeter.services.LogErrorService;
import ovh.excale.vgreeter.track.TrackPlayer;

import java.util.Optional;
import java.util.Set;

@Log4j2
@Component
public class PlaytestCommand extends AbstractSlashCommand {

	private final TrackRepository trackRepo;
	private final LogErrorService logErrorService;

	public PlaytestCommand(TrackRepository trackRepo, LogErrorService logErrorService) {
		super("playtest", "Test a track");
		this.trackRepo = trackRepo;
		this.logErrorService = logErrorService;

		this.getBuilder()
				.addOptionRequired("trackid", "The track to play", OptionType.INTEGER);

	}

	@Transactional
	@Override
	public @NonNull RestAction<?> execute(SlashCommandInteractionEvent event) {

		Guild guild = event.getGuild();
		Member member = event.getMember();

		if(guild == null)
			return event.reply("This command can be executed in a guild only")
					.setEphemeral(true);

		Set<Long> guildLocks = DiscordService.getGuildVoiceLocks();
		if(guildLocks.contains(guild.getIdLong()))
			return event.reply("The bot is already connected to a Voice Channel")
					.setEphemeral(true);

		//noinspection ConstantConditions
		AudioChannelUnion channel = member.getVoiceState()
				.getChannel();

		if(channel == null)
			return event.reply("You must be connected to a Voice Channel to use this command")
					.setEphemeral(true);

		//noinspection ConstantConditions
		long trackId = Long.parseLong(event.getOption("trackid")
				.getAsString());

		Optional<TrackEntity> opt = trackRepo.findById(trackId);

		if(opt.isEmpty())
			return event.reply("A track with that id doesn't exist")
					.setEphemeral(true);

		TrackPlayer trackPlayer = new TrackPlayer(opt.get(), logErrorService);
		if(!trackPlayer.canProvide()) {
			log.error("TrackPlayer cannot provide");
			return event.reply("There has been an internal error, retry or contact a developer.")
					.setEphemeral(true);
		}

		AudioManager audioManager = guild.getAudioManager();
		trackPlayer.setTrackEndAction(audioManager::closeAudioConnection);

		try {
			audioManager.setSendingHandler(trackPlayer);
			audioManager.openAudioConnection(channel);
			guildLocks.add(guild.getIdLong());
		} catch(InsufficientPermissionException ignored) {
			return event.reply("The bot is missing permission to connect or speak in that voice channel")
					.setEphemeral(true);
		}

		return event.reply("Playing track `#" + trackId + "`")
				.setEphemeral(true);

	}

}
