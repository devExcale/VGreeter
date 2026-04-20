package ovh.excale.vgreeter.services;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import club.minnced.discord.jdave.interop.JDaveSessionFactory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.audio.AudioModuleConfig;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ovh.excale.vgreeter.commands.core.CommandDispatcher;
import ovh.excale.vgreeter.commands.core.event.CommandUpdateEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@Service
public class DiscordService {

	@Getter
	private static final Set<Long> guildVoiceLocks = Collections.synchronizedSet(new HashSet<>());

	@Getter(onMethod_ = @Bean(destroyMethod = "shutdown"))
	private final JDA jda;

	public DiscordService(
		VoiceGreeterHandler eventHandler,
		CommandDispatcher commandDispatcher,
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
				CacheFlag.EMOJI,
				CacheFlag.STICKER,
				CacheFlag.SOUNDBOARD_SOUNDS,
				CacheFlag.SCHEDULED_EVENTS
			)
			.setActivity(Activity.listening("people"))
			.addEventListeners(eventHandler, commandDispatcher)
			.setAudioModuleConfig(
				new AudioModuleConfig().withDaveSessionFactory(new JDaveSessionFactory())
			)
			.build()
			.awaitReady();

		log.info("JDA connected");
	}

	@EventListener
	public void onCommandUpdateEvent(CommandUpdateEvent event) {

		CommandData[] commandData = event.getCommandData();

		String commandListString = jda
			.updateCommands()
			.addCommands(commandData)
			.complete()
			.stream()
			.map(Command::getName)
			.collect(Collectors.joining(", "));

		log.info("[Registered SlashCommands: {}] {}", commandData.length, commandListString);
	}

}
