package ovh.excale.vgreeter.track;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import ovh.excale.vgreeter.VGreeterApplication;
import ovh.excale.vgreeter.commands.button.CloseEmbedCommand;
import ovh.excale.vgreeter.commands.core.CommandDispatcher;
import ovh.excale.vgreeter.commands.slash.TracklistCommand;
import ovh.excale.vgreeter.entity.TrackEntity;
import ovh.excale.vgreeter.utilities.Emojis;

import java.awt.*;
import java.util.stream.Collectors;

import static java.lang.String.format;

@RequiredArgsConstructor
public class TracklistEmbed {

	public static final int DEFAULT_PAGE_SIZE = 15;

	private final Page<TrackEntity> trackPage;

	private final CommandDispatcher commandDispatcher = VGreeterApplication.getApplicationContext()
		.getBean(CommandDispatcher.class);

	public @NotNull EmbedBuilder buildEmbed() {

		int humanPageNumber = trackPage.getNumber() + 1;

		return new EmbedBuilder()
			.setTitle("Tracklist")
			.setFooter(format("Page %d/%d", humanPageNumber, trackPage.getTotalPages()))
			.setColor(Color.BLUE)
			.setDescription(
				trackPage.getContent()
					.stream()
					.map(track -> format("**#%d** *%s*", track.getId(), track.getTitle()))
					.collect(Collectors.joining("\n"))
			);
	}

	@SneakyThrows
	public Button[] buildButtons() {

		// Get previous page (or circle back)
		int prevZeroPage = trackPage.hasPrevious() ? trackPage.getNumber() - 1 : trackPage.getTotalPages() - 1;
		Button prevButton = Button.secondary(
			commandDispatcher.serializeBtnOptions(TracklistCommand.BTN_CHANGE_PAGE, prevZeroPage),
			Emojis.PREVIOUS
		);

		// Set next page (or circle back)
		int nextZeroPage = trackPage.hasNext() ? trackPage.getNumber() + 1 : 0;
		Button nextButton = Button.secondary(
			commandDispatcher.serializeBtnOptions(TracklistCommand.BTN_CHANGE_PAGE, nextZeroPage),
			Emojis.NEXT
		);

		// Reload page
		Button reloadButton = Button.secondary(
			commandDispatcher.serializeBtnOptions(TracklistCommand.BTN_CHANGE_PAGE, trackPage.getNumber()),
			Emojis.RELOAD
		);

		// Close embed
		Button closeButton = Button.secondary(
			commandDispatcher.serializeBtnOptions(CloseEmbedCommand.CMD_NAME),
			Emojis.CLOSE
		);

		// Disable previous and next buttons if there's only one page
		if (trackPage.getTotalPages() <= 1) {
			prevButton = prevButton.asDisabled();
			nextButton = nextButton.asDisabled();
		}

		return new Button[] { prevButton, nextButton, reloadButton, closeButton };
	}

}
