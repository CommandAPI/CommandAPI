package dev.jorel.commandapi;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.help.HelpTopic;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
	private final Predicate<CommandNode<Source>> isBukkitCommand;

	// Commands
	private final List<AbstractCommandAPICommand<?, ?, ?>> commandsToBuild = new ArrayList<>();
	private final RootCommandNode<CommandSourceStack> registeredCommands = new RootCommandNode<>();
	private RootCommandNode<CommandSourceStack> commandsToRegister = new RootCommandNode<>();
	private final Set<String> namespacedCommandsToRemove = new HashSet<>();

	private record UnregisterInformation(String commandName, boolean unregisterNamespaces, boolean unregisterBukkit) {
	}

	private final List<UnregisterInformation> unregisterInformationList = new ArrayList<>();

	private boolean canRegister = false;
	private boolean scheduleReloadTask = true;

	public PaperCommandRegistration(Supplier<CommandDispatcher<Source>> getBrigadierDispatcher, Predicate<CommandNode<Source>> isBukkitCommand) {
		this.getBrigadierDispatcher = getBrigadierDispatcher;
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
	public void preReloadDataPacks() {
		CommandAPIBukkit.get().updateHelpForCommands(CommandAPI.getRegisteredCommands());
	}

	@Override
	public boolean canRegister() {
		return canRegister;
	}

	// CommandAPI side of registration/unregistration
	@Override
	public LiteralCommandNode<Source> registerCommandNode(LiteralArgumentBuilder<Source> node, String namespace) {
		LiteralCommandNode<Source> built = node.build();
		commandsToRegister.addChild((LiteralCommandNode<CommandSourceStack>) built);
		if (!namespace.equals(CommandAPIPaper.getConfiguration().getPluginName().toLowerCase())) {
			// Register the namespace ourselves
			String defaultNamespace = CommandAPIPaper.getConfiguration().getPluginName().toLowerCase();
			LiteralCommandNode<Source> builtNamespace = CommandAPIHandler.getInstance().namespaceNode(built, namespace);
			commandsToRegister.addChild((LiteralCommandNode<CommandSourceStack>) builtNamespace);

			// Paper will register commands using the plugin namespace, but we don't want that here
			String pluginNamespacedWithoutNamespace = defaultNamespace + ":" + built.getName();
			String pluginNamespacedWithNamespace = defaultNamespace + ":" + builtNamespace.getName();
			namespacedCommandsToRemove.add(pluginNamespacedWithoutNamespace);
			namespacedCommandsToRemove.add(pluginNamespacedWithNamespace);
		}
		scheduleReloadTask();
		return built;
	}

	@Override
	public void unregister(String commandName, boolean unregisterNamespaces, boolean unregisterBukkit) {
		// Remove nodes from our dispatchers
		removeBrigadierCommands((RootCommandNode<Source>) registeredCommands, commandName, unregisterNamespaces,
			c -> !unregisterBukkit ^ isBukkitCommand.test(c)
		);
		removeBrigadierCommands((RootCommandNode<Source>) commandsToRegister, commandName, unregisterNamespaces,
			c -> !unregisterBukkit ^ isBukkitCommand.test(c)
		);

		// Remove from real dispatcher when rebuilding commands
		unregisterInformationList.add(new UnregisterInformation(commandName, unregisterNamespaces, unregisterBukkit));
		scheduleReloadTask();
	}

	private void scheduleReloadTask() {
		if (CommandAPI.canRegister() || !scheduleReloadTask) {
			// The server is currently starting or a task has already been scheduled
			// Either way, we don't want to schedule the task now
			return;
		}
		if (CommandAPIPaper.getPaper().isFoliaPresent()) {
			// Bukkit.reloadData() is not available on Folia
			throw new IllegalStateException("Cannot register/unregister commands after the server is done starting on Folia");
		}
		scheduleReloadTask = false;
		Bukkit.getGlobalRegionScheduler().runDelayed(CommandAPIPaper.getPaper().getPlugin(), task -> {
			Bukkit.reloadData();
			scheduleReloadTask = true;
		}, 1);
	}

	void addBootstrapCommand(AbstractCommandAPICommand<?, ?, ?> command) {
		commandsToBuild.add(command);
	}

	// Paper side of registration/unregistration
	private LifecycleEventManager<?> getManager(LifecycleEventOwner owner) {
		// For some reason, these are not methods on the interface
		if (owner instanceof BootstrapContext bootstrap) {
			return bootstrap.getLifecycleManager();
		} else if (owner instanceof JavaPlugin plugin) {
			return plugin.getLifecycleManager();
		} else {
			throw new IllegalArgumentException("LifecycleEventOwner was not a BootstrapContext or JavaPlugin");
		}
	}

	private void registerCommandsThroughPaper(ReloadableRegistrarEvent<Commands> event, RootCommandNode<CommandSourceStack> commands) {
		// Remove nodes from other plugins
		for (UnregisterInformation unregisterInformation : unregisterInformationList) {
			removeBrigadierCommands(getBrigadierDispatcher().getRoot(), unregisterInformation.commandName(), unregisterInformation.unregisterNamespaces(),
				// If we are unregistering a Bukkit command, ONLY unregister BukkitCommandNodes
				// If we are unregistering a Vanilla command, DO NOT unregister BukkitCommandNodes
				c -> !unregisterInformation.unregisterBukkit() ^ isBukkitCommand.test(c));
		}

		// Register our commands
		for (CommandNode<CommandSourceStack> commandNode : commands.getChildren()) {
			LiteralCommandNode<CommandSourceStack> node = (LiteralCommandNode<CommandSourceStack>) commandNode;
			event.registrar().register(node, getDescription(node.getLiteral()));
		}

		// Remove namespaced versions of our commands we didn't want Paper to create
		for (String commandName : namespacedCommandsToRemove) {
			removeBrigadierCommands(getBrigadierDispatcher().getRoot(), commandName, false, c -> true);
		}

		// Update the dispatcher file
		CommandAPIHandler.getInstance().writeDispatcherToFile();
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

	void registerBootstrapLifecycleEvent(LifecycleEventOwner owner) {
		// This might still actually be running as a JavaPlugin event (notably runs after datapack parsing)
		//  rather than a bootstrap event if we are being shaded by a plugin that doesn't load in bootstrap
		getManager(owner).registerEventHandler(LifecycleEvents.COMMANDS.newHandler(
			event -> {
				// We are far enough into the server load to actually
				//  register bootstrap commands with certain arguments
				canRegister = true;
				for (AbstractCommandAPICommand<?, ?, ?> command : commandsToBuild) {
					command.register(command.namespace);
				}
				commandsToBuild.clear();

				// We'll register these commands here instead of during the
				//  enable event so they can be used in datapacks
				if (!commandsToRegister.getChildren().isEmpty()) {
					for (CommandNode<CommandSourceStack> node : commandsToRegister.getChildren()) {
						registeredCommands.addChild(node);
					}
					commandsToRegister = new RootCommandNode<>();
				}

				// Register all of our commands
				registerCommandsThroughPaper(event, registeredCommands);
			}
			// Use a lower priority than default so that we run after other
			//  plugins register their commands, so we can remove them
		).priority(1));
	}

	void registerEnableLifecycleEvent(LifecycleEventOwner owner) {
		getManager(owner).registerEventHandler(LifecycleEvents.COMMANDS.newHandler(
			event -> {
				if (!commandsToRegister.getChildren().isEmpty()) {
					// This will trigger once if we want to register commands that were
					//  made in onEnable, but we aren't automatically reloading on server start.
					// If and when `minecraft:reload` is called, the bootstrap event will handle
					//  registering these commands.
					registerCommandsThroughPaper(event, commandsToRegister);
				}
			}
			// Always run after our own bootstrap event handler in case both
			//  of the event handlers are JavaPlugin event handlers
		).priority(2));
	}
}
