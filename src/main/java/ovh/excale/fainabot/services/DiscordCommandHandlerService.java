package ovh.excale.fainabot.services;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.stereotype.Service;
import ovh.excale.fainabot.models.GuildModel;
import ovh.excale.fainabot.repositories.GuildRepository;

import java.util.Optional;

@Service
public class DiscordCommandHandlerService {

	private final GuildRepository guildRepo;

	public DiscordCommandHandlerService(GuildRepository guildRepo) {
		this.guildRepo = guildRepo;
	}

	@SuppressWarnings("ConstantConditions")
	public void manageProbability(SlashCommandEvent event) {

		Guild guild = event.getGuild();
		Optional<GuildModel> opt = guildRepo.findById(guild.getIdLong());
		GuildModel guildModel = opt.orElseGet(() -> new GuildModel(guild.getIdLong()));

		int prevProbab = guildModel.getJoinProbability();
		Integer newProbab = Optional.ofNullable(event.getOption("percent"))
				.map(OptionMapping::getAsString)
				.map(Integer::parseInt)
				.orElse(null);

		if(newProbab == null) {
			event.reply("The join probability for this guild is " + prevProbab + "%")
					.queue();
			return;
		}

		if(newProbab < 0 || newProbab > 100) {
			event.reply("Probability must be between 0 and 100")
					.setEphemeral(true)
					.queue();
			return;
		}

		guildModel.setJoinProbability(newProbab);
		guildRepo.save(guildModel);
		event.reply("Changed this guild's join probability from " + prevProbab + "% to " + newProbab + "%")
				.queue();

	}

}
