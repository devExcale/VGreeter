package ovh.excale.vgreeter.commands.button;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;
import org.jetbrains.annotations.NotNull;
import ovh.excale.vgreeter.commands.core.AbstractButtonCommand;
import ovh.excale.vgreeter.commands.core.CommandOptions;
import ovh.excale.vgreeter.track.TrackIndex;

@Log4j2
public class TrackIndexButtonCommand extends AbstractButtonCommand {

	public TrackIndexButtonCommand() {
		super("trackindex", "List all the tracks");
	}

	@SneakyThrows
	@Override
	public @NotNull RestAction<?> execute(ButtonClickEvent event) {

		CommandOptions command = CommandOptions.fromJson(event.getComponentId());
		//noinspection DuplicatedCode
		TrackIndex index = new TrackIndex(command);

		try {

			index.fetch();

		} catch(IllegalArgumentException e) {
			return replyEphemeralWith(e.getMessage(), event);
		} catch(Exception e) {
			log.error(e.getMessage(), e);
			return replyEphemeralWith("There has been an internal error", event);
		}

		if(index.isEmpty())
			return replyEphemeralWith("Empty page", event);

		//noinspection ConstantConditions
		return event.getMessage()
				.delete()
				.and(event.getChannel()
						.sendMessage(index.buildEmbed()
								.build())
						.setActionRow(index.buildButtons()));

	}

	private static ReplyAction replyEphemeralWith(String message, Interaction event) {
		return event.reply(message)
				.setEphemeral(true);
	}

}
