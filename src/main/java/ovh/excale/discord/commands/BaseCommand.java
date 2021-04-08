package ovh.excale.discord.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class BaseCommand extends Command {

	public BaseCommand() {
		this.name = "basecommand";
		this.arguments = "";
		this.help = "Help Message";
		this.guildOnly = false;
	}

	@Override
	protected void execute(CommandEvent commandEvent) {

		// TODO: EXECUTE COMMAND

	}

}
