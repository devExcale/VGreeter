package ovh.excale.vgreeter.commands.track;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ovh.excale.vgreeter.VGreeterApplication;
import ovh.excale.vgreeter.commands.core.AbstractSlashCommand;
import ovh.excale.vgreeter.models.TrackModel;
import ovh.excale.vgreeter.repositories.TrackRepository;
import ovh.excale.vgreeter.utilities.Emojis;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
public class TrackIndexCommand extends AbstractSlashCommand {

	public static final String SUBCOMMAND_ALL = "all";
	public static final String SUBCOMMAND_NAME = "name";
	public static final String SUBCOMMAND_USER = "user";

	private final TrackRepository trackRepo;
	private final ButtonClickListener buttonListener;

	public TrackIndexCommand() {
		super("trackindex", "List all the tracks");
		// TODO:
		getBuilder()
				// [SUB] all
				.subcommand(SUBCOMMAND_ALL, "Search for all tracks")
				.addOption("page", "Page number", OptionType.INTEGER)
				// [SUB] name
				.subcommand(SUBCOMMAND_NAME, "Search for all tracks with something in the name")
				.addOptionRequired("name", "Track name", OptionType.STRING)
				.addOption("page", "Page number", OptionType.INTEGER)
				// [SUB] user
				.subcommand(SUBCOMMAND_USER, "Search for tracks by a user")
				.addOptionRequired("user", "The user to query for", OptionType.USER)
				.addOption("page", "Page number", OptionType.INTEGER);

		trackRepo = VGreeterApplication
				.getApplicationContext()
				.getBean(TrackRepository.class);

		buttonListener = new ButtonClickListener();

	}

	@Override
	public @NotNull RestAction<?> execute(SlashCommandEvent event) {

		int humanBasedPage = Optional
				.ofNullable(event.getOption("page"))
				.map(OptionMapping::getAsLong)
				.map(Long::intValue)
				.orElse(1);

		if(humanBasedPage < 1)
			return event
					.reply("Page option must be positive!")
					.setEphemeral(true);

		Page<TrackModel> trackPage;
		Map<String, String> options = new HashMap<>();

		String titleAppendix = null;
		String footerAppendix = null;
		String subcommand = event.getSubcommandName();
		options.put("command", getName());
		options.put("subcommand", subcommand);

		//noinspection ConstantConditions
		switch(subcommand) {

			case SUBCOMMAND_ALL:

				trackPage = trackRepo.findAll(
						PageRequest.of(humanBasedPage - 1, 15, Sort.by(Sort.Direction.ASC, "id")));

				break;

			case SUBCOMMAND_NAME:


				//noinspection ConstantConditions
				String trackName = event
						.getOption("name")
						.getAsString();

				String formattedTrackName = "%" + trackName.replaceAll("\\s+", "%") + "%";
				titleAppendix = "Name";
				footerAppendix = "Filter: \"" + trackName + "\"";

				options.put("track_name", trackName);
				trackPage = trackRepo.findAllByNameQuery(formattedTrackName,
						PageRequest.of(humanBasedPage - 1, 15, Sort.by(Sort.Direction.ASC, "id")));

				break;

			case SUBCOMMAND_USER:

				//noinspection ConstantConditions
				long userId = event
						.getOption("user")
						.getAsUser()
						.getIdLong();

				String userTag = event.getJDA()
						.retrieveUserById(userId)
						.complete()
						.getAsTag();

				titleAppendix = "User";
				footerAppendix = "Filter: " + userTag;

				options.put("user_id", Long.toString(userId));
				trackPage = trackRepo.findAllByUploaderIdIs(userId,
						PageRequest.of(humanBasedPage - 1, 15, Sort.by(Sort.Direction.ASC, "id")));

				break;

			default:
				return event
						.reply("Unknown option `" + event.getSubcommandName() + "`")
						.setEphemeral(true);

		}

		if(trackPage.isEmpty())
			return event
					.reply("Empty page")
					.setEphemeral(true);

		EmbedBuilder eb = computeEmbed(trackPage, titleAppendix, footerAppendix);

		// TODO: CHECK PERMS AND MESSAGECHANNEL
		return event.reply("Here's your track index!")
				.setEphemeral(true)
				.and(event.getChannel()
						.sendMessage(eb.build())
						.setActionRow(computeButtons(trackPage, options)));

	}

	@Override
	public boolean hasListener() {
		return true;
	}

	@Override
	public @Nullable EventListener getListener() {
		return buttonListener;
	}

	private @NotNull EmbedBuilder computeEmbed(Page<TrackModel> trackPage, @Nullable String titleAppendix, @Nullable String footerAppendix) {

		String pageCount = "Page " + (trackPage.getNumber() + 1) + "/" + trackPage.getTotalPages();
		String footer = footerAppendix == null ? pageCount : pageCount + "  |  " + footerAppendix;
		String title = titleAppendix == null ? "Track Index" : "Track Index (" + titleAppendix + ")";

		return new EmbedBuilder()
				.setTitle(title)
				.setFooter(footer)
				.setColor(Color.BLUE)
				.setDescription(trackPage
						.getContent()
						.stream()
						.map(track -> "**#" + track.getId() + "** *" + track.getName() + "*")
						.collect(Collectors.joining("\n")));

	}

	@SneakyThrows
	private Component[] computeButtons(Page<TrackModel> trackPage, Map<String, String> options) {

		ObjectMapper json = new ObjectMapper();
		int zeroBasedPage = trackPage.getNumber();

		// TODO: disable prev/next buttons on 1 page

		// <previous> button
		options.put("page", Integer.toString(trackPage.hasPrevious() ? zeroBasedPage - 1 : trackPage.getTotalPages() - 1));
		options.put("emoji", Emojis.PREVIOUS.getName());
		Button prevButton = Button.secondary(json.writeValueAsString(options), Emojis.PREVIOUS);

		// <next> button
		options.put("page", Integer.toString(!trackPage.isLast() ? zeroBasedPage + 1 : 0));
		options.put("emoji", Emojis.NEXT.getName());
		Button nextButton = Button.secondary(json.writeValueAsString(options), Emojis.NEXT);

		// <reload> button
		options.put("page", Integer.toString(zeroBasedPage));
		options.put("emoji", Emojis.RELOAD.getName());
		Button reloadButton = Button.secondary(json.writeValueAsString(options), Emojis.RELOAD);

		Map<String, String> closeCommand = new HashMap<>();
		closeCommand.put("command", "close");
		closeCommand.put("emoji", Emojis.CLOSE.getName());
		Button closeButton = Button.secondary(json.writeValueAsString(closeCommand), Emojis.CLOSE);

		return new Component[] { prevButton, nextButton, reloadButton, closeButton };

	}

	private static void replyEphemeralWith(String message, Interaction event) {
		event
				.reply(message)
				.setEphemeral(true)
				.queue();
	}

	public class ButtonClickListener extends ListenerAdapter {

		@SneakyThrows
		@Override
		public void onButtonClick(@NotNull ButtonClickEvent event) {

			ObjectReader jsonParser = new ObjectMapper()
					.readerForMapOf(String.class);
			String rawJson = event.getComponentId();

			Map<String, String> commandOptions = jsonParser.readValue(rawJson);
			String commandName = commandOptions.get("command");

			if(commandName == null) {
				replyEphemeralWith("Invalid button", event);
				log.debug("Invalid button (commandName) -> " + rawJson);
				return;
			}

			// TODO: GENERIC CLOSE "BUTTON COMMAND" (listener)
			if(commandName.equalsIgnoreCase("close")) {
				//noinspection ConstantConditions
				event
						.getMessage()
						.delete()
						.queue();
				return;
			}

			// TODO: AUTOMATIC "BUTTON COMMAND" FORWARDING
			if(!commandName.equalsIgnoreCase(getName()))
				return;

			String pageString = commandOptions.get("page");
			try {

				if(Integer.parseInt(pageString) < 0)
					throw new NullPointerException();

			} catch(NumberFormatException | NullPointerException e) {
				replyEphemeralWith("Invalid button", event);
				log.debug("Invalid button (page) -> " + rawJson);
				return;
			}

			Page<TrackModel> trackPage;
			int zeroBasedPage = Integer.parseInt(pageString);
			String subcommand = commandOptions.get("subcommand");

			if(subcommand == null) {
				replyEphemeralWith("Invalid button", event);
				log.debug("Invalid button (subcommand) -> " + rawJson);
				return;
			}

			String titleAppendix = null;
			String footerAppendix = null;

			switch(subcommand) {

				case SUBCOMMAND_ALL:

					trackPage = trackRepo.findAll(PageRequest.of(zeroBasedPage, 15, Sort.by(Sort.Direction.ASC, "id")));

					break;

				case SUBCOMMAND_NAME:

					String trackName = commandOptions.get("track_name");
					if(trackName == null) {
						replyEphemeralWith("Invalid button", event);
						log.debug("Invalid button (trackName) -> " + rawJson);
						return;
					}


					String formattedTrackName = "%" + trackName.replaceAll("\\s+", "%") + "%";
					titleAppendix = "Name";
					footerAppendix = "Filter: \"" + trackName + "\"";

					trackPage = trackRepo.findAllByNameQuery(formattedTrackName,
							PageRequest.of(zeroBasedPage, 15, Sort.by(Sort.Direction.ASC, "id")));

					break;

				case SUBCOMMAND_USER:

					long userId;
					try {

						userId = Long.parseLong(commandOptions.get("user_id"));

					} catch(NumberFormatException | NullPointerException e) {
						replyEphemeralWith("Invalid button", event);
						log.debug("Invalid button (userId) -> " + rawJson);
						return;
					}

					String userTag = event.getJDA()
							.retrieveUserById(userId)
							.complete()
							.getAsTag();

					titleAppendix = "User";
					footerAppendix = "Filter: " + userTag;

					trackPage = trackRepo.findAllByUploaderIdIs(userId,
							PageRequest.of(zeroBasedPage, 15, Sort.by(Sort.Direction.ASC, "id")));

					break;

				default:
					replyEphemeralWith("Unknown option `" + subcommand + "`", event);
					return;

			}

			if(trackPage.isEmpty()) {

				replyEphemeralWith("Empty page", event);
				return;

			}

			EmbedBuilder eb = computeEmbed(trackPage, titleAppendix, footerAppendix);

			//noinspection ConstantConditions
			event
					.getMessage()
					.delete()
					.and(event
							.getChannel()
							.sendMessage(eb.build())
							.setActionRow(computeButtons(trackPage, commandOptions)))
					.queue();

		}

	}

}
