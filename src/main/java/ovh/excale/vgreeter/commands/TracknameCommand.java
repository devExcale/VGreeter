package ovh.excale.vgreeter.commands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;
import ovh.excale.vgreeter.VGreeterApplication;
import ovh.excale.vgreeter.commands.core.AbstractSlashCommand;
import ovh.excale.vgreeter.models.TrackModel;
import ovh.excale.vgreeter.models.UserModel;
import ovh.excale.vgreeter.repositories.TrackRepository;

import java.util.Optional;
import java.util.regex.Pattern;

public class TracknameCommand extends AbstractSlashCommand {

	private static final Pattern TRACKNAME_PATTERN = Pattern.compile("[\\w\\d-_]+");

	private final TrackRepository trackRepo;

	public TracknameCommand() {
		super("trackname", "Edit the name of a track");

		this.getBuilder()
				.addOptionRequired("trackid", "Track's id", OptionType.INTEGER)
				.addOptionRequired("trackname", "Track's new name", OptionType.STRING);

		trackRepo = VGreeterApplication
				.getApplicationContext()
				.getBean(TrackRepository.class);

	}

	@Override
	public ReplyAction execute(SlashCommandEvent event) {

		//noinspection ConstantConditions
		long trackId = Long.parseLong(event.getOption("trackid")
				.getAsString());
		//noinspection ConstantConditions
		String trackname = event.getOption("trackname")
				.getAsString();

		if(!TRACKNAME_PATTERN.matcher(trackname)
				.matches())
			return event.reply("Invalid track name")
					.setEphemeral(true);

		Optional<TrackModel> opt = trackRepo.findById(trackId);

		if(!opt.isPresent())
			return event.reply("Invalid track id")
					.setEphemeral(true);

		TrackModel track = opt.get();
		UserModel userModel = track.getUploader();
		User user = event.getUser();

		if(userModel.getSnowflake() != user.getIdLong())
			return event.reply("You're not the uploaded of this track")
					.setEphemeral(true);

		if(trackRepo.existsByNameAndUploader(trackname, userModel))
			return event.reply("A track with that name already exists")
					.setEphemeral(true);

		track.setName(trackname);
		trackRepo.save(track);

		return event.reply("Track saved successfully")
				.setEphemeral(true);
	}

}
