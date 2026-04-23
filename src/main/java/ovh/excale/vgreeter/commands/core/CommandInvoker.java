package ovh.excale.vgreeter.commands.core;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.RestAction;
import ovh.excale.vgreeter.commands.core.annotation.CmdOption;
import ovh.excale.vgreeter.commands.core.exception.CommandInvocationException;
import ovh.excale.vgreeter.utilities.DiscordUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

public interface CommandInvoker<T extends GenericEvent> {

	void invoke(T event) throws CommandInvocationException;

	static void validateReturnType(Method method) {

		// Get qualified method name for error messages
		String className = method.getDeclaringClass().getName();
		String methodName = method.getName();
		String qualifiedName = className + "." + methodName;

		// Validate return type void or RestAction<?>
		Class<?> returnType = method.getReturnType();
		if(returnType != void.class && !RestAction.class.isAssignableFrom(returnType))
			throw new IllegalArgumentException(format(
				"Command method %s must return void or RestAction<?>, but returns %s.",
				qualifiedName, returnType.getName()
			));

	}

	static void validateParameterEventOnly(Method method) {

		// Get qualified method name for error messages
		String className = method.getDeclaringClass().getName();
		String methodName = method.getName();
		String qualifiedName = className + "." + methodName;

		// Check at most one parameter that extends GenericEvent only
		int eventParams = 0;
		for(var param : method.getParameters())

			if(GenericEvent.class.isAssignableFrom(param.getType()))
				eventParams++;
			else
				throw new IllegalArgumentException(format(
					"Parameter %s in command method %s must extend GenericEvent.",
					param.getName(), qualifiedName
				));

		if(eventParams > 1)
			throw new IllegalArgumentException(format(
				"Command method %s must have at most one parameter that extends GenericEvent, but found %d.",
				qualifiedName, eventParams
			));

	}

	static List<OptionData> validateParameterOptions(Method method) {

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
