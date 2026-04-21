package ovh.excale.vgreeter.commands.slash;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Pageable;
import ovh.excale.vgreeter.commands.core.CommandOptions;
import ovh.excale.vgreeter.commands.core.annotation.CommandController;
import ovh.excale.vgreeter.commands.core.annotation.Option;
import ovh.excale.vgreeter.commands.core.annotation.SlashMapping;
import ovh.excale.vgreeter.message.ErrorMessages;
import ovh.excale.vgreeter.repository.TrackRepository;
import ovh.excale.vgreeter.track.TracklistEmbed;
import ovh.excale.vgreeter.services.LogErrorService;

import java.util.Arrays;
import java.util.Optional;

import static ovh.excale.vgreeter.commands.slash.TracklistCommand.TRACKLIST;
import static ovh.excale.vgreeter.track.TracklistEmbed.*;
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

	private final TrackRepository trackRepo;

	@SlashMapping(
		name = TRACKLIST,
		subcommand = CMD_FILTER_ALL,
		description = "Search for all tracks"
	)
	public RestAction<?> searchAll(
		SlashCommandInteractionEvent event,
		@Option(name = "page", description = PAGE_LABEL, required = false) Long page
	) {

		// Get tracklist page
		TracklistEmbed tracklistEmbed = new TracklistEmbed(trackRepo.findAll(
			Pageable.ofSize(DEFAULT_PAGE_SIZE)
				.withPage(page == null ? 0 : page.intValue() - 1)
		));

		return event.replyEmbeds(tracklistEmbed.buildEmbed().build())
			.addComponents(ActionRow.of(Arrays.asList(tracklistEmbed.buildButtons())))
			.setEphemeral(true);
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

}
