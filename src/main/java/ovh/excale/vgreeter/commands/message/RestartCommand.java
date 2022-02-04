package ovh.excale.vgreeter.commands.message;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
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
	public @Nullable RestAction<?> execute(@NotNull final PrivateMessageReceivedEvent event) {

		Message message = event.getMessage();
		ArgumentsParser arguments = new ArgumentsParser(message.getContentRaw());

		boolean maintenance = arguments
				.getArgumentString(1)
				.map("maintenance"::equalsIgnoreCase)
				.orElse(false);
		String restartMessage = maintenance ? "*Restarting on maintenance mode...*" : "*Restarting...*";

		message.reply(restartMessage)
				// Wait for message to be sent and then restart
				.complete();

		final long authorId = event
				.getAuthor()
				.getIdLong();

		VGreeterApplication.restart(() -> VGreeterApplication
				.getApplicationContext()
				.getBean(JDA.class)
				.retrieveUserById(authorId)
				.flatMap(User::openPrivateChannel)
				.flatMap(dm -> dm.sendMessage("*Done!*"))
				.queue(), maintenance);

		return null;
	}

}
