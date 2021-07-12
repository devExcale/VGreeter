package ovh.excale.fainabot.services;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ovh.excale.fainabot.models.GuildModel;
import ovh.excale.fainabot.repositories.GuildRepository;
import ovh.excale.fainabot.utilities.TrackPlayer;

import java.util.*;

@Service
public class DiscordEventHandlerService extends ListenerAdapter {

	private final static Logger logger = LoggerFactory.getLogger(DiscordEventHandlerService.class);

	private final TrackService trackService;
	private final GuildRepository guildRepo;
	private final DiscordCommandHandlerService commandHandler;

	private final Set<Long> guildLocks;
	private final Random random;

	public DiscordEventHandlerService(TrackService trackService, GuildRepository guildRepo,
			DiscordCommandHandlerService commandHandler) {
		this.trackService = trackService;
		this.guildRepo = guildRepo;
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
			// TODO: async
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
