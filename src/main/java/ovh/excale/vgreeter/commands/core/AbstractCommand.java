package ovh.excale.vgreeter.commands.core;

import jakarta.annotation.Nullable;
import lombok.Getter;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.RestAction;

@Getter
public abstract class AbstractCommand<E extends GenericEvent> {

	protected final String name;
	protected final String description;

	private final Class<E> typeClass;

	protected AbstractCommand(String name, String description, Class<E> typeClass) {
		this.name = name;
		this.description = description;
		this.typeClass = typeClass;
	}

	public abstract RestAction<?> execute(E event);

	@SuppressWarnings("unused")
	public abstract boolean accepts(GenericEvent eventType);

	public boolean hasListener() {
		return false;
	}

	public @Nullable EventListener getListener() {
		return null;
	}
}
