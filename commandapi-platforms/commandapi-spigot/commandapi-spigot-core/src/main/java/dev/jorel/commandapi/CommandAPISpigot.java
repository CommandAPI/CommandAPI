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
import dev.jorel.commandapi.nms.NMS;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class CommandAPISpigot<Source> extends CommandAPIBukkit<Source> implements SpigotNMS<Source> {

	private static CommandAPISpigot<?> spigot;

	@SuppressWarnings("unchecked")
	protected CommandAPISpigot() {
		this.nms = (NMS<Source>) bukkitNMS();
		CommandAPISpigot.spigot = this;
		setInstance(this);
	}

	@SuppressWarnings("unchecked")
	public static <Source> CommandAPISpigot<Source> getSpigot() {
		return (CommandAPISpigot<Source>) spigot;
	}

	public static InternalSpigotConfig getConfiguration() {
		return (InternalSpigotConfig) CommandAPIBukkit.getConfiguration();
	}

	private static void setInternalConfig(InternalSpigotConfig config) {
		CommandAPIBukkit.config = config;
	}

	@Override
	public <T extends CommandAPIBukkitConfig<T>> void onLoad(CommandAPIBukkitConfig<T> config) {
		if (config instanceof CommandAPISpigotConfig spigotConfig) {
			CommandAPISpigot.setInternalConfig(new InternalSpigotConfig(spigotConfig));
		} else {
			CommandAPI.logError("CommandAPIBukkit was loaded with non-Bukkit config!");
			CommandAPI.logError("Attempts to access Bukkit-specific config variables will fail!");
		}
		onLoad();
	}

	@Override
	public void onEnable() {
		JavaPlugin plugin = getConfiguration().getPlugin();

		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			// Sort out permissions after the server has finished registering them all
			getCommandRegistrationStrategy().runTasksAfterServerStart();
			if (!getConfiguration().skipReloadDatapacks()) {
				reloadDataPacks();
			}
			updateHelpForCommands(CommandAPI.getRegisteredCommands());
		}, 0L);

		// Prevent command registration after server has loaded
		Bukkit.getServer().getPluginManager().registerEvents(new Listener() {
			// We want the lowest priority so that we always get to this first, in case a dependent plugin is using
			//  CommandAPI features in their own ServerLoadEvent listener for some reason
			@EventHandler(priority = EventPriority.LOWEST)
			public void onServerLoad(ServerLoadEvent event) {
				CommandAPI.stopCommandRegistration();
			}
		}, getConfiguration().getPlugin());

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
		throw new RuntimeException("Failed to wrap CommandSender " + sender + " to a CommandAPI-compatible BukkitCommandSender");
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
