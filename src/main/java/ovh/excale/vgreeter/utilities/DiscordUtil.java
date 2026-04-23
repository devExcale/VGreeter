package ovh.excale.vgreeter.utilities;

import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import ovh.excale.vgreeter.commands.core.annotation.CmdOption;

import java.lang.reflect.Parameter;

import static java.lang.String.format;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class DiscordUtil {

	public static ReplyCallbackAction replyEphemeralWith(String message, IReplyCallback event) {
		return event.reply(message)
			.setEphemeral(true);
	}

	public static OptionData optionData(CmdOption optionMeta, Parameter parameter) throws IllegalArgumentException {

		// Create option data for parameter
		OptionData optionData = new OptionData(
			optionType(parameter.getType()),
			optionMeta.name(),
			optionMeta.description(),
			optionMeta.required()
		);

		// Set option constraints if specified
		if(optionMeta.minValueD() != OptionData.MIN_NEGATIVE_NUMBER)
			optionData.setMinValue(optionMeta.minValueD());

		if(optionMeta.maxValueD() != OptionData.MAX_POSITIVE_NUMBER)
			optionData.setMaxValue(optionMeta.maxValueD());

		if(optionMeta.minValueL() != (long) OptionData.MIN_NEGATIVE_NUMBER)
			optionData.setMinValue(optionMeta.minValueL());

		if(optionMeta.maxValueL() != (long) OptionData.MAX_POSITIVE_NUMBER)
			optionData.setMaxValue(optionMeta.maxValueL());

		if(optionMeta.minLength() > 0)
			optionData.setMinLength(optionMeta.minLength());

		if(optionMeta.maxLength() < Integer.MAX_VALUE)
			optionData.setMaxLength(optionMeta.maxLength());

		return optionData;
	}

	public static OptionType optionType(Class<?> type) throws IllegalArgumentException {

		if(type == String.class)
			return OptionType.STRING;

		if(type == Long.class)
			return OptionType.INTEGER;

		if(type == Integer.class)
			return OptionType.INTEGER;

		if(type == Double.class)
			return OptionType.NUMBER;

		if(type == Boolean.class)
			return OptionType.BOOLEAN;

		throw new IllegalArgumentException("Unsupported option type: " + type.getName());
	}

	public static <T> T castOptionTo(Class<T> type, OptionMapping option) {

		if(option == null)
			return null;

		if(type == String.class)
			return type.cast(option.getAsString());

		if(type == Long.class)
			return type.cast(option.getAsLong());

		if(type == Integer.class)
			return type.cast(option.getAsInt());

		if(type == Double.class)
			return type.cast(option.getAsDouble());

		if(type == Boolean.class)
			return type.cast(option.getAsBoolean());

		throw new IllegalArgumentException(format("Unknown option type: %s", type.getName()));
	}

}
