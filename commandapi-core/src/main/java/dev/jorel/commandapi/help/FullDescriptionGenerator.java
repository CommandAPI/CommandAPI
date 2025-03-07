package dev.jorel.commandapi.help;

import java.util.Optional;

import javax.annotation.Nullable;

/**
 * A {@link FunctionalInterface} for generating command help full descriptions.
 * See {@link #getFullDescription(Object)}.
 * 
 * @param <CommandSender> The class for running platform commands.
 */
@FunctionalInterface
public interface FullDescriptionGenerator<CommandSender> {
	/**
	 * Returns an {@link Optional} containing the {@link String} that is the full description for this command help.
	 *
	 * @param forWho The {@code CommandSender} the full description should be generated for. For example, you
	 *               could test if this sender has permission to see a branch in your command. This
	 *               parameter may be null.
	 * @return An {@link Optional} {@link String} that is the full description for this command help.
	 */
	Optional<String> getFullDescription(@Nullable CommandSender forWho);
}
