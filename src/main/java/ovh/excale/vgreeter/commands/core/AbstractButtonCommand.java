package ovh.excale.vgreeter.commands.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractButtonCommand extends AbstractCommand<ButtonClickEvent> {

	protected AbstractButtonCommand(String name, String description) {
		super(name, description, ButtonClickEvent.class);
	}

	@Override
	public abstract @NotNull RestAction<?> execute(@NotNull ButtonClickEvent event);

	@Override
	public boolean accepts(GenericEvent event) {

		if(!(event instanceof ButtonClickEvent))
			return false;

		CommandOptions command;
		try {

			command = CommandOptions.fromJson(((ButtonClickEvent) event).getComponentId());

		} catch(JsonProcessingException e) {
			return false;
		}

		return name.equalsIgnoreCase(command.getCommand());

	}

}
