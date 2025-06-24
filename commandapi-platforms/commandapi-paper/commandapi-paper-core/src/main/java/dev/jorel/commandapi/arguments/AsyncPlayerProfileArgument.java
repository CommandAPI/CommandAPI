package dev.jorel.commandapi.arguments;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPIPaper;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * An argument that represents a list of the Paper PlayerProfile object with asynchronous support.
 *
 * @since 11.0.0
 */
public class AsyncPlayerProfileArgument extends SafeOverrideableArgument<CompletableFuture<List<PlayerProfile>>, PlayerProfile> {

	/**
	 * A PlayerProfile argument. Returns a {@link List<PlayerProfile>}
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
				return CommandAPIPaper.<CommandSourceStack>getPaper().getNMS().getProfile(cmdCtx, key);
			} catch (CommandSyntaxException e) {
				throw new RuntimeException(e);
			}
		});
	}
}