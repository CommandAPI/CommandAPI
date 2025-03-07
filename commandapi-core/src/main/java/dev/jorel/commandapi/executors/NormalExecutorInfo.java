package dev.jorel.commandapi.executors;

import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;

/**
 * A {@link FunctionalInterface} for a command execution that inputs a {@link ExecutionInfo}
 * and returns no result (see {@link NormalExecutorInfo#run(ExecutionInfo)}). When running 
 * this executor, the result will by 1 if successful and 0 otherwise.
 * 
 * @param <Sender> The class of the command sender for this executor.
 * @param <Source> The class for running Brigadier commands.
 */
@FunctionalInterface
public interface NormalExecutorInfo<Sender, Source> {
	/**
	 * Runs this executor.
	 *
	 * @param info The {@link ExecutionInfo} for this command.
	 * @throws WrapperCommandSyntaxException if something goes wrong while running this executor.
	 */
	void run(ExecutionInfo<Sender, Source> info) throws WrapperCommandSyntaxException;
}
