package ovh.excale.vgreeter.commands.core;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ovh.excale.vgreeter.VGreeterApplication;

import java.util.*;

@Service
public class CommandRegister {

	private final Map<String, AbstractSlashCommand> slashCommands;
	private final Map<String, AbstractMessageCommand> messageCommands;
	private final RegisterListener registerListener;

	private CommandRegister() {
		slashCommands = new HashMap<>();
		messageCommands = new HashMap<>();
		registerListener = new RegisterListener();
	}

	public CommandRegister register(AbstractSlashCommand command) {

		slashCommands.put(command.getName(), command);
		if(command.hasListener())
			registerListener.register(command.getListener());

		return this;
	}

	public CommandRegister register(AbstractMessageCommand command) {

		messageCommands.put(command.getName(), command);
		if(command.hasListener())
			registerListener.register(command.getListener());

		return this;
	}

	public CommandData[] getSlashCommandsData() {
		return slashCommands
				.values()
				.stream()
				.map(AbstractSlashCommand::getData)
				.toArray(CommandData[]::new);
	}

	public RegisterListener getListener() {
		return registerListener;
	}

	private class RegisterListener implements EventListener {

		public Set<EventListener> commandListeners;

		protected RegisterListener() {
			commandListeners = new HashSet<>();
		}

		protected void register(EventListener listener) {
			commandListeners.add(listener);
		}

		@Override
		public void onEvent(@NotNull GenericEvent genericEvent) {

			if(genericEvent instanceof SlashCommandEvent)

				onSlashCommand((SlashCommandEvent) genericEvent);

			else if(genericEvent instanceof PrivateMessageReceivedEvent) {

				PrivateMessageReceivedEvent messageEvent = (PrivateMessageReceivedEvent) genericEvent;
				String message = messageEvent
						.getMessage()
						.getContentRaw()
						.toLowerCase(Locale.ROOT);

				if(message.startsWith(AbstractMessageCommand.PREFIX))
					onMessageCommand(messageEvent);

			}

			for(EventListener commandListener : commandListeners)
				commandListener.onEvent(genericEvent);

		}

		private void onMessageCommand(PrivateMessageReceivedEvent event) {

			Message message = event.getMessage();
			String messageContent = message
					.getContentRaw()
					.toLowerCase(Locale.ROOT)
					.replaceFirst(AbstractMessageCommand.PREFIX, "");
			String[] split = messageContent.split(" ");

			AbstractMessageCommand command = messageCommands.get(split[0]);

			Optional
					.ofNullable(command)
					.map(c -> c.execute(event))
					.ifPresent(RestAction::queue);

		}

		private void onSlashCommand(SlashCommandEvent event) {

			if(VGreeterApplication.isInMaintenance()) {
				event
						.reply("The bot is currently under maintenance")
						.setEphemeral(true)
						.queue();
				return;
			}

			AbstractSlashCommand command = slashCommands.get(event.getName());
			ReplyAction action;

			if(command == null)
				action = event
						.reply("No such command")
						.setEphemeral(true);
			else
				action = command.execute(event);

			try {
				action.queue();
			} catch(ErrorResponseException ignored) {
			}
		}

	}

}
