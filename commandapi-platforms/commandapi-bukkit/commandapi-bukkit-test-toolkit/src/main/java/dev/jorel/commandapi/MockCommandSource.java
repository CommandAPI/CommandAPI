package dev.jorel.commandapi;

import com.google.common.base.Preconditions;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

/**
 * An NMS independent implementation of CommandSourceStack. Used by Brigadier when parsing and executing commands.
 *
 * @param bukkitSender The Bukkit {@link CommandSender} this source represents.
 * @param location     The {@link Location} where this source is running the command from.
 * @param entity       The {@link Entity} this source is running the command as.
 */
public record MockCommandSource(CommandSender bukkitSender, Location location, Entity entity) {
	public MockCommandSource(CommandSender bukkitSender) {
		this(Preconditions.checkNotNull(bukkitSender), getLocation(bukkitSender), getEntity(bukkitSender));
	}

	private static Location getLocation(CommandSender sender) {
		if (sender instanceof Entity entity) {
			return entity.getLocation();
		} else if (sender instanceof BlockCommandSender block) {
			return block.getBlock().getLocation();
		} else if (sender instanceof NativeProxyCommandSender proxy) {
			return proxy.getLocation();
		} else {
			return new Location(null, 0, 0, 0);
		}
	}

	private static Entity getEntity(CommandSender sender) {
		return sender instanceof Entity entity ? entity : null;
	}
}
