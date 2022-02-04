package ovh.excale.vgreeter.commands.core;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class CommandBuilder {

	public static CommandBuilderPrototype create(String name) {
		return new CommandBuilderPrototype(name);
	}

	private final CommandData commandData;
	private final List<SubcommandData> subcommands;
	private SubcommandData currentSubcommand;

	private Boolean subcommand;

	private CommandBuilder(String name, String description) {

		commandData = new CommandData(name, description);
		subcommands = new LinkedList<>();

		currentSubcommand = null;
		subcommand = null;
	}

	public CommandBuilder addOption(String name, String description, OptionType type) {

		if(subcommand == null)
			subcommand = false;

		if(subcommand)
			currentSubcommand.addOption(type, name, description, false);
		else
			commandData.addOption(type, name, description, false);

		return this;
	}

	public CommandBuilder addOptionRequired(String name, String description, OptionType type) {

		if(subcommand == null)
			subcommand = false;

		if(subcommand)
			currentSubcommand.addOption(type, name, description, true);
		else
			commandData.addOption(type, name, description, true);

		return this;
	}

	public CommandBuilder subcommand(String name, String description) {

		if(subcommand == null)
			subcommand = true;

		if(!subcommand)
			throw new IllegalStateException("Cannot add a subcommand with previous options");

		subcommands.add(currentSubcommand = new SubcommandData(name, description));

		return this;
	}

	public CommandData build() {

		if(subcommand != null && subcommand)
			commandData.addSubcommands(subcommands);

		return commandData;
	}

	public static final class CommandBuilderPrototype {

		private final String name;

		private CommandBuilderPrototype(String name) {
			this.name = Objects.requireNonNull(name);
		}

		public CommandBuilder setDescription(String description) {
			return new CommandBuilder(name, Objects.requireNonNull(description));
		}

	}

}
