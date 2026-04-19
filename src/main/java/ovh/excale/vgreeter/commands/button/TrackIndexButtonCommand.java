package ovh.excale.vgreeter.commands.button;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import ovh.excale.vgreeter.commands.core.AbstractButtonCommand;
import ovh.excale.vgreeter.commands.core.CommandOptions;
import ovh.excale.vgreeter.track.TrackIndex;
import ovh.excale.vgreeter.services.LogErrorService;

import java.util.Arrays;

import static ovh.excale.vgreeter.utilities.DiscordUtil.replyEphemeralWith;

@Log4j2
@Component
public class TrackIndexButtonCommand extends AbstractButtonCommand {

	private final LogErrorService logErrorService;

	public TrackIndexButtonCommand(LogErrorService logErrorService) {
		super("trackindex", "List all the tracks");
		this.logErrorService = logErrorService;
	}

	@SneakyThrows
	@Override
	public @NotNull RestAction<?> execute(@NonNull ButtonInteractionEvent event) {

		CommandOptions command = CommandOptions.fromJson(event.getComponentId());
		//noinspection DuplicatedCode
		TrackIndex index = new TrackIndex(command);

		try {

			index.fetch();

		} catch(IllegalArgumentException e) {
			return replyEphemeralWith(e.getMessage(), event);
		} catch(Exception e) {
			logErrorService.error(e);
			log.error(e.getMessage(), e);
			return replyEphemeralWith("There has been an internal error", event);
		}

		if(index.isEmpty())
			return replyEphemeralWith("Empty page", event);

		return event.editMessageEmbeds(index.buildEmbed().build())
				.setComponents(ActionRow.of(Arrays.asList(index.buildButtons())));

	}

}
