package ovh.excale.fainabot.services;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;
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
		ReplyAction reply;

		switch(event.getSubcommandName()) {

			case "set":

				int newProbab = Optional.of(event.getOption("percent"))
						.map(OptionMapping::getAsString)
						.map(Integer::parseInt)
						.get();

				if(newProbab >= 0 && newProbab <= 100) {
					guildModel.setJoinProbability(newProbab);
					guildRepo.save(guildModel);
					reply = event.reply("Changed the Join Probability from " + prevProbab + "% to " + newProbab + "%");
				} else
					reply = event.reply("Probability must be between 0 and 100")
							.setEphemeral(true);

				break;

			case "get":
				reply = event.reply("The Join Probability is " + prevProbab + "%");
				break;

			case "default":
				guildModel.setJoinProbability(GuildModel.DEFAULT_JOIN_PROBABILITY);
				guildRepo.save(guildModel);
				reply = event.reply("Reset the Join Probability to " + guildModel.getJoinProbability() + "%");
				break;

			default:
				reply = event.reply("Unknown option")
						.setEphemeral(true);

		}

		reply.queue();

	}

}
