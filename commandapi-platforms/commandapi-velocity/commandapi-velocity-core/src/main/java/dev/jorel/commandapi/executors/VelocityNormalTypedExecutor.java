package dev.jorel.commandapi.executors;

import com.velocitypowered.api.command.CommandSource;

import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;

/**
 * A {@link VelocityTypedExecutor} for {@link NormalExecutorInfo} lambdas that don't have an int result.
 * When running this executor succeeds, it simply returns 1.
 * 
 * @param executor The {@link NormalExecutorInfo} to invoke when running this executor.
 * @param types    The {@link ExecutorType}s that this executor accepts.
 * @param <Sender> The {@link CommandSource} class that this executor accepts.
 */
public record VelocityNormalTypedExecutor<Sender extends CommandSource>(

	/**
	 * @return The {@link NormalExecutorInfo} to invoke when running this executor.
	 */
	NormalExecutorInfo<Sender, CommandSource> executor,

	/**
	 * @return The {@link ExecutorType}s that this executor accepts.
	 */
	ExecutorType... types
) implements VelocityTypedExecutor<Sender> {
	@Override
	public int executeWith(ExecutionInfo<Sender, CommandSource> info) throws WrapperCommandSyntaxException {
		executor.run(info);
		return 1;
	}
}
