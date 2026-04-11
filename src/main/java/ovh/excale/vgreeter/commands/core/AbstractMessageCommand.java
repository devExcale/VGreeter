package ovh.excale.vgreeter.commands.core;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public abstract class AbstractMessageCommand extends AbstractCommand<MessageReceivedEvent> {

	public static final String PREFIX = "vg:";

	protected AbstractMessageCommand(String name, String description) {
		super(name, description, MessageReceivedEvent.class);
	}

	@Override
	public abstract @Nullable RestAction<?> execute(@NotNull MessageReceivedEvent event);

	public boolean accepts(GenericEvent event) {

		if(!(event instanceof MessageReceivedEvent))
			return false;

		MessageReceivedEvent messageEvent = (MessageReceivedEvent) event;
		if(messageEvent.isFromGuild())
			return false;

		String msgContent = messageEvent
				.getMessage()
				.getContentRaw()
				.toLowerCase(Locale.ROOT);

		return msgContent.startsWith(PREFIX + name);

	}

}
