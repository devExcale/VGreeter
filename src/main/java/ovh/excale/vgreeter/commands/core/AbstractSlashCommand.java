package ovh.excale.vgreeter.commands.core;

import lombok.Getter;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractSlashCommand extends AbstractCommand<SlashCommandInteractionEvent> {

	@Getter
	private final CommandBuilder builder;

	protected AbstractSlashCommand(String name, String description) {
		super(name, description, SlashCommandInteractionEvent.class);

		builder = CommandBuilder
				.create(name)
				.setDescription(description);

	}

	@Override
	public abstract @NotNull RestAction<?> execute(SlashCommandInteractionEvent event);

	@Override
	public boolean accepts(GenericEvent event) {

		if(!(event instanceof SlashCommandInteractionEvent slashCommandEvent))
			return false;

		return name.equals(slashCommandEvent.getName());
	}

	public CommandData getData() {
		return builder.build();
	}

}
