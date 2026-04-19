package ovh.excale.vgreeter.utilities;

import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.requests.RestAction;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class DiscordUtil {

	public static RestAction<?> replyEphemeralWith(String message, IReplyCallback event) {
		return event.reply(message)
			.setEphemeral(true);
	}

}
