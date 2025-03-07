package dev.jorel.commandapi.executors;

import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;

/**
 * An interface that includes what command senders it can execute ({@link #tryForSender(ExecutionInfo)}) 
 * and has a method that executes an executor with a given {@link ExecutionInfo} ({@link #executeWith(ExecutionInfo)}).
 * 
 * @param <CommandSender> The class for executing platform commands.
 * @param <Sender> The class that this executor accepts.
 * @param <Source> The class for running Brigadier commands.
 */
public interface TypedExecutor<CommandSender, Sender extends CommandSender, Source> {
	
	/**
	 * Checks if the sender in the given {@link ExecutionInfo} can use this executor.
	 * If it can, an {@link ExecutionInfo} compatible with this executor is returned.
	 * If not, null is returned.
	 * 
	 * @param info The {@link ExecutionInfo} that is trying to use this executor.
	 * @return An {@link ExecutionInfo} object that can use this executor, and null otherwise.
	 */
	ExecutionInfo<Sender, Source> tryForSender(ExecutionInfo<CommandSender, Source> info);

	/**
	 * Executes the command executor with the provided command sender and the provided arguments.
	 * 
	 * @param info The ExecutionInfo for this command
	 * @return the value returned by this command if the command succeeds, 0 if the command fails
	 * @throws WrapperCommandSyntaxException if an error occurs during the execution of this command
	 */
	int executeWith(ExecutionInfo<Sender, Source> info) throws WrapperCommandSyntaxException;
}
