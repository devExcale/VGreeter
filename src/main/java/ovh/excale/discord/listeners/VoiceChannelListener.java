package ovh.excale.discord.listeners;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ovh.excale.discord.FainaAudioHandler;

public class VoiceChannelListener extends ListenerAdapter {

	private final static Logger logger = LoggerFactory.getLogger(VoiceChannelListener.class);

	@Override
	public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {

		Member member = event.getMember();
		boolean isGirl = member.getRoles()
				.stream()
				.map(Role::getName)
				.anyMatch(s -> s.contains("girl") || s.contains("GIRL"));

		if(isGirl) {

			FainaAudioHandler audioHandler = new FainaAudioHandler();
			if(!audioHandler.canProvide())
				logger.error("Couldn't load audio file");

			VoiceChannel channel = event.getChannelJoined();
			AudioManager audioManager = event.getGuild()
					.getAudioManager();

			audioHandler.onTrackEnd(audioManager::closeAudioConnection);

			audioManager.setSendingHandler(audioHandler);
			audioManager.openAudioConnection(channel);

		}

	}

}
