package ovh.excale.vgreeter.commands.button;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import ovh.excale.vgreeter.commands.core.AbstractButtonCommand;

public class CloseEmbedCommand extends AbstractButtonCommand {

	public CloseEmbedCommand() {
		super("close", "Close and embed or a message");
	}

	@Override
	public @NotNull RestAction<?> execute(@NotNull ButtonClickEvent event) {

		// noinspection ConstantConditions
		return event.getMessage()
				.delete()
				.reason("Embed closed by " + event.getUser()
						.getAsMention())
				.onErrorFlatMap(t -> event.reply("Cannot close embed")
						.map(hook -> null));

	}

}
