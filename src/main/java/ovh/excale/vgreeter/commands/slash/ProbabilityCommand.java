package ovh.excale.vgreeter.commands.slash;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.RestAction;
import org.springframework.stereotype.Component;
import ovh.excale.vgreeter.VGreeterApplication;
import ovh.excale.vgreeter.commands.core.AbstractSlashCommand;
import ovh.excale.vgreeter.entity.GuildEntity;
import ovh.excale.vgreeter.repository.GuildRepository;

import java.util.Optional;

@Component
public class ProbabilityCommand extends AbstractSlashCommand {

	private final GuildRepository guildRepo;

	public ProbabilityCommand() {
		super("probab", "Manage the Voice Chat Join Probability");
		this.getBuilder()
				.subcommand("set", "Set the new Join Probability")
				.addOptionRequired("percent", "Join Probability (0 to 100)", OptionType.NUMBER)
				.subcommand("get", "Get the current Join Probability")
				.subcommand("default", "Reset the Join Probability to its default");

		guildRepo = VGreeterApplication
				.getApplicationContext()
				.getBean(GuildRepository.class);

	}

	@Override
	public RestAction<?> execute(SlashCommandInteractionEvent event) {

		Guild guild = event.getGuild();

		if(guild == null)
			return event.reply("This ain't a guild")
					.setEphemeral(true);

		Member member = event.getMember();
		Optional<GuildEntity> opt = guildRepo.findById(guild.getIdLong());
		GuildEntity guildEntity = opt.orElseGet(
			() -> GuildEntity.builder()
				.discordId(guild.getIdLong())
				.build()
		);

		float greetProbab100 = guildEntity.getGreetProbab() * 100;
		RestAction<?> reply;

		String subcommand = Optional.ofNullable(event.getSubcommandName())
				.orElse("");

		switch(subcommand) {

			case "set":

				//noinspection ConstantConditions
				if(!member.hasPermission(Permission.ADMINISTRATOR))
					return event.reply("You must have ADMINISTRATOR permission to use this command")
							.setEphemeral(true);

				//noinspection ConstantConditions
				float newGreetProbab100 = Optional.of(event.getOption("percent"))
					.map(OptionMapping::getAsString)
					.map(Float::parseFloat)
					.get();

				if(newGreetProbab100 < 0f || newGreetProbab100 > 100f)
					return event.reply("Probability must be between 0 and 100")
						.setEphemeral(true);

				guildEntity.setGreetProbab(newGreetProbab100 / 100f);
				guildRepo.save(guildEntity);
				reply = event.reply("Changed the Join Probability from " + greetProbab100 + "% to " + newGreetProbab100 + "%");

				break;

			case "get":

				reply = event.reply("The Join Probability is " + greetProbab100 + "%");

				break;

			case "default":

				//noinspection ConstantConditions
				if(!member.hasPermission(Permission.ADMINISTRATOR))
					return event.reply("You must have ADMINISTRATOR permission to use this command")
							.setEphemeral(true);

				guildEntity.setGreetProbab(GuildEntity.DEFAULT_GREET_PROBAB);
				guildRepo.save(guildEntity);
				greetProbab100 = guildEntity.getGreetProbab() * 100;
				reply = event.reply("Reset the Join Probability to " + greetProbab100 + "%");

				break;

			default:
				reply = event.reply("Unknown option")
						.setEphemeral(true);

		}

		return reply;
	}

}
