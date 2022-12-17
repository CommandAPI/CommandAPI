package dev.jorel.commandapi.executors;

import dev.jorel.commandapi.commandsenders.BukkitCommandSender;

/**
 * This record represents a BukkitExecutionInfo for a command. It provides the sender of a command, as well as it's arguments
 *
 * @param <Sender> The type of the sender of a command this BukkitExecutionInfo belongs to
 */
public record BukkitExecutionInfo<Sender>(

	/**
	 * @return The sender of this command
	 */
	Sender sender,

	/**
	 * @return The wrapper type of this command
	 */
	BukkitCommandSender<? extends Sender> senderWrapper,

	/**
	 * @return The arguments of this command
	 */
	CommandArguments args

) implements AbstractExecutionInfo<Sender, BukkitCommandSender<? extends Sender>> {
}
