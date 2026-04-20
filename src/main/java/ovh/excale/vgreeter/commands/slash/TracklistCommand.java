package ovh.excale.vgreeter.commands.slash;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import ovh.excale.vgreeter.commands.core.CommandOptions;
import ovh.excale.vgreeter.commands.core.annotation.CommandController;
import ovh.excale.vgreeter.commands.core.annotation.SlashMapping;
import ovh.excale.vgreeter.message.ErrorMessages;
import ovh.excale.vgreeter.track.TrackIndex;
import ovh.excale.vgreeter.services.LogErrorService;

import java.util.Arrays;
import java.util.Optional;

import static ovh.excale.vgreeter.commands.slash.TracklistCommand.TRACKLIST;
import static ovh.excale.vgreeter.track.TrackIndex.*;
import static ovh.excale.vgreeter.utilities.DiscordUtil.replyEphemeralWith;

@RequiredArgsConstructor
@Log4j2
@CommandController(
	commandName = TRACKLIST,
	commandDescription = "List all the tracks"
)
public class TracklistCommand {

	protected static final String TRACKLIST = "tracklist";

	private static final String PAGE_LABEL = "Page number";

	private final LogErrorService logErrorService;

	private final ErrorMessages msgError;

	@SlashMapping(
		name = TRACKLIST,
		subcommand = CMD_FILTER_ALL,
		description = "Search for all tracks"
	)
	public RestAction<?> searchAll(SlashCommandInteractionEvent event) {
		return replyEphemeralWith("Not implemented yet", event);
	}

	@SlashMapping(
		name = TRACKLIST,
		subcommand = CMD_FILTER_NAME,
		description = "Search for all tracks with something in the name"
	)
	public RestAction<?> searchByName(SlashCommandInteractionEvent event) {
		return replyEphemeralWith("Not implemented yet", event);
	}

	@SlashMapping(
		name = TRACKLIST,
		subcommand = CMD_FILTER_USER,
		description = "Search for tracks by a user"
	)
	public RestAction<?> searchByUser(SlashCommandInteractionEvent event) {
		return replyEphemeralWith("Not implemented yet", event);
	}

	public @NotNull RestAction<?> execute(SlashCommandInteractionEvent event) {

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
			logErrorService.error(e);
			log.error(e.getMessage(), e);
			return replyEphemeralWith("There has been an internal error", event);
		}

		if(index.isEmpty())
			return replyEphemeralWith("Empty page", event);

		return event.replyEmbeds(index.buildEmbed().build())
				.setEphemeral(true)
				.addComponents(ActionRow.of(Arrays.asList(index.buildButtons())));

	}

}
