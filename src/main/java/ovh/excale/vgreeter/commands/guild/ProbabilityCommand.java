package ovh.excale.vgreeter.commands.guild;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.jetbrains.annotations.NotNull;
import ovh.excale.vgreeter.commands.core.annotation.CmdOption;
import ovh.excale.vgreeter.commands.core.annotation.CommandController;
import ovh.excale.vgreeter.commands.core.annotation.SlashMapping;
import ovh.excale.vgreeter.entity.GuildEntity;
import ovh.excale.vgreeter.message.ErrorMessages;
import ovh.excale.vgreeter.message.ProbabilityMessages;
import ovh.excale.vgreeter.repository.GuildRepository;

import java.util.Objects;

import static ovh.excale.vgreeter.utilities.DiscordUtil.replyEphemeralWith;

@SuppressWarnings("DuplicatedCode")
@RequiredArgsConstructor
@CommandController(
	name = "probab",
	description = "Manage the Voice Chat Greet Probability"
)
public class ProbabilityCommand {

	private final GuildRepository guildRepo;

	private final ErrorMessages msgError;

	private final ProbabilityMessages msgProbab;

	@SlashMapping(
		name = "set",
		description = "Set the new Greet Probability"
	)
	public @NotNull ReplyCallbackAction probabSet(
		SlashCommandInteractionEvent event,
		@CmdOption(
			name = "percent", description = "Join Probability (0 to 100)",
			minValueD = 0, maxValueD = 100
		) Double newGreetProbab100
	) {

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

		// Check if the user has administrator permissions
		if(!member.hasPermission(Permission.ADMINISTRATOR))
			return replyEphemeralWith(msgError.getCmdNeedAdminPerms(), event);

		// Check if the new probability is between 0 and 100%
		if(newGreetProbab100 < 0f || newGreetProbab100 > 100f)
			return replyEphemeralWith(msgProbab.getErrorOutOfBounds(), event);

		// Update the probability (0-1 based)
		guildEntity.setGreetProbab(newGreetProbab100.floatValue() / 100f);
		guildRepo.save(guildEntity);

		return event.reply(msgProbab.getSetFromTo(greetProbab100, newGreetProbab100.floatValue()));
	}

	@SlashMapping(
		name = "get",
		description = "Set the new Greet Probability"
	)
	public @NotNull ReplyCallbackAction probabGet(
		SlashCommandInteractionEvent event
	) {

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
		float greetProbab100 = guildEntity.getGreetProbab() * 100f;

		return event.reply(msgProbab.getCurrentlySet(greetProbab100));
	}

	@SlashMapping(
		name = "default",
		description = "Set the new Greet Probability"
	)
	public @NotNull ReplyCallbackAction probabDefault(
		SlashCommandInteractionEvent event
	) {

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

		// Check if the user has administrator permissions
		if(!member.hasPermission(Permission.ADMINISTRATOR))
			return replyEphemeralWith(msgError.getCmdNeedAdminPerms(), event);

		// Update the probability (with default value(
		guildEntity.setGreetProbab(GuildEntity.DEFAULT_GREET_PROBAB);
		guildRepo.save(guildEntity);

		return event.reply(msgProbab.getResetDefault(guildEntity.getGreetProbab() * 100f));
	}

}
