package ovh.excale.vgreeter.commands.core.event;

import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Arrays;

public class CommandUpdateEvent {

	private final SlashCommandData[] commandData;

	public CommandUpdateEvent(SlashCommandData[] commandData) {
		this.commandData = Arrays.copyOf(commandData, commandData.length);
	}

	public SlashCommandData[] getCommandData() {
		return Arrays.copyOf(commandData, commandData.length);
	}

}
