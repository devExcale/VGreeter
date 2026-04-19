package ovh.excale.vgreeter.commands.slash;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.RestAction;
import ovh.excale.vgreeter.VGreeterApplication;
import ovh.excale.vgreeter.commands.core.AbstractSlashCommand;
import ovh.excale.vgreeter.entity.TrackEntity;
import ovh.excale.vgreeter.repository.TrackRepository;

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
	public RestAction<?> execute(SlashCommandInteractionEvent event) {

		RestAction<?> reply;
		User user = event.getUser();

		//noinspection ConstantConditions
		long trackId = event
				.getOption("trackid")
				.getAsLong();

		Optional<TrackEntity> opt = trackRepo.findById(trackId);
		if(opt.isEmpty())
			reply = event
					.reply("No track with such id")
					.setEphemeral(true);
		else {

			TrackEntity track = opt.get();
			Long userId = user.getIdLong();

			if(!userId.equals(track.getOwnerId()))
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
