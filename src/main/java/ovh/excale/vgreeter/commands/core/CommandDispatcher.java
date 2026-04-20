package ovh.excale.vgreeter.commands.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import ovh.excale.vgreeter.commands.core.annotation.ButtonMapping;
import ovh.excale.vgreeter.commands.core.annotation.CommandController;
import ovh.excale.vgreeter.commands.core.annotation.MessageMapping;
import ovh.excale.vgreeter.commands.core.annotation.SlashMapping;
import ovh.excale.vgreeter.commands.core.event.CommandUpdateEvent;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;

@RequiredArgsConstructor
@Log4j2
@Service
public class CommandDispatcher extends ListenerAdapter implements ApplicationListener<ContextRefreshedEvent> {

	public static final String PREFIX = "vg:";

	private final Map<String, SlashCommandInvoker> slashCommands = new HashMap<>();

	private final Map<String, ButtonCommandInvoker> buttonCommands = new HashMap<>();

	private final Map<String, MessageCommandInvoker> messageCommands = new HashMap<>();

	private final Map<String, SlashCommandData> commandData = new HashMap<>();

	private final ApplicationEventPublisher eventPublisher;

	public SlashCommandData[] getCommandData() {
		return commandData.values().toArray(SlashCommandData[]::new);
	}

	/**
	 * Scan for command controllers and their methods, create invokers,
	 * and setup slash commands' data on startup.
	 *
	 * @param event the context refreshed event triggered on application startup
	 */
	@Override
	public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {

		// Find all classes annotated with @CommandController
		ApplicationContext context = event.getApplicationContext();
		Map<String, Object> controllers = context.getBeansWithAnnotation(CommandController.class);

		// Loop beans with CommandController
		for(Object bean : controllers.values()) {

			Class<?> controllerClass = ClassUtils.getUserClass(bean);

			// If has subcommands, generate main command data
			createParentSlashCommandData(bean, controllerClass);

			for(Method method : controllerClass.getDeclaredMethods()) {

				Method resolvedMethod = BridgeMethodResolver.findBridgedMethod(
					ClassUtils.getMostSpecificMethod(method, controllerClass)
				);

				// Scan for Slash Commands
				if(resolvedMethod.isAnnotationPresent(SlashMapping.class)) {
					SlashMapping mapping = resolvedMethod.getAnnotation(SlashMapping.class);
					String fullname = format("%s %s", mapping.name(), mapping.subcommand()).trim();
					slashCommands.put(fullname, new SlashCommandInvoker(bean, resolvedMethod));
				}

				// Scan for Button Commands
				if(resolvedMethod.isAnnotationPresent(ButtonMapping.class)) {
					ButtonMapping mapping = resolvedMethod.getAnnotation(ButtonMapping.class);
					buttonCommands.put(mapping.name(), new ButtonCommandInvoker(bean, resolvedMethod));
				}

				// Scan for Message Commands
				if(resolvedMethod.isAnnotationPresent(MessageMapping.class)) {
					MessageMapping mapping = resolvedMethod.getAnnotation(MessageMapping.class);
					messageCommands.put(mapping.name(), new MessageCommandInvoker(bean, resolvedMethod));
				}

			}
		}

		// Generate the rest of command data
		createSlashCommandData();

		// Log summary of registered commands
		log.info(
			"[SlashCommands Found: {}] {}",
			slashCommands.size(),
			slashCommands.values()
				.stream()
				.map(SlashCommandInvoker::getMapping)
				.map(m -> format("%s %s", m.name(), m.subcommand()).trim())
				.collect(Collectors.joining(", "))
		);
		log.info(
			"[ButtonCommands Found: {}] {}",
			buttonCommands.size(),
			buttonCommands.values()
				.stream()
				.map(ButtonCommandInvoker::getMapping)
				.map(ButtonMapping::name)
				.collect(Collectors.joining(", "))
		);
		log.info(
			"[MessageCommands Found: {}] {}",
			messageCommands.size(),
			messageCommands.values()
				.stream()
				.map(MessageCommandInvoker::getMapping)
				.map(MessageMapping::name)
				.collect(Collectors.joining(", "))
		);

		// Notify listeners that slash command data is ready.
		eventPublisher.publishEvent(new CommandUpdateEvent(getCommandData()));

	}

	/**
	 * Create the slash command data for all registered slash commands and subcommands.
	 */
	private void createSlashCommandData() {

		// Loop all SlashCommands to add commands and subcommands
		for(SlashCommandInvoker cmd : slashCommands.values()) {

			Method method = cmd.getMethod();
			SlashMapping mapping = cmd.getMapping();
			String name = mapping.name();
			String subcommand = mapping.subcommand();
			String description = mapping.description();
			OptionData[] options = cmd.getOptions();

			if(subcommand.isEmpty()) {

				// Verify no command exists with the same name
				if(commandData.containsKey(name))
					throw new IllegalArgumentException(format(
						"Duplicate command name '%s' found in method '%s' of class '%s'. Commands must be unique.",
						name, method.getName(), method.getDeclaringClass().getName()
					));

				// Create and register main command
				commandData.put(name, Commands.slash(name, description).addOptions(options));

			} else {

				// Verify parent command exists
				if(!commandData.containsKey(name))
					throw new IllegalArgumentException(format(
						"Parent command '%s' for subcommand '%s' not found in method '%s' of class '%s'.",
						name, subcommand, method.getName(), method.getDeclaringClass().getName()
					));

				// Add subcommand to parent command
				commandData.get(name)
					.addSubcommands(new SubcommandData(subcommand, description).addOptions(options));

			}

		}
	}

	/**
	 * Create parent slash command data for classes annotated with @CommandController
	 * that have a non-empty commandName.
	 *
	 * @param bean the instance of the class annotated with @CommandController to create parent command data for
	 */
	private void createParentSlashCommandData(Object bean, Class<?> controllerClass) {

		// Get controller info
		CommandController cmdMeta = controllerClass.getAnnotation(CommandController.class);
		if(cmdMeta == null)
			throw new IllegalStateException(format(
				"Bean '%s' was selected as a command controller, but @CommandController could not be resolved from user class '%s'.",
				bean.getClass().getName(), controllerClass.getName()
			));

		String cmdName = cmdMeta.commandName();
		String cmdDesc = cmdMeta.commandDescription();

		// Do not generate parent command if no subcommands
		if(cmdName.isEmpty())
			return;

		// Ensure no other command with the same name exists
		if(commandData.containsKey(cmdName))
			throw new IllegalArgumentException(format(
				"Duplicate command name '%s' found in class '%s'. Command names must be unique.",
				cmdName, controllerClass
					.getName()
			));

		// Add parent command data for subcommands
		commandData.put(cmdName, Commands.slash(cmdName, cmdDesc));

	}

	@Override
	public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

		String command = event.getName();
		String subcommand = event.getSubcommandName();
		if(subcommand == null)
			subcommand = "";

		String fullname = format("%s %s", command, subcommand).trim();

		SlashCommandInvoker invoker = slashCommands.get(fullname);
		if(invoker != null)
			invoker.invoke(event);
		else
			log.warn("No invoker found for command `{}`", fullname);

	}

	@Override
	public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
		try {
			// Reusing your CommandOptions logic to extract the command name from JSON payload
			CommandOptions options = CommandOptions.fromJson(event.getComponentId());
			ButtonCommandInvoker invoker = buttonCommands.get(options.getCommand());

			if(invoker != null)
				invoker.invoke(event);

		} catch(JsonProcessingException _) {
			// Ignore buttons that are not mapped command payloads.
		}
	}

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {

		String content = event.getMessage()
			.getContentRaw()
			.toLowerCase(Locale.ROOT);
		if(!content.startsWith(PREFIX))
			return;

		// Prefix extraction (e.g., "vg:help" -> "help")
		String commandName = content.substring(PREFIX.length())
			.split("\\s+")[0];

		MessageCommandInvoker invoker = messageCommands.get(commandName);
		if(invoker != null)
			invoker.invoke(event);

	}

}