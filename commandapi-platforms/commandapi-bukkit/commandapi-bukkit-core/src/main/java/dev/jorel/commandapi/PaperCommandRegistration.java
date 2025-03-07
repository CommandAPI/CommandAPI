package dev.jorel.commandapi;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import io.papermc.paper.plugin.configuration.PluginMeta;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Handles logic for registering commands after Paper build 65, where <a href="https://github.com/PaperMC/Paper/pull/8235">https://github.com/PaperMC/Paper/pull/8235</a>
 * changed a bunch of the behind-the-scenes logic.
 */
public class PaperCommandRegistration<Source> extends CommandRegistrationStrategy<Source> {
	// References to necessary methods
	private final Supplier<CommandDispatcher<Source>> getBrigadierDispatcher;
	private final Runnable reloadHelpTopics;
	private final Predicate<CommandNode<Source>> isBukkitCommand;

	// Store registered commands nodes for eventual reloads
	private final RootCommandNode<Source> registeredNodes = new RootCommandNode<>();

	private static final Object paperCommandsInstance;
	private static final Field dispatcherField;

	private static final Constructor<?> pluginCommandNodeConstructor;
	private static final SafeVarHandle<CommandNode<?>, Object> metaField;

	static {
		Object paperCommandsInstanceObject = null;
		Field dispatcherFieldObject = null;

		try {
			Class<?> paperCommands = Class.forName("io.papermc.paper.command.brigadier.PaperCommands");
			paperCommandsInstanceObject = paperCommands.getField("INSTANCE").get(null);
			dispatcherFieldObject = paperCommands.getDeclaredField("dispatcher");
		} catch (ReflectiveOperationException e) {
			// Doesn't happen, or rather, shouldn't happen
		}

		paperCommandsInstance = paperCommandsInstanceObject;
		dispatcherField = dispatcherFieldObject;
		dispatcherField.setAccessible(true);

		Constructor<?> commandNode;
		SafeVarHandle<CommandNode<?>, ?> metaFieldHandle = null;
		try {
			commandNode = Class.forName("io.papermc.paper.command.brigadier.PluginCommandNode").getDeclaredConstructor(String.class, PluginMeta.class, LiteralCommandNode.class, String.class);
		} catch (ClassNotFoundException | NoSuchMethodException e) {
			try {
				// If this happens, plugin commands on Paper are not identified with the PluginCommandNode anymore
				Class<?> pluginCommandMeta = Class.forName("io.papermc.paper.command.brigadier.PluginCommandMeta");
				commandNode = pluginCommandMeta.getDeclaredConstructor(PluginMeta.class, String.class, List.class);
				metaFieldHandle = SafeVarHandle.ofOrNull(CommandNode.class, "pluginCommandMeta", "pluginCommandMeta", pluginCommandMeta);
			} catch (ClassNotFoundException | NoSuchMethodException e1) {
				commandNode = null;
			}
		}
		pluginCommandNodeConstructor = commandNode;
		metaField = (SafeVarHandle<CommandNode<?>, Object>) metaFieldHandle;
	}

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

	@SuppressWarnings("unchecked")
	public CommandDispatcher<Source> getPaperDispatcher() {
		try {
			return (CommandDispatcher<Source>) dispatcherField.get(paperCommandsInstance);
		} catch (IllegalAccessException e) {
			// This doesn't happen
			return null;
		}
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
	public void postCommandRegistration(RegisteredCommand<CommandSender> registeredCommand, LiteralCommandNode<Source> resultantNode, List<LiteralCommandNode<Source>> aliasNodes) {
		// Nothing to do
	}

	@Override
	public void registerCommandNode(LiteralCommandNode<Source> node, String namespace) {
		CommandAPIHandler<?, ?, Source> commandAPIHandler = CommandAPIHandler.getInstance();

		LiteralCommandNode<Source> commandNode = asPluginCommand(node);
		LiteralCommandNode<Source> namespacedCommandNode = asPluginCommand(commandAPIHandler.namespaceNode(commandNode, namespace));

		// Track registered command nodes for reloads
		registeredNodes.addChild(commandNode);
		registeredNodes.addChild(namespacedCommandNode);

		// Register commands
		RootCommandNode<Source> root = getPaperDispatcher().getRoot();
		root.addChild(commandNode);
		root.addChild(namespacedCommandNode);
	}

	@Override
	public void unregister(String commandName, boolean unregisterNamespaces, boolean unregisterBukkit) {
		CommandAPIHandler<?, ?, Source> handler = CommandAPIHandler.getInstance();

		// Remove nodes from the  dispatcher
		handler.removeBrigadierCommands(getPaperDispatcher().getRoot(), commandName, unregisterNamespaces,
			// If we are unregistering a Bukkit command, ONLY unregister BukkitCommandNodes
			// If we are unregistering a Vanilla command, DO NOT unregister BukkitCommandNodes
			c -> !unregisterBukkit ^ isBukkitCommand.test(c));

		// CommandAPI commands count as non-Bukkit
		if (!unregisterBukkit) {
			// Don't add nodes back after a reload
			handler.removeBrigadierCommands(registeredNodes, commandName, unregisterNamespaces, c -> true);
		}
	}

	@Override
	public void preReloadDataPacks() {
		RootCommandNode<Source> root = getPaperDispatcher().getRoot();
		for (CommandNode<Source> commandNode : registeredNodes.getChildren()) {
			root.addChild(commandNode);
		}
		reloadHelpTopics.run();
		CommandAPIBukkit.get().updateHelpForCommands(CommandAPI.getRegisteredCommands());
	}

	@SuppressWarnings("unchecked")
	private LiteralCommandNode<Source> asPluginCommand(LiteralCommandNode<Source> commandNode) {
		try {
			if (metaField == null) {
				return (LiteralCommandNode<Source>) pluginCommandNodeConstructor.newInstance(
					commandNode.getLiteral(),
					CommandAPIBukkit.getConfiguration().getPlugin().getPluginMeta(),
					commandNode,
					getDescription(commandNode.getLiteral())
				);
			} else {
				setPluginCommandMeta(commandNode);
				return commandNode;
			}
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	private void setPluginCommandMeta(LiteralCommandNode<Source> node) {
		try {
			metaField.set(node, pluginCommandNodeConstructor.newInstance(
				CommandAPIBukkit.getConfiguration().getPlugin().getPluginMeta(),
				getDescription(node.getLiteral()),
				getAliasesForCommand(node.getLiteral())
			));
		} catch (ReflectiveOperationException e) {
			// This doesn't happen
		}
	}

	private String getDescription(String commandName) {
		String namespaceStripped;
		if (commandName.contains(":")) {
			namespaceStripped = commandName.split(":")[1];
		} else {
			namespaceStripped = commandName;
		}
		for (RegisteredCommand<?> command : CommandAPI.getRegisteredCommands()) {
			if (command.commandName().equals(namespaceStripped) || Arrays.asList(command.aliases()).contains(namespaceStripped)) {
				Optional<String> shortDescription = command.helpTopic().getShortDescription();
				return shortDescription.orElse("A command by the " + CommandAPIBukkit.getConfiguration().getPlugin().getName() + " plugin.");
			}
		}
		return "";
	}

	private List<String> getAliasesForCommand(String commandName) {
		String namespaceStripped;
		if (commandName.contains(":")) {
			namespaceStripped = commandName.split(":")[1];
		} else {
			namespaceStripped = commandName;
		}
		return List.of(CommandAPIHandler.getInstance().registeredCommands.get(namespaceStripped).aliases());
	}

}
