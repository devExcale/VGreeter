package ovh.excale.vgreeter.commands.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractButtonCommand extends AbstractCommand<ButtonInteractionEvent> {

	protected AbstractButtonCommand(String name, String description) {
		super(name, description, ButtonInteractionEvent.class);
	}

	@Override
	public abstract @NotNull RestAction<?> execute(@NotNull ButtonInteractionEvent event);

	@Override
	public boolean accepts(GenericEvent event) {

		if(!(event instanceof ButtonInteractionEvent buttonInteractionEvent))
			return false;

		CommandOptions command;
		try {

			command = CommandOptions.fromJson(buttonInteractionEvent.getComponentId());

		} catch(JsonProcessingException _) {
			return false;
		}

		return name.equalsIgnoreCase(command.getCommand());
	}

}
