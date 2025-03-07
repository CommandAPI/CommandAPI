package dev.jorel.commandapi.executors;

import com.velocitypowered.api.command.CommandSource;

import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;

/**
 * A {@link VelocityTypedExecutor} for {@link ResultingExecutorInfo} lambdas that have an int result.
 * 
 * @param executor The {@link ResultingExecutorInfo} to invoke when running this executor.
 * @param types    The {@link ExecutorType}s that this executor accepts.
 * @param <Sender> The {@link CommandSource} class that this executor accepts.
 */
public record VelocityResultingTypedExecutor<Sender extends CommandSource>(

	/**
	 * @return The {@link ResultingExecutorInfo} to invoke when running this executor.
	 */
	ResultingExecutorInfo<Sender, CommandSource> executor,

	/**
	 * @return The {@link ExecutorType}s that this executor accepts.
	 */
	ExecutorType... types
) implements VelocityTypedExecutor<Sender> {
	@Override
	public int executeWith(ExecutionInfo<Sender, CommandSource> info) throws WrapperCommandSyntaxException {
		return executor.run(info);
	}
}
