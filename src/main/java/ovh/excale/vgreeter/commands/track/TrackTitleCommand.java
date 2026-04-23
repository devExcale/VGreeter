package ovh.excale.vgreeter.commands.track;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.jspecify.annotations.NonNull;
import ovh.excale.vgreeter.commands.core.annotation.CmdOption;
import ovh.excale.vgreeter.commands.core.annotation.CommandController;
import ovh.excale.vgreeter.commands.core.annotation.SlashMapping;
import ovh.excale.vgreeter.entity.MemberEntity;
import ovh.excale.vgreeter.entity.TrackEntity;
import ovh.excale.vgreeter.message.TrackMessages;
import ovh.excale.vgreeter.repository.TrackRepository;

import java.util.Optional;
import java.util.regex.Pattern;

import static ovh.excale.vgreeter.utilities.DiscordUtil.replyEphemeralWith;

@RequiredArgsConstructor
@CommandController
public class TrackTitleCommand {

	private static final Pattern TRACKNAME_PATTERN = Pattern.compile("[\\w\\d-_]+");

	private final TrackRepository trackRepo;

	private final TrackMessages msgTrack;

	@SlashMapping(
		name = "tracktitle",
		description = "Edit the title of a track"
	)
	public @NonNull RestAction<?> editTracktitle(
		SlashCommandInteractionEvent event,
		@CmdOption(name = "trackid", description = "Track's id") Long trackId,
		@CmdOption(name = "title", description = "Track's new title") String trackTitle
	) {

		// Find track and validate ownership
		User user = event.getUser();
		Optional<TrackEntity> trackEntityOpt = trackRepo.findByIdAndOwnerId(trackId, user.getIdLong());

		if(trackEntityOpt.isEmpty())
			return replyEphemeralWith(msgTrack.getErrorOwnNoSuchId(trackId), event);

		// Validate track name
		if(!TRACKNAME_PATTERN.matcher(trackTitle).matches())
			return replyEphemeralWith(msgTrack.getInvalidTitle(trackTitle), event);

		TrackEntity track = trackEntityOpt.get();
		MemberEntity memberEntity = track.getOwner();

		// Check if track with the same title already exists for the user
		if(trackRepo.existsByTitleAndOwner(trackTitle, memberEntity))
			return replyEphemeralWith(msgTrack.getUniqueTitleAndOwner(trackTitle, trackId), event);

		// Update track title
		track.setTitle(trackTitle);
		trackRepo.save(track);

		return event.reply("Track saved successfully")
				.setEphemeral(true);
	}

}
