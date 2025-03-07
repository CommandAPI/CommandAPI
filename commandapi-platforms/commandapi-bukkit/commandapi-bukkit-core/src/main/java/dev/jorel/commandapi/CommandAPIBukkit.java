package dev.jorel.commandapi;

import static dev.jorel.commandapi.preprocessor.Unimplemented.REASON.REQUIRES_CRAFTBUKKIT;
import static dev.jorel.commandapi.preprocessor.Unimplemented.REASON.REQUIRES_CSS;
import static dev.jorel.commandapi.preprocessor.Unimplemented.REASON.REQUIRES_MINECRAFT_SERVER;
import static dev.jorel.commandapi.preprocessor.Unimplemented.REASON.VERSION_SPECIFIC_IMPLEMENTATION;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.kyori.adventure.text.ComponentLike;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import dev.jorel.commandapi.commandnodes.DifferentClientNode;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.help.HelpTopic;
import org.bukkit.inventory.Recipe;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.SuggestionProviders;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.help.BukkitHelpTopicWrapper;
import dev.jorel.commandapi.help.CommandAPIHelpTopic;
import dev.jorel.commandapi.help.CustomCommandAPIHelpTopic;
import dev.jorel.commandapi.nms.NMS;
import dev.jorel.commandapi.preprocessor.Unimplemented;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;

// CommandAPIBukkit is an CommandAPIPlatform, but also needs all of the methods from
// NMS, so it implements NMS. Our implementation of CommandAPIBukkit is now derived
// using the version handler (and thus, deferred to our NMS-specific implementations)
public abstract class CommandAPIBukkit<Source> implements CommandAPIPlatform<Argument<?>, CommandSender, Source>, NMS<Source> {

	// References to utility classes
	private static CommandAPIBukkit<?> instance;
	private static InternalBukkitConfig config;
	private PaperImplementations<Source> paper;
	private CommandRegistrationStrategy<Source> commandRegistrationStrategy;

	protected CommandAPIBukkit() {
		CommandAPIBukkit.instance = this;
	}

	@SuppressWarnings("unchecked")
	public static <Source> CommandAPIBukkit<Source> get() {
		if (CommandAPIBukkit.instance != null) {
			return (CommandAPIBukkit<Source>) instance;
		} else {
			throw new IllegalStateException("Tried to access CommandAPIBukkit instance, but it was null! Are you using CommandAPI features before calling CommandAPI#onLoad?");
		}
	}

	public PaperImplementations<Source> getPaper() {
		return paper;
	}

	public static InternalBukkitConfig getConfiguration() {
		if (config != null) {
			return config;
		} else {
			throw new IllegalStateException("Tried to access InternalBukkitConfig, but it was null! Did you load the CommandAPI properly with CommandAPI#onLoad?");
		}
	}

	public CommandRegistrationStrategy<Source> getCommandRegistrationStrategy() {
		return commandRegistrationStrategy;
	}

	@Override
	public void onLoad(CommandAPIConfig<?> config) {
		if (config instanceof CommandAPIBukkitConfig bukkitConfig) {
			CommandAPIBukkit.setInternalConfig(new InternalBukkitConfig(bukkitConfig));
		} else {
			CommandAPI.logError("CommandAPIBukkit was loaded with non-Bukkit config!");
			CommandAPI.logError("Attempts to access Bukkit-specific config variables will fail!");
		}

		checkDependencies();
	}

	private static void setInternalConfig(InternalBukkitConfig internalBukkitConfig) {
		CommandAPIBukkit.config = internalBukkitConfig;
	}

	private void checkDependencies() {
		// Log successful hooks
		Class<?> nbtContainerClass = CommandAPI.getConfiguration().getNBTContainerClass();
		if (nbtContainerClass != null && CommandAPI.getConfiguration().getNBTContainerConstructor() != null) {
			CommandAPI.logNormal("Hooked into an NBT API with class " + nbtContainerClass.getName());
		}
		// We don't need to log if no NBT was found, constructing an NBTCompoundArgument without one will do that for us

		try {
			Class.forName("org.spigotmc.SpigotConfig");
			CommandAPI.logNormal("Hooked into Spigot successfully for Chat/ChatComponents");
		} catch (ClassNotFoundException e) {
			if (CommandAPI.getConfiguration().hasVerboseOutput()) {
				CommandAPI.logWarning("Could not hook into Spigot for Chat/ChatComponents");
			}
		}

		try {
			Class.forName("net.kyori.adventure.text.Component");
			CommandAPI.logNormal("Hooked into Adventure for AdventureChat/AdventureChatComponents");
		} catch (ClassNotFoundException e) {
			if (CommandAPI.getConfiguration().hasVerboseOutput()) {
				CommandAPI.logWarning("Could not hook into Adventure for AdventureChat/AdventureChatComponents");
			}
		}

		boolean isPaperPresent;

		try {
			Class.forName("io.papermc.paper.event.server.ServerResourcesReloadedEvent");
			isPaperPresent = true;
			CommandAPI.logNormal("Hooked into Paper for paper-specific API implementations");
		} catch (ClassNotFoundException e) {
			isPaperPresent = false;
			if (CommandAPI.getConfiguration().hasVerboseOutput()) {
				CommandAPI.logWarning("Could not hook into Paper for /minecraft:reload. Consider upgrading to Paper: https://papermc.io/");
			}
		}

		boolean isFoliaPresent;

		try {
			Class.forName("io.papermc.paper.threadedregions.RegionizedServerInitEvent");
			isFoliaPresent = true;
			CommandAPI.logNormal("Hooked into Folia for folia-specific API implementations");
			CommandAPI.logNormal("Folia support is still in development. Please report any issues to the CommandAPI developers!");
		} catch (ClassNotFoundException e) {
			isFoliaPresent = false;
		}

		paper = new PaperImplementations<>(isPaperPresent, isFoliaPresent, this);

		commandRegistrationStrategy = createCommandRegistrationStrategy();
	}

	@Override
	public void onEnable() {
		JavaPlugin plugin = config.getPlugin();

		new Schedulers(paper).scheduleSyncDelayed(plugin, () -> {
			commandRegistrationStrategy.runTasksAfterServerStart();

			if (paper.isFoliaPresent()) {
				CommandAPI.logNormal("Skipping initial datapack reloading because Folia was detected");
			} else {
				if (!getConfiguration().skipReloadDatapacks()) {
					reloadDataPacks();
				}
			}
			updateHelpForCommands(CommandAPI.getRegisteredCommands());
		}, 0L);

		// Prevent command registration after server has loaded
		Bukkit.getServer().getPluginManager().registerEvents(new Listener() {
			// We want the lowest priority so that we always get to this first, in case a dependent plugin is using
			//  CommandAPI features in their own ServerLoadEvent listener for some reason
			@EventHandler(priority = EventPriority.LOWEST)
			public void onServerLoad(ServerLoadEvent event) {
				CommandAPI.stopCommandRegistration();
			}
		}, plugin);

		paper.registerEvents(plugin);
	}

	/*
	 * Generate and register help topics
	 */
	void updateHelpForCommands(List<RegisteredCommand<CommandSender>> commands) {
		Map<String, HelpTopic> helpTopicsToAdd = new HashMap<>();

		for (RegisteredCommand<CommandSender> command : commands) {
			String namespaceAddon = (command.namespace().isEmpty() ? "" : command.namespace() + ":");
			String commandName = namespaceAddon + command.commandName();
			CommandAPIHelpTopic<CommandSender> commandAPIHelpTopic = command.helpTopic();

			// Don't override other plugin's help topics
			if (Bukkit.getPluginCommand(commandName) == null) {
				final HelpTopic helpTopic;
				if (commandAPIHelpTopic instanceof BukkitHelpTopicWrapper bukkitHelpTopic) {
					helpTopic = bukkitHelpTopic.helpTopic();
				} else {
					helpTopic = new CustomCommandAPIHelpTopic(commandName, command.aliases(), commandAPIHelpTopic, command.rootNode());
				}
				helpTopicsToAdd.put("/" + commandName, helpTopic);
			}

			for (String alias : command.aliases()) {
				String aliasName = namespaceAddon + alias;

				// Don't override other plugin's help topics
				if (Bukkit.getPluginCommand(aliasName) != null) {
					continue;
				}

				final HelpTopic helpTopic;
				if (commandAPIHelpTopic instanceof BukkitHelpTopicWrapper bukkitHelpTopic) {
					helpTopic = bukkitHelpTopic.helpTopic();
				} else {
					// We want to get all aliases (including the original command name),
					// except for the current alias
					List<String> aliases = new ArrayList<>(Arrays.asList(command.aliases()));
					aliases.add(command.commandName());
					aliases.remove(alias);

					helpTopic = new CustomCommandAPIHelpTopic(aliasName, aliases.toArray(String[]::new), commandAPIHelpTopic, command.rootNode());
				}
				helpTopicsToAdd.put("/" + aliasName, helpTopic);
			}
		}

		// We have to use helpTopics.put (instead of .addTopic) because we're overwriting an existing help topic, not adding a new help topic
		getHelpMap().putAll(helpTopicsToAdd);
	}

	@Override
	public void onDisable() {
		// Nothing to do
	}

	@Override
	@Unimplemented(because = REQUIRES_CSS)
	public abstract CommandSender getCommandSenderFromCommandSource(Source cs);

	@Override
	@Unimplemented(because = REQUIRES_CRAFTBUKKIT)
	public abstract Source getBrigadierSourceFromCommandSender(CommandSender sender);

	public void registerPermission(String string) {
		try {
			Bukkit.getPluginManager().addPermission(new Permission(string));
		} catch (IllegalArgumentException ignored) {
			// Exception is thrown if we attempt to register a permission that already exists
			//  If it already exists, that's totally fine, so just ignore the exception
		}
	}

	@Override
	@Unimplemented(because = REQUIRES_MINECRAFT_SERVER)
	public abstract SuggestionProvider<Source> getSuggestionProvider(SuggestionProviders suggestionProvider);

	/**
	 * {@inheritDoc}
	 * On Bukkit, namespaces must not be empty, and can only contain 0-9, a-z, underscores, periods, and hyphens.
	 */
	@Override
	public String validateNamespace(ExecutableCommand<?, CommandSender> command, String namespace) {
		if (namespace.isEmpty()) {
			CommandAPI.logNormal("Registering command '" + command.getName() + "' using the default namespace because an empty namespace was given!");
			return config.getNamespace();
		}
		if (!CommandAPIHandler.NAMESPACE_PATTERN.matcher(namespace).matches()) {
			CommandAPI.logNormal("Registering comand '" + command.getName() + "' using the default namespace because an invalid namespace (" + namespace + ") was given. Only 0-9, a-z, underscores, periods and hyphens are allowed!");
			return config.getNamespace();
		}

		// Namespace is good, return it
		return namespace;
	}

	@Override
	public void preCommandRegistration(String commandName) {
		// Warn if the command we're registering already exists in this plugin's
		// plugin.yml file
		final PluginCommand pluginCommand = Bukkit.getPluginCommand(commandName);
		if (pluginCommand == null) {
			return;
		}
		String pluginName = pluginCommand.getPlugin().getName();
		if (config.getPlugin().getName().equals(pluginName)) {
			CommandAPI.logWarning(
				"Plugin command /%s is registered by Bukkit (%s). Did you forget to remove this from your plugin.yml file?"
					.formatted(commandName, pluginName));
		} else {
			CommandAPI.logNormal(
				"Plugin command /%s is registered by Bukkit (%s). You may have to use /minecraft:%s to execute your command."
					.formatted(commandName, pluginName, commandName));
		}
	}

	@Override
	public void postCommandRegistration(RegisteredCommand<CommandSender> registeredCommand, LiteralCommandNode<Source> resultantNode, List<LiteralCommandNode<Source>> aliasNodes) {
		commandRegistrationStrategy.postCommandRegistration(registeredCommand, resultantNode, aliasNodes);

		// Register the command's permission string (if it exists) to Bukkit's manager
		CommandPermission permission = registeredCommand.rootNode().permission();
		permission.getPermission().ifPresent(this::registerPermission);

		if (!CommandAPI.canRegister()) {
			// Adding the command to the help map usually happens in `CommandAPIBukkit#onEnable`
			//  We'll make sure to retrieve the merged versions from CommandAPIHandler
			CommandAPIHandler<?, CommandSender, ?> handler = CommandAPIHandler.getInstance();
			Map<String, RegisteredCommand<CommandSender>> registeredCommands = handler.registeredCommands;
			updateHelpForCommands(List.of(
				registeredCommands.get(registeredCommand.commandName()),
				registeredCommands.get(registeredCommand.namespace() + ":" + registeredCommand.commandName())
			));

			// Sending command dispatcher packets usually happens when Players join the server
			for (Player p : Bukkit.getOnlinePlayers()) {
				p.updateCommands();
			}
		}
	}

	@Override
	public void registerCommandNode(LiteralCommandNode<Source> node, String namespace) {
		DifferentClientNode.rewriteAllChildren(null, node, true);
		commandRegistrationStrategy.registerCommandNode(node, namespace);
	}

	@Override
	public void unregister(String commandName, boolean unregisterNamespaces) {
		unregisterInternal(commandName, unregisterNamespaces, false);
	}

	/**
	 * Unregisters a command from the CommandGraph, so it can't be run anymore. This Bukkit-specific unregister has an
	 * additional parameter, {@code unregisterBukkit}, compared to {@link CommandAPI#unregister(String, boolean)}.
	 *
	 * @param commandName          the name of the command to unregister
	 * @param unregisterNamespaces whether the unregistration system should attempt to remove versions of the
	 *                             command that start with a namespace. E.g. `minecraft:command`, `bukkit:command`,
	 *                             or `plugin:command`. If true, these namespaced versions of a command are also
	 *                             unregistered.
	 * @param unregisterBukkit     whether the unregistration system should unregister Vanilla or Bukkit commands. If true,
	 *                             only Bukkit commands are unregistered, otherwise only Vanilla commands are unregistered.
	 *                             For the purposes of this parameter, commands registered using the CommandAPI are Vanilla
	 *                             commands, and commands registered by other plugin using Bukkit API are Bukkit commands.
	 */
	public static void unregister(String commandName, boolean unregisterNamespaces, boolean unregisterBukkit) {
		if (!unregisterBukkit) {
			// If we're unregistering vanilla commands (which includes CommandAPI commands),
			//  attempt to remove their information from the `registeredCommands` map.
			//  `CommandAPIHandler#unregister` usually does this, but this method bypasses that.
			CommandAPIHandler<?, CommandSender, ?> handler = CommandAPIHandler.getInstance();
			Map<String, RegisteredCommand<CommandSender>> registeredCommands = handler.registeredCommands;

			registeredCommands.remove(commandName);
			if (unregisterNamespaces) CommandAPIHandler.removeCommandNamespace(registeredCommands, commandName, e -> true);
		}

		CommandAPIBukkit.get().unregisterInternal(commandName, unregisterNamespaces, unregisterBukkit);
	}

	private void unregisterInternal(String commandName, boolean unregisterNamespaces, boolean unregisterBukkit) {
		CommandAPI.logInfo("Unregistering command /" + commandName);

		commandRegistrationStrategy.unregister(commandName, unregisterNamespaces, unregisterBukkit);

		if (!CommandAPI.canRegister()) {
			// Help topics (from Bukkit and CommandAPI) are only setup after plugins enable, so we only need to worry
			//  about removing them once the server is loaded.
			getHelpMap().remove("/" + commandName);

			// Notify players
			for (Player p : Bukkit.getOnlinePlayers()) {
				p.updateCommands();
			}
		}
	}

	@Override
	public final CommandDispatcher<Source> getBrigadierDispatcher() {
		return commandRegistrationStrategy.getBrigadierDispatcher();
	}

	@Override
	@Unimplemented(because = {REQUIRES_MINECRAFT_SERVER, VERSION_SPECIFIC_IMPLEMENTATION})
	public abstract Optional<JsonObject> getArgumentTypeProperties(ArgumentType<?> type);

	@Unimplemented(because = REQUIRES_MINECRAFT_SERVER) // What are the odds?
	public abstract <T> T getMinecraftServer();

	@Override
	public CommandAPILogger getLogger() {
		return new DefaultLogger();
	}

	private static class DefaultLogger extends Logger implements CommandAPILogger {
		protected DefaultLogger() {
			super("CommandAPI", null);
			setParent(Bukkit.getServer().getLogger());
			setLevel(Level.ALL);
		}

		@Override
		public void severe(String message, Throwable exception) {
			super.log(Level.SEVERE, message, exception);
		}
	}

	@Override
	@Unimplemented(because = VERSION_SPECIFIC_IMPLEMENTATION)
	public abstract void reloadDataPacks();

	@Override
	public void updateRequirements(CommandSender player) {
		((Player) player).updateCommands();
	}

	@Override
	public Argument<String> newConcreteMultiLiteralArgument(String nodeName, String[] literals) {
		return new MultiLiteralArgument(nodeName, literals);
	}

	@Override
	public Argument<String> newConcreteLiteralArgument(String nodeName, String literal) {
		return new LiteralArgument(nodeName, literal);
	}

	/**
	 * Forces a command to return a success value of 0
	 *
	 * @param message Description of the error message, formatted as an array of base components
	 * @return a {@link WrapperCommandSyntaxException} that wraps Brigadier's
	 * {@link CommandSyntaxException}
	 */
	public static WrapperCommandSyntaxException failWithBaseComponents(BaseComponent... message) {
		return CommandAPI.failWithMessage(BukkitTooltip.messageFromBaseComponents(message));
	}

	/**
	 * Forces a command to return a success value of 0
	 *
	 * @param message Description of the error message, formatted as an adventure chat component
	 * @return a {@link WrapperCommandSyntaxException} that wraps Brigadier's
	 * {@link CommandSyntaxException}
	 */
	public static WrapperCommandSyntaxException failWithAdventureComponent(Component message) {
		return CommandAPI.failWithMessage(BukkitTooltip.messageFromAdventureComponent(message));
	}

	/**
	 * Forces a command to return a success value of 0
	 *
	 * @param message Description of the error message, formatted as an adventure chat component
	 * @return a {@link WrapperCommandSyntaxException} that wraps Brigadier's
	 * {@link CommandSyntaxException}
	 */
	public static WrapperCommandSyntaxException failWithAdventureComponent(ComponentLike message) {
		return CommandAPI.failWithMessage(BukkitTooltip.messageFromAdventureComponent(message.asComponent()));
	}

	/**
	 * Initializes the CommandAPI's implementation of an NBT API. If you are shading
	 * the CommandAPI, you should be using
	 * {@link CommandAPIConfig#initializeNBTAPI(Class, Function)} in your
	 * {@code onLoad} method instead of calling this method.
	 *
	 * @param <T>                     the type that the NBT compound container class
	 *                                is
	 * @param nbtContainerClass       the NBT compound container class. For example,
	 *                                {@code NBTContainer.class}
	 * @param nbtContainerConstructor a function that takes an Object (NMS
	 *                                {@code NBTTagCompound}) and returns an
	 *                                instance of the provided NBT compound
	 *                                container. For example,
	 *                                {@code NBTContainer::new}.
	 */
	public static <T> void initializeNBTAPI(Class<T> nbtContainerClass, Function<Object, T> nbtContainerConstructor) {
		getConfiguration().lateInitializeNBT(nbtContainerClass, nbtContainerConstructor);
	}

	protected void registerBukkitRecipesSafely(Iterator<Recipe> recipes) {
		Recipe recipe;
		while (recipes.hasNext()) {
			recipe = recipes.next();
			try {
				Bukkit.addRecipe(recipe);
				if (recipe instanceof Keyed keyedRecipe) {
					CommandAPI.logInfo("Re-registering recipe: " + keyedRecipe.getKey());
				}
			} catch (IllegalStateException e) { // From CraftingManager - "Duplicate recipe ignored with ID %id%"
				assert true; // Can't re-register registered recipes. Not an error.
			} catch (Exception e) {
				if (recipe instanceof Keyed keyedRecipe) {
					CommandAPI.logError("Failed to register recipe " + keyedRecipe.getKey() + ": " + e.getMessage());
				} else {
					CommandAPI.logError("Failed to register recipe: " + e.getMessage());
				}
			}
		}
	}

	@Override
	public Predicate<CommandSender> getPermissionCheck(CommandPermission permission) {
		final Predicate<CommandSender> senderCheck;

		if (permission.equals(CommandPermission.NONE)) {
			// No permissions always passes
			senderCheck = CommandPermission.TRUE();
		} else if (permission.equals(CommandPermission.OP)) {
			senderCheck = CommandSender::isOp;
		} else {
			Optional<String> permissionStringWrapper = permission.getPermission();
			if (permissionStringWrapper.isPresent()) {
				String permissionString = permissionStringWrapper.get();
				// check permission
				senderCheck = sender -> sender.hasPermission(permissionString);
			} else {
				// No permission always passes
				senderCheck = CommandPermission.TRUE();
			}
		}

		// Negate if specified
		return permission.isNegated() ? senderCheck.negate() : senderCheck;
	}
}
