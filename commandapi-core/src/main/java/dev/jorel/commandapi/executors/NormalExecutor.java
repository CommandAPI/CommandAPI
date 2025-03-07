package dev.jorel.commandapi.executors;

import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;

/**
 * A {@link FunctionalInterface} for a command execution that inputs a command sender and {@link CommandArguments}
 * and returns no result (see {@link NormalExecutor#run(Object, CommandArguments)}). When running this executor, 
 * the result will by 1 if successful and 0 otherwise.
 * 
 * @param <Sender> The class of the command sender for this executor.
 * @param <Source> The class for running Brigadier commands.
 */
@FunctionalInterface
public interface NormalExecutor<Sender, Source> extends NormalExecutorInfo<Sender, Source> {
	@Override
	default void run(ExecutionInfo<Sender, Source> info) throws WrapperCommandSyntaxException {
		this.run(info.sender(), info.args());
	}

	/**
	 * Runs this executor.
	 *
	 * @param sender    The command sender.
	 * @param arguments The {@link CommandArguments} for this command.
	 * @throws WrapperCommandSyntaxException if something goes wrong while running this executor.
	 */
	void run(Sender sender, CommandArguments arguments) throws WrapperCommandSyntaxException;
}
