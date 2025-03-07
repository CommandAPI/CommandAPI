package dev.jorel.commandapi.executors;

import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;

/**
 * A {@link FunctionalInterface} for a command execution that inputs a command sender and {@link CommandArguments}
 * and returns an int result (see {@link ResultingExecutor#run(Object, CommandArguments)}).
 * 
 * @param <Sender> The class of the command sender for this executor.
 * @param <Source> The class for running Brigadier commands.
 */
@FunctionalInterface
public interface ResultingExecutor<Sender, Source> extends ResultingExecutorInfo<Sender, Source> {
	@Override
	default int run(ExecutionInfo<Sender, Source> info) throws WrapperCommandSyntaxException {
		return this.run(info.sender(), info.args());
	}

	/**
	 * Runs this executor.
	 *
	 * @param sender    The command sender.
	 * @param arguments The {@link CommandArguments} for this command.
	 * @return The int result of running this executor.
	 * @throws WrapperCommandSyntaxException if something goes wrong while running this executor.
	 */
	int run(Sender sender, CommandArguments arguments) throws WrapperCommandSyntaxException;
}
