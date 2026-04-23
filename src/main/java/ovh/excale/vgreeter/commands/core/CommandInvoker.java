package ovh.excale.vgreeter.commands.core;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.requests.RestAction;
import ovh.excale.vgreeter.commands.core.exception.CommandInvocationException;

import java.lang.reflect.Method;

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

}
