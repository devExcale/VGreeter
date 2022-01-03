package ovh.excale.vgreeter.commands.track;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ovh.excale.vgreeter.VGreeterApplication;
import ovh.excale.vgreeter.commands.core.AbstractSlashCommand;
import ovh.excale.vgreeter.models.TrackModel;
import ovh.excale.vgreeter.repositories.TrackRepository;
import ovh.excale.vgreeter.utilities.ArgumentsParser;
import ovh.excale.vgreeter.utilities.Emojis;

import java.awt.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TrackIndexCommand extends AbstractSlashCommand {

	public static final String BUTTON_COMMAND = "trackindex";

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
	public ReplyAction execute(SlashCommandEvent event) {

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
		String subcommand = event.getSubcommandName();

		//noinspection ConstantConditions
		switch(subcommand) {

			case SUBCOMMAND_ALL:
				trackPage = trackRepo.findAll(
						PageRequest.of(humanBasedPage - 1, 15, Sort.by(Sort.Direction.ASC, "id")));
				break;

			case SUBCOMMAND_NAME:
				//noinspection ConstantConditions
				String optionName = event
						.getOption("name")
						.getAsString();
				trackPage = trackRepo.findAllByNameLike(optionName,
						PageRequest.of(humanBasedPage - 1, 15, Sort.by(Sort.Direction.ASC, "id")));
				break;

			case SUBCOMMAND_USER:
				//noinspection ConstantConditions
				User optionUser = event
						.getOption("user")
						.getAsUser();
				trackPage = trackRepo.findAllByUploaderIdIs(optionUser.getIdLong(),
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

		EmbedBuilder eb = computeEmbed(trackPage);

		// TODO: CHECK PERMS AND MESSAGECHANNEL
		event
				.getChannel()
				.sendMessage(eb.build())
				.setActionRow(computeButtons(trackPage, subcommand))
				.queueAfter(2, TimeUnit.SECONDS);

		return event
				.reply("Here's your track index!")
				.setEphemeral(true);

	}

	@Override
	public boolean hasListener() {
		return true;
	}

	@Override
	public @Nullable EventListener getListener() {
		return buttonListener;
	}

	private @NotNull EmbedBuilder computeEmbed(Page<TrackModel> trackPage) {

		return new EmbedBuilder()
				.setTitle("Track Index")
				.setFooter("Page " + (trackPage.getNumber() + 1) + "/" + trackPage.getTotalPages())
				.setColor(Color.BLUE)
				.setDescription(trackPage
						.getContent()
						.stream()
						.map(track -> "**#" + track.getId() + "** *" + track.getName() + "*")
						.collect(Collectors.joining("\n")));

	}

	// TODO: cyclic prev/next (first/last page)
	// TODO: json as component_id
	private Component[] computeButtons(Page<TrackModel> trackPage, String subcommand, Object... args) {

		int zeroBasedPage = trackPage.getNumber();
		String joinedArgs = (args.length == 0)
				? ""
				: Arrays
						.stream(args)
						.map(Object::toString)
						.collect(Collectors.joining(":"));

		Button prevButton = Button
				.secondary(String.format("%s:%s:%s", BUTTON_COMMAND, subcommand,
						argsAppendPageNumber(joinedArgs, zeroBasedPage)), Emojis.PREVIOUS)
				.withDisabled(!trackPage.hasPrevious());

		Button nextButton = Button
				.secondary(String.format("%s:%s:%s", BUTTON_COMMAND, subcommand,
						argsAppendPageNumber(joinedArgs, zeroBasedPage + 2)), Emojis.NEXT)
				.withDisabled(!trackPage.hasNext());

		Button reloadButton = Button.secondary(String.format("%s:%s:%s", BUTTON_COMMAND, subcommand,
				argsAppendPageNumber(joinedArgs, zeroBasedPage + 1)), Emojis.RELOAD);

		Button closeButton = Button.secondary(String.format("%s:%s", BUTTON_COMMAND, "close"), Emojis.CLOSE);

		return new Component[] { prevButton, nextButton, reloadButton, closeButton };

	}

	private static String argsAppendPageNumber(String joinedArgs, int page) {

		if(joinedArgs.isEmpty())
			return String.valueOf(page);
		else
			return joinedArgs + ":" + page;

	}

	private static void replyEphemeralWith(String message, Interaction event) {
		event
				.reply(message)
				.setEphemeral(true)
				.queue();
	}

	public class ButtonClickListener extends ListenerAdapter {

		@Override
		public void onButtonClick(@NotNull ButtonClickEvent event) {

			// 0: command, 1: subcommand, 2+:args
			ArgumentsParser parser = new ArgumentsParser(event.getComponentId(), ":");

			String command = parser.getArgumentString(0, "");
			if(!command.equalsIgnoreCase(BUTTON_COMMAND))
				return;

			List<Object> args = new LinkedList<>();
			Page<TrackModel> trackPage;
			int zeroBasedPage;

			String subcommand = parser.getArgumentString(1, "");
			switch(subcommand) {

				case "close":
					//noinspection ConstantConditions
					event
							.getMessage()
							.delete()
							.queue();
					return;

				case SUBCOMMAND_ALL:

					zeroBasedPage = parser
							.getArgumentInteger(2)
							.map(i -> i - 1)
							.orElse(0);

					if(zeroBasedPage < 1) {

						replyEphemeralWith("Invalid button", event);
						return;

					}

					args.add(zeroBasedPage);

					trackPage = trackRepo.findAll(PageRequest.of(zeroBasedPage, 15, Sort.by(Sort.Direction.ASC, "id")));

					break;

				case SUBCOMMAND_NAME:

					Optional<String> optStr = parser.getArgumentString(2);
					if(!optStr.isPresent()) {

						replyEphemeralWith("Invalid button", event);
						return;

					}

					String trackSubname = optStr.get();
					zeroBasedPage = parser
							.getArgumentInteger(3)
							.map(i -> i - 1)
							.orElse(0);

					if(zeroBasedPage < 1) {

						replyEphemeralWith("Invalid button", event);
						return;

					}

					args.add(trackSubname);
					args.add(zeroBasedPage);

					trackPage = trackRepo.findAllByNameLike(trackSubname,
							PageRequest.of(zeroBasedPage, 15, Sort.by(Sort.Direction.ASC, "id")));

					break;

				case SUBCOMMAND_USER:

					Optional<Long> optLong = parser.getArgumentLong(2);
					if(!optLong.isPresent()) {

						replyEphemeralWith("Invalid button", event);
						return;

					}

					long uploaderId = optLong.get();
					zeroBasedPage = parser
							.getArgumentInteger(3)
							.map(i -> i - 1)
							.orElse(0);

					if(zeroBasedPage < 1) {

						replyEphemeralWith("Invalid button", event);
						return;

					}

					args.add(uploaderId);
					args.add(zeroBasedPage);

					trackPage = trackRepo.findAllByUploaderIdIs(uploaderId,
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

			EmbedBuilder eb = computeEmbed(trackPage);

			//noinspection ConstantConditions
			event
					.getMessage()
					.delete()
					.and(event
							.getChannel()
							.sendMessage(eb.build())
							.setActionRow(computeButtons(trackPage, subcommand, args.toArray())))
					.queue();

		}

	}

}
