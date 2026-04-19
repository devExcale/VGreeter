package ovh.excale.vgreeter.commands.slash;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.RestAction;
import ovh.excale.vgreeter.VGreeterApplication;
import ovh.excale.vgreeter.commands.core.AbstractSlashCommand;
import ovh.excale.vgreeter.entity.TrackEntity;
import ovh.excale.vgreeter.entity.MemberEntity;
import ovh.excale.vgreeter.repository.TrackRepository;

import java.util.Optional;
import java.util.regex.Pattern;

public class TrackNameCommand extends AbstractSlashCommand {

	private static final Pattern TRACKNAME_PATTERN = Pattern.compile("[\\w\\d-_]+");

	private final TrackRepository trackRepo;

	public TrackNameCommand() {
		super("trackname", "Edit the name of a track");

		this.getBuilder()
				.addOptionRequired("trackid", "Track's id", OptionType.INTEGER)
				.addOptionRequired("trackname", "Track's new name", OptionType.STRING);

		trackRepo = VGreeterApplication
				.getApplicationContext()
				.getBean(TrackRepository.class);

	}

	@Override
	public RestAction<?> execute(SlashCommandInteractionEvent event) {

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

		Optional<TrackEntity> opt = trackRepo.findById(trackId);

		if(!opt.isPresent())
			return event.reply("Invalid track id")
					.setEphemeral(true);

		TrackEntity track = opt.get();
		MemberEntity userModel = track.getOwner();
		User user = event.getUser();

		if(userModel.getDiscordId() != user.getIdLong())
			return event.reply("You're not the uploaded of this track")
					.setEphemeral(true);

		if(trackRepo.existsByTitleAndOwner(trackname, userModel))
			return event.reply("A track with that name already exists")
					.setEphemeral(true);

		track.setTitle(trackname);
		trackRepo.save(track);

		return event.reply("Track saved successfully")
				.setEphemeral(true);
	}

}
