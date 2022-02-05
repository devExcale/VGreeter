package ovh.excale.vgreeter.commands.slash;

import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;
import org.jetbrains.annotations.NotNull;
import ovh.excale.vgreeter.commands.core.AbstractSlashCommand;
import ovh.excale.vgreeter.commands.core.CommandOptions;
import ovh.excale.vgreeter.track.TrackIndex;

import java.util.Optional;

import static ovh.excale.vgreeter.track.TrackIndex.*;

@Log4j2
public class TrackIndexSlashCommand extends AbstractSlashCommand {

	private static final String KEY_TRACK = "trackname";
	private static final String KEY_USER = "user";

	public TrackIndexSlashCommand() {
		super("trackindex", "List all the tracks");

		getBuilder()
				// [SUB] all
				.subcommand(FILTER_ALL, "Search for all tracks")
				.addOption("page", "Page number", OptionType.INTEGER)
				// [SUB] name
				.subcommand(FILTER_NAME, "Search for all tracks with something in the name")
				.addOptionRequired(KEY_TRACK, "Track name", OptionType.STRING)
				.addOption("page", "Page number", OptionType.INTEGER)
				// [SUB] user
				.subcommand(FILTER_USER, "Search for tracks by a user")
				.addOptionRequired(KEY_USER, "The user to query for", OptionType.USER)
				.addOption("page", "Page number", OptionType.INTEGER);

	}

	@Override
	public @NotNull RestAction<?> execute(SlashCommandEvent event) {

		int page = Optional.ofNullable(event.getOption("page"))
				.map(OptionMapping::getAsLong)
				.map(Long::intValue)
				.orElse(1);

		CommandOptions command = new CommandOptions(event.getName(), event.getSubcommandName()).setPage(page);
		event.getOptions()
				.stream()
				.filter(option -> !"page".equals(option.getName()))
				.forEach(option -> command.putOption(option.getName(), option.getAsString()));

		//noinspection DuplicatedCode
		TrackIndex index = new TrackIndex(command);
		try {

			index.fetch();

		} catch(IllegalArgumentException e) {
			return replyEphemeralWith(e.getMessage(), event);
		} catch(Exception e) {
			log.error(e.getMessage(), e);
			return replyEphemeralWith("There has been an internal error", event);
		}

		if(index.isEmpty())
			return replyEphemeralWith("Empty page", event);

		return event.reply("Here's your track index")
				.setEphemeral(true)
				.and(event.getChannel()
						.sendMessage(index.buildEmbed()
								.build())
						.setActionRow(index.buildButtons()));

	}

	private static ReplyAction replyEphemeralWith(String message, Interaction event) {
		return event.reply(message)
				.setEphemeral(true);
	}

}
