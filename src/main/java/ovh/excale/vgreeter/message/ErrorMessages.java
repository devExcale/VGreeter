package ovh.excale.vgreeter.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.text.MessageFormat;
import java.util.UUID;

@AllArgsConstructor
@ConfigurationProperties(prefix = "error")
public class ErrorMessages {

	/**
	 * Internal server error.
	 * Has a placeholder for the error UUID.
	 */
	private final String internalErrorUuid;

	/**
	 * Internal server error.
	 * Has a placeholder for the error UUID.
	 */
	public String getInternalErrorUuid(UUID uuid) {
		return MessageFormat.format(internalErrorUuid, uuid);
	}

	/**
	 * Command is used in a DM, but it can only be used in a guild.
	 */
	@Getter
	private final String cmdGuildOnly;

	/**
	 * Command is used in a guild, but the user is not connected to a voice channel.
	 */
	@Getter
	private final String memberMustConnectVc;

	/**
	 * Command is used in a guild, but the bot does not have permissions to connect to the user's voice channel.
	 * Has a placeholder for the voice channel name.
	 */
	private final String botNoVcPerms;

	/**
	 * Command is used in a guild, but the bot does not have permissions to connect to the user's voice channel.
	 * Has a placeholder for the voice channel name.
	 */
	public String getBotNoVcPerms(String channel) {
		return MessageFormat.format(botNoVcPerms, channel);
	}

	/**
	 * Command is used in a guild, but the bot is already connected to a voice channel.
	 * Has a placeholder for the voice channel name.
	 */
	public final String botConnectedToVc;

	/**
	 * Command is used in a guild, but the bot is already connected to a voice channel.
	 * Has a placeholder for the voice channel name.
	 */
	public String getBotConnectedToVc(String channel) {
		return MessageFormat.format(botConnectedToVc, channel);
	}

}
