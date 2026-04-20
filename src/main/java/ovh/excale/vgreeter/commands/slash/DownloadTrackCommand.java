package ovh.excale.vgreeter.commands.slash;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jspecify.annotations.NonNull;
import ovh.excale.vgreeter.commands.core.annotation.CommandController;
import ovh.excale.vgreeter.commands.core.annotation.Option;
import ovh.excale.vgreeter.commands.core.annotation.SlashMapping;
import ovh.excale.vgreeter.entity.TrackEntity;
import ovh.excale.vgreeter.message.ErrorMessages;
import ovh.excale.vgreeter.message.TrackMessages;
import ovh.excale.vgreeter.repository.TrackRepository;

import java.util.Optional;

import static ovh.excale.vgreeter.utilities.DiscordUtil.replyEphemeralWith;

@RequiredArgsConstructor
@CommandController
public class DownloadTrackCommand {

	private final TrackRepository trackRepo;

	private final ErrorMessages msgError;

	private final TrackMessages msgTrack;

	// TODO: 30sec cooldown (whole-guild scope) on download, probably with stopwatch and queue

	@Transactional
	@SlashMapping(
		name = "download",
		description = "Download a track given its id"
	)
	public @NonNull RestAction<?> downloadTrack(
		SlashCommandInteractionEvent event,
		@Option(name = "trackid", description = "The id of the track to download") Long trackId
	) {

		Guild guild = event.getGuild();

		// Check if the command is used in a guild
		if(guild == null)
			return replyEphemeralWith(msgError.getCmdGuildOnly(), event);

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
