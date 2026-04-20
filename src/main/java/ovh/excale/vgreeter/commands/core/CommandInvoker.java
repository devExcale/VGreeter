package ovh.excale.vgreeter.commands.core;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.RestAction;
import ovh.excale.vgreeter.commands.core.annotation.Option;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static ovh.excale.vgreeter.utilities.DiscordUtil.getOptionType;

public interface CommandInvoker<T extends GenericEvent> {

	void invoke(T event);

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
		String className = method.getDeclaringClass().getName();
		String methodName = method.getName();
		String qualifiedName = className + "." + methodName;

		List<OptionData> options = new ArrayList<>(25);

		// Validate all parameters
		int eventParams = 0;
		for(var param : method.getParameters()) {

			Option optionMeta = param.getAnnotation(Option.class);

			if(GenericEvent.class.isAssignableFrom(param.getType()))

				// GenericEvent found
				eventParams++;

			else if(optionMeta == null)

				// Parameter is not a GenericEvent and is not annotated with @Option
				throw new IllegalArgumentException(format(
					"Parameter %s in command method %s must either extend GenericEvent or be annotated with @Option.",
					param.getName(), qualifiedName
				));

			else

				// Create option data for parameter
				options.add(new OptionData(
					getOptionType(param.getType()),
					optionMeta.name(),
					optionMeta.description(),
					optionMeta.required()
				));
		}

		if(eventParams > 1)
			throw new IllegalArgumentException(format(
				"Command method %s must have at most one parameter that extends GenericEvent, but found %d.",
				qualifiedName, eventParams
			));

		return options;
	}

}
