package dev.jorel.commandapi.arguments;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.OfflinePlayer;

import java.util.concurrent.CompletableFuture;

/**
 * An argument that represents the Bukkit OfflinePlayer object with asynchronous support.
 *
 * @since 9.7.1
 */
public class AsyncOfflinePlayerArgument extends SafeOverrideableArgument<CompletableFuture<OfflinePlayer>, OfflinePlayer> {

	/**
	 * A Player argument. Produces a single player, regardless of whether
	 * <code>@a</code>, <code>@p</code>, <code>@r</code> or <code>@e</code> is used.
	 *
	 * @param nodeName the name of the node for this argument
	 */
	public AsyncOfflinePlayerArgument(String nodeName) {
		super(nodeName, CommandAPIBukkit.get()._ArgumentProfile(), OfflinePlayer::getName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<CompletableFuture<OfflinePlayer>> getPrimitiveType() {
		return (Class<CompletableFuture<OfflinePlayer>>) (Class<?>) CompletableFuture.class;
	}

	@Override
	public CommandAPIArgumentType getArgumentType() {
		return CommandAPIArgumentType.ASYNC_OFFLINE_PLAYER;
	}

	@Override
	public <CommandSourceStack> CompletableFuture<OfflinePlayer> parseArgument(CommandContext<CommandSourceStack> cmdCtx, String key, CommandArguments previousArgs) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return CommandAPIBukkit.<CommandSourceStack>get().getOfflinePlayer(cmdCtx, key);
			} catch (CommandSyntaxException e) {
				throw new RuntimeException(e);
			}
		});
	}
}
