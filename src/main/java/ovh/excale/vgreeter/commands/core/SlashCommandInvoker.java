package ovh.excale.vgreeter.commands.core;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.RestAction;
import ovh.excale.vgreeter.commands.core.annotation.CmdOption;
import ovh.excale.vgreeter.commands.core.annotation.SlashMapping;
import ovh.excale.vgreeter.commands.core.exception.CommandInvocationException;
import ovh.excale.vgreeter.utilities.DiscordUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

/**
 * CommandInvoker implementation for handling slash commands.
 * <p>
 * This class is responsible for invoking methods annotated with {@link SlashMapping} when a slash command is executed.
 * It resolves method parameters based on the command options provided in the SlashCommandInteractionEvent.
 */
@Getter
@Log4j2
public class SlashCommandInvoker implements CommandInvoker<SlashCommandInteractionEvent> {

	/**
	 * The instance of the class containing the command method to be invoked.
	 */
	private final Object bean;

	/**
	 * The method to be invoked when the slash command is executed.
	 * This method is annotated with {@link SlashMapping}.
	 */
	private final Method method;

	/**
	 * The SlashMapping annotation instance containing metadata about the slash command,
	 * such as its name and description.
	 */
	private final SlashMapping mapping;

	/**
	 * A list of OptionData objects representing the options for the slash command,
	 * inferred from the method parameters annotated with {@link CmdOption}.
	 */
	private final List<OptionData> optionsData;

	/**
	 * Constructs a SlashCommandInvoker for the given bean and method.
	 *
	 * @param bean the instance of the class containing the command method
	 * @param method the method to be invoked when the slash command is executed, must be annotated with @SlashMapping
	 */
	public SlashCommandInvoker(Object bean, Method method) {

		this.bean = bean;
		this.method = method;
		this.mapping = method.getAnnotation(SlashMapping.class);

		CommandInvoker.validateReturnType(method);
		optionsData = inferOptionsData();

	}

	/**
	 * Invokes the command method with arguments resolved from the SlashCommandInteractionEvent.
	 *
	 * @param event the SlashCommandInteractionEvent containing the command context and options
	 * @throws CommandInvocationException if an error occurs during method invocation
	 */
	@Override
	public void invoke(SlashCommandInteractionEvent event) throws CommandInvocationException {
		try {

			Parameter[] params = method.getParameters();
			Object[] args = new Object[params.length];

			// Resolve arguments for each parameter
			for(int i = 0; i < params.length; i++)
				args[i] = getArgument(event, params[i]);

			// Invoke method with the dynamic argument array
			Object result = method.invoke(bean, args);

			// Run callback if provided
			if(result instanceof RestAction<?> restAction)
				restAction.queue();

		} catch (Exception e) {

			throw new CommandInvocationException(format(
				"Failed to invoke SlashCommand `%s` in class `%s`: %s",
				mapping.name().trim(),
				bean.getClass().getName(),
				e.getMessage()
			), e);

		}
	}

	/**
	 * Resolves the argument for a given parameter based on its type and annotations.
	 *
	 * @param event SlashCommandInteractionEvent containing the command context and options
	 * @param param The parameter for which to resolve the argument
	 * @return The resolved argument to be passed to the command method
	 * @throws IllegalArgumentException if a required option is missing or if type conversion fails
	 */
	private Object getArgument(SlashCommandInteractionEvent event, Parameter param) throws IllegalArgumentException {

		// Get parameter type
		Class<?> paramType = param.getType();

		// Inject discord event if requested
		if(paramType.isAssignableFrom(SlashCommandInteractionEvent.class))
			return event;

		// Inject parameters annotated with @CmdOption
		CmdOption optionMeta = param.getAnnotation(CmdOption.class);
		OptionMapping jdaOption = event.getOption(optionMeta.name());

		// Raise exception if required option is missing
		if(optionMeta.required() && jdaOption == null)
			throw new IllegalArgumentException(format(
				"Required option '%s' is missing for parameter '%s' in method '%s.%s'.",
				optionMeta.name(), param.getName(), method.getDeclaringClass().getName(), method.getName()
			));

		try {

			// Convert option value to parameter type
			return DiscordUtil.castOptionTo(paramType, jdaOption);

		} catch(IllegalArgumentException e) {
			throw new IllegalArgumentException(format(
				"Failed to convert option '%s' to type '%s' for parameter '%s' in method '%s.%s'.",
				optionMeta.name(),
				paramType.getName(),
				param.getName(),
				method.getDeclaringClass().getName(),
				method.getName()
			), e);
		}

	}

	/**
	 * Infers the list of OptionData for the slash command
	 * by analyzing the method parameters annotated with {@link CmdOption}.
	 *
	 * @return a list of OptionData representing the options for the slash command
	 * @throws IllegalArgumentException if a parameter is not a GenericEvent and is not annotated with @CmdOption,
	 *                                  or if more than one parameter extends GenericEvent
	 */
	private List<OptionData> inferOptionsData() throws IllegalArgumentException {

		// Get qualified method name for error messages
		String className = method.getDeclaringClass().getSimpleName();
		String methodName = method.getName();
		String qualifiedMethodName = className + "." + methodName;

		Parameter[] params = method.getParameters();
		List<OptionData> options = new ArrayList<>(params.length);

		// Validate all parameters
		int eventParams = 0;
		for(Parameter param : params) {

			CmdOption optionMeta = param.getAnnotation(CmdOption.class);

			if(GenericEvent.class.isAssignableFrom(param.getType()))
				// GenericEvent found
				eventParams++;

			else if(optionMeta == null)
				// Parameter is not a GenericEvent and is not annotated with @Option
				throw new IllegalArgumentException(format(
					"Parameter `%s` in `%s` must either extend %s or be annotated with @%s.",
					param.getName(),
					qualifiedMethodName,
					SlashCommandInteractionEvent.class.getSimpleName(),
					CmdOption.class.getSimpleName()
				));

			else
				// Add option data to list
				options.add(DiscordUtil.optionData(optionMeta, param));

		}

		if(eventParams > 1)
			throw new IllegalArgumentException(format(
				"Method `%s` must have at most one parameter that extends %s, but found %d.",
				qualifiedMethodName,
				GenericEvent.class.getSimpleName(),
				eventParams
			));

		return options;
	}

}
