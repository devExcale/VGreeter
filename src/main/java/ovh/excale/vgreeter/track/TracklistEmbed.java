package ovh.excale.vgreeter.track;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import ovh.excale.vgreeter.commands.core.CommandDispatcher;
import ovh.excale.vgreeter.commands.misc.CloseEmbedCommand;
import ovh.excale.vgreeter.commands.tracklist.TracklistCommand;
import ovh.excale.vgreeter.entity.TrackEntity;
import ovh.excale.vgreeter.utilities.Emojis;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

@RequiredArgsConstructor
@Component
public class TracklistEmbed {

	private final CommandDispatcher commandDispatcher;

	public @NotNull EmbedBuilder buildEmbed(@NotNull Page<TrackEntity> trackPage) {

		int totalPages = trackPage.getTotalPages();
		int humanPageNumber = (totalPages > 0) ? trackPage.getNumber() + 1 : 0;

		String description = trackPage.isEmpty()
			? "No tracks found."
			: trackPage.getContent()
				.stream()
				.map(track -> format("**#%d** *%s*", track.getId(), track.getTitle()))
				.collect(Collectors.joining("\n"));

		return new EmbedBuilder()
			.setTitle("Tracklist")
			.setFooter(format("Page %d/%d", humanPageNumber, trackPage.getTotalPages()))
			.setColor(Color.BLUE)
			.setDescription(description);
	}

	// TODO: Remove @SneakyThrows
	@SneakyThrows
	public List<Button> buildButtons(@NotNull Page<TrackEntity> trackPage) {

		// Compute page indices for buttons
		int totalPages = trackPage.getTotalPages();
		int currZeroPage = trackPage.getNumber();
		int prevZeroPage = trackPage.hasPrevious() ? currZeroPage - 1 : totalPages - 1;
		int nextZeroPage = trackPage.hasNext() ? currZeroPage + 1 : 0;

		// [Close] Close the embed
		Button btnClose = Button.secondary(
			commandDispatcher.serializeBtnOptions(CloseEmbedCommand.CMD_NAME),
			Emojis.CLOSE
		);

		// [Previous Page] Go to the previous page (circular)
		Button btnPrev = Button.secondary(
			commandDispatcher.serializeBtnOptions(TracklistCommand.BTN_CHANGE_PAGE, prevZeroPage),
			Emojis.PREVIOUS
		);

		// [Next Page] Go to the next page (circular)
		Button btnNext = Button.secondary(
			commandDispatcher.serializeBtnOptions(TracklistCommand.BTN_CHANGE_PAGE, nextZeroPage),
			Emojis.NEXT
		);

		if (totalPages <= 1) {
			btnPrev = btnPrev.asDisabled();
			btnNext = btnNext.asDisabled();
		}

		return List.of(btnClose, btnPrev, btnNext);
	}

}
