package dev.jorel.commandapi.help;

import java.util.Optional;

import javax.annotation.Nullable;

import dev.jorel.commandapi.RegisteredCommand;

/**
 * A {@link FunctionalInterface} for generating command help usages.
 * See {@link #getUsage(Object, RegisteredCommand.Node)}.
 * 
 * @param <CommandSender> The class for running platform commands.
 */
@FunctionalInterface
public interface UsageGenerator<CommandSender> {
	/**
	 * Returns an {@link Optional} containing a {@code String[]}, where each item in the array
	 * represents a possible way to use the command.
	 *
	 * @param forWho       The {@code CommandSender} the usage should be generated for. For example, you
	 *                     could test if this sender has permission to see a branch in your command. This
	 *                     parameter may be null.
	 * @param argumentTree The {@link RegisteredCommand.Node} that is the root node of the command the usage
	 *                     should be generated for. This parameter may be null.
	 * @return An {@link Optional} {@link String} array with the usage for this command help.
	 */
	Optional<String[]> getUsage(@Nullable CommandSender forWho, @Nullable RegisteredCommand.Node<CommandSender> argumentTree);
}
