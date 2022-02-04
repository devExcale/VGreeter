package ovh.excale.vgreeter.commands.core;

import lombok.Getter;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;

// TODO: MERGE AbstractSlashCommand AND AbstractMessageCommand WITH A PARENT AbstractCommand CLASS
public abstract class AbstractSlashCommand extends AbstractCommand<SlashCommandEvent> {

	@Getter
	private final CommandBuilder builder;

	protected AbstractSlashCommand(String name, String description) {
		super(name, description, SlashCommandEvent.class);

		builder = CommandBuilder
				.create(name)
				.setDescription(description);

	}

	@Override
	public abstract @NotNull RestAction<?> execute(SlashCommandEvent event);

	@Override
	public boolean accepts(GenericEvent event) {
		return event instanceof SlashCommandEvent && name.equals(((SlashCommandEvent) event).getName());
	}

	public CommandData getData() {
		return builder.build();
	}

}
