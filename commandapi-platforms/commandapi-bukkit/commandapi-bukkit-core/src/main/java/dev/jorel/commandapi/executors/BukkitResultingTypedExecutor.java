package dev.jorel.commandapi.executors;

import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;

/**
 * A {@link BukkitTypedExecutor} for {@link ResultingExecutorInfo} lambdas that have an int result.
 * 
 * @param executor The {@link ResultingExecutorInfo} to invoke when running this executor.
 * @param types    The {@link ExecutorType}s that this executor accepts.
 * @param <Sender> The {@link CommandSender} class that this executor accepts.
 * @param <Source> The class for executing Brigadier commands.
 */
public record BukkitResultingTypedExecutor<Sender extends CommandSender, Source>(

	/**
	 * @return The {@link ResultingExecutorInfo} to invoke when running this executor.
	 */
	ResultingExecutorInfo<Sender, Source> executor,

	/**
	 * @return The {@link ExecutorType}s that this executor accepts.
	 */
	ExecutorType... types
) implements BukkitTypedExecutor<Sender, Source> {
	@Override
	public int executeWith(ExecutionInfo<Sender, Source> info) throws WrapperCommandSyntaxException {
		return executor.run(info);
	}
}
