package dev.jorel.commandapi;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.SuggestionProviders;
import dev.jorel.commandapi.commandsenders.AbstractCommandSender;
import dev.jorel.commandapi.commandsenders.AbstractPlayer;
import dev.jorel.commandapi.commandsenders.BukkitBlockCommandSender;
import dev.jorel.commandapi.commandsenders.BukkitCommandSender;
import dev.jorel.commandapi.commandsenders.BukkitConsoleCommandSender;
import dev.jorel.commandapi.commandsenders.BukkitEntity;
import dev.jorel.commandapi.commandsenders.BukkitNativeProxyCommandSender;
import dev.jorel.commandapi.commandsenders.BukkitPlayer;
import dev.jorel.commandapi.commandsenders.BukkitProxiedCommandSender;
import dev.jorel.commandapi.commandsenders.BukkitRemoteConsoleCommandSender;
import dev.jorel.commandapi.network.BukkitCommandAPIMessenger;
import dev.jorel.commandapi.nms.SpigotNMS;
import dev.jorel.commandapi.preprocessor.Unimplemented;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static dev.jorel.commandapi.preprocessor.Unimplemented.REASON.REQUIRES_CRAFTBUKKIT;

public abstract class CommandAPISpigot<Source> implements BukkitPlatform<Source>, SpigotNMS<Source> {

	private static CommandAPIBukkit<?> bukkit;
	private static CommandAPISpigot<?> spigot;

	private BukkitCommandAPIMessenger messenger;

	@SuppressWarnings("unchecked")
	protected CommandAPISpigot() {
		CommandAPISpigot.bukkit = (CommandAPIBukkit<Source>) bukkitNMS();
		CommandAPISpigot.spigot = this;

		bukkit.setInstance(this);
	}

	@SuppressWarnings("unchecked")
	public static <Source> CommandAPIBukkit<Source> getBukkit() {
		return (CommandAPIBukkit<Source>) bukkit;
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
		bukkit.onLoad();
	}

	@Override
	public void onEnable() {
		JavaPlugin plugin = getConfiguration().getPlugin();

		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			// Sort out permissions after the server has finished registering them all
			bukkit.getCommandRegistrationStrategy().runTasksAfterServerStart();
			if (!getConfiguration().skipReloadDatapacks()) {
				reloadDataPacks();
			}
			bukkit.updateHelpForCommands(CommandAPI.getRegisteredCommands());
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
	public void onDisable() {
		bukkit.onDisable();
	}

	@Override
	public CommandMap getCommandMap() {
		return bukkit.getSimpleCommandMap();
	}

	@Override
	public Platform activePlatform() {
		return Platform.SPIGOT;
	}

	@Override
	public BukkitCommandSender<? extends CommandSender> getSenderForCommand(CommandContext<Source> cmdCtx, boolean forceNative) {
		return CommandAPISpigot.<Source>getBukkit().getSenderForCommand(cmdCtx, forceNative);
	}

	@Override
	public BukkitCommandSender<? extends CommandSender> getCommandSenderFromCommandSource(Source source) {
		return CommandAPISpigot.<Source>getBukkit().getCommandSenderFromCommandSource(source);
	}

	@Override
	public Source getBrigadierSourceFromCommandSender(AbstractCommandSender<? extends CommandSender> sender) {
		return CommandAPISpigot.<Source>getBukkit().getBrigadierSourceFromCommandSender(sender);
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

	@Override
	public void registerPermission(String string) {
		bukkit.registerPermission(string);
	}

	@Override
	public SuggestionProvider<Source> getSuggestionProvider(SuggestionProviders suggestionProvider) {
		return CommandAPISpigot.<Source>getBukkit().getSuggestionProvider(suggestionProvider);
	}

	@Override
	public void preCommandRegistration(String commandName) {
		bukkit.preCommandRegistration(commandName);
	}

	@Override
	public void postCommandRegistration(RegisteredCommand registeredCommand, LiteralCommandNode<Source> resultantNode, List<LiteralCommandNode<Source>> aliasNodes) {
		CommandAPISpigot.<Source>getBukkit().postCommandRegistration(registeredCommand, resultantNode, aliasNodes);
	}

	@Override
	public LiteralCommandNode<Source> registerCommandNode(LiteralArgumentBuilder<Source> node, String namespace) {
		return CommandAPISpigot.<Source>getBukkit().registerCommandNode(node, namespace);
	}

	@Override
	public void unregister(String commandName, boolean unregisterNamespaces) {
		bukkit.unregister(commandName, unregisterNamespaces);
	}

	@Override
	public BukkitCommandAPIMessenger setupMessenger() {
		messenger = new BukkitCommandAPIMessenger(getConfiguration().getPlugin());
		return messenger;
	}

	public BukkitCommandAPIMessenger getMessenger() {
		return messenger;
	}

	@Override
	public CommandDispatcher<Source> getBrigadierDispatcher() {
		return CommandAPISpigot.<Source>getBukkit().getBrigadierDispatcher();
	}

	@Override
	public void createDispatcherFile(File file, CommandDispatcher<Source> dispatcher) throws IOException {
		CommandAPISpigot.<Source>getBukkit().createDispatcherFile(file, dispatcher);
	}

	@Override
	public void reloadDataPacks() {
		bukkit.reloadDataPacks();
	}

	@Override
	public void updateRequirements(AbstractPlayer<?> player) {
		bukkit.updateRequirements(player);
	}

	@Override
	public CommandAPICommand newConcreteCommandAPICommand(CommandMetaData<CommandSender> meta) {
		return bukkit.newConcreteCommandAPICommand(meta);
	}

	@Override
	public MultiLiteralArgument newConcreteMultiLiteralArgument(String nodeName, String[] literals) {
		return (MultiLiteralArgument) bukkit.newConcreteMultiLiteralArgument(nodeName, literals);
	}

	@Override
	public LiteralArgument newConcreteLiteralArgument(String nodeName, String literal) {
		return (LiteralArgument) bukkit.newConcreteLiteralArgument(nodeName, literal);
	}

}
