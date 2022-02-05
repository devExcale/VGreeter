package ovh.excale.vgreeter.track;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.Component;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ovh.excale.vgreeter.VGreeterApplication;
import ovh.excale.vgreeter.commands.core.CommandOptions;
import ovh.excale.vgreeter.models.TrackModel;
import ovh.excale.vgreeter.repositories.TrackRepository;
import ovh.excale.vgreeter.utilities.Emojis;

import java.awt.*;
import java.util.Objects;
import java.util.stream.Collectors;

import static ovh.excale.vgreeter.commands.core.CommandKeyword.TRACK_NAME;
import static ovh.excale.vgreeter.commands.core.CommandKeyword.USER_ID;

public class TrackIndex {

	public static final String FILTER_ALL = "all";
	public static final String FILTER_NAME = "name";
	public static final String FILTER_USER = "user";
	public static final int DEFAULT_PAGE_SIZE = 15;

	private final CommandOptions options;
	private final TrackRepository trackRepo;
	private Page<TrackModel> trackPage;

	@Setter
	@Getter
	private Color embedColor;
	private String filterContent;

	@Setter
	@Getter
	private int pageSize;

	public TrackIndex(CommandOptions options) {
		this.options = Objects.requireNonNull(options);
		trackRepo = VGreeterApplication.getApplicationContext()
				.getBean(TrackRepository.class);

		trackPage = null;
		filterContent = null;
		embedColor = Color.BLUE;
		pageSize = DEFAULT_PAGE_SIZE;

	}

	private void trackPageCheck() throws IllegalStateException {
		if(trackPage == null)
			throw new IllegalStateException();
	}

	public boolean isEmpty() {
		return trackPage == null || !trackPage.hasContent();
	}

	public void fetch() throws IllegalArgumentException {

		int humanBasedPage = options.getPageSafe();
		if(humanBasedPage < 1)
			throw new IllegalArgumentException("Page option must be positive");

		Sort sorting = Sort.by(Sort.Direction.ASC, "id");
		String filter = options.hasSubcommand() ? options.getSubcommand() : FILTER_ALL;

		switch(filter) {

			case FILTER_ALL:

				trackPage = trackRepo.findAll(PageRequest.of(humanBasedPage - 1, pageSize, sorting));

				break;

			case FILTER_NAME:

				String trackName = options.getOption(TRACK_NAME.ext)
						.orElseThrow(() -> new IllegalArgumentException("Missing parameter " + TRACK_NAME.ext));
				filterContent = trackName;

				String formattedTrackName = "%" + trackName.replaceAll("\\s+", "%") + "%";
				trackPage = trackRepo.findAllByNameQuery(formattedTrackName,
						PageRequest.of(humanBasedPage - 1, pageSize, sorting));

				break;

			case FILTER_USER:

				long userId = options.getOption(USER_ID.ext)
						.map(Long::valueOf)
						.orElseThrow(() -> new IllegalArgumentException("Missing parameter " + USER_ID.ext));

				filterContent = String.format("<@%d>", userId);
				trackPage = trackRepo.findAllByUploaderIdIs(userId,
						PageRequest.of(humanBasedPage - 1, pageSize, sorting));

				break;

			default:
				throw new IllegalArgumentException("Unknown filter " + filter);

		}

	}

	public @NotNull EmbedBuilder buildEmbed() {

		trackPageCheck();

		String filterOut = (filterContent != null) ? "\n\n| _**Filter:** " + filterContent + "_" : "";

		return new EmbedBuilder()
				.setTitle("TrackIndex")
				.setFooter("Page " + (trackPage.getNumber() + 1) + "/" + trackPage.getTotalPages())
				.setColor(embedColor)
				.setDescription(trackPage.getContent()
						.stream()
						.map(track -> "**#" + track.getId() + "** *" + track.getName() + "*")
						.collect(Collectors.joining("\n"))
						.concat(filterOut));

	}

	@SneakyThrows
	public Component[] buildButtons() {

		trackPageCheck();

		int zeroBasedPage = trackPage.getNumber();
		boolean enablePageChange = trackPage.getTotalPages() != 1;

		// <previous> button
		options.setPage(trackPage.hasPrevious() ? zeroBasedPage : trackPage.getTotalPages());
		Button prevButton = Button.secondary(enablePageChange ? options.json() : "{\"_\":0}", Emojis.PREVIOUS)
				.withDisabled(!enablePageChange);

		System.out.println(options.json());

		// <next> button
		options.setPage(!trackPage.isLast() ? zeroBasedPage + 2 : 0);
		Button nextButton = Button.secondary(enablePageChange ? options.json() : "{\"_\":1}", Emojis.NEXT);

		System.out.println(options.json());

		// <reload> button
		options.setPage(zeroBasedPage + 1);
		Button reloadButton = Button.secondary(options.json(), Emojis.RELOAD);

		System.out.println(options.json());

		CommandOptions closeCommand = new CommandOptions("close");
		Button closeButton = Button.secondary(closeCommand.json(), Emojis.CLOSE);

		return new Component[] { prevButton, nextButton, reloadButton, closeButton };

	}

}
