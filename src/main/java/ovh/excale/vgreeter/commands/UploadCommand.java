package ovh.excale.vgreeter.commands;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.gagravarr.ogg.OggFile;
import org.gagravarr.opus.OpusFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ovh.excale.vgreeter.VGreeterApplication;
import ovh.excale.vgreeter.commands.core.AbstractMessageCommand;
import ovh.excale.vgreeter.models.TrackModel;
import ovh.excale.vgreeter.models.UserModel;
import ovh.excale.vgreeter.repositories.TrackRepository;
import ovh.excale.vgreeter.repositories.UserRepository;
import ovh.excale.vgreeter.services.TrackService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UploadCommand extends AbstractMessageCommand {

	private static final Logger logger = LoggerFactory.getLogger(UploadCommand.class);
	private static final Pattern TRACK_NAME_PATTERN = Pattern.compile("([\\w\\d-_]+)\\.opus");

	private final UserRepository userRepo;
	private final TrackService trackService;

	public UploadCommand() {
		super("upload", "");

		userRepo = VGreeterApplication
				.getApplicationContext()
				.getBean(UserRepository.class);

		trackService = VGreeterApplication
				.getApplicationContext()
				.getBean(TrackService.class);

	}

	@Override
	public @Nullable RestAction<?> execute(@NotNull PrivateMessageReceivedEvent event) {

		// TODO: COMMAND PARAMETERS

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
			return message.reply("The track must be **opus encoded**");

		Message.Attachment attachment = attachments.get(0);
		String filename = attachment.getFileName();
		int size = attachment.getSize();

		if(size > TrackService.DEFAULT_MAX_TRACK_SIZE)
			return message.reply("The file is too big (Max. " + TrackService.DEFAULT_MAX_TRACK_SIZE + ")");

		Matcher filenameMatcher = TRACK_NAME_PATTERN.matcher(filename.toLowerCase(Locale.ROOT));
		if(!filenameMatcher.matches())
			return message.reply("Filename or extension invalid (filename must be alphanumeric" +
					" and can only contain *dashes* `-` and *underscores* `_`, extension must be `.opus`)");

		InputStream in;
		try {

			in = attachment
					.retrieveInputStream()
					.join();

		} catch(Exception e) {

			logger.warn("Error while retrieving Track InputStream", e);
			return message.reply(
					"There has been an internal error while computing the file, please retry. " +
							"If the error persists, contact a developer");

		}

		byte[] data = new byte[size];
		try {

			int read = 0, c;
			do {

				c = in.read(data, read, size - read);
				if(c > 0)
					read += c;

			} while(c > 0);

			in.close();

			if(read != size) {
				logger.warn("Size mismatch while reading InputStream. Expected size: " + size + ", read: " + read);
				data = Arrays.copyOfRange(data, 0, read);
			}

			new OpusFile(new OggFile(new ByteArrayInputStream(data)));

			// TODO: USE TRACK_SERVICE
			TrackRepository trackRepo = trackService.getTrackRepo();
			String trackName = filenameMatcher.group(1);

			if(trackRepo.existsByNameAndUploader(trackName, userModel))
				return message.reply("You've already uploaded a track with the same name");

			TrackModel track = new TrackModel()
					.setName(filenameMatcher.group(1))
					.setUploader(userModel)
					.setSize((long) data.length)
					.setData(data);
			trackRepo.save(track);

		} catch(IOException e) {

			logger.warn("Error while reading Track InputStream", e);
			return message.reply(
					"There has been an internal error while computing the file, please retry. " +
							"If the error persists, contact a developer");

		} catch(IllegalArgumentException e) {
			// Not an opus track
			return message.reply("The track is not **opus-encoded**.");
		}

		return message.reply("Track successfully inserted!");
	}

}
