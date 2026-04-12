package ovh.excale.vgreeter.commands.slash;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.FileUpload;
import ovh.excale.vgreeter.VGreeterApplication;
import ovh.excale.vgreeter.commands.core.AbstractSlashCommand;
import ovh.excale.vgreeter.models.TrackModel;
import ovh.excale.vgreeter.repositories.TrackRepository;

import java.util.Optional;

public class TrackDownloadCommand extends AbstractSlashCommand {

	private final TrackRepository trackRepo;

	public TrackDownloadCommand() {
		super("trackdownload", "download command placeholder");

		this.getBuilder()
				.addOptionRequired("trackid", "The track to download", OptionType.INTEGER);

		trackRepo = VGreeterApplication.getApplicationContext()
				.getBean(TrackRepository.class);

	}

	// TODO: 30sec cooldown (whole-guild scope) on download, probably with stopwatch and queue

	@Override
	public RestAction<?> execute(SlashCommandInteractionEvent event) {

		Guild guild = event.getGuild();

		if(guild == null)
			return event.reply("This command can be executed in a guild only")
					.setEphemeral(true);

		//noinspection ConstantConditions
		long trackId = Long.parseLong(event.getOption("trackid")
				.getAsString());

		Optional<TrackModel> opt = trackRepo.findById(trackId);

		if(!opt.isPresent())
			return event.reply("No track with such id")
					.setEphemeral(true);

		TrackModel track = opt.get();

		return event.reply(String.format("Track `#%d`", track.getId()))
				.addFiles(FileUpload.fromData(track.getData(), track.getName() + ".opus"));

	}

}
