package ovh.excale.vgreeter.commands.core;

import lombok.Getter;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.Nullable;

@Getter
public abstract class AbstractCommand<EventType extends GenericEvent> {

	protected final String name;
	protected final String description;

	private final Class<EventType> typeClass;

	protected AbstractCommand(String name, String description, Class<EventType> typeClass) {
		this.name = name;
		this.description = description;
		this.typeClass = typeClass;
	}

	public abstract RestAction<?> execute(EventType event);

	public abstract boolean accepts(GenericEvent eventType);

	// TODO: implement method in individual commands
	public /*abstract*/ boolean bypassesMaintenance() { return true; }

	public boolean hasListener() {
		return false;
	}

	public @Nullable EventListener getListener() {
		return null;
	}
}
