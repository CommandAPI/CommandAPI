package dev.jorel.commandapi.executors;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;

/**
 * A {@link TypedExecutor} for Velocity. The {@link CommandSource}s accepted by 
 * this executor can be defined by overriding the method {@link #types()}.
 * 
 * @param <Sender> The {@link CommandSource} class that this executor accepts.
 */
public interface VelocityTypedExecutor<Sender extends CommandSource> extends TypedExecutor<CommandSource, Sender, CommandSource> {
	@Override
	default ExecutionInfo<Sender, CommandSource> tryForSender(ExecutionInfo<CommandSource, CommandSource> info) {
		CommandSource sender = info.sender();

		for (ExecutorType type : types()) {
			// Check if we can cast to the defined sender type
			if (switch (type) {
				case ALL -> true;
				case PLAYER -> sender instanceof Player;
				case CONSOLE -> sender instanceof ConsoleCommandSource;
			}) {
				return (ExecutionInfo<Sender, CommandSource>) info;
			}
		}
		return null;
	}

	/**
	 * @return The {@link ExecutorType}s that this executor accepts.
	 */
	ExecutorType[] types();
}
