package ovh.excale.fainabot.commands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;
import ovh.excale.fainabot.utilities.CommandBuilder;

public abstract class AbstractCommand {

	private final String name;
	private final String description;
	private final CommandBuilder builder;

	protected AbstractCommand(String name, String description) {
		this.name = name;
		this.description = description;

		builder = CommandBuilder.create(name)
				.setDescription(description);

	}

	public abstract ReplyAction execute(SlashCommandEvent event);

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	protected CommandBuilder getBuilder() {
		return builder;
	}

	public CommandData getData() {
		return builder.build();
	}

}
