package ovh.excale.vgreeter.commands.core;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.requests.RestAction;
import ovh.excale.vgreeter.commands.core.annotation.ButtonMapping;

import java.lang.reflect.Method;

@Getter
@Log4j2
public class ButtonCommandInvoker implements CommandInvoker<ButtonInteractionEvent> {

	private final Object bean;
	private final Method method;
	private final ButtonMapping mapping;

	public ButtonCommandInvoker(Object bean, Method method) {

		this.bean = bean;
		this.method = method;
		this.mapping = method.getAnnotation(ButtonMapping.class);

		CommandInvoker.validateReturnType(method);
		CommandInvoker.validateParameterEventOnly(method);

	}

	@Override
	public void invoke(ButtonInteractionEvent event) {
		try {

			// Invoke the method on the target bean
			Object result = method.invoke(bean, event);

			// If the method returns a RestAction, queue it automatically
			if(result instanceof RestAction<?> restAction)
				restAction.queue();

		} catch(Exception e) {
			// TODO: Proper error handling
			log.error("Error invoking command method: {}", e.getMessage(), e);
		}
	}

}
