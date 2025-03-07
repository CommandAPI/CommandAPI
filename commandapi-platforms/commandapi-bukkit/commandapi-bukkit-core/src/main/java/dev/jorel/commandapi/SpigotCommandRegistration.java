package dev.jorel.commandapi;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import dev.jorel.commandapi.commandnodes.DifferentClientNode;
import dev.jorel.commandapi.preprocessor.RequireField;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Handles logic for registering commands on Spigot and old versions of Paper
 */
@RequireField(in = SimpleCommandMap.class, name = "knownCommands", ofType = Map.class)
public class SpigotCommandRegistration<Source> extends CommandRegistrationStrategy<Source> {
	// References to necessary objects
	private final CommandDispatcher<Source> brigadierDispatcher;
	private final SimpleCommandMap commandMap;

	// References to necessary methods
	private final Supplier<CommandDispatcher<Source>> getResourcesDispatcher;
	private final Predicate<Command> isVanillaCommandWrapper;
	private final Function<CommandNode<Source>, Command> wrapToVanillaCommandWrapper;
	private final Predicate<CommandNode<Source>> isBukkitCommandWrapper;

	// We need to fix permissions and namespaces on Bukkit
	//  Bukkit does a bunch of mucking about moving nodes between Brigadier CommandDispatchers and its own CommandMap,
	//  and these variables help us set that all straight
	private final Set<String> namespacesToFix = new HashSet<>();
	private RootCommandNode<Source> minecraftCommandNamespaces = new RootCommandNode<>();
	private final TreeMap<String, CommandPermission> permissionsToFix = new TreeMap<>();

	// Reflection
	private final SafeVarHandle<SimpleCommandMap, Map<String, Command>> commandMapKnownCommands;

	public SpigotCommandRegistration(
		CommandDispatcher<Source> brigadierDispatcher, SimpleCommandMap commandMap,
		Supplier<CommandDispatcher<Source>> getResourcesDispatcher, Predicate<Command> isVanillaCommandWrapper,
		Function<CommandNode<Source>, Command> wrapToVanillaCommandWrapper, Predicate<CommandNode<Source>> isBukkitCommandWrapper
	) {
		this.brigadierDispatcher = brigadierDispatcher;
		this.commandMap = commandMap;

		this.getResourcesDispatcher = getResourcesDispatcher;
		this.isVanillaCommandWrapper = isVanillaCommandWrapper;
		this.wrapToVanillaCommandWrapper = wrapToVanillaCommandWrapper;
		this.isBukkitCommandWrapper = isBukkitCommandWrapper;

		this.commandMapKnownCommands = SafeVarHandle.ofOrNull(SimpleCommandMap.class, "knownCommands", "knownCommands", Map.class);
	}

	// Provide access to internal functions that may be useful to developers

	/**
	 * Returns the Brigadier CommandDispatcher used when commands are sent to Players
	 *
	 * @return A Brigadier CommandDispatcher
	 */
	public CommandDispatcher<Source> getResourcesDispatcher() {
		return getResourcesDispatcher.get();
	}

	/**
	 * Checks if a Command is an instance of the OBC VanillaCommandWrapper
	 *
	 * @param command The Command to check
	 * @return true if Command is an instance of VanillaCommandWrapper
	 */
	public boolean isVanillaCommandWrapper(Command command) {
		return isVanillaCommandWrapper.test(command);
	}

	/**
	 * Wraps a Brigadier command node as Bukkit's VanillaCommandWrapper
	 *
	 * @param node The LiteralCommandNode to wrap
	 * @return A VanillaCommandWrapper representing the given node
	 */
	public Command wrapToVanillaCommandWrapper(CommandNode<Source> node) {
		return wrapToVanillaCommandWrapper.apply(node);
	}

	/**
	 * Checks if a Brigadier command node is being handled by Bukkit's BukkitCommandWrapper
	 *
	 * @param node The CommandNode to check
	 * @return true if the CommandNode is being handled by Bukkit's BukkitCommandWrapper
	 */
	public boolean isBukkitCommandWrapper(CommandNode<Source> node) {
		return isBukkitCommandWrapper.test(node);
	}

	// Utility methods
	private String unpackInternalPermissionNodeString(CommandPermission perm) {
		final Optional<String> optionalPerm = perm.getPermission();
		if (perm.isNegated() || perm.equals(CommandPermission.NONE) || perm.equals(CommandPermission.OP)) {
			return "";
		} else if (optionalPerm.isPresent()) {
			return optionalPerm.get();
		} else {
			throw new IllegalStateException("Invalid permission detected: " + perm +
				"! This should never happen - if you're seeing this message, please " +
				"contact the developers of the CommandAPI, we'd love to know how you managed to get this error!");
		}
	}

	// Implement CommandRegistrationStrategy methods
	@Override
	public CommandDispatcher<Source> getBrigadierDispatcher() {
		return brigadierDispatcher;
	}

	@Override
	public void runTasksAfterServerStart() {
		// Fix namespaces first thing when starting the server
		fixNamespaces();
		// Sort out permissions after the server has finished registering them all
		fixPermissions();
	}

	private void fixNamespaces() {
		CommandAPIHandler<?, ?, Source> handler = CommandAPIHandler.getInstance();
		Map<String, Command> knownCommands = commandMapKnownCommands.get(commandMap);
		RootCommandNode<Source> resourcesRootNode = getResourcesDispatcher.get().getRoot();

		// Remove namespaces
		for (String command : namespacesToFix) {
			knownCommands.remove(command);
			handler.removeBrigadierCommands(resourcesRootNode, command, false, c -> true);
		}

		// Add back certain minecraft: namespace commands
		RootCommandNode<Source> brigadierRootNode = brigadierDispatcher.getRoot();
		for (CommandNode<Source> node : minecraftCommandNamespaces.getChildren()) {
			knownCommands.put(node.getName(), wrapToVanillaCommandWrapper.apply(node));
			resourcesRootNode.addChild(node);

			// VanillaCommandWrappers in the CommandMap defer to the Brigadier dispatcher when executing.
			// While the minecraft namespace usually does not exist in the Brigadier dispatcher, in the case of a
			//  command conflict we do need this node to exist separately from the unnamespaced version to keep the
			//  commands separate.
			brigadierRootNode.addChild(node);
		}
		// Clear minecraftCommandNamespaces for dealing with command conflicts after the server is enabled
		//  See `SpigotCommandRegistration#postCommandRegistration`
		minecraftCommandNamespaces = new RootCommandNode<>();
	}

	/*
	 * Makes permission checks more "Bukkit" like and less "Vanilla Minecraft" like
	 */
	private void fixPermissions() {
		if (!permissionsToFix.isEmpty()) {
			CommandAPI.logInfo("Linking permissions to commands:");

			for (Map.Entry<String, CommandPermission> entry : permissionsToFix.entrySet()) {
				String cmdName = entry.getKey();
				CommandPermission perm = entry.getValue();
				CommandAPI.logInfo("  " + perm.toString() + " -> /" + cmdName);

				final String permNode = unpackInternalPermissionNodeString(perm);

				/*
				 * Sets the permission. If you have to be OP to run this command, we set the
				 * permission to null. Doing so means that Bukkit's "testPermission" will always
				 * return true, however since the command's permission check occurs internally
				 * via the CommandAPI, this isn't a problem.
				 *
				 * If anyone dares tries to use testPermission() on this command, seriously,
				 * what are you doing and why?
				 */
				Command command = commandMap.getCommand(cmdName);
				if (command != null && isVanillaCommandWrapper.test(command)) {
					command.setPermission(permNode);
				}
			}
		}
		CommandAPI.logNormal("Linked " + permissionsToFix.size() + " Bukkit permissions to commands");
	}

	@Override
	public void postCommandRegistration(RegisteredCommand<CommandSender> registeredCommand, LiteralCommandNode<Source> resultantNode, List<LiteralCommandNode<Source>> aliasNodes) {
		if (!CommandAPI.canRegister()) {
			// Usually, when registering commands during server startup, we can just put our commands into the
			// `net.minecraft.server.MinecraftServer#vanillaCommandDispatcher` and leave it. As the server finishes setup,
			// it and the CommandAPI do some extra stuff to make everything work, and we move on.
			// So, if we want to register commands while the server is running, we need to do all that extra stuff, and
			// that is what this code does.
			// We could probably call all those methods to sync everything up, but in the spirit of avoiding side effects
			// and avoiding doing things twice for existing commands, this is a distilled version of those methods.

			Map<String, Command> knownCommands = commandMapKnownCommands.get(commandMap);
			RootCommandNode<Source> root = getResourcesDispatcher.get().getRoot();

			String name = resultantNode.getLiteral();
			String namespace = registeredCommand.namespace();
			String permNode = unpackInternalPermissionNodeString(registeredCommand.rootNode().permission());

			registerCommand(knownCommands, root, name, permNode, namespace, resultantNode);

			// Do the same for the aliases
			for (LiteralCommandNode<Source> node : aliasNodes) {
				registerCommand(knownCommands, root, node.getLiteral(), permNode, namespace, node);
			}

			Collection<CommandNode<Source>> minecraftNamespacesToFix = minecraftCommandNamespaces.getChildren();
			if (!minecraftNamespacesToFix.isEmpty()) {
				// Adding new `minecraft` namespace nodes to the Brigadier dispatcher
				//  usually happens in `CommandAPIBukkit#fixNamespaces`.
				// Note that the previous calls to `CommandAPIBukkit#registerCommand` in this method
				//  will have already dealt with adding the nodes here to the resources dispatcher.
				// We also have to set the permission to simulate the result of `CommandAPIBukkit#fixPermissions`.
				RootCommandNode<Source> brigadierRootNode = brigadierDispatcher.getRoot();
				for (CommandNode<Source> node : minecraftNamespacesToFix) {
					Command minecraftNamespaceCommand = wrapToVanillaCommandWrapper.apply(node);
					knownCommands.put(node.getName(), minecraftNamespaceCommand);
					minecraftNamespaceCommand.setPermission(permNode);
					brigadierRootNode.addChild(node);
				}
				minecraftCommandNamespaces = new RootCommandNode<>();
			}
		} else {
			CommandPermission permission = registeredCommand.rootNode().permission();

			// Since the VanillaCommandWrappers aren't created yet, we need to remember to
			//  fix those permissions once the server is enabled. Using `putIfAbsent` to
			//  default to the first permission associated with this command.
			String commandName = registeredCommand.commandName().toLowerCase();
			permissionsToFix.putIfAbsent(commandName, permission);

			// Do the same for the namespaced version of the command (which is never empty on Bukkit forks)
			String namespace = registeredCommand.namespace().toLowerCase();
			permissionsToFix.putIfAbsent(namespace + ":" + commandName, permission);

			// Do the same for the aliases
			for (String alias : registeredCommand.aliases()) {
				alias = alias.toLowerCase();
				permissionsToFix.putIfAbsent(alias, permission);
				permissionsToFix.putIfAbsent(namespace + ":" + alias, permission);
			}
		}
	}

	private void registerCommand(Map<String, Command> knownCommands, RootCommandNode<Source> root, String name, String permNode, String namespace, LiteralCommandNode<Source> resultantNode) {
		CommandAPIHandler<?, ?, Source> commandAPIHandler = CommandAPIHandler.getInstance();

		// Wrapping Brigadier nodes into VanillaCommandWrappers and putting them in the CommandMap usually happens
		// in `CraftServer#setVanillaCommands`
		Command command = wrapToVanillaCommandWrapper.apply(resultantNode);
		knownCommands.putIfAbsent(name, command);

		// Adding permissions to these Commands usually happens in `CommandAPIBukkit#fixPermissions`
		command.setPermission(permNode);

		// Adding commands to the other (Why bukkit/spigot?!) dispatcher usually happens in `CraftServer#syncCommands`
		root.addChild(resultantNode);

		// Handle namespace
		LiteralCommandNode<Source> namespacedNode = commandAPIHandler.namespaceNode(resultantNode, namespace);
		if (namespace.equals("minecraft")) {
			// The minecraft namespace version should be registered as a straight alias of the original command, since
			//  the `minecraft:name` node does not exist in the Brigadier dispatcher, which is referenced by
			//  VanillaCommandWrapper (note this is not true if there is a command conflict, but
			//  `CommandAPIBukkit#postCommandRegistration` will deal with this later using `minecraftCommandNamespaces`).
			knownCommands.putIfAbsent("minecraft:" + name, command);
		} else {
			// A custom namespace should be registered like a separate command, so that it can reference the namespaced
			//  node, rather than the original unnamespaced node
			Command namespacedCommand = wrapToVanillaCommandWrapper.apply(namespacedNode);
			knownCommands.putIfAbsent(namespacedCommand.getName(), namespacedCommand);
			namespacedCommand.setPermission(permNode);
		}
		// In both cases, add the node to the resources dispatcher
		root.addChild(namespacedNode);
	}

	@Override
	public void registerCommandNode(LiteralCommandNode<Source> node, String namespace) {
		CommandAPIHandler<?, ?, Source> commandAPIHandler = CommandAPIHandler.getInstance();
		RootCommandNode<Source> rootNode = brigadierDispatcher.getRoot();


		if (!CommandAPIBukkit.get().getPaper().isPaperPresent()) {
			// If Paper is not present, then we don't have the AsyncPlayerSendCommandsEvent,
			//  which allows us to rewrite nodes for a specific client. Best we can do on Spigot
			//  is rewrite the nodes now and indicate we don't have a specific client with null.
			DifferentClientNode.rewriteAllChildren(null, node, false);
		}

		String name = node.getLiteral();
		if (namespace.equals("minecraft")) {
			if (namespacesToFix.contains("minecraft:" + name)) {
				// This command wants to exist as `minecraft:name`
				// However, another command has requested that `minecraft:name` be removed
				// We'll keep track of everything that should be `minecraft:name` in
				//  `minecraftCommandNamespaces` and fix this later in `#fixNamespaces`
				minecraftCommandNamespaces.addChild(commandAPIHandler.namespaceNode(node, "minecraft"));
			}
		} else {
			// Make sure to remove the `minecraft:name` and
			//  `minecraft:namespace:name` commands Bukkit will create
			fillNamespacesToFix(name, namespace + ":" + name);

			// Create the namespaced node
			rootNode.addChild(commandAPIHandler.namespaceNode(node, namespace));
		}

		// Add the main node to dispatcher
		//  We needed to wait until after `fillNamespacesToFix` was called to do this, in case a previous
		//  `minecraft:name` version of the command needed to be saved separately before this node was added
		rootNode.addChild(node);
	}

	private void fillNamespacesToFix(String... namespacedCommands) {
		CommandAPIHandler<?, ?, Source> commandAPIHandler = CommandAPIHandler.getInstance();
		for (String namespacedCommand : namespacedCommands) {
			// We'll remove these commands later when fixNamespaces is called
			if (!namespacesToFix.add("minecraft:" + namespacedCommand)) {
				continue;
			}

			// If this is the first time considering this command for removal
			// and there is already a command with this name in the dispatcher
			// then, the command currently in the dispatcher is supposed to appear as `minecraft:command`
			CommandNode<Source> currentNode = brigadierDispatcher.getRoot().getChild(namespacedCommand);
			if (currentNode != null) {
				// We'll keep track of everything that should be `minecraft:command` in
				//  `minecraftCommandNamespaces` and fix this later in `#fixNamespaces`
				// TODO: Ideally, we should be working without this cast to LiteralCommandNode. I don't know if this can fail
				minecraftCommandNamespaces.addChild(commandAPIHandler.namespaceNode((LiteralCommandNode<Source>) currentNode, "minecraft"));
			}
		}
	}

	@Override
	public void unregister(String commandName, boolean unregisterNamespaces, boolean unregisterBukkit) {
		CommandAPIHandler<?, ?, Source> handler = CommandAPIHandler.getInstance();

		if (!unregisterBukkit) {
			// Remove nodes from the Vanilla dispatcher
			// This dispatcher doesn't usually have namespaced version of commands (those are created when commands
			//  are transferred to Bukkit's CommandMap), but if they ask, we'll do it
			handler.removeBrigadierCommands(brigadierDispatcher.getRoot(), commandName, unregisterNamespaces, c -> true);
		}

		if (unregisterBukkit || !CommandAPI.canRegister()) {
			// We need to remove commands from Bukkit's CommandMap if we're unregistering a Bukkit command, or
			//  if we're unregistering after the server is enabled, because `CraftServer#setVanillaCommands` will have
			//  moved the Vanilla command into the CommandMap
			Map<String, Command> knownCommands = commandMapKnownCommands.get(commandMap);

			// If we are unregistering a Bukkit command, DO NOT unregister VanillaCommandWrappers
			// If we are unregistering a Vanilla command, ONLY unregister VanillaCommandWrappers
			boolean isMainVanilla = isVanillaCommandWrapper.test(knownCommands.get(commandName));
			if (unregisterBukkit ^ isMainVanilla) knownCommands.remove(commandName);

			if (unregisterNamespaces) {
				CommandAPIHandler.removeCommandNamespace(knownCommands, commandName, c -> unregisterBukkit ^ isVanillaCommandWrapper.test(c));
			}
		}

		if (!CommandAPI.canRegister()) {
			// If the server is enabled, we have extra cleanup to do

			// Remove commands from the resources dispatcher
			handler.removeBrigadierCommands(getResourcesDispatcher.get().getRoot(), commandName, unregisterNamespaces,
				// If we are unregistering a Bukkit command, ONLY unregister BukkitCommandWrappers
				// If we are unregistering a Vanilla command, DO NOT unregister BukkitCommandWrappers
				c -> !unregisterBukkit ^ isBukkitCommandWrapper.test(c));
		}
	}

	@Override
	public void preReloadDataPacks() {
		// Nothing to do
	}
}
