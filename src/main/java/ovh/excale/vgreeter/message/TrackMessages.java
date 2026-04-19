package ovh.excale.vgreeter.message;

import lombok.AllArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.text.MessageFormat;

@AllArgsConstructor
@ConfigurationProperties(prefix = "track")
public class TrackMessages {

	/**
	 * Message shown when a track is played.
	 * Has placeholders for the track ID and title.
	 */
	private final String playingIdTitle;

	/**
	 * Message shown when a track is played.
	 * Has placeholders for the track ID and title.
	 */
	public String getPlayingIdTitle(long id, String title) {
		return MessageFormat.format(playingIdTitle, id, title);
	}

}
