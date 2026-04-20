package ovh.excale.vgreeter.message;

import lombok.AllArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.text.MessageFormat;

@AllArgsConstructor
@ConfigurationProperties(prefix = "track")
public class TrackMessages {

	/**
	 * Message shown when a track with the provided ID does not exist.
	 * Has a placeholder for the track ID.
	 */
	private final String errorNoSuchId;

	/**
	 * Message shown when a track with the provided ID does not exist.
	 * Has a placeholder for the track ID.
	 */
	public String getErrorNoSuchId(long id) {
		return MessageFormat.format(errorNoSuchId, id);
	}

	/**
	 * Message shown when a user doesn't own a track with the provided ID.
	 * Has a placeholder for the track ID.
	 */
	private final String errorOwnNoSuchId;

	/**
	 * Message shown when a user doesn't own a track with the provided ID.
	 * Has a placeholder for the track ID.
	 */
	public String getErrorOwnNoSuchId(long id) {
		return MessageFormat.format(errorOwnNoSuchId, id);
	}

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

	/**
	 * Message shown when a track is deleted.
	 * Has placeholders for the track ID and title.
	 */
	private final String deletedIdTitle;

	/**
	 * Message shown when a track is deleted.
	 * Has placeholders for the track ID and title.
	 */
	public String getDeletedIdTitle(long id, String title) {
		return MessageFormat.format(deletedIdTitle, id, title);
	}

	/**
	 * Message shown when a track title is invalid.
	 * Has a placeholder for the title.
	 */
	private final String invalidTitle;

	/**
	 * Message shown when a track title is invalid.
	 * Has a placeholder for the title.
	 */
	public String getInvalidTitle(String title) {
		return MessageFormat.format(invalidTitle, title);
	}

	/**
	 * Message shown when a track title is not unique for the user.
	 * Has a placeholder for the title and track ID.
	 */
	private final String uniqueTitleAndOwner;

	/**
	 * Message shown when a track title is not unique for the user.
	 * Has a placeholder for the title and track ID.
	 */
	public String getUniqueTitleAndOwner(String title, long id) {
		return MessageFormat.format(uniqueTitleAndOwner, title, id);
	}

}
