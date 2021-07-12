package ovh.excale.fainabot.services;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;
import java.util.stream.Collectors;

@Service
public class DiscordService {

	private final static Logger logger = LoggerFactory.getLogger(DiscordService.class);

	private final JDA jda;

	public DiscordService(DiscordEventHandlerService handler,
			@Value("${DISCORD_TOKEN}") String token) throws LoginException, InterruptedException {

		jda = JDABuilder.create(token, GatewayIntent.GUILD_VOICE_STATES)
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
				.addCommands(new CommandData("probab",
						"Manage the voice chat join probability for this guild").addOption(OptionType.INTEGER,
						"percent",
						"Join probability (0 to 100)"))
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
