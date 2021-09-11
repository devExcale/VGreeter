package ovh.excale.vgreeter.services;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.gagravarr.ogg.OggFile;
import org.gagravarr.opus.OpusFile;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ovh.excale.vgreeter.models.GuildModel;
import ovh.excale.vgreeter.models.TrackModel;
import ovh.excale.vgreeter.models.UserModel;
import ovh.excale.vgreeter.repositories.GuildRepository;
import ovh.excale.vgreeter.repositories.TrackRepository;
import ovh.excale.vgreeter.repositories.UserRepository;
import ovh.excale.vgreeter.utilities.TrackPlayer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DiscordEventHandlerService extends ListenerAdapter {

	private final static Logger logger = LoggerFactory.getLogger(DiscordEventHandlerService.class);
	private static final Pattern TRACK_NAME_PATTERN = Pattern.compile("([\\w\\d-_]+)\\.opus");

	private final UserRepository userRepo;
	private final GuildRepository guildRepo;
	private final TrackService trackService;

	private final Random random;

	public DiscordEventHandlerService(UserRepository userRepo, GuildRepository guildRepo, TrackService trackService) {
		this.userRepo = userRepo;
		this.guildRepo = guildRepo;
		this.trackService = trackService;
		random = new Random();
	}

	@Override
	public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {

		Guild guild = event.getGuild();
		User user = event.getMember()
				.getUser();

		Set<Long> guildLocks = DiscordService.getGuildVoiceLocks();
		if(user.isBot() || guildLocks.contains(guild.getIdLong()))
			return;

		int joinProbability;

		Optional<GuildModel> opt = guildRepo.findById(guild.getIdLong());
		if(opt.isPresent())
			joinProbability = opt.get()
					.getJoinProbability();
		else {
			GuildModel guildModel = new GuildModel(guild.getIdLong());
			joinProbability = guildModel.getJoinProbability();
			guildRepo.save(guildModel);
		}

		if(random.nextInt(100) + 1 > joinProbability)
			return;

		TrackPlayer trackPlayer = new TrackPlayer(trackService.randomTrack());
		if(!trackPlayer.canProvide()) {
			logger.error("TrackPlayer cannot provide");
			return;
		}

		VoiceChannel channel = event.getChannelJoined();
		AudioManager audioManager = event.getGuild()
				.getAudioManager();

		trackPlayer.setTrackEndAction(audioManager::closeAudioConnection);

		try {
			audioManager.setSendingHandler(trackPlayer);
			audioManager.openAudioConnection(channel);
			guildLocks.add(guild.getIdLong());
		} catch(InsufficientPermissionException ignored) {
		}

	}

	@Override
	public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {

		Guild guild = event.getGuild();
		User user = event.getMember()
				.getUser();
		SelfUser selfUser = event.getJDA()
				.getSelfUser();

		Set<Long> guildLocks = DiscordService.getGuildVoiceLocks();
		if(user.getIdLong() == selfUser.getIdLong())
			guildLocks.remove(guild.getIdLong());

	}

	@Override
	public void onPrivateMessageReceived(@NotNull PrivateMessageReceivedEvent event) {

		User user = event.getAuthor();
		if(user.isBot())
			return;

		Message message = event.getMessage();
		Optional<UserModel> opt = userRepo.findById(user.getIdLong());

		if(!opt.isPresent() || opt.get()
				.getAltname() == null) {
			message.reply("You must set an `/altname` first to upload a track")
					.queue();
			return;
		}

		UserModel userModel = opt.get();

		List<Message.Attachment> attachments = message.getAttachments();
		if(attachments.isEmpty()) {
			message.reply("To upload a track you must send me an **opus encoded** file")
					.queue();
			return;
		}

		Message.Attachment attachment = attachments.get(0);
		String filename = attachment.getFileName();
		int size = attachment.getSize();

		if(size > TrackService.DEFAULT_MAX_TRACK_SIZE) {
			message.reply("The file is too big")
					.queue();
			return;
		}

		Matcher filenameMatcher = TRACK_NAME_PATTERN.matcher(filename.toLowerCase(Locale.ROOT));
		if(!filenameMatcher.matches()) {
			message.reply(
					"Filename or extension invalid (filename must be alphanumeric and can only contain *dashes* `-` and *underscores* `_`, extension must be `.opus`)")
					.queue();
			return;
		}

		attachment.retrieveInputStream()
				.thenAcceptAsync(in -> {

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
							logger.warn("Size mismatch while reading InputStream. Expected size: " + size + ", read: " +
									read);
							data = Arrays.copyOfRange(data, 0, read);
						}

						new OpusFile(new OggFile(new ByteArrayInputStream(data)));

						// TODO: USE TRACK_SERVICE
						TrackRepository trackRepo = trackService.getTrackRepo();
						String trackname = filenameMatcher.group(1);

						if(trackRepo.existsByNameAndUploader(trackname, userModel)) {
							message.reply("You've already uploaded a track with the same name")
									.queue();
							return;
						}

						TrackModel track = new TrackModel().setName(filenameMatcher.group(1))
								.setUploader(userModel)
								.setSize((long) data.length)
								.setData(data);
						trackRepo.save(track);

						message.reply("Track successfully inserted!")
								.queue();

					} catch(IOException e) {
						throw new UncheckedIOException(e);
					} catch(IllegalArgumentException e) {

						// Not an opus track
						message.reply("The provided file is not an **opus-encoded** track.")
								.queue();

					}
				})
				.exceptionally(e -> {
					message.reply(
							"There has been an internal error while computing the file, please retry or contact a developer")
							.queue();
					logger.warn("Error while opening OpusFile", e);
					return null;
				});

	}

}
