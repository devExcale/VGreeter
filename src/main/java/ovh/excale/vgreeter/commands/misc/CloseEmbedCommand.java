package ovh.excale.vgreeter.commands.misc;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import ovh.excale.vgreeter.commands.core.annotation.ButtonMapping;
import ovh.excale.vgreeter.commands.core.annotation.CommandController;

import java.util.Collections;

@CommandController
public class CloseEmbedCommand {

	public static final String CMD_NAME = "CloseEmbed";

	@ButtonMapping(name = CMD_NAME)
	public @NotNull RestAction<?> closeEmbed(@NotNull ButtonInteractionEvent event) {

		return event.editMessage("Closed")
				.setEmbeds(Collections.emptyList())
				.setComponents(Collections.emptyList());

	}

}
