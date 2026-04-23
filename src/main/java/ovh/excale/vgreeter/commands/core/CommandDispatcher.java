package ovh.excale.vgreeter.commands.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import ovh.excale.vgreeter.commands.core.annotation.ButtonMapping;
import ovh.excale.vgreeter.commands.core.annotation.CommandController;
import ovh.excale.vgreeter.commands.core.annotation.MessageMapping;
import ovh.excale.vgreeter.commands.core.annotation.SlashMapping;
import ovh.excale.vgreeter.commands.core.event.CommandUpdateEvent;
import ovh.excale.vgreeter.commands.core.exception.CommandInvocationException;
import ovh.excale.vgreeter.entity.LogErrorEntity;
import ovh.excale.vgreeter.message.ErrorMessages;
import ovh.excale.vgreeter.services.LogErrorService;

import java.io.IOException;
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

	private final ApplicationEventPublisher eventPublisher;

	private final LogErrorService logErrorService;

	private final Map<String, SlashCommandInvoker> slashInvokers = new HashMap<>();
	private final Map<Integer, ButtonCommandInvoker> buttonInvokers = new HashMap<>();
	private final Map<String, MessageCommandInvoker> messageInvokers = new HashMap<>();

	private final Map<String, SlashCommandData> commandData = new HashMap<>();
	private final ErrorMessages msgError;

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
		SlashCommandData[] cmdData = commandData.values().toArray(SlashCommandData[]::new);
		eventPublisher.publishEvent(new CommandUpdateEvent(cmdData));

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
		try {

			if(invoker == null)
				throw new IllegalArgumentException(format(
					"No invoker found for SlashCommand `%s`.", fullname
				));

			invoker.invoke(event);

		} catch(CommandInvocationException | IllegalArgumentException e) {

			//noinspection DuplicatedCode: I won't extract this simple block for slash and button interactions only
			log.error(e.getMessage(), e);

			// Retrieve user and guild info for logging
			Long userId = Optional.of(event.getUser())
				.map(ISnowflake::getIdLong)
				.get();

			Long guildId = Optional.ofNullable(event.getGuild())
				.map(ISnowflake::getIdLong)
				.orElse(null);

			// Log the error
			LogErrorEntity logInfo = logErrorService.error(e, userId, guildId);

			// Reply to the user with generic error
			event.reply(msgError.getInternalErrorUuid(logInfo.getId()))
				.setEphemeral(true)
				.queue();

		}

	}

	@Override
	public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
		try {

			// Unpack command options
			String buttonId = event.getComponentId();
			byte[] packedOptions = Base64.getDecoder().decode(buttonId);

			// Peek cmdHashId from packed options
			int cmdHashId;
			try(MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(packedOptions)) {

				unpacker.unpackArrayHeader();
				cmdHashId = unpacker.unpackInt();

			}

			// Invoke command
			ButtonCommandInvoker invoker = buttonInvokers.get(cmdHashId);

			if(invoker == null)
				throw new IllegalArgumentException(format(
					"No invoker found for ButtonCommand `%s`.", buttonId
				));

			invoker.invoke(event);

		} catch(CommandInvocationException | IllegalArgumentException | IOException e) {

			//noinspection DuplicatedCode: I won't extract this simple block for slash and button interactions only
			log.error(e.getMessage(), e);

			// Retrieve user and guild info for logging
			Long userId = Optional.of(event.getUser())
				.map(ISnowflake::getIdLong)
				.get();

			Long guildId = Optional.ofNullable(event.getGuild())
				.map(ISnowflake::getIdLong)
				.orElse(null);

			// Log the error
			LogErrorEntity logInfo = logErrorService.error(e, userId, guildId);

			// Reply to the user with generic error
			event.editMessage(msgError.getInternalErrorUuid(logInfo.getId()))
				.setEmbeds(Collections.emptyList())
				.setComponents(Collections.emptyList())
				.queue();

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

		// Invoke command
		MessageCommandInvoker invoker = messageInvokers.get(commandName);
		try {

			if(invoker == null)
				throw new IllegalArgumentException(format(
					"No invoker found for MessageCommand `%s`.", commandName
				));

			invoker.invoke(event);

		} catch(CommandInvocationException | IllegalArgumentException e) {

			// Retrieve user and guild info for logging
			Long userId = Optional.of(event.getAuthor())
				.map(ISnowflake::getIdLong)
				.get();

			Long guildId = Optional.ofNullable(event.isFromGuild() ? event.getGuild() : null)
				.map(ISnowflake::getIdLong)
				.orElse(null);

			// Log the error
			log.error(e.getMessage(), e);
			LogErrorEntity logInfo = logErrorService.error(e, userId, guildId);

			// Reply to the user with generic error
			event.getMessage()
				.reply(msgError.getInternalErrorUuid(logInfo.getId()))
				.queue();

		}
	}

	public String serializeBtnOptions(
		String btnCmdName, Object... options
	) throws IllegalArgumentException, IOException {

		// Convert name to hash id
		CRC32 crc = new CRC32();
		crc.update(btnCmdName.getBytes());
		int cmdHashId = (int) crc.getValue();

		// Find invoker
		ButtonCommandInvoker invoker = buttonInvokers.get(cmdHashId);
		if(invoker == null)
			throw new IllegalArgumentException(format(
				"No invoker found for ButtonCommand `%s`.", btnCmdName
			));

		// Get parameter types and validate options count (exclude event)
		Class<?>[] optionTypes = invoker.getOptionTypes();
		if(options.length != optionTypes.length - 1)
			throw new IllegalArgumentException(format(
				"Expected %d options for ButtonCommand `%s`, but got %d.",
				optionTypes.length - 1, btnCmdName, options.length
			));

		byte[] packedOptions;
		try(MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {

			// Open packed options with cmdHashId
			packer.packArrayHeader(optionTypes.length);
			packer.packInt(cmdHashId);

			// Validate option types and pack them
			int iOpt = 0;
			for(Class<?> optionType : optionTypes) {

				// Skip event parameter
				if(ButtonInteractionEvent.class.isAssignableFrom(optionType))
					continue;

				// Validate option type
				Object option = options[iOpt];
				if(optionType != option.getClass())
					throw new IllegalArgumentException(format(
						"Expected option of type `%s` for parameter %d in ButtonCommand `%s`, but got `%s`.",
						optionType.getName(), iOpt + 1, btnCmdName, option.getClass()
							.getName()
					));

				// Pack option value
				packArgument(packer, option);
				iOpt++;

			}

			// Close packer and get packed options
			packer.close();
			packedOptions = packer.toByteArray();

		}

		// Encode packed options to Base64
		String base64Options = Base64.getEncoder()
			.encodeToString(packedOptions);

		// Check if encoded options exceed Discord's button ID limit
		if(base64Options.length() > Button.ID_MAX_LENGTH)
			throw new IllegalArgumentException(format(
				"Encoded options for ButtonCommand `%s` exceed maximum length of %d characters: %s.",
				btnCmdName, Button.ID_MAX_LENGTH, base64Options
			));

		return base64Options;
	}

	public static void packArgument(MessagePacker packer, Object argument) throws IOException {

		switch(argument) {

			case String str -> packer.packString(str);

			case Long l -> packer.packLong(l);

			case Integer i -> packer.packInt(i);

			case Double d -> packer.packDouble(d);

			case Boolean b -> packer.packBoolean(b);

			default -> throw new IllegalArgumentException(format(
				"Unsupported argument type: `%s`",
				argument.getClass()
					.getName()
			));

		}
	}

	public static <T> T unpackArgument(MessageUnpacker unpacker, Class<T> argType) throws IOException {

		if(argType == String.class)
			return argType.cast(unpacker.unpackString());

		if(argType == Long.class)
			return argType.cast(unpacker.unpackLong());

		if(argType == Integer.class)
			return argType.cast(unpacker.unpackInt());

		if(argType == Double.class)
			return argType.cast(unpacker.unpackDouble());

		if(argType == Boolean.class)
			return argType.cast(unpacker.unpackBoolean());

		throw new IllegalArgumentException(format(
			"Unsupported argument type: `%s`",
			argType.getName()
		));
	}

}