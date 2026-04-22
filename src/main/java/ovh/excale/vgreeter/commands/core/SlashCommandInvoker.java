package ovh.excale.vgreeter.commands.core;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.RestAction;
import ovh.excale.vgreeter.commands.core.annotation.CmdOption;
import ovh.excale.vgreeter.commands.core.annotation.SlashMapping;
import ovh.excale.vgreeter.commands.core.exception.CommandInvocationException;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static java.lang.String.format;
import static ovh.excale.vgreeter.utilities.DiscordUtil.castOptionTo;

@Getter
@Log4j2
public class SlashCommandInvoker implements CommandInvoker<SlashCommandInteractionEvent> {

	private final Object bean;
	private final Method method;
	private final SlashMapping mapping;
	private final OptionData[] options;

	public SlashCommandInvoker(Object bean, Method method) {

		this.bean = bean;
		this.method = method;
		this.mapping = method.getAnnotation(SlashMapping.class);

		CommandInvoker.validateReturnType(method);
		options = CommandInvoker.validateParameterOptions(method)
			.toArray(OptionData[]::new);

	}

	@Override
	public void invoke(SlashCommandInteractionEvent event) throws CommandInvocationException {
		try {

			Parameter[] params = method.getParameters();
			Object[] args = new Object[params.length];

			for(int i = 0; i < params.length; i++) {

				// Get parameter and its type
				Parameter param = params[i];
				Class<?> paramType = param.getType();

				// Inject discord event if requested
				if(paramType.isAssignableFrom(SlashCommandInteractionEvent.class)) {
					args[i] = event;
					continue;
				}

				// Inject parameters annotated with @Option
				CmdOption optionMeta = param.getAnnotation(CmdOption.class);
				OptionMapping jdaOption = event.getOption(optionMeta.name());

				// Raise exception if required option is missing
				if(optionMeta.required() && jdaOption == null)
					throw new IllegalArgumentException(format(
						"Required option '%s' is missing for parameter '%s' in method '%s.%s'.",
						optionMeta.name(), param.getName(), method.getDeclaringClass().getName(), method.getName()
					));

				// Convert option value to parameter type
				try {
					args[i] = castOptionTo(paramType, jdaOption);
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
}
