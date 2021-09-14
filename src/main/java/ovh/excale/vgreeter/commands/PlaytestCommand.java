package ovh.excale.vgreeter.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ovh.excale.vgreeter.VGreeterApplication;
import ovh.excale.vgreeter.commands.core.AbstractSlashCommand;
import ovh.excale.vgreeter.models.TrackModel;
import ovh.excale.vgreeter.repositories.TrackRepository;
import ovh.excale.vgreeter.services.DiscordService;
import ovh.excale.vgreeter.utilities.TrackPlayer;

import java.util.Optional;
import java.util.Set;

public class PlaytestCommand extends AbstractSlashCommand {

	private static final Logger logger = LoggerFactory.getLogger(PlaytestCommand.class);

	public PlaytestCommand() {
		super("playtest", "Test a track");

		this.getBuilder()
				.addOptionRequired("trackid", "The track to play", OptionType.INTEGER);

	}

	@Override
	public ReplyAction execute(SlashCommandEvent event) {

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
		VoiceChannel channel = member.getVoiceState()
				.getChannel();

		if(channel == null)
			return event.reply("You must be connected to a Voice Channel to use this command")
					.setEphemeral(true);

		TrackRepository trackRepo = VGreeterApplication
				.getApplicationContext()
				.getBean(TrackRepository.class);

		//noinspection ConstantConditions
		long trackId = Long.parseLong(event.getOption("trackid")
				.getAsString());

		Optional<TrackModel> opt = trackRepo.findById(trackId);

		if(!opt.isPresent())
			return event.reply("A track with that id doesn't exist")
					.setEphemeral(true);

		TrackPlayer trackPlayer = new TrackPlayer(opt.get());
		if(!trackPlayer.canProvide()) {
			logger.error("TrackPlayer cannot provide");
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
		}

		return event.reply("Playing track `#" + trackId + "`")
				.setEphemeral(true);

	}

}
