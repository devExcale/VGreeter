package ovh.excale.vgreeter.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;
import ovh.excale.vgreeter.VGreeterApplication;
import ovh.excale.vgreeter.commands.core.AbstractSlashCommand;
import ovh.excale.vgreeter.models.GuildModel;
import ovh.excale.vgreeter.repositories.GuildRepository;

import java.util.Optional;

public class ProbabilityCommand extends AbstractSlashCommand {

	private final GuildRepository guildRepo;

	public ProbabilityCommand() {
		super("probab", "Manage the Voice Chat Join Probability");
		this.getBuilder()
				.subcommand("set", "Set the new Join Probability")
				.addOptionRequired("percent", "Join Probability (0 to 100)", OptionType.INTEGER)
				.subcommand("get", "Get the current Join Probability")
				.subcommand("default", "Reset the Join Probability to its default");

		guildRepo = VGreeterApplication
				.getApplicationContext()
				.getBean(GuildRepository.class);

	}

	@Override
	public ReplyAction execute(SlashCommandEvent event) {

		Guild guild = event.getGuild();

		if(guild == null)
			return event.reply("This ain't a guild")
					.setEphemeral(true);

		Member member = event.getMember();
		Optional<GuildModel> opt = guildRepo.findById(guild.getIdLong());
		GuildModel guildModel = opt.orElseGet(() -> GuildModel
				.builder()
				.id(guild.getIdLong())
				.build());

		int prevProbab = guildModel.getJoinProbability();
		ReplyAction reply;

		String subcommand = Optional.ofNullable(event.getSubcommandName())
				.orElse("");

		switch(subcommand) {

			case "set":

				//noinspection ConstantConditions
				if(!member.hasPermission(Permission.ADMINISTRATOR))
					return event.reply("You must have ADMINISTRATOR permission to use this command")
							.setEphemeral(true);

				//noinspection ConstantConditions
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

				//noinspection ConstantConditions
				if(!member.hasPermission(Permission.ADMINISTRATOR))
					return event.reply("You must have ADMINISTRATOR permission to use this command")
							.setEphemeral(true);

				guildModel.setJoinProbability(GuildModel.DEFAULT_JOIN_PROBABILITY);
				guildRepo.save(guildModel);
				reply = event.reply("Reset the Join Probability to " + guildModel.getJoinProbability() + "%");

				break;

			default:
				reply = event.reply("Unknown option")
						.setEphemeral(true);

		}

		return reply;
	}

}
