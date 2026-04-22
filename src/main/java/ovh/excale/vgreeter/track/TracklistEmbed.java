package ovh.excale.vgreeter.track;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import ovh.excale.vgreeter.commands.button.CloseEmbedCommand;
import ovh.excale.vgreeter.commands.core.CommandDispatcher;
import ovh.excale.vgreeter.commands.core.CommandOptions;
import ovh.excale.vgreeter.entity.TrackEntity;
import ovh.excale.vgreeter.utilities.Emojis;

import java.awt.*;
import java.util.stream.Collectors;

import static java.lang.String.format;

@RequiredArgsConstructor
public class TracklistEmbed {

	public static final String CMD_FILTER_ALL = "all";

	public static final String CMD_FILTER_NAME = "name";

	public static final String CMD_FILTER_USER = "user";

	public static final int DEFAULT_PAGE_SIZE = 15;

	private final Page<TrackEntity> trackPage;

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

		// <previous> button
		Button prevButton = Button.secondary("<previous>", Emojis.PREVIOUS)
			.withDisabled(true);

		// <next> button
		Button nextButton = Button.secondary("<next>", Emojis.NEXT)
			.withDisabled(true);

		// <reload> button
		Button reloadButton = Button.secondary("<reload>", Emojis.RELOAD)
			.withDisabled(true);

		Button closeButton = Button.secondary(
			CommandDispatcher.encodeButtonId(CloseEmbedCommand.CMD_NAME),
			Emojis.CLOSE
		);

		return new Button[] { prevButton, nextButton, reloadButton, closeButton };

	}

}
