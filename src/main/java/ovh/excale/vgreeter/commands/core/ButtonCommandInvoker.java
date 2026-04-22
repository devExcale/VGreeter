package ovh.excale.vgreeter.commands.core;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.requests.RestAction;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import ovh.excale.vgreeter.commands.core.annotation.BtnOption;
import ovh.excale.vgreeter.commands.core.annotation.ButtonMapping;
import ovh.excale.vgreeter.commands.core.exception.CommandInvocationException;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static java.lang.String.format;

@Getter
@Log4j2
public class ButtonCommandInvoker implements CommandInvoker<ButtonInteractionEvent> {

	private final Object bean;
	private final Method method;
	private final ButtonMapping mapping;
	private final Class<?>[] optionTypes;

	public ButtonCommandInvoker(Object bean, Method method) {

		this.bean = bean;
		this.method = method;
		this.mapping = method.getAnnotation(ButtonMapping.class);

		CommandInvoker.validateReturnType(method);
		this.optionTypes = extractOptionTypes();

	}

	private Class<?>[] extractOptionTypes() {

		// Get qualified method name for error messages
		String className = method.getDeclaringClass().getSimpleName();
		String methodName = method.getName();
		String qualifiedMethodName = className + "." + methodName;

		Parameter[] params = method.getParameters();
		List<Class<?>> options = new ArrayList<>(params.length);

		int eventParams = 0;
		for(Parameter param : params) {

			//noinspection ExtractMethodRecommender
			BtnOption optionMeta = param.getAnnotation(BtnOption.class);
			boolean buttonEvent = ButtonInteractionEvent.class.isAssignableFrom(param.getType());

			// Parameter not ButtonInteractionEvent nor annotated with @BtnOption
			if(!buttonEvent && optionMeta == null)
				throw new IllegalArgumentException(format(
					"Parameter `%s` in %s `%s` must either extend %s or be annotated with @%s.",
					param.getName(),
					ButtonMapping.class.getSimpleName(),
					qualifiedMethodName,
					ButtonInteractionEvent.class.getSimpleName(),
					BtnOption.class.getSimpleName()
				));

			// ButtonInteractionEvent found
			if(buttonEvent)
				eventParams++;

			// Save parameter type
			options.add(param.getType());

		}

		if(eventParams > 1)
			throw new IllegalArgumentException(format(
				"Method `%s` must have at most one parameter that extends %s, but found %d.",
				qualifiedMethodName,
				ButtonInteractionEvent.class.getSimpleName(),
				eventParams
			));

		return options.toArray(Class<?>[]::new);
	}

	@Override
	public void invoke(ButtonInteractionEvent event) throws CommandInvocationException {

		String payloadBase64 = event.getComponentId();
		byte[] payloadBytes = Base64.getDecoder().decode(payloadBase64);

		try(MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(payloadBytes)) {

			// Skip the packed command hash stored by the dispatcher
			int argsCount = unpacker.unpackArrayHeader();
			unpacker.unpackInt();

			// Get method parameters and prepare arguments array
			Object[] args = new Object[argsCount];
			for(int i = 0; i < argsCount; i++) {

				Class<?> type = optionTypes[i];
				args[i] = ButtonInteractionEvent.class != type
					? CommandDispatcher.unpackArgument(unpacker, type)
					: event;

			}

			// Invoke the method on the target bean
			Object result = method.invoke(bean, args);

			// If the method returns a RestAction, queue it automatically
			if(result instanceof RestAction<?> restAction)
				restAction.queue();

		} catch (Exception e) {

			throw new CommandInvocationException(format(
				"Failed to invoke ButtonCommand `%s` in class `%s`: %s",
				mapping.name().trim(),
				bean.getClass().getName(),
				e.getMessage()
			), e);

		}
	}

}
