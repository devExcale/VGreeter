package ovh.excale.vgreeter.track;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import ovh.excale.vgreeter.commands.core.CommandDispatcher;
import ovh.excale.vgreeter.commands.misc.CloseEmbedCommand;
import ovh.excale.vgreeter.commands.tracklist.TracklistCommand;
import ovh.excale.vgreeter.entity.MemberEntity;
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

	public Generator with(
		@NotNull Page<TrackEntity> trackPage
	) {
		return new Generator(trackPage, null);
	}

	public Generator with(
		@NotNull Page<TrackEntity> trackPage,
		@Nullable MemberEntity trackOwner
	) {
		return new Generator(trackPage, trackOwner);
	}

	public class Generator {

		private final Page<TrackEntity> trackPage;

		private final MemberEntity trackOwner;

		private final int totalPages;
		private final int humanPageNumber;

		private Generator(Page<TrackEntity> trackPage, MemberEntity trackOwner) {
			this.trackPage = trackPage;
			this.trackOwner = trackOwner;

			this.totalPages = trackPage.getTotalPages();
			this.humanPageNumber = (totalPages > 0) ? trackPage.getNumber() + 1 : 0;
		}

		public String getTitle() {

			String filter;
			if(trackOwner != null)
				filter = "@" + trackOwner.getDiscordUsername();
			else
				filter = "All tracks";


			return "Tracklist: " + filter;
		}

		public String getFooter() {
			return format("Page %d/%d", humanPageNumber, totalPages);
		}

		public @NotNull EmbedBuilder getEmbedBuilder() {

			String description = trackPage.isEmpty()
				? "No tracks found."
				: trackPage.getContent()
					.stream()
					.map(track -> format("**#%d** *%s*", track.getId(), track.getTitle()))
					.collect(Collectors.joining("\n"));

			return new EmbedBuilder()
				.setTitle(getTitle())
				.setFooter(getFooter())
				.setColor(Color.BLUE)
				.setDescription(description);
		}

		public MessageEmbed buildEmbed() {
			return this.getEmbedBuilder()
				.build();
		}

		// TODO: Remove @SneakyThrows
		@SneakyThrows
		public List<Button> buildButtons() {

			// Compute page indices for buttons
			int currZeroPage = trackPage.getNumber();
			int prevZeroPage = trackPage.hasPrevious() ? currZeroPage - 1 : totalPages - 1;
			int nextZeroPage = trackPage.hasNext() ? currZeroPage + 1 : 0;

			if(prevZeroPage == nextZeroPage) {
				prevZeroPage = -1;
				nextZeroPage = -2;
			}

			Long ownerId = (trackOwner != null) ? trackOwner.getDiscordId() : 0L;

			// [Close] Close the embed
			Button btnClose = Button.secondary(
				commandDispatcher.serializeBtnOptions(CloseEmbedCommand.CMD_NAME),
				Emojis.CLOSE
			);

			// [Previous Page] Go to the previous page (circular)
			Button btnPrev = Button.secondary(
				commandDispatcher.serializeBtnOptions(TracklistCommand.BTN_CHANGE_PAGE, prevZeroPage, ownerId),
				Emojis.PREVIOUS
			);

			// [Next Page] Go to the next page (circular)
			Button btnNext = Button.secondary(
				commandDispatcher.serializeBtnOptions(TracklistCommand.BTN_CHANGE_PAGE, nextZeroPage, ownerId),
				Emojis.NEXT
			);

			if (totalPages <= 1) {
				btnPrev = btnPrev.asDisabled();
				btnNext = btnNext.asDisabled();
			}

			return List.of(btnClose, btnPrev, btnNext);
		}

	}

}
