package ovh.excale.vgreeter.commands.core;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import ovh.excale.vgreeter.commands.core.CommandKeyword.KeywordDecoder;
import ovh.excale.vgreeter.commands.core.CommandKeyword.KeywordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@JsonInclude(NON_EMPTY)
@Accessors(chain = true)
@Getter
@Setter
public class CommandOptions {

	private static final ObjectMapper objWriter = new ObjectMapper();
	private static final ObjectReader objReader = objWriter.readerFor(CommandOptions.class);

	public static CommandOptions fromJson(String payload) throws JsonProcessingException {
		return objReader.readValue(payload);
	}

	@JsonProperty(CommandKeyword.COMMAND)
	private final String command;

	@JsonProperty(CommandKeyword.SUBCOMMAND)
	private final String subcommand;

	@JsonProperty(CommandKeyword.PAGE)
	private Integer page;

	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	@JsonAnySetter
	@JsonAnyGetter
	@JsonSerialize(keyUsing = KeywordEncoder.class)
	@JsonDeserialize(keyUsing = KeywordDecoder.class)
	private final Map<String, String> extendedOptions;

	public CommandOptions(String command) {
		this(command, null);
	}

	@JsonCreator
	public CommandOptions(@JsonProperty(CommandKeyword.COMMAND) String command,
			@JsonProperty(CommandKeyword.SUBCOMMAND) String subcommand) {
		this.command = Objects.requireNonNull(command);
		this.subcommand = subcommand;
		extendedOptions = new HashMap<>();
	}

	public boolean hasSubcommand() {
		return subcommand != null && !subcommand.isEmpty();
	}

	@JsonIgnore
	public int getPageSafe() {
		return page != null ? page : 1;
	}

	public Optional<String> getOption(String key) {
		return Optional.ofNullable(extendedOptions.get(key));
	}

	public CommandOptions putOption(String key, Object value) {
		extendedOptions.put(key, value.toString());
		return this;
	}

	public String json() throws JsonProcessingException {
		return objWriter.writeValueAsString(this);
	}

	@Override
	public String toString() {
		return "CommandOptions{" + "command='" + command + '\'' + ", subcommand='" + subcommand + '\'' + ", page=" +
				page + ", extendedOptions=" + extendedOptions + '}';
	}

}


