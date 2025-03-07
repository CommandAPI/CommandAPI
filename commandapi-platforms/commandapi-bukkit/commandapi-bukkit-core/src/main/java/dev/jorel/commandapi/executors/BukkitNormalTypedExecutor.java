package dev.jorel.commandapi.executors;

import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;

/**
 * A {@link BukkitTypedExecutor} for {@link NormalExecutorInfo} lambdas that don't have an int result.
 * When running this executor succeeds, it simply returns 1.
 * 
 * @param executor The {@link NormalExecutorInfo} to invoke when running this executor.
 * @param types    The {@link ExecutorType}s that this executor accepts.
 * @param <Sender> The {@link CommandSender} class that this executor accepts.
 * @param <Source> The class for executing Brigadier commands.
 */
public record BukkitNormalTypedExecutor<Sender extends CommandSender, Source>(

	/**
	 * @return The {@link NormalExecutorInfo} to invoke when running this executor.
	 */
	NormalExecutorInfo<Sender, Source> executor,

	/**
	 * @return The {@link ExecutorType}s that this executor accepts.
	 */
	ExecutorType... types
) implements BukkitTypedExecutor<Sender, Source> {
	@Override
	public int executeWith(ExecutionInfo<Sender, Source> info) throws WrapperCommandSyntaxException {
		executor.run(info);
		return 1;
	}
}
