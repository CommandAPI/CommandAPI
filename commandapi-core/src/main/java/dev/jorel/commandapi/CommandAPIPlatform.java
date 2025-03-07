package dev.jorel.commandapi;

import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.jorel.commandapi.arguments.AbstractArgument;
import dev.jorel.commandapi.arguments.SuggestionProviders;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * @param <Argument> The implementation of AbstractArgument used for the platform
 * @param <CommandSender> The class for running platforms commands
 * @param <Source> The class for running Brigadier commands
 */
public interface CommandAPIPlatform<Argument
/// @cond DOX
extends AbstractArgument<?, ?, Argument, CommandSender>
/// @endcond
, CommandSender, Source> {
	// Platform-specific loading, enabling, and disabling tasks

	/**
	 * Platform-specific stuff that should happen when the CommandAPI is loaded,
	 * such as checking dependencies and initializing helper classes.
	 *
	 * @param config the configuration to use for the CommandAPI.
	 */
	public abstract void onLoad(CommandAPIConfig<?> config);

	/**
	 * Platform-specific stuff that should happen when the CommandAPI is enabled,
	 * such as registering event listeners.
	 */
	public abstract void onEnable();

	/**
	 * Platform-specific stuff that should happen when the CommandAPI is disabled.
	 */
	public abstract void onDisable();

	// Converting between platform CommandSender and Brigadier Source

	/**
	 * Converts the class used by Brigadier when running commands into the platform's CommandSender
	 *
	 * @param source The Brigadier source object
	 * @return The CommandSender represented by the source object
	 */
	public abstract CommandSender getCommandSenderFromCommandSource(Source source);

	/**
	 * Converts a CommandSender to an object Brigadier can use when running its commands
	 *
	 * @param sender The CommandSender to convert
	 * @return The Brigadier Source object represented by the sender
	 */
	public abstract Source getBrigadierSourceFromCommandSender(CommandSender sender);

	// Some commands have existing suggestion providers
	public abstract SuggestionProvider<Source> getSuggestionProvider(SuggestionProviders suggestionProvider);

	/**
	 * Ensures the given String is a valid command namespace on this platform. If the namespace
	 * is not valid, this method will return a String that should be used instead.
	 *
	 * @param command   The command being registered with the given namespace.
	 * @param namespace The String that wants to be used as a namespace.
	 * @return The String that should be used as the namespace. If the given String is a valid namespace, it will be returned.
	 */
	public abstract String validateNamespace(ExecutableCommand<?, CommandSender> command, String namespace);

	/**
	 * Stuff to run before a command is generated. For Bukkit, this involves checking
	 * if a command was declared in the plugin.yml when it isn't supposed to be.
	 *
	 * @param commandName The name of the command about to be registered
	 */
	public abstract void preCommandRegistration(String commandName);

	/**
	 * Stuff to run after a command has been generated.
	 *
	 * @param registeredCommand A {@link RegisteredCommand} instance that holds the CommandAPI information for the command
	 * @param resultantNode     The node that was registered
	 * @param aliasNodes        Any alias nodes that were also registered as a part of this registration process
	 */
	public abstract void postCommandRegistration(RegisteredCommand<CommandSender> registeredCommand, LiteralCommandNode<Source> resultantNode, List<LiteralCommandNode<Source>> aliasNodes);

	/**
	 * Builds and registers a Brigadier command node.
	 *
	 * @param node      The Brigadier {@link LiteralArgumentBuilder} to build and register.
	 * @param namespace The namespace to register the command with.
	 * @return The built node.
	 */
	default LiteralCommandNode<Source> registerCommandNode(LiteralArgumentBuilder<Source> node, String namespace) {
		LiteralCommandNode<Source> built = node.build();
		registerCommandNode(built, namespace);
		return built;
	}

	/**
	 * Registers a Brigadier command node.
	 *
	 * @param node      The Brigadier {@link LiteralArgumentBuilder} to register.
	 * @param namespace The namespace to register the command with.
	 */
	public abstract void registerCommandNode(LiteralCommandNode<Source> node, String namespace);


	/**
	 * Unregisters a command from the CommandGraph so it can't be run anymore.
	 *
	 * @param commandName          the name of the command to unregister
	 * @param unregisterNamespaces whether the unregistration system should attempt to remove versions of the
	 *                             command that start with a namespace. Eg. `minecraft:command`, `bukkit:command`,
	 *                             or `plugin:command`
	 */
	public abstract void unregister(String commandName, boolean unregisterNamespaces);

	/**
	 * @return The Brigadier CommandDispatcher tree being used by the platform's server
	 */
	public abstract CommandDispatcher<Source> getBrigadierDispatcher();

	public abstract Optional<JsonObject> getArgumentTypeProperties(ArgumentType<?> type);

	/**
	 * @return A new default Logger meant for the CommandAPI to use. This logger will be used by the CommandAPI
	 * if one is not set using {@link CommandAPI#setLogger(CommandAPILogger)}.
	 */
	public default CommandAPILogger getLogger() {
		return new CommandAPILogger() {
			private static final String PREFIX = "[CommandAPI] ";
			private static final String YELLOW = "\u001B[33m";
			private static final String RED = "\u001B[31m";
			private static final String RESET = "\u001B[0m";

			@Override
			public void info(String message) {
				System.out.println(PREFIX + message);
			}

			@Override
			public void warning(String message) {
				System.out.println(YELLOW + PREFIX + message + RESET);
			}

			@Override
			public void severe(String message) {
				System.out.println(RED + PREFIX + message + RESET);
			}

			@Override
			public void severe(String message, Throwable throwable) {
				System.out.println(RED + PREFIX + message + RESET);
				throwable.printStackTrace(System.out);
			}
		};
	}

	/**
	 * Reloads the server's data packs to include CommandAPI commands
	 */
	public abstract void reloadDataPacks();

	/**
	 * Updates the player's view of the requirements for them to execute a command.
	 *
	 * @param player the player whose requirements should be updated
	 */
	public abstract void updateRequirements(CommandSender player);

	public abstract Argument newConcreteMultiLiteralArgument(String nodeName, String[] literals);

	public abstract Argument newConcreteLiteralArgument(String nodeName, String literal);

	/**
	 * Generates a {@link Predicate} that evaluates whether a command sender meets the given permission.
	 *
	 * @param permission The {@link CommandPermission} to check for.
	 * @return A {@link Predicate} that tests if a command sender meets the given permission.
	 */
	public abstract Predicate<CommandSender> getPermissionCheck(CommandPermission permission);
}
