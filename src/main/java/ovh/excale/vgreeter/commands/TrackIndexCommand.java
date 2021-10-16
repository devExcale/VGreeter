package ovh.excale.vgreeter.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
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
import ovh.excale.vgreeter.utilities.Emojis;

import java.awt.*;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TrackIndexCommand extends AbstractSlashCommand {

	public static final String BUTTON_COMMAND = "trackindex:";

	private final TrackRepository trackRepo;
	private final ButtonClickListener buttonListener;

	public TrackIndexCommand() {
		super("trackindex", "List all the tracks");
		getBuilder().addOption("page", "Page number", OptionType.INTEGER);

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

		Page<TrackModel> trackPage = trackRepo.findAll(
				PageRequest.of(humanBasedPage - 1, 15, Sort.by(Sort.Direction.ASC, "id")));

		if(trackPage.isEmpty())
			return event
					.reply("Empty page")
					.setEphemeral(true);

		EmbedBuilder eb = computeEmbed(trackPage);

		// TODO: CHECK PERMS AND MESSAGECHANNEL
		event
				.getChannel()
				.sendMessage(eb.build())
				.setActionRow(computeButtons(trackPage))
				.queueAfter(2, TimeUnit.SECONDS);

		String userMention = event
				.getUser()
				.getAsMention();

		return event.reply("Track Index requested by " + userMention);

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

	private Component[] computeButtons(Page<TrackModel> trackPage) {

		int zeroBasedPage = trackPage.getNumber();

		Button prevButton = Button
				.secondary(BUTTON_COMMAND + zeroBasedPage, Emojis.PREVIOUS)
				.withDisabled(!trackPage.hasPrevious());

		Button nextButton = Button
				.secondary(BUTTON_COMMAND + (zeroBasedPage + 2), Emojis.NEXT)
				.withDisabled(!trackPage.hasNext());

		Button reloadButton = Button.secondary(BUTTON_COMMAND + (zeroBasedPage + 1), Emojis.RELOAD);

		return new Component[] { prevButton, nextButton, reloadButton };

	}

	// TODO: CLOSE INDEX BUTTON
	public class ButtonClickListener extends ListenerAdapter {

		@Override
		public void onButtonClick(@NotNull ButtonClickEvent event) {

			// TODO: COMPONENT_ID COMMAND CHECK
			String stringPage = event
					.getComponentId()
					.replace(BUTTON_COMMAND, "");
			int humanBasedPage;

			try {

				humanBasedPage = Integer.parseInt(stringPage);

			} catch(NumberFormatException e) {
				event
						.reply("Invalid button")
						.setEphemeral(true)
						.queue();
				return;
			}

			if(humanBasedPage < 1) {
				event
						.reply("Invalid button")
						.setEphemeral(true)
						.queue();
				return;
			}

			Page<TrackModel> trackPage = trackRepo.findAll(
					PageRequest.of(humanBasedPage - 1, 15, Sort.by(Sort.Direction.ASC, "id")));

			if(trackPage.isEmpty()) {
				event
						.reply("Empty page")
						.setEphemeral(true)
						.queue();
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
							.setActionRow(computeButtons(trackPage)))
					.queue();

		}

	}

}
