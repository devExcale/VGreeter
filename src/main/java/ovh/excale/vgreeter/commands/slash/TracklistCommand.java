package ovh.excale.vgreeter.commands.slash;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.springframework.data.domain.Pageable;
import ovh.excale.vgreeter.commands.core.annotation.*;
import ovh.excale.vgreeter.message.ErrorMessages;
import ovh.excale.vgreeter.repository.TrackRepository;
import ovh.excale.vgreeter.services.LogErrorService;
import ovh.excale.vgreeter.track.TracklistEmbed;

import java.util.Arrays;
import java.util.Objects;

import static ovh.excale.vgreeter.commands.slash.TracklistCommand.TRACKLIST;
import static ovh.excale.vgreeter.track.TracklistEmbed.*;
import static ovh.excale.vgreeter.utilities.DiscordUtil.replyEphemeralWith;

@RequiredArgsConstructor
@Log4j2
@CommandController(
	name = TRACKLIST,
	description = "List all the tracks"
)
public class TracklistCommand {

	protected static final String TRACKLIST = "tracklist";

	private static final String PAGE_LABEL = "Page number";

	private final LogErrorService logErrorService;

	private final ErrorMessages msgError;

	private final TrackRepository trackRepo;

	@SlashMapping(
		name = CMD_FILTER_ALL,
		description = "Search for all tracks"
	)
	public RestAction<?> searchAll(
		SlashCommandInteractionEvent event,
		@CmdOption(name = "page", description = PAGE_LABEL, required = false) Long humanPage
	) {

		// Get tracklist page
		TracklistEmbed tracklistEmbed = new TracklistEmbed(trackRepo.findAll(
			Pageable.ofSize(DEFAULT_PAGE_SIZE)
				.withPage(humanPage == null ? 0 : humanPage.intValue() - 1)
		));

		return event.replyEmbeds(tracklistEmbed.buildEmbed().build())
			.addComponents(ActionRow.of(Arrays.asList(tracklistEmbed.buildButtons())))
			.setEphemeral(true);
	}

	@SlashMapping(
		name = CMD_FILTER_NAME,
		description = "Search for all tracks with something in the name"
	)
	public RestAction<?> searchByName(SlashCommandInteractionEvent event) {
		return replyEphemeralWith("Not implemented yet", event);
	}

	@SlashMapping(
		name = CMD_FILTER_USER,
		description = "Search for tracks by a user"
	)
	public RestAction<?> searchByUser(SlashCommandInteractionEvent event) {
		return replyEphemeralWith("Not implemented yet", event);
	}

	@ButtonMapping(name = "TracklistChangePage")
	public RestAction<?> changePage(
		ButtonInteractionEvent event,
		@BtnOption Long indexPage
	) {

		Objects.requireNonNull(indexPage);

		// Get new tracklist page
		TracklistEmbed tracklistEmbed = new TracklistEmbed(trackRepo.findAll(
			Pageable.ofSize(DEFAULT_PAGE_SIZE)
				.withPage(indexPage.intValue() - 1)
		));

		return event.editMessageEmbeds(tracklistEmbed.buildEmbed().build())
			.setComponents(ActionRow.of(Arrays.asList(tracklistEmbed.buildButtons())));
	}

}
