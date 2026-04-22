package ovh.excale.vgreeter.commands.core;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import ovh.excale.vgreeter.commands.core.annotation.MessageMapping;
import ovh.excale.vgreeter.commands.core.exception.CommandInvocationException;

import java.lang.reflect.Method;

import static java.lang.String.format;

@Getter
@Log4j2
public class MessageCommandInvoker implements CommandInvoker<MessageReceivedEvent> {

	private final Object bean;
	private final Method method;
	private final MessageMapping mapping;

	public MessageCommandInvoker(Object bean, Method method) {

		this.bean = bean;
		this.method = method;
		this.mapping = method.getAnnotation(MessageMapping.class);

		CommandInvoker.validateReturnType(method);
		CommandInvoker.validateParameterEventOnly(method);

	}

	@Override
	public void invoke(MessageReceivedEvent event) throws CommandInvocationException {
		try {

			// Invoke the method on the target bean
			Object result = method.invoke(bean, event);

			// If the method returns a RestAction, queue it automatically
			if(result instanceof RestAction<?> restAction)
				restAction.queue();

		} catch (Exception e) {

			throw new CommandInvocationException(format(
				"Failed to invoke MessageCommand `%s` in class `%s`: %s",
				mapping.name().trim(),
				bean.getClass().getName(),
				e.getMessage()
			), e);

		}
	}

}
