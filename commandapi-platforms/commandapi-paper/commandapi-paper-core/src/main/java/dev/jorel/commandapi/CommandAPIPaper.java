package dev.jorel.commandapi;

import dev.jorel.commandapi.commandsenders.BukkitBlockCommandSender;
import dev.jorel.commandapi.commandsenders.BukkitCommandSender;
import dev.jorel.commandapi.commandsenders.BukkitConsoleCommandSender;
import dev.jorel.commandapi.commandsenders.BukkitEntity;
import dev.jorel.commandapi.commandsenders.BukkitFeedbackForwardingCommandSender;
import dev.jorel.commandapi.commandsenders.BukkitNativeProxyCommandSender;
import dev.jorel.commandapi.commandsenders.BukkitPlayer;
import dev.jorel.commandapi.commandsenders.BukkitProxiedCommandSender;
import dev.jorel.commandapi.commandsenders.BukkitRemoteConsoleCommandSender;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.nms.BundledNMS;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import io.papermc.paper.event.server.ServerResourcesReloadedEvent;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandAPIPaper<Source> extends CommandAPIBukkit<Source> {

	private static CommandAPIPaper<?> paper;

	private boolean isPaperPresent = true;
	private boolean isFoliaPresent = false;
	private final Class<? extends CommandSender> feedbackForwardingCommandSender;
	private final Class<? extends CommandSender> nullCommandSender;

	private CommandAPILogger bootstrapLogger;

	private LifecycleEventOwner lifecycleEventOwner;

	@SuppressWarnings("unchecked")
	protected CommandAPIPaper(InternalPaperConfig config, BundledNMS<Source> nms) {
		CommandAPIPaper.paper = this;
		CommandAPIBukkit.config = config;
		this.lifecycleEventOwner = config.getLifecycleEventOwner();

		super.nms = nms;

		Class<? extends CommandSender> tempFeedbackForwardingCommandSender = null;
		Class<? extends CommandSender> tempNullCommandSender = null;
		try {
			tempFeedbackForwardingCommandSender = (Class<? extends CommandSender>) Class.forName("io.papermc.paper.commands.FeedbackForwardingSender");
		} catch (ClassNotFoundException e) {
			// uhh...
		}
		try {
			tempNullCommandSender = (Class<? extends CommandSender>) Class.forName("io.papermc.paper.brigadier.NullCommandSender");
		} catch (ClassNotFoundException e) {
			// Uhh..
		}

		this.feedbackForwardingCommandSender = tempFeedbackForwardingCommandSender;
		this.nullCommandSender = tempNullCommandSender;
	}

	@SuppressWarnings("unchecked")
	public static <Source> CommandAPIPaper<Source> getPaper() {
		if (paper != null) {
			return (CommandAPIPaper<Source>) paper;
		}
		throw new IllegalStateException("Tried to access CommandAPIPaper instance, but it was null! Are you using CommandAPI features before calling CommandAPI#onLoad?");
	}

	public static InternalPaperConfig getConfiguration() {
		return (InternalPaperConfig) CommandAPIBukkit.getConfiguration();
	}

	public LifecycleEventOwner getLifecycleEventOwner() {
		return lifecycleEventOwner;
	}

	@Override
	public BundledNMS<Source> getNMS() {
		if (nms != null) {
			return (BundledNMS<Source>) this.nms;
		}
		throw new IllegalStateException("Tried to access NMS instance, but it was null! Are you using CommandAPI features before calling CommandAPI#onLoad?");
	}

	@Override
	public void onLoad() {
		super.onLoad();
		checkPaperDependencies();
		PaperCommandRegistration registration = (PaperCommandRegistration) CommandAPIBukkit.get().getCommandRegistrationStrategy();
		registration.registerLifecycleEvent();
	}

	/**
	 * Enables the CommandAPI. This should be placed at the start of your
	 * <code>onEnable()</code> method.
	 */
	@Override
	public void onEnable() {
		super.plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin(getConfiguration().getPluginName());
		this.lifecycleEventOwner = super.plugin;

		new Schedulers(paper.isFoliaPresent).scheduleSyncDelayed(plugin, () -> {
			CommandAPIBukkit.get().getCommandRegistrationStrategy().runTasksAfterServerStart();
			if (paper.isFoliaPresent) {
				CommandAPI.logNormal("Skipping initial datapack reloading because Folia was detected");
			} else {
				if (!getConfiguration().skipReloadDatapacks()) {
					CommandAPIBukkit.get().reloadDataPacks();
				}
			}
			CommandAPIBukkit.get().updateHelpForCommands(CommandAPI.getRegisteredCommands());
		}, 0L);

		super.stopCommandRegistrations();

		// Basically just a check to ensure we're actually running Paper
		if (paper.isPaperPresent) {
			Bukkit.getServer().getPluginManager().registerEvents(new Listener() {
				@EventHandler
				public void onServerReloadResources(ServerResourcesReloadedEvent event) {
					CommandAPIBukkit.get().getCommandRegistrationStrategy().preReloadDataPacks();

					if (getConfiguration().isCommandAPIPlugin()) {
						CommandAPI.logNormal("/minecraft:reload detected. Reloading CommandAPI commands!");
						CommandAPIBukkit.get().reloadDataPacks();
					}
				}
			}, plugin);
			CommandAPI.logNormal("Hooked into Paper ServerResourcesReloadedEvent");
		} else {
			CommandAPI.logNormal("Did not hook into Paper ServerResourcesReloadedEvent while using commandapi-paper. Are you actually using Paper?");
		}

		PaperCommandRegistration registration = (PaperCommandRegistration) super.getCommandRegistrationStrategy();
		registration.registerLifecycleEvent();
	}

	private void checkPaperDependencies() {
		try {
			Class.forName("net.kyori.adventure.text.Component");
			CommandAPI.logNormal("Hooked into Adventure for AdventureChat/AdventureChatComponents");
		} catch (ClassNotFoundException e) {
			if (CommandAPI.getConfiguration().hasVerboseOutput()) {
				CommandAPI.logWarning("Could not hook into Adventure for AdventureChat/AdventureChatComponents");
			}
		}

		isPaperPresent = false;

		try {
			Class.forName("io.papermc.paper.event.server.ServerResourcesReloadedEvent");
			isPaperPresent = true;
			CommandAPI.logNormal("Hooked into Paper for paper-specific API implementations");
		} catch (ClassNotFoundException e) {
			isPaperPresent = false;
			if (CommandAPI.getConfiguration().hasVerboseOutput()) {
				CommandAPI.logWarning("Could not hook into Paper for /minecraft:reload. Consider upgrading to Paper: https://papermc.io/");
			}
		}

		isFoliaPresent = false;

		try {
			Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
			isFoliaPresent = true;
			CommandAPI.logNormal("Hooked into Folia for folia-specific API implementations");
			CommandAPI.logNormal("Folia support is still in development. Please report any issues to the CommandAPI developers!");
		} catch (ClassNotFoundException e) {
			isFoliaPresent = false;
		}
	}

	public CommandMap getCommandMap() {
		return Bukkit.getCommandMap();
	}

	@Override
	public Platform activePlatform() {
		return Platform.PAPER;
	}

	@Override
	public CommandRegistrationStrategy<Source> createCommandRegistrationStrategy() {
		return getNMS().createCommandRegistrationStrategy();
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
		if (this.feedbackForwardingCommandSender.isInstance(sender)) {
			// We literally cannot type this at compile-time, so let's use a placeholder CommandSender instance
			return new BukkitFeedbackForwardingCommandSender<CommandSender>(this.feedbackForwardingCommandSender.cast(sender));
		}
		if (this.nullCommandSender != null && this.nullCommandSender.isInstance(sender)) {
			// Since this should only be during a function load or setting up
			//  help topics, this is just a placeholder to evade the exception.
			return null;
		}
		throw new RuntimeException("Failed to wrap CommandSender " + sender + " to a CommandAPI-compatible BukkitCommandSender");
	}

	/**
	 * Forces a command to return a success value of 0
	 *
	 * @param message Description of the error message, formatted as an adventure chat component
	 * @return a {@link WrapperCommandSyntaxException} that wraps Brigadier's
	 * {@link com.mojang.brigadier.exceptions.CommandSyntaxException}
	 */
	public static WrapperCommandSyntaxException failWithAdventureComponent(ComponentLike message) {
		return CommandAPI.failWithMessage(BukkitTooltip.messageFromAdventureComponent(message));
	}

	@SuppressWarnings("ConstantValue")
	@Override
	public CommandAPILogger getLogger() {
		if (Bukkit.getServer() != null) {
			return super.getLogger();
		}
		if (bootstrapLogger == null) {
			bootstrapLogger = CommandAPILogger.fromSlf4jLogger(ComponentLogger.logger("CommandAPI"));
		}
		return bootstrapLogger;
	}

}
