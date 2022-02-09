package ovh.excale.vgreeter.commands.message;

import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.gagravarr.ogg.OggFile;
import org.gagravarr.opus.OpusFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.excale.vgreeter.VGreeterApplication;
import ovh.excale.vgreeter.commands.core.AbstractMessageCommand;
import ovh.excale.vgreeter.models.TrackModel;
import ovh.excale.vgreeter.models.UserModel;
import ovh.excale.vgreeter.repositories.TrackRepository;
import ovh.excale.vgreeter.repositories.UserRepository;
import ovh.excale.vgreeter.services.AudioConverterService;
import ovh.excale.vgreeter.services.TrackService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
public class TrackUploadCommand extends AbstractMessageCommand {

	private static final Pattern TRACK_NAME_PATTERN = Pattern.compile("([\\w\\d-_]+)\\.opus");

	private final UserRepository userRepo;
	private final TrackService trackService;
	private final AudioConverterService audioConverter;

	public TrackUploadCommand() {
		super("upload", "");

		userRepo = VGreeterApplication
				.getApplicationContext()
				.getBean(UserRepository.class);

		trackService = VGreeterApplication
				.getApplicationContext()
				.getBean(TrackService.class);

		audioConverter = VGreeterApplication
				.getApplicationContext()
				.getBean(AudioConverterService.class);

	}

	@Override
	public @Nullable RestAction<?> execute(@NotNull PrivateMessageReceivedEvent event) {

		// TODO: COMMAND PARAMETERS
		// TODO: MOVE THIS CHECK TO SUPER
		User user = event.getAuthor();
		if(user.isBot())
			return null;

		Message message = event.getMessage();
		Optional<UserModel> opt = userRepo.findById(user.getIdLong());

		boolean hasNotAltname = !opt.isPresent() || opt
				.get()
				.getAltname() == null;

		if(hasNotAltname)
			return message.reply("You must set an `/altname` first to upload a track");

		UserModel userModel = opt.get();

		List<Message.Attachment> attachments = message.getAttachments();
		if(attachments.isEmpty())
			return message.reply("You must send me a file containing the track you want to upload");

		Message.Attachment attachment = attachments.get(0);
		String filename = attachment.getFileName();
		int size = attachment.getSize();

		// TODO: set 4 MB max for audio input, 16 or 32 KB for opus output
//		if(size > TrackService.DEFAULT_MAX_TRACK_SIZE)
//			return message.reply("The file is too big (Max. " + TrackService.DEFAULT_MAX_TRACK_SIZE + ")");

		InputStream in;
		try {

			in = attachment
					.retrieveInputStream()
					.join();

		} catch(Exception e) {

			log.warn("Error while retrieving Track InputStream", e);
			return message.reply("There has been an internal error while computing the file, please retry. " +
					"If the error persists, contact a developer");

		}

		byte[] trackData;
		try {

			trackData = audioConverter.toOpus(in);

		} catch(IllegalArgumentException e) {
			return message.reply("The provided file doesn't have a valid audio stream");
		}

		if(trackData.length == 0)
			return message.reply("There has been an error while converting the file");

		// TODO: USE TRACK_SERVICE
		TrackRepository trackRepo = trackService.getTrackRepo();

		// Matcher filenameMatcher = TRACK_NAME_PATTERN.matcher(filename.toLowerCase(Locale.ROOT));
		// String trackName = filename.toLowerCase();

		if(trackRepo.existsByNameAndUploader(filename, userModel))
			return message.reply("You've already uploaded a track with the same name");

		TrackModel track = TrackModel
				.builder()
				.name(filename)
				.uploader(userModel)
				.size((long) trackData.length)
				.data(trackData)
				.build();
		trackRepo.save(track);

		return message.reply("Track successfully inserted!");
	}

}
