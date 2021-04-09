package ovh.excale.discord.listeners;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

public class VoiceChannelListener extends ListenerAdapter {

	@Override
	public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {

		Member member = event.getMember();
		boolean isGirl = member.getRoles()
				.stream()
				.map(Role::getName)
				.anyMatch(s -> s.contains("girl") || s.contains("GIRL"));

		if(isGirl) {

			VoiceChannel channel = event.getChannelJoined();
			AudioManager audioManager = event.getGuild()
					.getAudioManager();

			audioManager.openAudioConnection(channel);

			

		}

	}

}
