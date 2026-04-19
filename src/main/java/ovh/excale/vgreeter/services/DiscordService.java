package ovh.excale.vgreeter.services;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import club.minnced.discord.jdave.interop.JDaveSessionFactory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.audio.AudioModuleConfig;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import ovh.excale.vgreeter.commands.button.CloseEmbedCommand;
import ovh.excale.vgreeter.commands.button.TrackIndexButtonCommand;
import ovh.excale.vgreeter.commands.core.AbstractCommand;
import ovh.excale.vgreeter.commands.slash.ProbabilityCommand;
import ovh.excale.vgreeter.commands.message.RestartCommand;
import ovh.excale.vgreeter.commands.slash.UploadHelpCommand;
import ovh.excale.vgreeter.commands.core.CommandRegister;
import ovh.excale.vgreeter.commands.message.TrackUploadCommand;
import ovh.excale.vgreeter.commands.slash.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@Service
public class DiscordService {

	@Getter
	private static final Set<Long> guildVoiceLocks = Collections.synchronizedSet(new HashSet<>());

	private final JDA jda;

	public DiscordService(
		VoiceChannelHandler eventHandler,
		CommandRegister commandRegister,
		Map<String, AbstractCommand<?>> commands,
		@Value("${env.DISCORD_TOKEN}") String token
	) throws InterruptedException {

		jda = JDABuilder
			.create(
				token,
				GatewayIntent.GUILD_VOICE_STATES,
				GatewayIntent.DIRECT_MESSAGES,
				GatewayIntent.MESSAGE_CONTENT
			)
			.disableCache(
				CacheFlag.ACTIVITY,
				CacheFlag.ONLINE_STATUS,
				CacheFlag.CLIENT_STATUS,
				CacheFlag.MEMBER_OVERRIDES,
				CacheFlag.EMOJI
			)
			.setActivity(Activity.listening("people"))
			.addEventListeners(eventHandler, commandRegister.getListener())
			.setAudioModuleConfig(
				new AudioModuleConfig().withDaveSessionFactory(new JDaveSessionFactory())
			)
			.build()
			.awaitReady();

		log.info("JDA connected");

		// Register all commands
		for(AbstractCommand<?> command : commands.values())
			commandRegister.register(command);

		String commandListString = jda
				.updateCommands()
				.addCommands(commandRegister.getSlashCommandsData())
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
