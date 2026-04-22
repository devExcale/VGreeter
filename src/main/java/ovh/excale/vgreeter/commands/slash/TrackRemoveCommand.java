package ovh.excale.vgreeter.commands.slash;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.jspecify.annotations.NonNull;
import ovh.excale.vgreeter.commands.core.annotation.CommandController;
import ovh.excale.vgreeter.commands.core.annotation.CmdOption;
import ovh.excale.vgreeter.commands.core.annotation.SlashMapping;
import ovh.excale.vgreeter.entity.TrackEntity;
import ovh.excale.vgreeter.message.TrackMessages;
import ovh.excale.vgreeter.repository.TrackRepository;

import java.util.Optional;

import static ovh.excale.vgreeter.utilities.DiscordUtil.replyEphemeralWith;

@RequiredArgsConstructor
@CommandController
public class TrackRemoveCommand {

	private final TrackRepository trackRepo;

	private final TrackMessages msgTrack;

	@SlashMapping(
		name = "trackremove",
		description = "Delete a track given its id"
	)
	public @NonNull RestAction<?> deleteTrack(
		SlashCommandInteractionEvent event,
		@CmdOption(name = "trackid", description = "The id of the track to delete") Long trackId
	) {

		// Find the track with the provided id
		User user = event.getUser();
		Optional<TrackEntity> trackEntityOpt = trackRepo.findByIdAndOwnerId(trackId, user.getIdLong());

		if(trackEntityOpt.isEmpty())
			return replyEphemeralWith(msgTrack.getErrorOwnNoSuchId(trackId), event);

		// Delete the track
		TrackEntity track = trackEntityOpt.get();
		trackRepo.delete(track);

		return replyEphemeralWith(msgTrack.getDeletedIdTitle(trackId, track.getTitle()), event);
	}

}
