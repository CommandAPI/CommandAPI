package dev.jorel.commandapi.help;

import java.util.Optional;

/**
 * A {@link FunctionalInterface} for generating command help short descriptions.
 * See {@link #getShortDescription()}.
 */
@FunctionalInterface
public interface ShortDescriptionGenerator {
	/**
	 * @return An {@link Optional} {@link String} that is the short description for this command help.
	 */
	Optional<String> getShortDescription();
}
