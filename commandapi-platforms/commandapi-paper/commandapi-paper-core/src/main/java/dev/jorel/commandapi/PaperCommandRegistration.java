package dev.jorel.commandapi;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.help.HelpTopic;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Handles logic for registering commands after Paper build 65, where <a href="https://github.com/PaperMC/Paper/pull/8235">https://github.com/PaperMC/Paper/pull/8235</a>
 * changed a bunch of the behind-the-scenes logic.
 */
@SuppressWarnings("UnstableApiUsage") // We know we are using new Paper Command API stuff
public class PaperCommandRegistration<Source> extends CommandRegistrationStrategy<Source> {
	// References to necessary methods
	private final Supplier<CommandDispatcher<Source>> getBrigadierDispatcher;
	private final Runnable reloadHelpTopics;
	private final Predicate<CommandNode<Source>> isBukkitCommand;

	private final boolean[] lifecycleEventRegistered = new boolean[2];
	private final CommandDispatcher<CommandSourceStack> bootstrapDispatcher = new CommandDispatcher<>();
	private final CommandDispatcher<CommandSourceStack> pluginDispatcher = new CommandDispatcher<>();

	private final List<UnregisterInformation> unregisterInformationList = new ArrayList<>();

	public PaperCommandRegistration(Supplier<CommandDispatcher<Source>> getBrigadierDispatcher, Runnable reloadHelpTopics, Predicate<CommandNode<Source>> isBukkitCommand) {
		this.getBrigadierDispatcher = getBrigadierDispatcher;
		this.reloadHelpTopics = reloadHelpTopics;
		this.isBukkitCommand = isBukkitCommand;
	}

	// Provide access to internal functions that may be useful to developers

	/**
	 * Checks if a Brigadier command node came from wrapping a Bukkit command
	 *
	 * @param node The CommandNode to check
	 * @return true if the CommandNode is being handled by Paper's BukkitCommandNode
	 */
	public boolean isBukkitCommand(CommandNode<Source> node) {
		return isBukkitCommand.test(node);
	}

	// Implement CommandRegistrationStrategy methods
	@Override
	public CommandDispatcher<Source> getBrigadierDispatcher() {
		return getBrigadierDispatcher.get();
	}

	@Override
	public void runTasksAfterServerStart() {
		// Nothing to do
	}

	@Override
	public void postCommandRegistration(RegisteredCommand registeredCommand, LiteralCommandNode<Source> resultantNode, List<LiteralCommandNode<Source>> aliasNodes) {
		// Nothing to do
	}

	@Override
	@SuppressWarnings("ConstantValue") // `getServer` actually is `null` when we are in bootstrap
	public LiteralCommandNode<Source> registerCommandNode(LiteralArgumentBuilder<Source> node, String namespace) {
		LiteralCommandNode<Source> built = node.build();
		if (Bukkit.getServer() == null) {
			bootstrapDispatcher.getRoot().addChild((CommandNode<CommandSourceStack>) built);
		} else {
			pluginDispatcher.getRoot().addChild((CommandNode<CommandSourceStack>) built);
		}
		if (!CommandAPI.canRegister()) {
			// Since we register commands into our dispatchers, we need to run the lifecycle events again
			// This can happen when using /minecraft:reload or this method
			Bukkit.reloadData();
		}
		return built;
	}

	@Override
	public void unregister(String commandName, boolean unregisterNamespaces, boolean unregisterBukkit) {
		// Remove nodes from our dispatchers
		removeBrigadierCommands(
			(RootCommandNode<Source>) bootstrapDispatcher.getRoot(), commandName, unregisterNamespaces,
			c -> !unregisterBukkit ^ isBukkitCommand.test(c)
		);
		removeBrigadierCommands(
			(RootCommandNode<Source>) pluginDispatcher.getRoot(), commandName, unregisterNamespaces,
			c -> !unregisterBukkit ^ isBukkitCommand.test(c)
		);

		// Remove from real dispatcher when rebuilding commands
		unregisterInformationList.add(new UnregisterInformation(commandName, unregisterNamespaces, unregisterBukkit));
		if (!CommandAPI.canRegister()) {
			Bukkit.reloadData();
		}
	}

	@Override
	public void preReloadDataPacks() {
		reloadHelpTopics.run(); // TODO: Is this necessary
		CommandAPIBukkit.get().updateHelpForCommands(CommandAPI.getRegisteredCommands());
	}

	@SuppressWarnings("ConstantValue") // `getServer` actually is `null` when we are in bootstrap
	void registerLifecycleEvent() {
		boolean bootstrap = Bukkit.getServer() == null;
		if (bootstrap && !lifecycleEventRegistered[0]) {
			BootstrapContext context = (BootstrapContext) CommandAPIPaper.getPaper().getLifecycleEventOwner();
			lifecycleEventRegistered[0] = true;
			registerLifecycleEvent(context.getLifecycleManager(), bootstrapDispatcher);
			return;
		}
		if (!bootstrap && !lifecycleEventRegistered[1]) {
			JavaPlugin plugin = (JavaPlugin) CommandAPIPaper.getPaper().getLifecycleEventOwner();
			lifecycleEventRegistered[1] = true;
			registerLifecycleEvent(plugin.getLifecycleManager(), pluginDispatcher);

			plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event -> {
				if (!unregisterInformationList.isEmpty()) {
					for (UnregisterInformation unregisterInformation : unregisterInformationList) {
						// Remove nodes from the dispatcher
						removeBrigadierCommands(getBrigadierDispatcher().getRoot(), unregisterInformation.commandName(), unregisterInformation.unregisterNamespaces(),
							// If we are unregistering a Bukkit command, ONLY unregister BukkitCommandNodes
							// If we are unregistering a Vanilla command, DO NOT unregister BukkitCommandNodes
							c -> !unregisterInformation.unregisterBukkit() ^ isBukkitCommand.test(c));
					}

					// Update the dispatcher file
					CommandAPIHandler.getInstance().writeDispatcherToFile();
				}
			}).priority(1));
		}
	}

	private void registerLifecycleEvent(LifecycleEventManager<?> lifecycleEventManager, CommandDispatcher<CommandSourceStack> dispatcher) {
		lifecycleEventManager.registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event -> {
			for (CommandNode<CommandSourceStack> commandNode : dispatcher.getRoot().getChildren()) {
				LiteralCommandNode<CommandSourceStack> node = (LiteralCommandNode<CommandSourceStack>) commandNode;
				event.registrar().register(node, getDescription(node.getLiteral()));
			}
		}).priority(2));
	}

	private String getDescription(String commandName) {
		String namespaceStripped;
		if (commandName.contains(":")) {
			namespaceStripped = commandName.split(":")[1];
		} else {
			namespaceStripped = commandName;
		}
		for (RegisteredCommand command : CommandAPI.getRegisteredCommands()) {
			if (command.commandName().equals(namespaceStripped) || Arrays.asList(command.aliases()).contains(namespaceStripped)) {
				Object helpTopic = command.helpTopic().orElse(null);
				if (helpTopic != null) {
					return ((HelpTopic) helpTopic).getShortText();
				} else {
					return command.shortDescription().orElse("A command by the " + CommandAPIBukkit.getConfiguration().getPluginName() + " plugin.");
				}
			}
		}
		return "";
	}

	private record UnregisterInformation(String commandName, boolean unregisterNamespaces, boolean unregisterBukkit) {}

}
