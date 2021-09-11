package ovh.excale.vgreeter.commands;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class CommandRegister {

	private final Map<String, AbstractCommand> commands;
	private final Listener listener;

	private CommandRegister() {
		commands = new HashMap<>();
		listener = new Listener();
	}

	public CommandRegister register(AbstractCommand command) {

		commands.put(command.getName(), command);
		if(command.hasListener())
			listener.register(command.getListener());

		return this;

	}

	public CommandData[] getData() {
		return commands
				.values()
				.stream()
				.map(AbstractCommand::getData)
				.toArray(CommandData[]::new);
	}

	public Listener getListener() {
		return listener;
	}

	private class Listener implements EventListener {

		public Set<EventListener> commandListeners;

		protected Listener() {
			commandListeners = new HashSet<>();
		}

		protected void register(EventListener listener) {
			commandListeners.add(listener);
		}

		@Override
		public void onEvent(@NotNull GenericEvent genericEvent) {

			if(genericEvent instanceof SlashCommandEvent)
				onSlashCommand((SlashCommandEvent) genericEvent);
			else
				for(EventListener commandListener : commandListeners)
					commandListener.onEvent(genericEvent);

		}

		private void onSlashCommand(SlashCommandEvent event) {

			AbstractCommand command = commands.get(event.getName());
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
