package ovh.excale.vgreeter.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@AllArgsConstructor
@ConfigurationProperties(prefix = "probab")
public class ProbabilityMessages {

	/**
	 * Provided probability value is out of bounds (not between 0 and 100).
	 */
	@Getter
	private final String errorOutOfBounds;

	/**
	 * Update the Greet Probability.
	 * Has placeholders for the old and new probability values.
	 */
	private final String setFromTo;

	/**
	 * Update the Greet Probability.
	 * Has placeholders for the old and new probability values.
	 */
	public String getSetFromTo(float from, float to) {
		return String.format(setFromTo, from, to);
	}

	/**
	 * Show the currently set Greet Probability.
	 * Has a placeholder for the probability value.
	 */
	private final String currentlySet;

	/**
	 * Show the currently set Greet Probability.
	 * Has a placeholder for the probability value.
	 */
	public String getCurrentlySet(float probab) {
		return String.format(currentlySet, probab);
	}

	/**
	 * Reset the Greet Probability to its default value.
	 * Has a placeholder for the default probability value.
	 */
	private final String resetDefault;

	/**
	 * Reset the Greet Probability to its default value.
	 * Has a placeholder for the default probability value.
	 */
	public String getResetDefault(float defaultProbab) {
		return String.format(resetDefault, defaultProbab);
	}

}
