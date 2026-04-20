package ovh.excale.vgreeter.commands.slash;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.RestAction;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import ovh.excale.vgreeter.commands.core.AbstractSlashCommand;
import ovh.excale.vgreeter.entity.GuildEntity;
import ovh.excale.vgreeter.message.ErrorMessages;
import ovh.excale.vgreeter.message.ProbabilityMessages;
import ovh.excale.vgreeter.repository.GuildRepository;

import java.util.Objects;
import java.util.Optional;

import static ovh.excale.vgreeter.utilities.DiscordUtil.replyEphemeralWith;

@Component
public class ProbabilityCommand extends AbstractSlashCommand {

	private final GuildRepository guildRepo;

	private final ErrorMessages msgError;

	private final ProbabilityMessages msgProbab;

	public ProbabilityCommand(
		GuildRepository guildRepo,
		ErrorMessages msgError,
		ProbabilityMessages msgProbab
	) {
		super("probab", "Manage the Voice Chat Join Probability");

		this.guildRepo = guildRepo;
		this.msgError = msgError;
		this.msgProbab = msgProbab;

		this.getBuilder()
				.subcommand("set", "Set the new Join Probability")
				.addOptionRequired("percent", "Join Probability (0 to 100)", OptionType.NUMBER)
				.subcommand("get", "Get the current Join Probability")
				.subcommand("default", "Reset the Join Probability to its default");

	}

	@Override
	public @NonNull RestAction<?> execute(SlashCommandInteractionEvent event) {

		Guild guild = event.getGuild();
		Member member = event.getMember();

		// Check if the command is used in a guild
		if(guild == null)
			return replyEphemeralWith(msgError.getCmdGuildOnly(), event);

		// Member is never null if guild is set
		Objects.requireNonNull(member);

		// Fetch guild settings (or create if don't exist)
		GuildEntity guildEntity = guildRepo.findByIdOrSave(
			guild.getIdLong(),
			() -> GuildEntity.builder()
				.discordId(guild.getIdLong())
				.name(guild.getName())
				.build()
		);

		// Get the current probability and convert it to percentage for display
		float greetProbab100 = guildEntity.getGreetProbab() * 100;
		RestAction<?> reply;

		String subcommand = event.getSubcommandName();
		if(subcommand == null)
			subcommand = "";

		switch(subcommand) {

			case "set":

				// Check if the user has administrator permissions
				if(!member.hasPermission(Permission.ADMINISTRATOR))
					return replyEphemeralWith(msgError.getCmdNeedAdminPerms(), event);

				// Get the new probability from the command options and parse it
				float newGreetProbab100 = Optional.of(Objects.requireNonNull(event.getOption("percent")))
					.map(OptionMapping::getAsString)
					.map(Float::parseFloat)
					.get();

				// Check if the new probability is between 0 and 100%
				if(newGreetProbab100 < 0f || newGreetProbab100 > 100f)
					return replyEphemeralWith(msgProbab.getErrorOutOfBounds(), event);

				// Update the probability (0-1 based)
				guildEntity.setGreetProbab(newGreetProbab100 / 100f);
				guildRepo.save(guildEntity);

				reply = event.reply(msgProbab.getSetFromTo(greetProbab100, newGreetProbab100));

				break;

			case "get":

				reply = event.reply(msgProbab.getCurrentlySet(greetProbab100));

				break;

			case "default":

				// Check if the user has administrator permissions
				if(!member.hasPermission(Permission.ADMINISTRATOR))
					return replyEphemeralWith(msgError.getCmdNeedAdminPerms(), event);

				// Update the probability (with default value(
				guildEntity.setGreetProbab(GuildEntity.DEFAULT_GREET_PROBAB);
				guildRepo.save(guildEntity);

				reply = event.reply(msgProbab.getResetDefault(guildEntity.getGreetProbab() * 100));

				break;

			default:
				reply = replyEphemeralWith(msgError.getUnknownOption(subcommand), event);

		}

		return reply;
	}

}
