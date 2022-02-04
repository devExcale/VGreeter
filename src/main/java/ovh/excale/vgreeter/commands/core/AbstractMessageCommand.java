package ovh.excale.vgreeter.commands.core;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public abstract class AbstractMessageCommand extends AbstractCommand<PrivateMessageReceivedEvent> {

	public static final String PREFIX = "vg:";

	protected AbstractMessageCommand(String name, String description) {
		super(name, description, PrivateMessageReceivedEvent.class);
	}

	@Override
	public abstract @Nullable RestAction<?> execute(@NotNull PrivateMessageReceivedEvent event);

	public boolean accepts(GenericEvent event) {

		if(!(event instanceof PrivateMessageReceivedEvent))
			return false;

		String msgContent = ((PrivateMessageReceivedEvent) event)
				.getMessage()
				.getContentRaw()
				.toLowerCase(Locale.ROOT);

		return msgContent.startsWith(PREFIX + name);

	}

}
