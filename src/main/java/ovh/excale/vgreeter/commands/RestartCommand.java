package ovh.excale.vgreeter.commands;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ovh.excale.vgreeter.VGreeterApplication;
import ovh.excale.vgreeter.commands.core.AbstractMessageCommand;
import ovh.excale.vgreeter.utilities.ArgumentsParser;

// TODO: OWNER/MOD ONLY
public class RestartCommand extends AbstractMessageCommand {

	public RestartCommand() {
		super("restart", "");
	}

	@Override
	public @Nullable RestAction<?> execute(@NotNull PrivateMessageReceivedEvent event) {

		Message message = event.getMessage();
		ArgumentsParser arguments = new ArgumentsParser(message.getContentRaw());

		boolean maintenance = arguments.getArgumentBoolean(1, false);
		String restartMessage = maintenance ? "*Restarting on maintenance mode...*" : "*Restarting...*";

		message
				.reply(restartMessage)
				.complete();

		VGreeterApplication.restart(maintenance);

		// TODO: SUCCESS MESSAGE ON COMPLETE

		return null;
	}

}
