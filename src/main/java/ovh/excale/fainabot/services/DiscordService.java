package ovh.excale.fainabot.services;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import ovh.excale.fainabot.models.GuildModel;
import ovh.excale.fainabot.repositories.GuildRepository;
import ovh.excale.fainabot.utilities.TrackPlayer;

import javax.security.auth.login.LoginException;
import java.util.*;

@Service
public class DiscordService implements EventListener {

	private final static Logger logger = LoggerFactory.getLogger(DiscordService.class);

	private final TrackService trackService;
	private final GuildRepository guildRepo;
	private final Set<Long> guildLocks = Collections.synchronizedSet(new HashSet<>());
	private final JDA jda;
	private final Random random;

	public DiscordService(TrackService trackService, GuildRepository guildRepo,
			@Value("${DISCORD_TOKEN}") String token) throws LoginException, InterruptedException {

		this.trackService = trackService;
		this.guildRepo = guildRepo;

		random = new Random();
		jda = JDABuilder.create(token, GatewayIntent.GUILD_VOICE_STATES)
				.disableCache(CacheFlag.ACTIVITY,
						CacheFlag.ONLINE_STATUS,
						CacheFlag.CLIENT_STATUS,
						CacheFlag.MEMBER_OVERRIDES,
						CacheFlag.EMOTE)
				.setActivity(Activity.listening("people"))
				.addEventListeners(this)
				.build()
				.awaitReady();

		logger.info("JDA connected");

	}

	@Override
	public void onEvent(@NotNull GenericEvent event) {

		if(event instanceof GuildVoiceJoinEvent) {

			GuildVoiceJoinEvent joinEvent = (GuildVoiceJoinEvent) event;
			Guild guild = joinEvent.getGuild();

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

			VoiceChannel channel = joinEvent.getChannelJoined();
			AudioManager audioManager = joinEvent.getGuild()
					.getAudioManager();

			trackPlayer.setTrackEndAction(() -> {
				guildLocks.remove(guild.getIdLong());
				audioManager.closeAudioConnection();
			});

			audioManager.setSendingHandler(trackPlayer);
			audioManager.openAudioConnection(channel);
			guildLocks.add(guild.getIdLong());

		}

	}

	@Bean
	public JDA getJda() {
		return jda;
	}

}
