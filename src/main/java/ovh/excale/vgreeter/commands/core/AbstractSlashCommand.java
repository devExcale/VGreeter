package ovh.excale.vgreeter.commands.core;

import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;
import org.jetbrains.annotations.Nullable;
import ovh.excale.vgreeter.utilities.CommandBuilder;

// TODO: MERGE AbstractSlashCommand AND AbstractMessageCommand WITH A PARENT AbstractCommand CLASS
public abstract class AbstractSlashCommand {

	@Getter
	private final String name;

	@Getter
	private final String description;

	@Getter
	private final CommandBuilder builder;

	protected AbstractSlashCommand(String name, String description) {
		this.name = name;
		this.description = description;

		builder = CommandBuilder
				.create(name)
				.setDescription(description);

	}

	public abstract ReplyAction execute(SlashCommandEvent event);

	public CommandData getData() {
		return builder.build();
	}

	public boolean hasListener() {
		return false;
	}

	public @Nullable EventListener getListener() {
		return null;
	}

}
