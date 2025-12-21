package dev.jorel.commandapi;

import dev.jorel.commandapi.commandsenders.BukkitBlockCommandSender;
import dev.jorel.commandapi.commandsenders.BukkitCommandSender;
import dev.jorel.commandapi.commandsenders.BukkitConsoleCommandSender;
import dev.jorel.commandapi.commandsenders.BukkitEntity;
import dev.jorel.commandapi.commandsenders.BukkitNativeProxyCommandSender;
import dev.jorel.commandapi.commandsenders.BukkitPlayer;
import dev.jorel.commandapi.commandsenders.BukkitProxiedCommandSender;
import dev.jorel.commandapi.commandsenders.BukkitRemoteConsoleCommandSender;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.nms.SpigotNMS;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

public abstract class CommandAPISpigot<Source> extends CommandAPIBukkit<Source> implements SpigotNMS<Source> {
	private static CommandAPISpigot<?> spigot;

	protected CommandAPISpigot(InternalSpigotConfig config) {
		this.nms = bukkitNMS();
		CommandAPIBukkit.config = config;
		CommandAPISpigot.spigot = this;
	}

	@SuppressWarnings("unchecked")
	public static <Source> CommandAPISpigot<Source> getSpigot() {
		if (spigot != null) {
			return (CommandAPISpigot<Source>) spigot;
		}
		throw new IllegalStateException("Tried to access CommandAPISpigot instance, but it was null! Are you using CommandAPI features before calling CommandAPI#onLoad?");
	}

	public static InternalSpigotConfig getConfiguration() {
		return (InternalSpigotConfig) CommandAPIBukkit.getConfiguration();
	}

	/**
	 * Enables the CommandAPI. This should be placed at the start of your
	 * <code>onEnable()</code> method.
	 */
	@Override
	public void onEnable() {
		super.plugin = getConfiguration().getPlugin();

		Bukkit.getScheduler().scheduleSyncDelayedTask(CommandAPIBukkit.get().plugin, () -> {
			// Sort out permissions after the server has finished registering them all
			CommandAPIBukkit.get().getCommandRegistrationStrategy().runTasksAfterServerStart();
			if (!getConfiguration().skipReloadDatapacks()) {
				CommandAPIBukkit.get().reloadDataPacks();
			}
			CommandAPIBukkit.get().updateHelpForCommands(CommandAPI.getRegisteredCommands());
		}, 0L);

		super.stopCommandRegistrations();
	}

	@Override
	public CommandMap getCommandMap() {
		return getNMS().getSimpleCommandMap();
	}

	@Override
	public Platform activePlatform() {
		return Platform.SPIGOT;
	}

	@Override
	public BukkitCommandSender<? extends CommandSender> wrapCommandSender(CommandSender sender) {
		if (sender instanceof BlockCommandSender block) {
			return new BukkitBlockCommandSender(block);
		}
		if (sender instanceof ConsoleCommandSender console) {
			return new BukkitConsoleCommandSender(console);
		}
		if (sender instanceof Player player) {
			return new BukkitPlayer(player);
		}
		if (sender instanceof org.bukkit.entity.Entity entity) {
			return new BukkitEntity(entity);
		}
		if (sender instanceof NativeProxyCommandSender nativeProxy) {
			return new BukkitNativeProxyCommandSender(nativeProxy);
		}
		if (sender instanceof ProxiedCommandSender proxy) {
			return new BukkitProxiedCommandSender(proxy);
		}
		if (sender instanceof RemoteConsoleCommandSender remote) {
			return new BukkitRemoteConsoleCommandSender(remote);
		}
		if (isNullCommandSender(sender)) {
			return null;
		}
		throw new RuntimeException("Failed to wrap CommandSender " + sender + " to a CommandAPI-compatible BukkitCommandSender");
	}

	@Override
	public void registerPermission(String string) {
		try {
			Bukkit.getPluginManager().addPermission(new Permission(string));
		} catch (IllegalArgumentException e) {
			assert true; // nop, not an error.
		}
	}

	/**
	 * Forces a command to return a success value of 0
	 *
	 * @param message Description of the error message, formatted as an array of base components
	 * @return a {@link dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException} that wraps Brigadier's
	 * {@link com.mojang.brigadier.exceptions.CommandSyntaxException}
	 */
	public static WrapperCommandSyntaxException failWithBaseComponents(BaseComponent... message) {
		return CommandAPI.failWithMessage(BukkitTooltip.messageFromBaseComponents(message));
	}

}
