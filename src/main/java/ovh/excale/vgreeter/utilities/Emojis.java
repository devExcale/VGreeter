package ovh.excale.vgreeter.utilities;

import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.entities.emoji.Emoji;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class Emojis {

	public static final Emoji PREVIOUS = Emoji.fromUnicode("◀");

	public static final Emoji NEXT = Emoji.fromUnicode("▶");

	public static final Emoji RELOAD = Emoji.fromUnicode("🔄");

	public static final Emoji CLOSE = Emoji.fromUnicode("❌");

}
