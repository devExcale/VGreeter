package ovh.excale.vgreeter.utilities;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

// TODO: USER/MEMBER PARSE
public class ArgumentsParser {

	private final String rawArguments;
	private final String[] splitArguments;

	public ArgumentsParser(String rawArguments) {
		this.rawArguments = rawArguments;
		splitArguments = rawArguments
				.replaceAll(" {2,}", " ")
				.trim()
				.split(" ");
	}

	public String getRawArguments() {
		return rawArguments;
	}

	public String[] getSplitArguments() {
		return splitArguments;
	}

	public <T> Optional<T> getArgumentCast(int index, Function<String, T> parser) {

		Optional<T> opt = Optional.empty();
		if(index < splitArguments.length)
			try {

				opt = Optional.of(parser.apply(splitArguments[index]));

			} catch(Exception ignored) {
			}

		return opt;
	}

	public Optional<String> getArgumentString(int index) {
		return getArgumentCast(index, String::toString);
	}

	public @NotNull String getArgumentString(int index, @NotNull String defValue) {
		return getArgumentString(index).orElse(defValue);
	}

	public Optional<Integer> getArgumentInteger(int index) {
		return getArgumentCast(index, Integer::parseInt);
	}

	public int getArgumentInteger(int index, int defValue) {
		return getArgumentInteger(index).orElse(defValue);
	}

	public Optional<Long> getArgumentLong(int index) {
		return getArgumentCast(index, Long::parseLong);
	}

	public long getArgumentLong(int index, long defValue) {
		return getArgumentLong(index).orElse(defValue);
	}

	public Optional<Boolean> getArgumentBoolean(int index) {
		return getArgumentCast(index, Boolean::parseBoolean);
	}

	public boolean getArgumentBoolean(int index, boolean defValue) {
		return getArgumentBoolean(index).orElse(defValue);
	}

	public Optional<String> getArgumentText(int index) {

		Optional<String> opt = Optional.empty();
		if(index < splitArguments.length)
			opt = Optional.of(String.join(" ", Arrays.copyOfRange(splitArguments, index, splitArguments.length)));

		return opt;
	}

	public @NotNull String getArgumentText(int index, @NotNull String defValue) {
		return getArgumentText(index).orElse(defValue);
	}

}
