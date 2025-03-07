package dev.jorel.commandapi.executors;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.PaperImplementations;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;

/**
 * A {@link TypedExecutor} for Bukkit. The {@link CommandSender}s accepted by 
 * this executor can be defined by overriding the method {@link #types()}.
 * 
 * @param <Sender> The {@link CommandSender} class that this executor accepts.
 * @param <Source> The class for executing Brigadier commands.
 */
public interface BukkitTypedExecutor<Sender extends CommandSender, Source> extends TypedExecutor<CommandSender, Sender, Source> {
	@Override
	default ExecutionInfo<Sender, Source> tryForSender(ExecutionInfo<CommandSender, Source> info) {
		CommandSender sender = info.sender();

		for (ExecutorType type : types()) {
			// Check if we can cast to the defined sender type
			if (switch (type) {
				case ALL -> true;
				case PLAYER -> sender instanceof Player;
				case ENTITY -> sender instanceof Entity;
				case CONSOLE -> sender instanceof ConsoleCommandSender;
				case BLOCK -> sender instanceof BlockCommandSender;
				case PROXY -> sender instanceof ProxiedCommandSender;
				case NATIVE -> {
					// If we're a NATIVE executor, always accept and convert sender to a NativeProxyCommandSender
					NativeProxyCommandSender proxyCommandSender = CommandAPIBukkit.<Source>get().getNativeProxyCommandSender(sender, info.cmdCtx().getSource());
					info = info.copyWithNewSender(proxyCommandSender);
					yield true;
				}
				case REMOTE -> sender instanceof RemoteConsoleCommandSender;
				case FEEDBACK_FORWARDING -> {
					PaperImplementations<?> paper = CommandAPIBukkit.get().getPaper();
					yield paper.isPaperPresent() && paper.getFeedbackForwardingCommandSender().isInstance(sender);
				}
			}) {
				return (ExecutionInfo<Sender, Source>) info;
			}
		}
		return null;
	}

	/**
	 * @return The {@link ExecutorType}s that this executor accepts.
	 */
	ExecutorType[] types();
}
