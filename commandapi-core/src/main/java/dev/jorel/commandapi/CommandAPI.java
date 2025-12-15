package dev.jorel.commandapi;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.jorel.commandapi.arguments.AbstractArgument;
import dev.jorel.commandapi.commandsenders.AbstractPlayer;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class to register commands with the 1.13 command UI
 *
 */
public class CommandAPI {
	// Cannot be instantiated
	private CommandAPI() {
	}

	static {
		onDisable();
	}

	private static boolean canRegister;
	private static boolean loaded;
	private static InternalConfig config;
	private static CommandAPILogger logger;
	private static String loadedStack;

	// Accessing static variables

	/**
	 * Returns whether the CommandAPI is currently loaded. This should be true when
	 * {@link CommandAPI#onLoad(CommandAPIConfig)} is called. If the CommandAPI is
	 * loaded, commands are available to register.
	 *
	 * @return whether the CommandAPI has been loaded properly
	 */
	public static boolean isLoaded() {
		return loaded;
	}

	/**
	 * Flag that commands should no longer be registered. After running this,
	 * {@link CommandAPI#canRegister()} will return false.
	 */
	public static void stopCommandRegistration() {
		CommandAPI.canRegister = false;
	}

	/**
	 * Determines whether command registration is permitted via the CommandAPI
	 *
	 * @return true if commands can still be registered
	 */
	public static boolean canRegister() {
		return canRegister;
	}

	/**
	 * Returns the internal configuration used to manage the CommandAPI
	 *
	 * @return the internal configuration used to manage the CommandAPI
	 */
	public static InternalConfig getConfiguration() {
		if(config != null) {
			return config;
		} else {
			throw new IllegalStateException("Tried to access InternalConfig, but it was null! Are you using CommandAPI features before calling CommandAPI#onLoad?");
		}
	}

	public static void setLogger(CommandAPILogger logger) {
		CommandAPI.logger = logger;
	}

	/**
	 * @return the CommandAPI's logger
	 */
	public static CommandAPILogger getLogger() {
		if (logger != null) {
			// A custom logger was defined, use that
			return logger;
		}
		return CommandAPIHandler.getInstance().getPlatform().getLogger();
	}

	// Loading, enabling, and disabling

	/**
	 * Initializes the CommandAPI for loading. This should be placed at the start of
	 * your <code>onLoad()</code> method.
	 *
	 * @param config the configuration to use for the CommandAPI. This should be a {@link CommandAPIConfig}
	 *               subclass corresponding to the active platform.
	 */
	public static void onLoad(CommandAPIConfig<?> config) {
		if (!loaded) {
			// Store the current stack trace to help diagnose multi-loading errors
			final StringBuilder currentStack = new StringBuilder();
			for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
				currentStack.append(e.toString());
				currentStack.append("\n");
			}
			CommandAPI.loadedStack = currentStack.toString();

			// Setup variables
			CommandAPI.config = new InternalConfig(config);

			// Initialize handlers
			LoadContext loadContext = CommandAPIVersionHandler.getPlatform(config);
			CommandAPIPlatform<?, ?, ?> platform = loadContext.platform();
			new CommandAPIHandler<>(platform);
			loadContext.context().run();

			// Load the platform
			CommandAPIHandler.getInstance().onLoad();

			// Log platform load
			logNormal("Loaded platform " + getPlatformMessage(platform));

			loaded = true;
		} else {
			getLogger().severe("You've tried to call the CommandAPI's onLoad() method more than once!");
			final StringBuilder currentStack = new StringBuilder();
			for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
				currentStack.append(e.toString());
				currentStack.append("\n");
			}
			getLogger().severe("The CommandAPI was first loaded here:\n\n" + CommandAPI.loadedStack +
				"\n\nBut it is now being loaded here:\n\n" + currentStack.toString());
			
		}
	}

	static String getPlatformMessage(Object platform) {
		List<String> platformClassHierarchyList = new ArrayList<>();
		Class<?> platformClass = platform.getClass();
		// Goes up through class inheritance only (ending at Object, but we don't want to include that)
		// CommandAPIPlatform is an interface, so it is not included
		while (platformClass != null && platformClass != Object.class) {
			platformClassHierarchyList.add(platformClass.getSimpleName());
			platformClass = platformClass.getSuperclass();
		}
		return String.join(" > ", platformClassHierarchyList);
	}

	/**
	 * Enables the CommandAPI. This should be placed at the start of your
	 * <code>onEnable()</code> method.
	 */
	public static void onEnable() {
		CommandAPIHandler.getInstance().onEnable();
	}

	/**
	 * Unloads the CommandAPI.
	 */
	public static void onDisable() {
		CommandAPI.canRegister = true;
		CommandAPI.config = null;
		CommandAPI.logger = null;
		CommandAPI.loaded = false;

		// This method is called automatically when the class loads to set up variables, in which case
		// CommandAPIHandler will not have been initialized
		CommandAPIHandler<?, ?, ?> handler = null;
		try {
			handler = CommandAPIHandler.getInstance();
		} catch (IllegalStateException ignored) {
			// Not an error, CommandAPIHandler is not in loaded state anyway
		}
		if (handler != null) handler.onDisable();
	}

	// Logging

	/**
	 * Logs a message to the console using Logger.info() if the configuration has
	 * verbose logging enabled
	 *
	 * @param message the message to log to the console
	 */
	public static void logInfo(String message) {
		if (config.hasVerboseOutput() && !config.hasSilentLogs()) {
			getLogger().info(message);
		}
	}

	/**
	 * Logs a message from the CommandAPI. If silent logs are enabled, this message
	 * is not logged.
	 *
	 * @param message the message to log
	 */
	public static void logNormal(String message) {
		if (!config.hasSilentLogs()) {
			getLogger().info(message);
		}
	}

	/**
	 * Logs a warning from the CommandAPI. If silent logs are enabled, this warning
	 * is not logged.
	 *
	 * @param message the message to log as a warning
	 */
	public static void logWarning(String message) {
		if (!config.hasSilentLogs()) {
			getLogger().warning(message);
		}
	}

	/**
	 * Logs an error from the CommandAPI. This always gets logged, even if silent
	 * logs are enabled.
	 *
	 * @param message the message to log as an error
	 */
	public static void logError(String message) {
		getLogger().severe(message);
	}

	/**
	 * Logs an exception from the CommandAPI. This always gets logged, even if silent
	 * logs are enabled.
	 *
	 * @param message the message to log as an error
	 * @param throwable the exception that was thrown before calling this method
	 */
	public static void logException(String message, Throwable throwable) {
		getLogger().severe(message, throwable);
	}

	/**
	 * Reloads all the datapacks that are on the server. This should be used if
	 * you change a datapack and want to reload a server. Execute this method after
	 * running /minecraft:reload, NOT before.
	 */
	public static void reloadDatapacks() {
		CommandAPIHandler.getInstance().getPlatform().reloadDataPacks();
	}

	/**
	 * Updates the requirements required for a given player to execute a command.
	 *
	 * @param player the player whose requirements should be updated
	 */
	public static <CommandSender, Player extends CommandSender> void updateRequirements(Player player) {
		@SuppressWarnings("unchecked")
		CommandAPIPlatform<?, CommandSender, ?> platform = (CommandAPIPlatform<?, CommandSender, ?>) CommandAPIHandler.getInstance().getPlatform();
		platform.updateRequirements((AbstractPlayer<?>) platform.wrapCommandSender(player));
	}

	// Produce WrapperCommandSyntaxException

	/**
	 * Forces a command to return a success value of 0
	 *
	 * @param message Description of the error message
	 * @return a {@link WrapperCommandSyntaxException} that wraps Brigadier's
	 * {@link CommandSyntaxException}
	 */
	public static WrapperCommandSyntaxException failWithString(String message) {
		return failWithMessage(Tooltip.messageFromString(message));
	}

	/**
	 * Forces a command to return a success value of 0
	 *
	 * @param message Description of the error message, formatted as a brigadier message
	 * @return a {@link WrapperCommandSyntaxException} that wraps Brigadier's
	 * {@link CommandSyntaxException}
	 */
	public static WrapperCommandSyntaxException failWithMessage(Message message) {
		return new WrapperCommandSyntaxException(new SimpleCommandExceptionType(message).create());
	}

	// Command registration and unregistration

	/**
	 * Registers several commands with given namespaces
	 *
	 * @param commands The commands to register.
	 * @param namespaces The namespaces for corresponding commands.
	 * @throws NullPointerException if one of the namespace is null
	 * @throws IllegalArgumentException if different amount of command and namespaces are present, or both of them is empty
	 */
	public static <Impl extends AbstractCommandAPICommand<Impl, Argument, CommandSender>, Argument extends AbstractArgument<?, ?, Argument, CommandSender>, CommandSender>
	void registerSeveral(List<AbstractCommandAPICommand<Impl, Argument, CommandSender>> commands, List<String> namespaces) {
		if(commands.size() != namespaces.size()) throw new IllegalArgumentException("Commands and namespaces should be present with the same size");
		if(commands.isEmpty()) throw new IllegalArgumentException("At least one command should be present");

		int size = commands.size();
		for (int i = 0; i < size; i++) {
			registerInternal(
				commands.get(i),
				namespaces.get(i),
				false
			);
		}
		CommandAPIHandler.getInstance().platform.finishNodeRegistration();
	}

	/**
	 * Registers the command with a given namespace
	 *
	 * @param namespace The namespace of a command. Can't be null
	 * @throws NullPointerException if the namespace is null
	 */
	public static <Impl extends AbstractCommandAPICommand<Impl, Argument, CommandSender>, Argument extends AbstractArgument<?, ?, Argument, CommandSender>, CommandSender>
	void register(AbstractCommandAPICommand<Impl, Argument, CommandSender> command, String namespace) {
		registerInternal(command, namespace, true);
	}

	@SuppressWarnings("unchecked")
	private static <Impl extends AbstractCommandAPICommand<Impl, Argument, CommandSender>, Argument extends AbstractArgument<?, ?, Argument, CommandSender>, CommandSender>
	void registerInternal(AbstractCommandAPICommand<Impl, Argument, CommandSender> command, String namespace, boolean finishProcess) {
		if (namespace == null) {
			// Only reachable through Velocity
			throw new NullPointerException("Parameter 'namespace' was null when registering command /" + command.meta.commandName + "!");
		}
		Argument[] argumentsArray = (Argument[]) (command.arguments == null ? new AbstractArgument[0] : command.arguments.toArray(AbstractArgument[]::new));

		// Check GreedyArgument constraints
		command.checkGreedyArgumentConstraints(argumentsArray);
		command.checkHasExecutors();

		// Assign the command's permissions to arguments if the arguments don't already
		// have one
		for (Argument argument : argumentsArray) {
			if (argument.getArgumentPermission() == null) {
				argument.withPermission(command.meta.permission);
			}
		}

		if (command.executor.hasAnyExecutors()) {
			// Need to cast handler to the right CommandSender type so that argumentsArray and executor are accepted
			CommandAPIHandler<Argument, CommandSender, ?> handler = (CommandAPIHandler<Argument, CommandSender, ?>) CommandAPIHandler.getInstance();

			// Create a List<Argument[]> that is used to register optional arguments
			for (Argument[] args : command.getArgumentsToRegister(argumentsArray)) {
				handler.register(command.meta, args, command.executor, command.isConverted, namespace, finishProcess);
			}
		}

		// Convert subcommands into multiliteral arguments
		for (Impl subcommand : command.subcommands) {
			AbstractCommandAPICommand.flatten(command.copy(), new ArrayList<>(), subcommand, namespace);
		}
	}

	/**
	 * Unregisters a command
	 *
	 * @param command the name of the command to unregister
	 */
	public static void unregister(String command) {
		unregister(command, false);
	}

	/**
	 * Unregisters a command
	 *
	 * @param command the name of the command to unregister
	 * @param unregisterNamespaces whether the unregistration system should attempt to remove versions of the
	 *                                command that start with a namespace. E.g. `minecraft:command`, `bukkit:command`,
	 *                                or `plugin:command`. If true, these namespaced versions of a command are also
	 *                                unregistered.
	 */
	public static void unregister(String command, boolean unregisterNamespaces) {
		CommandAPIHandler.getInstance().getPlatform().unregister(command, unregisterNamespaces);
	}

	public static void unregisterSeveral(List<String> commandNameList) {
		unregisterSeveral(commandNameList, false);
	}

	public static void unregisterSeveral(List<String> commandNameList, boolean unregisterNamespaces) {
		CommandAPIHandler.getInstance().getPlatform().unregisterSeveral(commandNameList, unregisterNamespaces);
	}

	/**
	 * Registers a command. Used with the CommandAPI's Annotation API.
	 *
	 * @param commandClass the class to register
	 */
	public static void registerCommand(Class<?> commandClass) {
		try {
			Class.forName(commandClass.getName() + "$Command").getDeclaredMethod("register").invoke(null);
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return A list of all {@link RegisteredCommand}{@code s} that have been
	 * registered by the CommandAPI so far. The returned list is immutable.
	 */
	public static List<RegisteredCommand> getRegisteredCommands() {
		return Collections.unmodifiableList(CommandAPIHandler.getInstance().registeredCommands);
	}
}
