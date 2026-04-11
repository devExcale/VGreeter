package ovh.excale.vgreeter.commands.button;

import java.util.Collections;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import ovh.excale.vgreeter.commands.core.AbstractButtonCommand;

public class CloseEmbedCommand extends AbstractButtonCommand {

	public CloseEmbedCommand() {
		super("close", "Close and embed or a message");
	}

	@Override
	public @NotNull RestAction<?> execute(@NotNull ButtonInteractionEvent event) {

		return event.editMessage("Closed")
				.setEmbeds(Collections.emptyList())
				.setComponents(Collections.emptyList());

	}

}
