package ovh.excale.vgreeter.services;

import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ovh.excale.vgreeter.models.GuildModel;
import ovh.excale.vgreeter.repositories.GuildRepository;
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
	public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {

		Guild guild = event.getGuild();
		User user = event.getMember()
				.getUser();

		Set<Long> guildLocks = DiscordService.getGuildVoiceLocks();
		if(user.isBot() || guildLocks.contains(guild.getIdLong()))
			return;

		int joinProbability;

		Optional<GuildModel> opt = guildRepo.findById(guild.getIdLong());
		if(opt.isPresent())
			joinProbability = opt.get()
					.getJoinProbability();
		else {
			GuildModel guildModel = GuildModel
					.builder()
					.id(guild.getIdLong())
					.build();
			joinProbability = guildModel.getJoinProbability();
			guildRepo.save(guildModel);
		}

		if(random.nextInt(100) + 1 > joinProbability)
			return;

		TrackPlayer trackPlayer = new TrackPlayer(trackService.randomTrack());
		if(!trackPlayer.canProvide()) {
			log.error("TrackPlayer cannot provide");
			return;
		}

		VoiceChannel channel = event.getChannelJoined();
		AudioManager audioManager = event.getGuild()
				.getAudioManager();

		trackPlayer.setTrackEndAction(audioManager::closeAudioConnection);

		try {
			audioManager.setSendingHandler(trackPlayer);
			audioManager.openAudioConnection(channel);
			guildLocks.add(guild.getIdLong());
		} catch(InsufficientPermissionException ignored) {
		}

	}

	@Override
	public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {

		Guild guild = event.getGuild();
		User user = event.getMember()
				.getUser();
		SelfUser selfUser = event.getJDA()
				.getSelfUser();

		Set<Long> guildLocks = DiscordService.getGuildVoiceLocks();
		if(user.getIdLong() == selfUser.getIdLong())
			guildLocks.remove(guild.getIdLong());

	}

}
