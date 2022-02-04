package ovh.excale.vgreeter.commands.core;

import com.sun.javafx.sg.prism.NGExternalNode;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

@Service
public class CommandRegister {

	private final ListenerRegister listenerRegister;
	private final Map<Class<? extends GenericEvent>, Set<? extends AbstractCommand<?>>> masterRecord;

	private CommandRegister() {
		listenerRegister = new ListenerRegister();
		masterRecord = new HashMap<>();
	}

	public <Command extends AbstractCommand<?>> CommandRegister register(Command command) {

		Class<? extends GenericEvent> commandType = command.getTypeClass();
		//noinspection unchecked
		Set<Command> commandSet = (Set<Command>) masterRecord.computeIfAbsent(commandType, k -> new HashSet<>());

		commandSet.add(command);
		if(command.hasListener())
			listenerRegister.register(command.getListener());

		return this;

	}

	public CommandData[] getSlashCommandsData() {

		//noinspection unchecked
		return Optional.ofNullable((Set<AbstractSlashCommand>) masterRecord.get(SlashCommandEvent.class))
				.map(Collection::stream)
				.orElseGet(Stream::empty)
				.map(AbstractSlashCommand::getData)
				.toArray(CommandData[]::new);

	}

	public ListenerRegister getListener() {
		return listenerRegister;
	}

	private class ListenerRegister implements EventListener {

		public Set<EventListener> commandListeners;

		protected ListenerRegister() {
			commandListeners = new HashSet<>();
		}

		protected void register(EventListener listener) {
			commandListeners.add(listener);
		}

		@SneakyThrows
		@Override
		public void onEvent(@NotNull GenericEvent event) {

			AbstractCommand<?> abstractCommand = masterRecord.entrySet()
					.stream()
					.filter(entry -> entry.getKey()
							.isInstance(event))
					.map(Map.Entry::getValue)
					.flatMap(Collection::stream)
					.filter(command -> command.accepts(event))
					.findFirst()
					.orElse(null);

			if(abstractCommand != null) {

				//noinspection OptionalGetWithoutIsPresent
				Method execMethod = Arrays.stream(abstractCommand.getClass()
								.getDeclaredMethods())
						.filter(method -> method.getName()
								.equals("execute"))
						.findFirst()
						.get();

				Optional.ofNullable(execMethod.invoke(abstractCommand, event))
						.map(result -> ((RestAction<?>) result))
						.ifPresent(RestAction::queue);

			}

			for(EventListener commandListener : commandListeners)
				commandListener.onEvent(event);

		}

	}

}
