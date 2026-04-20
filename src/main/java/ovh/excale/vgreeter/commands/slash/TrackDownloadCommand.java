package ovh.excale.vgreeter.commands.slash;

import jakarta.transaction.Transactional;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import ovh.excale.vgreeter.commands.core.AbstractSlashCommand;
import ovh.excale.vgreeter.entity.TrackEntity;
import ovh.excale.vgreeter.message.ErrorMessages;
import ovh.excale.vgreeter.message.TrackMessages;
import ovh.excale.vgreeter.repository.TrackRepository;

import java.util.Objects;
import java.util.Optional;

import static ovh.excale.vgreeter.utilities.DiscordUtil.replyEphemeralWith;

@Component
public class TrackDownloadCommand extends AbstractSlashCommand {

	private final TrackRepository trackRepo;

	private final ErrorMessages msgError;

	private final TrackMessages msgTrack;

	public TrackDownloadCommand(
		TrackRepository trackRepo,
		ErrorMessages msgError,
		TrackMessages msgTrack
	) {
		super("trackdownload", "download command placeholder");

		this.trackRepo = trackRepo;
		this.msgError = msgError;
		this.msgTrack = msgTrack;

		this.getBuilder()
			.addOptionRequired("trackid", "The track to download", OptionType.INTEGER);

	}

	// TODO: 30sec cooldown (whole-guild scope) on download, probably with stopwatch and queue

	@Transactional
	@Override
	public @NonNull RestAction<?> execute(SlashCommandInteractionEvent event) {

		Guild guild = event.getGuild();

		// Check if the command is used in a guild
		if(guild == null)
			return replyEphemeralWith(msgError.getCmdGuildOnly(), event);

		long trackId = Long.parseLong(
			Objects.requireNonNull(event.getOption("trackid"))
			.getAsString()
		);

		// Get the track with the provided id
		Optional<TrackEntity> trackOpt = trackRepo.findById(trackId);
		if(trackOpt.isEmpty())
			return replyEphemeralWith(msgTrack.getErrorNoSuchId(trackId), event);

		TrackEntity track = trackOpt.get();
		String msg = String.format("Track `#%d: %s`", track.getId(), track.getTitle());
		String filename = track.getTitle() + ".opus";

		return event.reply(msg)
			.addFiles(FileUpload.fromData(track.getOpusBytes(), filename));
	}

}
