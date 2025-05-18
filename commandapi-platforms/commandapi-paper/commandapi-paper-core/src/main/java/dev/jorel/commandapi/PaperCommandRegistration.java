package dev.jorel.commandapi;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import org.bukkit.help.HelpTopic;

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

	public CommandDispatcher<Source> getPaperDispatcher() {
		return CommandAPIPaper.getPaper().getPaperCommandDispatcher();
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

		if (!CommandAPIPaper.getPaper().isDispatcherValid()) {
			// If it's not valid, then we're registering outside of lifecycle events
			// Add to registered command nodes
			registeredNodes.addChild(commandNode);
			registeredNodes.addChild(namespacedCommandNode);
		}

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

	private LiteralCommandNode<Source> asPluginCommand(LiteralCommandNode<Source> commandNode) {
		return CommandAPIPaper.getPaper().asPluginCommand(commandNode, getDescription(commandNode.getLiteral()), getAliasesForCommand(commandNode.getLiteral()));
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
