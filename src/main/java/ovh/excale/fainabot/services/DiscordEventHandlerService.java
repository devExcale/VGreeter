package ovh.excale.fainabot.services;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.gagravarr.ogg.OggFile;
import org.gagravarr.opus.OpusFile;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ovh.excale.fainabot.models.GuildModel;
import ovh.excale.fainabot.models.TrackModel;
import ovh.excale.fainabot.models.UserModel;
import ovh.excale.fainabot.repositories.GuildRepository;
import ovh.excale.fainabot.repositories.TrackRepository;
import ovh.excale.fainabot.repositories.UserRepository;
import ovh.excale.fainabot.utilities.TrackPlayer;

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
	private final DiscordCommandHandlerService commandHandler;

	private final Set<Long> guildLocks;
	private final Random random;

	public DiscordEventHandlerService(UserRepository userRepo, GuildRepository guildRepo, TrackService trackService,
			DiscordCommandHandlerService commandHandler) {
		this.userRepo = userRepo;
		this.guildRepo = guildRepo;
		this.trackService = trackService;
		this.commandHandler = commandHandler;
		guildLocks = Collections.synchronizedSet(new HashSet<>());
		random = new Random();
	}

	@Override
	public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {

		Guild guild = event.getGuild();

		if(guildLocks.contains(guild.getIdLong()))
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

		trackPlayer.setTrackEndAction(() -> {
			guildLocks.remove(guild.getIdLong());
			audioManager.closeAudioConnection();
		});

		audioManager.setSendingHandler(trackPlayer);
		audioManager.openAudioConnection(channel);
		guildLocks.add(guild.getIdLong());

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

	@Override
	public void onSlashCommand(@NotNull SlashCommandEvent event) {

		if(event.getGuild() == null) {
			event.reply("This ain't a guild")
					.setEphemeral(true)
					.queue();
			return;
		}

		//noinspection SwitchStatementWithTooFewBranches
		switch(event.getName()) {

			case "probab":
				commandHandler.manageProbability(event);
				break;

		}

	}

}
