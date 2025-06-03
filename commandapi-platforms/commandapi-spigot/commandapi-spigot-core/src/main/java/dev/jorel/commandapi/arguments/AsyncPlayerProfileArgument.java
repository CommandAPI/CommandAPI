package dev.jorel.commandapi.arguments;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPISpigot;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.profile.PlayerProfile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * An argument that represents the Bukkit OfflinePlayer object with asynchronous support.
 *
 * @since 9.7.1
 */
public class AsyncPlayerProfileArgument extends SafeOverrideableArgument<CompletableFuture<List<PlayerProfile>>, PlayerProfile> {

	/**
	 * A Player argument. Produces a single player, regardless of whether
	 * <code>@a</code>, <code>@p</code>, <code>@r</code> or <code>@e</code> is used.
	 *
	 * @param nodeName the name of the node for this argument
	 */
	public AsyncPlayerProfileArgument(String nodeName) {
		super(nodeName, CommandAPIBukkit.get().getNMS()._ArgumentProfile(), PlayerProfile::getName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<CompletableFuture<List<PlayerProfile>>> getPrimitiveType() {
		return (Class<CompletableFuture<List<PlayerProfile>>>) (Class<?>) CompletableFuture.class;
	}

	@Override
	public CommandAPIArgumentType getArgumentType() {
		return CommandAPIArgumentType.ASYNC_OFFLINE_PLAYER;
	}

	@Override
	public <CommandSourceStack> CompletableFuture<List<PlayerProfile>> parseArgument(CommandContext<CommandSourceStack> cmdCtx, String key, CommandArguments previousArgs) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return CommandAPISpigot.<CommandSourceStack>getSpigot().getProfile(cmdCtx, key);
			} catch (CommandSyntaxException e) {
				throw new RuntimeException(e);
			}
		});
	}
}