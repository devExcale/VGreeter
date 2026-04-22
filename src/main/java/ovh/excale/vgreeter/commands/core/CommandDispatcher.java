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
import java.util.zip.CRC32;

import static java.lang.String.format;

@RequiredArgsConstructor
@Log4j2
@Service
public class CommandDispatcher extends ListenerAdapter implements ApplicationListener<ContextRefreshedEvent> {

	public static final String PREFIX = "vg:";

	private final Map<String, SlashCommandInvoker> slashInvokers = new HashMap<>();

	private final Map<Integer, ButtonCommandInvoker> buttonInvokers = new HashMap<>();

	private final Map<String, MessageCommandInvoker> messageInvokers = new HashMap<>();

	private final Map<String, SlashCommandData> commandData = new HashMap<>();

	private final ApplicationEventPublisher eventPublisher;

	public static String encodeButtonId(String name) {
		CRC32 crc = new CRC32();
		crc.update(name.getBytes());
		return Integer.toHexString((int) crc.getValue());
	}

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

		// Dispatch each controller to find command methods and create invokers
		for(Object bean : controllers.values())
			dispatchCommandController(bean);

		// Log summary of registered commands
		log.info(
			"[SlashCommands Found: {}] {}",
			slashInvokers.size(),
			slashInvokers.keySet()
				.stream()
				.sorted()
				.collect(Collectors.joining(", "))
		);
		log.info(
			"[ButtonCommands Found: {}] {}",
			buttonInvokers.size(),
			buttonInvokers.values()
				.stream()
				.map(ButtonCommandInvoker::getMapping)
				.map(ButtonMapping::name)
				.sorted()
				.collect(Collectors.joining(", "))
		);
		log.info(
			"[MessageCommands Found: {}] {}",
			messageInvokers.size(),
			messageInvokers.values()
				.stream()
				.map(MessageCommandInvoker::getMapping)
				.map(MessageMapping::name)
				.sorted()
				.collect(Collectors.joining(", "))
		);

		// Notify listeners that slash command data is ready.
		eventPublisher.publishEvent(new CommandUpdateEvent(getCommandData()));

	}

	/**
	 * Scan the given bean for annotated command methods.
	 *
	 * @param bean the instance of the class annotated with @CommandController to scan
	 */
	private void dispatchCommandController(Object bean) {

		// Get controller info
		Class<?> controllerClass = ClassUtils.getUserClass(bean);
		CommandController cmdMeta = controllerClass.getAnnotation(CommandController.class);

		if(cmdMeta == null)
			throw new IllegalStateException(format(
				"Bean `%s` was selected as a command controller, " +
				"but @CommandController could not be resolved from defined class `%s`.",
				bean.getClass().getName(), controllerClass.getName()
			));

		// Get parent command info
		String parentName = cmdMeta.name().trim();
		String parentDesc = cmdMeta.description().trim();

		// Get or compute slash command data
		SlashCommandData cmdData = commandData.computeIfAbsent(
			parentName,
			name -> name.isBlank() ? null : Commands.slash(name, parentDesc)
		);

		// Update description if not empty
		if(cmdData != null && !parentDesc.isBlank())
			cmdData.setDescription(parentDesc);

		// Loop all methods of the class
		for(Method method : controllerClass.getDeclaredMethods()) {

			// Find declared method
			Method resolvedMethod = BridgeMethodResolver.findBridgedMethod(
				ClassUtils.getMostSpecificMethod(method, controllerClass)
			);

			// Scan for Slash Commands
			if(resolvedMethod.isAnnotationPresent(SlashMapping.class))
				dispatchSlashCommand(bean, resolvedMethod, parentName, cmdData);

			// Scan for Button Commands
			if(resolvedMethod.isAnnotationPresent(ButtonMapping.class))
				dispatchButtonCommand(bean, resolvedMethod);

			// Scan for Message Commands
			if(resolvedMethod.isAnnotationPresent(MessageMapping.class))
				dispatchMessageCommand(bean, resolvedMethod);

		}

	}

	/**
	 * Infer and register a slash command invoker for the given method, and update the parent command data if necessary.
	 *
	 * @param bean the instance of the class containing the slash command method
	 * @param resolvedMethod the method annotated with @SlashMapping to create an invoker for
	 * @param parentName the name of the parent command (empty if this is a main command)
	 * @param parentCmdData the SlashCommandData of the parent command (null if this is a main command)
	 */
	private void dispatchSlashCommand(
		Object bean,
		Method resolvedMethod,
		String parentName,
		SlashCommandData parentCmdData
	) {

		// Find mapping and compute fields
		SlashMapping mapping = resolvedMethod.getAnnotation(SlashMapping.class);
		String name = mapping.name().trim();
		String fullname = format("%s %s", parentName, name).trim();
		String description = mapping.description();

		// Check if the command is already registered
		if(slashInvokers.containsKey(fullname))
			throw new IllegalArgumentException(format(
				"Duplicate SlashCommand `%s` found.", fullname
			));

		// Register CommandInvoker
		SlashCommandInvoker invoker = new SlashCommandInvoker(bean, resolvedMethod);
		slashInvokers.put(fullname, invoker);

		if(parentCmdData != null) {

			// Create subcommand data and add it to parent command
			SubcommandData subCmdData = new SubcommandData(name, description)
				.addOptions(invoker.getOptions());
			parentCmdData.addSubcommands(subCmdData);

		} else {

			// Create main command data and register it
			SlashCommandData cmdData = Commands.slash(fullname, description)
				.addOptions(invoker.getOptions());
			commandData.put(fullname, cmdData);

		}

	}

	/**
	 * Infer and register a button command invoker for the given method.
	 *
	 * @param bean the instance of the class containing the button command method
	 * @param resolvedMethod the method annotated with @ButtonMapping to create an invoker for
	 */
	private void dispatchButtonCommand(Object bean, Method resolvedMethod) {

		// Find mapping and compute fields
		ButtonMapping mapping = resolvedMethod.getAnnotation(ButtonMapping.class);
		String name = mapping.name().trim();
		CRC32 crc = new CRC32();
		crc.update(name.getBytes());
		int cmdHashId = (int) crc.getValue();

		// Verify no collision hash
		ButtonCommandInvoker invoker = buttonInvokers.get(cmdHashId);
		if(invoker != null)
			throw new IllegalArgumentException(format(
				"Duplicate ButtonCommand hash `%s/%s` found.",
				invoker.getMapping().name().trim(), name
			));

		// Register CommandInvoker
		buttonInvokers.put(cmdHashId, new ButtonCommandInvoker(bean, resolvedMethod));

	}

	/**
	 * Infer and register a message command invoker for the given method.
	 *
	 * @param bean the instance of the class containing the message command method
	 * @param resolvedMethod the method annotated with @MessageMapping to create an invoker for
	 */
	private void dispatchMessageCommand(Object bean, Method resolvedMethod) {

		// Find mapping and compute fields
		MessageMapping mapping = resolvedMethod.getAnnotation(MessageMapping.class);
		String name = mapping.name().trim();

		// Verify no collision hash
		MessageCommandInvoker invoker = messageInvokers.get(name);
		if(invoker != null)
			throw new IllegalArgumentException(format(
				"Duplicate MessageCommand `%s` found.", name
			));

		// Register CommandInvoker
		messageInvokers.put(mapping.name(), new MessageCommandInvoker(bean, resolvedMethod));

	}

	@Override
	public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

		// Compute full command name
		String fullname = Optional.ofNullable(event.getSubcommandName())
			.map(sub -> event.getName() + " " + sub)
			.orElseGet(event::getName)
			.trim();

		// Invoke command
		SlashCommandInvoker invoker = slashInvokers.get(fullname);
		if(invoker != null)
			invoker.invoke(event);
		else
			log.warn("No invoker found for command `{}`", fullname);

	}

	@Override
	public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {

		// Compute command hash from hex-encoded button id
		String buttonId = event.getComponentId();
		int cmdHashId = Integer.parseUnsignedInt(buttonId, 16);

		// Invoke command
		ButtonCommandInvoker invoker = buttonInvokers.get(cmdHashId);
		if(invoker != null)
			invoker.invoke(event);

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

		// Invoke command
		MessageCommandInvoker invoker = messageInvokers.get(commandName);
		if(invoker != null)
			invoker.invoke(event);

	}

}