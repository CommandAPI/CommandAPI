package dev.jorel.commandapi;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.jorel.commandapi.arguments.AbstractArgument;
import dev.jorel.commandapi.arguments.SuggestionProviders;
import dev.jorel.commandapi.commandsenders.AbstractCommandSender;
import dev.jorel.commandapi.commandsenders.AbstractPlayer;
import dev.jorel.commandapi.network.CommandAPIMessenger;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
	void onLoad(CommandAPIConfig<?> config);

	/**
	 * Platform-specific stuff that should happen when the CommandAPI is enabled
	 */
	void onEnable();

	/**
	 * Platform-specific stuff that should happen when the CommandAPI is disabled.
	 */
	void onDisable();

	// Converting between CommandSender, AbstractCommandSender, and Brigadier Sources

	/**
	 * Converts a Brigadier CommandContext into an AbstractCommandSender wrapping the platform's CommandSender
	 *
	 * @param cmdCtx      A Brigadier CommandContext
	 * @param forceNative True if the CommandSender should be forced into a native CommandSender
	 * @return An AbstractCommandSender wrapping the CommandSender represented by the CommandContext
	 */
	AbstractCommandSender<? extends CommandSender> getSenderForCommand(CommandContext<Source> cmdCtx, boolean forceNative);

	/**
	 * Converts the class used by Brigadier when running commands into an AbstractCommandSender wrapping the platform's CommandSender
	 *
	 * @param source The Brigadier source object
	 * @return An AbstractCommandSender wrapping the CommandSender represented by the source object
	 */
	AbstractCommandSender<? extends CommandSender> getCommandSenderFromCommandSource(Source source);

	/**
	 * Converts a CommandSender wrapped in an AbstractCommandSender to an object Brigadier can use when running its commands
	 *
	 * @param sender The CommandSender to convert, wrapped in an AbstractCommandSender
	 * @return The Brigadier Source object represented by the sender
	 */
	Source getBrigadierSourceFromCommandSender(AbstractCommandSender<? extends CommandSender> sender);

	/**
	 * Wraps a CommandSender in an AbstractCommandSender class, the inverse operation to {@link AbstractCommandSender#getSource()}
	 *
	 * @param sender The CommandSender to wrap
	 * @return An AbstractCommandSender with a class appropriate to the underlying class of the CommandSender
	 */
	AbstractCommandSender<? extends CommandSender> wrapCommandSender(CommandSender sender);

	// Registers a permission. Bukkit's permission system requires permissions to be "registered"
	// before they can be used.
	void registerPermission(String string);

	// Some commands have existing suggestion providers
	SuggestionProvider<Source> getSuggestionProvider(SuggestionProviders suggestionProvider);

	/**
	 * Stuff to run before a command is generated. For Bukkit, this involves checking
	 * if a command was declared in the plugin.yml when it isn't supposed to be.
	 *
	 * @param commandName The name of the command about to be registered
	 */
	void preCommandRegistration(String commandName);

	/**
	 * Stuff to run after a command has been generated.
	 *
	 * @param registeredCommand A {@link RegisteredCommand} instance that holds the CommandAPI information for the command
	 * @param resultantNode     The node that was registered
	 * @param aliasNodes        Any alias nodes that were also registered as a part of this registration process
	 */
	void postCommandRegistration(RegisteredCommand registeredCommand, LiteralCommandNode<Source> resultantNode, List<LiteralCommandNode<Source>> aliasNodes);

	/**
	 * Registers a Brigadier command node and returns the built node.
	 */
	LiteralCommandNode<Source> registerCommandNode(LiteralArgumentBuilder<Source> node, String namespace);


	/**
	 * Unregisters a command from the CommandGraph so it can't be run anymore.
	 *
	 * @param commandName the name of the command to unregister
	 * @param unregisterNamespaces whether the unregistration system should attempt to remove versions of the
	 *                                command that start with a namespace. Eg. `minecraft:command`, `bukkit:command`,
	 *                                or `plugin:command`
	 */
	void unregister(String commandName, boolean unregisterNamespaces);

	/**
	 * Creates a new {@link CommandAPIMessenger} for this platform. This is only intended to be called once by
	 * {@link CommandAPIHandler} when it is enabling the CommandAPI.
	 *
	 * @return The new {@link CommandAPIMessenger} that was created.
	 */
	CommandAPIMessenger<?, ?> setupMessenger();

	/**
	 * @return The Brigadier CommandDispatcher tree being used by the platform's server
	 */
	CommandDispatcher<Source> getBrigadierDispatcher();

	/**
	 * Creates a JSON file that describes the hierarchical structure of the commands
	 * that have been registered by the server.
	 *
	 * @param file       The JSON file to write to
	 * @param dispatcher The Brigadier CommandDispatcher
	 * @throws IOException When the file fails to be written to
	 */
	void createDispatcherFile(File file, CommandDispatcher<Source> dispatcher) throws IOException;

	/**
	 * @return A new default Logger meant for the CommandAPI to use. This logger will be used by the CommandAPI
	 * if one is not set using {@link CommandAPI#setLogger(CommandAPILogger)}.
	 */
	default CommandAPILogger getLogger() {
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
	void reloadDataPacks();

	/**
	 * Updates the requirements required for a given player to execute a command.
	 *
	 * @param player the player to update
	 */
	void updateRequirements(AbstractPlayer<?> player);

	// Create the concrete instances of objects implemented by the platform
	AbstractCommandAPICommand<?, Argument, CommandSender> newConcreteCommandAPICommand(CommandMetaData<CommandSender> meta);

	Argument newConcreteMultiLiteralArgument(String nodeName, String[] literals);

	Argument newConcreteLiteralArgument(String nodeName, String literal);
}
