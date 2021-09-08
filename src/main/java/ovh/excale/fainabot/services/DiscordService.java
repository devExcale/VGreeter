package ovh.excale.fainabot.services;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import ovh.excale.fainabot.commands.*;

import javax.security.auth.login.LoginException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DiscordService {

	private static final Logger logger = LoggerFactory.getLogger(DiscordService.class);
	private static final Set<Long> guildVoiceLocks = Collections.synchronizedSet(new HashSet<>());

	private final JDA jda;

	public DiscordService(DiscordEventHandlerService eventHandler, CommandRegister commands,
			@Value("${DISCORD_TOKEN}") String token) throws LoginException, InterruptedException {

		jda = JDABuilder.create(token,
				GatewayIntent.GUILD_VOICE_STATES,
				GatewayIntent.DIRECT_MESSAGES,
				GatewayIntent.GUILD_VOICE_STATES)
				.disableCache(CacheFlag.ACTIVITY,
						CacheFlag.ONLINE_STATUS,
						CacheFlag.CLIENT_STATUS,
						CacheFlag.MEMBER_OVERRIDES,
						CacheFlag.EMOTE)
				.setActivity(Activity.listening("people"))
				.addEventListeners(eventHandler, commands.getListener())
				.build()
				.awaitReady();


		jda.updateCommands()
				.addCommands(commands.register(new ProbabilityCommand())
						.register(new AltnameCommand())
						.register(new UploadCommand())
						.register(new PlaytestCommand())
						.register(new TracknameCommand())
						.register(new TrackIndexCommand())
						.getData())
				.queue(commandList -> logger.info("[Registered commands] " + commandList.stream()
						.map(Command::getName)
						.collect(Collectors.joining(", "))), e -> logger.warn("Couldn't update commands", e));

		logger.info("JDA connected");

	}

	public static Set<Long> getGuildVoiceLocks() {
		return guildVoiceLocks;
	}

	@Bean
	public JDA getJda() {
		return jda;
	}

}
