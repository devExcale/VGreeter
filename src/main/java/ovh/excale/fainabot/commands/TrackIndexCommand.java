package ovh.excale.fainabot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ovh.excale.fainabot.FainaBotApplication;
import ovh.excale.fainabot.models.TrackModel;
import ovh.excale.fainabot.repositories.TrackRepository;

import java.awt.*;
import java.util.Optional;
import java.util.stream.Collectors;

public class TrackIndexCommand extends AbstractCommand {

	private final TrackRepository trackRepo;

	public TrackIndexCommand() {
		super("trackindex", "List all the tracks");
		getBuilder().addOption("page", "Page number", OptionType.INTEGER);

		trackRepo = FainaBotApplication
				.getApplicationContext()
				.getBean(TrackRepository.class);

	}

	@Override
	public ReplyAction execute(SlashCommandEvent event) {

		int page = Optional
				.ofNullable(event.getOption("page"))
				.map(OptionMapping::getAsLong)
				.map(Long::intValue)
				.orElse(1);

		if(page < 1)
			return event
					.reply("Page option must be positive!")
					.setEphemeral(true);

		Page<TrackModel> trackPage = trackRepo.findAll(PageRequest.of(page - 1, 15));

		if(trackPage.isEmpty())
			return event
					.reply("Empty page")
					.setEphemeral(true);

		EmbedBuilder eb = new EmbedBuilder()
				.setTitle("Track Index")
				.setFooter("Page " + (trackPage.getNumber() + 1) + "/" + trackPage.getTotalPages())
				.setColor(Color.BLUE)
				.setDescription(trackPage
						.getContent()
						.stream()
						.map(track -> "**#" + track.getId() + "** *" + track.getName() + "*")
						.collect(Collectors.joining("\n")));

		return event.replyEmbeds(eb.build());

	}

}
