package ovh.excale.fainabot.services;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import ovh.excale.fainabot.utilities.CommandBuilder;

import javax.security.auth.login.LoginException;
import java.util.stream.Collectors;

@Service
public class DiscordService {

	private static final Logger logger = LoggerFactory.getLogger(DiscordService.class);

	private final JDA jda;

	public DiscordService(DiscordEventHandlerService handler,
			@Value("${DISCORD_TOKEN}") String token) throws LoginException, InterruptedException {

		jda = JDABuilder.create(token, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.DIRECT_MESSAGES)
				.disableCache(CacheFlag.ACTIVITY,
						CacheFlag.ONLINE_STATUS,
						CacheFlag.CLIENT_STATUS,
						CacheFlag.MEMBER_OVERRIDES,
						CacheFlag.EMOTE)
				.setActivity(Activity.listening("people"))
				.addEventListeners(handler)
				.build()
				.awaitReady();

		jda.updateCommands()
				.addCommands(CommandBuilder.create("probab")
						.setDescription("Manage the Voice Chat Join Probability")
						.subcommand("set", "Set the new Join Probability")
						.addOptionRequired("percent", "Join Probability (0 to 100)", OptionType.INTEGER)
						.subcommand("get", "Get the current Join Probability")
						.subcommand("default", "Reset the Join Probability to its default")
						.build())
				.queue(commands -> logger.info("[Registered commands] " + commands.stream()
						.map(Command::getName)
						.collect(Collectors.joining(", "))), e -> logger.warn("Couldn't update commands", e));

		logger.info("JDA connected");

	}

	@Bean
	public JDA getJda() {
		return jda;
	}

}
