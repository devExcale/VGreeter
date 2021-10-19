package ovh.excale.vgreeter.commands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;
import ovh.excale.vgreeter.VGreeterApplication;
import ovh.excale.vgreeter.commands.core.AbstractSlashCommand;
import ovh.excale.vgreeter.models.TrackModel;
import ovh.excale.vgreeter.repositories.TrackRepository;
import ovh.excale.vgreeter.repositories.UserRepository;

import java.util.Optional;

public class TrackRemoveCommand extends AbstractSlashCommand {

	private final TrackRepository trackRepo;

	public TrackRemoveCommand() {
		super("trackremove", "Delete a track");
		this
				.getBuilder()
				.addOptionRequired("trackid", "The track to remove", OptionType.INTEGER);

		trackRepo = VGreeterApplication
				.getApplicationContext()
				.getBean(TrackRepository.class);

	}

	@Override
	public ReplyAction execute(SlashCommandEvent event) {

		ReplyAction reply;
		User user = event.getUser();

		//noinspection ConstantConditions
		long trackId = event
				.getOption("trackid")
				.getAsLong();

		Optional<TrackModel> opt = trackRepo.findById(trackId);
		if(!opt.isPresent())
			reply = event
					.reply("No track with such id")
					.setEphemeral(true);
		else {

			TrackModel track = opt.get();
			Long userId = user.getIdLong();

			if(!userId.equals(track.getUploaderId()))
				reply = event
						.reply("You're not the uploader of track `" + trackId + "`")
						.setEphemeral(true);
			else {

				trackRepo.delete(track);
				reply = event
						.reply("Track `" + trackId + "` deleted successfully")
						.setEphemeral(true);

			}

		}

		return reply;
	}

}
