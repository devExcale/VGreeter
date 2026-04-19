package ovh.excale.vgreeter.commands.message;

import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.gagravarr.ogg.OggFile;
import org.gagravarr.opus.OpusFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import ovh.excale.vgreeter.VGreeterApplication;
import ovh.excale.vgreeter.commands.core.AbstractMessageCommand;
import ovh.excale.vgreeter.entity.TrackEntity;
import ovh.excale.vgreeter.entity.MemberEntity;
import ovh.excale.vgreeter.repository.TrackRepository;
import ovh.excale.vgreeter.repository.MemberRepository;
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
@Component
public class TrackUploadCommand extends AbstractMessageCommand {

	private static final Pattern TRACK_NAME_PATTERN = Pattern.compile("([\\w\\d-_]+)\\.opus");

	private final MemberRepository memberRepo;
	private final TrackService trackService;
	private final TrackRepository trackRepo;

	public TrackUploadCommand(
		MemberRepository memberRepo,
		TrackService trackService,
		TrackRepository trackRepo
	) {
		super("upload", "");

		this.memberRepo = memberRepo;
		this.trackService = trackService;
		this.trackRepo = trackRepo;
	}

	@Override
	public @Nullable RestAction<?> execute(@NotNull MessageReceivedEvent event) {

		// TODO: COMMAND PARAMETERS

		User user = event.getAuthor();
		if(user.isBot())
			return null;

		// Try get member
		Message message = event.getMessage();
		Optional<MemberEntity> opt = memberRepo.findById(user.getIdLong());
		MemberEntity memberEntity;

		if(opt.isEmpty()) {

			// Register new member
			memberEntity = MemberEntity.builder()
					.discordId(user.getIdLong())
					.discordUsername(user.getName())
					.build();
			memberRepo.save(memberEntity);

		} else {
			memberEntity = opt.get();
		}

		List<Message.Attachment> attachments = message.getAttachments();
		if(attachments.isEmpty())
			return message.reply("The track must be **opus encoded**");

		Message.Attachment attachment = attachments.getFirst();
		String filename = attachment.getFileName();
		int size = attachment.getSize();

		long memberTrackMaxSize = memberEntity.getTrackMaxSize();
		if(size > memberTrackMaxSize)
			return message.reply("The file is too big (Max. " + memberTrackMaxSize + ")");

		Matcher filenameMatcher = TRACK_NAME_PATTERN.matcher(filename.toLowerCase(Locale.ROOT));
		if(!filenameMatcher.matches())
			return message.reply("Filename or extension invalid (filename must be alphanumeric" +
					" and can only contain *dashes* `-` and *underscores* `_`, extension must be `.opus`)");

		InputStream in;
		try {

			in = attachment
					.getProxy()
					.download()
					.join();

		} catch(Exception e) {

			log.warn("Error while retrieving Track InputStream", e);
			return message.reply("There has been an internal error while computing the file, please retry. " +
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
				log.warn("Size mismatch while reading InputStream. Expected size: {}, read: {}", size, read);
				data = Arrays.copyOfRange(data, 0, read);
			}

			new OpusFile(new OggFile(new ByteArrayInputStream(data)));

			String trackName = filenameMatcher.group(1);
			if(trackRepo.existsByTitleAndOwner(trackName, memberEntity))
				return message.reply("You've already uploaded a track with the same name");

			TrackEntity track = TrackEntity.builder()
					.title(filenameMatcher.group(1))
					.owner(memberEntity)
					.opusBytes(data)
					.build();
			trackRepo.save(track);

		} catch(IOException e) {

			log.warn("Error while reading Track InputStream", e);
			return message.reply("There has been an internal error while computing the file, please retry. " +
					"If the error persists, contact a developer");

		} catch(IllegalArgumentException e) {
			// Not an opus track
			return message.reply("The track is not **opus-encoded**.");
		}

		return message.reply("Track successfully inserted!");
	}

}
