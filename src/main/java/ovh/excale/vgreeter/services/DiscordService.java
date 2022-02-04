package ovh.excale.vgreeter.services;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import ovh.excale.vgreeter.commands.slash.AltnameCommand;
import ovh.excale.vgreeter.commands.slash.ProbabilityCommand;
import ovh.excale.vgreeter.commands.message.RestartCommand;
import ovh.excale.vgreeter.commands.slash.UploadHelpCommand;
import ovh.excale.vgreeter.commands.core.CommandRegister;
import ovh.excale.vgreeter.commands.message.TrackUploadCommand;
import ovh.excale.vgreeter.commands.slash.*;

import javax.security.auth.login.LoginException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@Service
public class DiscordService {

	@Getter
	private static final Set<Long> guildVoiceLocks = Collections.synchronizedSet(new HashSet<>());

	private final JDA jda;

	public DiscordService(VoiceChannelHandler eventHandler, CommandRegister commands,
			@Value("${env.DISCORD_TOKEN}") String token) throws LoginException, InterruptedException {

		jda = JDABuilder
				.create(token, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.DIRECT_MESSAGES,
						GatewayIntent.GUILD_VOICE_STATES)
				.disableCache(CacheFlag.ACTIVITY, CacheFlag.ONLINE_STATUS, CacheFlag.CLIENT_STATUS,
						CacheFlag.MEMBER_OVERRIDES, CacheFlag.EMOTE)
				.setActivity(Activity.listening("people"))
				.addEventListeners(eventHandler, commands.getListener())
				.build()
				.awaitReady();

		log.info("JDA connected");

		String commandListString = jda
				.updateCommands()
				.addCommands(commands
						// SLASH COMMANDS
						.register(new ProbabilityCommand())
						.register(new AltnameCommand())
						.register(new UploadHelpCommand())
						.register(new PlaytestCommand())
						.register(new TrackNameCommand())
						.register(new TrackIndexCommand())
						.register(new TrackRemoveCommand())
						.register(new TrackDownloadCommand())
						// MESSAGE COMMANDS
						.register(new RestartCommand())
						.register(new TrackUploadCommand())
						.getSlashCommandsData())
				.complete()
				.stream()
				.map(Command::getName)
				.collect(Collectors.joining(", "));

		log.info("[Registered SlashCommands] " + commandListString);

	}

	@Bean(destroyMethod = "shutdown")
	public JDA getJda() {
		return jda;
	}

}
