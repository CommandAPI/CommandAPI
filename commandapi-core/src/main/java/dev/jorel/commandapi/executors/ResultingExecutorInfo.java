package dev.jorel.commandapi.executors;

import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;

/**
 * A {@link FunctionalInterface} for a command execution that inputs a {@link ExecutionInfo}
 * and returns an int result (see {@link ResultingExecutorInfo#run(ExecutionInfo)}).
 * 
 * @param <Sender> The class of the command sender for this executor.
 * @param <Source> The class for running Brigadier commands.
 */
@FunctionalInterface
public interface ResultingExecutorInfo<Sender, Source> {
	/**
	 * Runs this executor.
	 *
	 * @param info The {@link ExecutionInfo} for this command.
	 * @return The int result of running this executor.
	 * @throws WrapperCommandSyntaxException if something goes wrong while running this executor.
	 */
	int run(ExecutionInfo<Sender, Source> info) throws WrapperCommandSyntaxException;
}
