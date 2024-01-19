package dev.jorel.commandapi;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import io.papermc.paper.plugin.configuration.PluginMeta;
import org.bukkit.help.HelpTopic;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
		// Deal with retrieving the dispatcher in the PaperCommands class
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

		// Deal with retrieving a constructor to mark a command as a plugin command
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
				try {
					// If this happens, the PluginCommandMeta as been renamed to APICommandMeta
					Class<?> apiCommandMeta = Class.forName("io.papermc.paper.command.brigadier.APICommandMeta");
					commandNode = apiCommandMeta.getDeclaredConstructor(PluginMeta.class, String.class, List.class, String.class, boolean.class);
					metaFieldHandle = SafeVarHandle.ofOrNull(CommandNode.class, "apiCommandMeta", "apiCommandMeta", apiCommandMeta);
				} catch (ClassNotFoundException | NoSuchMethodException e2) {
					try {
						// If this happens, the boolean parameter was removed from APICommandMeta
						Class<?> apiCommandMeta = Class.forName("io.papermc.paper.command.brigadier.APICommandMeta");
						commandNode = apiCommandMeta.getDeclaredConstructor(PluginMeta.class, String. class, List.class, String.class);
						metaFieldHandle = SafeVarHandle.ofOrNull(CommandNode.class, "apiCommandMeta", "apiCommandMeta", apiCommandMeta);
					} catch (ClassNotFoundException |NoSuchMethodException e3) {
						commandNode = null;
					}
				}
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
	public void postCommandRegistration(RegisteredCommand registeredCommand, LiteralCommandNode<Source> resultantNode, List<LiteralCommandNode<Source>> aliasNodes) {
		// Nothing to do
	}

	@Override
	public LiteralCommandNode<Source> registerCommandNode(LiteralArgumentBuilder<Source> node, String namespace) {
		LiteralCommandNode<Source> commandNode = asPluginCommand(node.build());
		LiteralCommandNode<Source> namespacedCommandNode = asPluginCommand(CommandAPIHandler.getInstance().namespaceNode(commandNode, namespace));

		// Add to registered command nodes
		registeredNodes.addChild(commandNode);
		registeredNodes.addChild(namespacedCommandNode);

		// Register commands
		RootCommandNode<Source> root = getPaperDispatcher().getRoot();
		root.addChild(commandNode);
		root.addChild(namespacedCommandNode);

		return commandNode;
	}

	@Override
	public void unregister(String commandName, boolean unregisterNamespaces, boolean unregisterBukkit) {
		// Remove nodes from the dispatcher
		removeBrigadierCommands(getBrigadierDispatcher().getRoot(), commandName, unregisterNamespaces,
			// If we are unregistering a Bukkit command, ONLY unregister BukkitCommandNodes
			// If we are unregistering a Vanilla command, DO NOT unregister BukkitCommandNodes
			c -> !unregisterBukkit ^ isBukkitCommand.test(c));

		// CommandAPI commands count as non-Bukkit
		if (!unregisterBukkit) {
			// Don't add nodes back after a reload
			removeBrigadierCommands(registeredNodes, commandName, unregisterNamespaces, c -> true);
		}

		// Update the dispatcher file
		CommandAPIHandler.getInstance().writeDispatcherToFile();
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
		} catch (ReflectiveOperationException | IllegalArgumentException e) {
			try {
				metaField.set(node, pluginCommandNodeConstructor.newInstance(
					CommandAPIBukkit.getConfiguration().getPlugin().getPluginMeta(),
					getDescription(node.getLiteral()),
					getAliasesForCommand(node.getLiteral()),
					CommandAPIBukkit.getConfiguration().getNamespace(), // Don't think this matters actually
					false // Indicates a server side only command
				));
			} catch (ReflectiveOperationException | IllegalArgumentException e1) {
				try {
					metaField.set(node, pluginCommandNodeConstructor.newInstance(
						CommandAPIBukkit.getConfiguration().getPlugin().getPluginMeta(),
						getDescription(node.getLiteral()),
						getAliasesForCommand(node.getLiteral()),
						CommandAPIBukkit.getConfiguration().getNamespace() // Probably still doesn't matter
					));
				} catch (ReflectiveOperationException e2) {
					// This doesn't happen
				}
			}
		}
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
					return command.shortDescription().orElse("A command by the " + CommandAPIBukkit.getConfiguration().getPlugin().getName() + " plugin.");
				}
			}
		}
		return "";
	}

	private List<String> getAliasesForCommand(String commandName) {
		Set<String> aliases = new HashSet<>();
		String namespaceStripped;
		if (commandName.contains(":")) {
			namespaceStripped = commandName.split(":")[1];
		} else {
			namespaceStripped = commandName;
		}
		for (RegisteredCommand command : CommandAPI.getRegisteredCommands()) {
			if (command.commandName().equals(namespaceStripped)) {
				aliases.addAll(Arrays.asList(command.aliases()));
			}
		}
		return new ArrayList<>(aliases);
	}

}
