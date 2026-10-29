package dev.jorel.commandapi;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;

import org.bukkit.command.Command;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A {@link CommandRegistrationStrategy} for testing the Paper platform. Paper's {@link PaperCommandRegistration}
 * only adds commands to the dispatcher when Paper's {@code LifecycleEvents.COMMANDS} event runs, which never happens
 * in a MockBukkit environment. Instead, this strategy immediately does what that event would do: it registers commands
 * like Paper's command registrar, then removes the unwanted plugin namespaces like {@link PaperCommandRegistration}.
 * <p>
 * Bukkit commands are never part of the dispatcher here (they only exist in MockBukkit's command map), so they are
 * never removed from the dispatcher.
 */
public class MockPaperCommandRegistration<Source> extends CommandRegistrationStrategy<Source> {
	private final Supplier<CommandDispatcher<Source>> getBrigadierDispatcher;

	// Same as PaperCommandRegistration, commands are collected here and then registered with Paper
	private final CommandDispatcher<Source> pluginDispatcher = new CommandDispatcher<>();
	private final Set<String> commandsToRemove = new HashSet<>();

	public MockPaperCommandRegistration(Supplier<CommandDispatcher<Source>> getBrigadierDispatcher) {
		this.getBrigadierDispatcher = getBrigadierDispatcher;
	}

	@Override
	public CommandDispatcher<Source> getBrigadierDispatcher() {
		return getBrigadierDispatcher.get();
	}

	@Override
	public LiteralCommandNode<Source> registerCommandNode(LiteralArgumentBuilder<Source> node, String namespace) {
		// Same as PaperCommandRegistration#registerCommandNode
		LiteralCommandNode<Source> built = node.build();
		pluginDispatcher.getRoot().addChild(built);

		String defaultNamespace = CommandAPIPaper.getConfiguration().getPluginName().toLowerCase();
		if (!namespace.equals(defaultNamespace)) {
			// Paper will register commands using the plugin namespace, but we don't want that here
			commandsToRemove.add(defaultNamespace + ":" + built.getName());

			// Register the namespace ourselves, unless the command is already namespaced (for example, an alias like
			//  `minecraft:test`). Namespacing it again would create a double namespace like `minecraft:minecraft:test`.
			if (!built.getName().contains(":")) {
				LiteralCommandNode<Source> builtNamespace = CommandAPIHandler.getInstance().namespaceNode(built, namespace);
				pluginDispatcher.getRoot().addChild(builtNamespace);
				commandsToRemove.add(defaultNamespace + ":" + builtNamespace.getName());
			}
		}

		runCommandsLifecycleEvent(defaultNamespace);
		return built;
	}

	/**
	 * Does what PaperCommandRegistration's handler for Paper's {@code LifecycleEvents.COMMANDS} event does after the
	 * server has started, which Paper runs when the commands are reloaded after registering a command
	 */
	private void runCommandsLifecycleEvent(String defaultNamespace) {
		for (CommandNode<Source> commandNode : pluginDispatcher.getRoot().getChildren()) {
			registerWithPaper((LiteralCommandNode<Source>) commandNode, defaultNamespace);
		}
		for (String commandName : commandsToRemove) {
			removeBrigadierCommands(getBrigadierDispatcher().getRoot(), commandName, false, c -> true);
		}
	}

	/**
	 * Registers a command like Paper's command registrar ({@code PaperCommands#registerWithFlags}) does: the command
	 * itself, plus a {@code namespace:command} redirect using the plugin's namespace
	 */
	private void registerWithPaper(LiteralCommandNode<Source> node, String pluginNamespace) {
		RootCommandNode<Source> root = getBrigadierDispatcher().getRoot();
		root.removeCommand(node.getLiteral());
		root.addChild(node);

		String namespaced = pluginNamespace + ":" + node.getLiteral();
		LiteralArgumentBuilder<Source> redirect = LiteralArgumentBuilder.<Source>literal(namespaced)
			.executes(node.getCommand())
			.requires(node.getRequirement());
		LiteralCommandNode<Source> redirectNode;
		if (node.getChildren().isEmpty()) {
			redirectNode = redirect.build();
		} else {
			redirectNode = redirect.redirect(node).build();
		}
		root.removeCommand(namespaced);
		root.addChild(redirectNode);
	}

	@Override
	public void unregister(String commandName, boolean unregisterNamespaces, boolean unregisterBukkit) {
		// Same as PaperCommandRegistration#unregister and its command lifecycle event handler. On Paper, Bukkit commands
		//  are part of the dispatcher (as BukkitCommandNodes), which PaperCommandRegistration only removes when
		//  unregistering Bukkit commands. In MockBukkit, Bukkit commands only exist in the command map instead.
		if (unregisterBukkit) {
			Map<String, Command> knownCommands = CommandAPIBukkit.get().getNMS().getSimpleCommandMap().getKnownCommands();
			knownCommands.remove(commandName);
			if (unregisterNamespaces) {
				removeCommandNamespace(knownCommands, commandName, c -> true);
			}
		} else {
			removeBrigadierCommands(pluginDispatcher.getRoot(), commandName, unregisterNamespaces, c -> true);
			removeBrigadierCommands(getBrigadierDispatcher().getRoot(), commandName, unregisterNamespaces, c -> true);
		}

		// Update the dispatcher file
		CommandAPIHandler.getInstance().writeDispatcherToFile();
	}

	@Override
	public void preReloadDataPacks() {
		CommandAPIBukkit.get().updateHelpForCommands(CommandAPI.getRegisteredCommands());
	}
}
