package dev.jorel.commandapi.executors;

import com.mojang.brigadier.context.CommandContext;

/**
 * This interface represents the information of running a command. It provides the sender of a command, as well as its arguments.
 *
 * @param sender          The sender of this command
 * @param args            The arguments of this command
 * @param cmdCtx          The Brigadier {@link CommandContext} that is running the commands
 * @param <CommandSender> The type of the sender of a command this ExecutionInfo belongs to
 * @param <Source>        The class for running Brigadier commands
 */
public record ExecutionInfo<CommandSender, Source>(

	/**
	 * @return The sender of this command
	 */
	CommandSender sender,

	/**
	 * @return The arguments of this command
	 */
	CommandArguments args,

	/**
	 * @return cmdCtx The Brigadier {@link CommandContext} that is running the commands
	 */
	CommandContext<Source> cmdCtx
) {
	/**
	 * Copies this {@link ExecutionInfo} for a different command sender
	 * 
	 * @param <Sender> The class of the new command sender
	 * @param newSender The new command sender
	 * @return A new {@link ExecutionInfo} object that uses the given command sender
	 */
	public <Sender extends CommandSender> ExecutionInfo<Sender, Source> copyWithNewSender(Sender newSender) {
		return new ExecutionInfo<>(newSender, args, cmdCtx);
	}
}
