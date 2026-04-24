package ovh.excale.vgreeter.commands.tracklist;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ovh.excale.vgreeter.commands.core.annotation.*;
import ovh.excale.vgreeter.entity.MemberEntity;
import ovh.excale.vgreeter.entity.TrackEntity;
import ovh.excale.vgreeter.repository.MemberRepository;
import ovh.excale.vgreeter.repository.TrackRepository;
import ovh.excale.vgreeter.services.TrackService;
import ovh.excale.vgreeter.track.TracklistEmbed;

import java.util.Optional;

import static ovh.excale.vgreeter.commands.tracklist.TracklistCommand.CMD_TRACKLIST;
import static ovh.excale.vgreeter.utilities.DiscordUtil.replyEphemeralWith;

@RequiredArgsConstructor
@Log4j2
@CommandController(
	name = CMD_TRACKLIST,
	description = "List all the tracks"
)
public class TracklistCommand {

	public static final String CMD_TRACKLIST = "tracklist";

	private static final String OPTDESC_PAGE_NUMBER = "Page number";

	// TODO: User filter description
	private static final String OPTDESC_USER = "User ID";

	public static final String SUBCMD_ALL = "all";

	public static final String SUBCMD_TITLE = "title";

	public static final String SUBCMD_USER = "user";

	public static final String BTN_CHANGE_PAGE = "TracklistChangePage";

	private final TrackService trackService;

	private final TrackRepository trackRepo;

	private final MemberRepository memberRepo;

	private final TracklistEmbed tracklistEmbed;

	/**
	 * Search for all tracks, with pagination.
	 *
	 * @param event slash command event
	 * @param humanPage optional one-based page number (defaults to 1 if not provided or invalid)
	 * @return a reply action with the tracklist embed and pagination buttons
	 */
	@SlashMapping(
		name = SUBCMD_ALL,
		description = "Search for all tracks"
	)
	public ReplyCallbackAction searchAll(
		SlashCommandInteractionEvent event,
		@CmdOption(name = "page", description = OPTDESC_PAGE_NUMBER, required = false, minValueL = 1) Long humanPage
	) {

		// Fetch the page
		Page<TrackEntity> trackPage = trackRepo.findAll(
			Pageable.ofSize(TrackService.DEFAULT_PAGE_SIZE)
				.withPage(normalizeHumanPage(humanPage, TrackService.DEFAULT_PAGE_SIZE))
		);

		TracklistEmbed.Generator embedGen = tracklistEmbed.with(trackPage);

		return event.replyEmbeds(embedGen.buildEmbed())
			.addComponents(ActionRow.of(embedGen.buildButtons()))
			.setEphemeral(true);
	}

	@SlashMapping(
		name = SUBCMD_TITLE,
		description = "Search for all tracks by their title"
	)
	public ReplyCallbackAction searchByName(SlashCommandInteractionEvent event) {
		return replyEphemeralWith("Not implemented yet", event);
	}

	@SlashMapping(
		name = SUBCMD_USER,
		description = "Search for tracks by a user"
	)
	public ReplyCallbackAction searchByUser(
		SlashCommandInteractionEvent event,
		@CmdOption(name = "user", description = OPTDESC_USER) User owner,
		@CmdOption(name = "page", description = OPTDESC_PAGE_NUMBER, required = false, minValueL = 1) Long humanPage
	) {

		// Fetch the owner data if provided
		MemberEntity ownerEntity = memberRepo.findByIdOrSave(
			owner.getIdLong(),
			() -> MemberEntity.builder()
				.discordId(owner.getIdLong())
				.discordUsername(owner.getName())
				.build()
		);

		// Fetch the page
		Page<TrackEntity> trackPage = trackRepo.findAllByOwnerId(
			owner.getIdLong(),
			Pageable.ofSize(TrackService.DEFAULT_PAGE_SIZE)
				.withPage(normalizeHumanPage(humanPage, TrackService.DEFAULT_PAGE_SIZE))
		);

		TracklistEmbed.Generator embedGen = tracklistEmbed.with(trackPage, ownerEntity);

		return event.replyEmbeds(embedGen.buildEmbed())
			.addComponents(ActionRow.of(embedGen.buildButtons()))
			.setEphemeral(true);
	}

	/**
	 * Handle pagination button clicks to change the page of the tracklist embed.
	 *
	 * @param event button interaction event
	 * @param indexPage zero-based page index from the button options
	 * @return a message edit action to update the embed with the new page of tracks
	 */
	@ButtonMapping(name = BTN_CHANGE_PAGE)
	public MessageEditCallbackAction changePage(
		ButtonInteractionEvent event,
		@BtnOption Integer indexPage,
		@BtnOption Long ownerId
	) {

		// Fetch the owner data if provided
		MemberEntity owner = null;
		if(ownerId > 0)
			owner = memberRepo.findById(ownerId)
				.orElse(null);

		// Fetch the page
		Page<TrackEntity> trackPage;

		if(owner == null)
			trackPage = trackRepo.findAll(
				Pageable.ofSize(TrackService.DEFAULT_PAGE_SIZE)
					.withPage(normalizeIndexPage(indexPage.longValue(), TrackService.DEFAULT_PAGE_SIZE))
			);
		else
			trackPage = trackRepo.findAllByOwnerId(
				ownerId,
				Pageable.ofSize(TrackService.DEFAULT_PAGE_SIZE)
					.withPage(normalizeIndexPage(indexPage.longValue(), TrackService.DEFAULT_PAGE_SIZE))
			);

		TracklistEmbed.Generator embedGen = tracklistEmbed.with(trackPage, owner);

		return event.editMessageEmbeds(embedGen.buildEmbed())
			.setComponents(ActionRow.of(embedGen.buildButtons()));
	}

	/**
	 * Normalize a human-friendly page number (one-based) to a zero-based index,
	 * and clamp it to the valid range of pages.
	 *
	 * @param humanPage one-based page number
	 * @param pageSize number of items per page
	 * @return the zero-based page index
	 */
	private int normalizeHumanPage(@Nullable Long humanPage, int pageSize) {

		int totalPages = trackService.totalPages(pageSize);
		if(totalPages <= 0)
			return 0;

		return Optional.ofNullable(humanPage)
			.map(Long::intValue)
			.map(hp -> Math.clamp(1, hp, totalPages))
			.map(hp -> hp - 1)
			.orElse(0);

	}

	/**
	 * Normalize a zero-based page index, and clamp it to the valid range of pages.
	 *
	 * @param indexPage zero-based page index
	 * @param pageSize number of items per page
	 * @return the normalized zero-based page index
	 */
	private int normalizeIndexPage(@Nullable Long indexPage, int pageSize) {

		int totalPages = trackService.totalPages(pageSize);
		if(totalPages <= 0)
			return 0;

		return Optional.ofNullable(indexPage)
			.map(Long::intValue)
			.map(ip -> Math.clamp(0, ip, totalPages - 1))
			.orElse(0);

	}


}
