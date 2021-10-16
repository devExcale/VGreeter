package ovh.excale.vgreeter.commands.core;

import lombok.Getter;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractMessageCommand {

	public static final String PREFIX = "vg:";

	@Getter
	private final String name;

	@Getter
	private final String description;

	protected AbstractMessageCommand(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public abstract @Nullable RestAction<?> execute(@NotNull PrivateMessageReceivedEvent event);

	public boolean hasListener() {
		return false;
	}

	public @Nullable EventListener getListener() {
		return null;
	}

}
