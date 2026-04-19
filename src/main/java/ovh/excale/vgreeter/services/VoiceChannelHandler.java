package ovh.excale.vgreeter.services;

import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ovh.excale.vgreeter.entity.GuildEntity;
import ovh.excale.vgreeter.repository.GuildRepository;
import ovh.excale.vgreeter.track.TrackPlayer;

import java.util.Optional;
import java.util.Random;
import java.util.Set;

@Log4j2
@Service
public class VoiceChannelHandler extends ListenerAdapter {

	private final GuildRepository guildRepo;
	private final TrackService trackService;

	private final Random random;

	public VoiceChannelHandler(GuildRepository guildRepo, TrackService trackService) {
		this.guildRepo = guildRepo;
		this.trackService = trackService;
		random = new Random();
	}

	// TODO: DISABLE VOICE EVENT HANDLING UNDER MAINTENANCE

	@Override
	public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {

		Guild guild = event.getGuild();
		User user = event.getMember().getUser();
		Set<Long> guildLocks = DiscordService.getGuildVoiceLocks();

		if(event.getChannelLeft() != null) {
			SelfUser selfUser = event.getJDA().getSelfUser();
			if(user.getIdLong() == selfUser.getIdLong())
				guildLocks.remove(guild.getIdLong());
		}

		if(event.getChannelJoined() == null)
			return;

		if(user.isBot() || guildLocks.contains(guild.getIdLong()))
			return;

		float greetProbab;

		Optional<GuildEntity> opt = guildRepo.findById(guild.getIdLong());
		if(opt.isPresent())
			greetProbab = opt.get().getGreetProbab();
		else {
			GuildEntity guildModel = GuildEntity.builder().discordId(guild.getIdLong()).build();
			greetProbab = guildModel.getGreetProbab();
			guildRepo.save(guildModel);
		}

		if(random.nextFloat() > greetProbab)
			return;

		TrackPlayer trackPlayer = new TrackPlayer(trackService.randomTrack());
		if(!trackPlayer.canProvide()) {
			log.error("TrackPlayer cannot provide");
			return;
		}

		AudioChannelUnion channel = event.getChannelJoined();
		AudioManager audioManager = guild.getAudioManager();
		trackPlayer.setTrackEndAction(audioManager::closeAudioConnection);

		try {
			audioManager.setSendingHandler(trackPlayer);
			audioManager.openAudioConnection(channel);
			guildLocks.add(guild.getIdLong());
		} catch(InsufficientPermissionException ignored) {
			// The bot doesn't have permissions to connect to the Voice Channel, do nothing
		}

	}


}
