package dev.jorel.commandapi;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.jorel.commandapi.arguments.AbstractArgument;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.SuggestionProviders;
import dev.jorel.commandapi.commandsenders.AbstractCommandSender;
import dev.jorel.commandapi.commandsenders.AbstractPlayer;
import dev.jorel.commandapi.commandsenders.BukkitCommandSender;
import dev.jorel.commandapi.network.BukkitCommandAPIMessenger;
import dev.jorel.commandapi.nms.NMS;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class CommandAPIBukkit<Source> implements BukkitPlatform<Source> {

	// References to utility classes
	private static CommandAPIBukkit<?> bukkit;
	protected static InternalBukkitConfig config;
	private BukkitCommandAPIMessenger messenger;

	protected JavaPlugin plugin;
	protected NMS<Source> nms;
	private CommandRegistrationStrategy<Source> commandRegistrationStrategy;

	private CommandAPILogger logger;

	private final boolean isPaperBrigAPI;

	protected CommandAPIBukkit() {
		CommandAPIBukkit.bukkit = this;

		boolean paperCommandSourceStackPresent;
		try {
			Class.forName("io.papermc.paper.command.brigadier.CommandSourceStack");
			paperCommandSourceStackPresent = true;
		} catch (ClassNotFoundException e) {
			paperCommandSourceStackPresent = false;
		}
		this.isPaperBrigAPI = paperCommandSourceStackPresent;
	}

	@SuppressWarnings("unchecked")
	public static <Source> CommandAPIBukkit<Source> get() {
		if (CommandAPIBukkit.bukkit != null) {
			return (CommandAPIBukkit<Source>) bukkit;
		}
		throw new IllegalStateException("Tried to access CommandAPIBukkit instance, but it was null! Are you using CommandAPI features before calling CommandAPI#onLoad?");
	}

	public JavaPlugin getPlugin() {
		return plugin;
	}

	public NMS<Source> getNMS() {
		if (nms != null) {
			return nms;
		}
		throw new IllegalStateException("Tried to access NMS instance, but it was null! Are you using CommandAPI features before calling CommandAPI#onLoad?");
	}

	public static InternalBukkitConfig getConfiguration() {
		if (config != null) {
			return config;
		}
		throw new IllegalStateException("Tried to access InternalBukkitConfig, but it was null! Did you load the CommandAPI properly with CommandAPI#onLoad?");
	}

	public CommandRegistrationStrategy<Source> getCommandRegistrationStrategy() {
		return commandRegistrationStrategy;
	}

	public void onLoad() {
		checkDependencies();
	}

	protected void stopCommandRegistrations() {
		// Prevent command registration after server has loaded
		Bukkit.getServer().getPluginManager().registerEvents(new Listener() {
			// We want the lowest priority so that we always get to this first, in case a dependent plugin is using
			//  CommandAPI features in their own ServerLoadEvent listener for some reason
			@EventHandler(priority = EventPriority.LOWEST)
			public void onServerLoad(ServerLoadEvent event) {
				CommandAPI.stopCommandRegistration();
			}
		}, plugin);
	}

	protected void checkDependencies() {
		// Log successful hooks
		Class<?> nbtContainerClass = CommandAPIBukkit.getConfiguration().getNBTContainerClass();
		if (nbtContainerClass != null && CommandAPIBukkit.getConfiguration().getNBTContainerConstructor() != null) {
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

		commandRegistrationStrategy = createCommandRegistrationStrategy();
	}

	/*
	 * Generate and register help topics
	 */
	private String generateCommandHelpPrefix(String command) {
		return (Bukkit.getPluginCommand(command) == null ? "/" : "/minecraft:") + command;
	}

	private String generateCommandHelpPrefix(String command, String namespace) {
		return (Bukkit.getPluginCommand(command) == null ? "/" + namespace + ":" : "/minecraft:") + command;
	}

	private void generateHelpUsage(StringBuilder sb, RegisteredCommand command) {
		// Generate usages
		String[] usages = getUsageList(command);

		if (usages.length == 0) {
			// Might happen if the developer calls `.withUsage()` with no parameters
			// They didn't give any usage, so we won't put any there
			return;
		}

		sb.append(ChatColor.GOLD).append("Usage: ").append(ChatColor.WHITE);
		// If 1 usage, put it on the same line, otherwise format like a list
		if (usages.length == 1) {
			sb.append(usages[0]);
		} else {
			for (String usage : usages) {
				sb.append("\n- ").append(usage);
			}
		}
	}

	private String[] getUsageList(RegisteredCommand currentCommand) {
		List<RegisteredCommand> commandsWithIdenticalNames = new ArrayList<>();

		// Collect every command with the same name
		for (RegisteredCommand registeredCommand : CommandAPIHandler.getInstance().registeredCommands) {
			if (registeredCommand.commandName().equals(currentCommand.commandName())) {
				commandsWithIdenticalNames.add(registeredCommand);
			}
		}

		// Generate command usage or fill it with a user provided one
		final String[] usages;
		final Optional<String[]> usageDescription = currentCommand.usageDescription();
		if (usageDescription.isPresent()) {
			usages = usageDescription.get();
		} else {
			// TODO: Figure out if default usage generation should be updated
			final int numCommandsWithIdenticalNames = commandsWithIdenticalNames.size();
			usages = new String[numCommandsWithIdenticalNames];
			for (int i = 0; i < numCommandsWithIdenticalNames; i++) {
				final RegisteredCommand command = commandsWithIdenticalNames.get(i);
				StringBuilder usageString = new StringBuilder();
				usageString.append("/").append(command.commandName()).append(" ");
				for (AbstractArgument<?, ?, ?, ?> arg : command.arguments()) {
					usageString.append(arg.getHelpString()).append(" ");
				}
				usages[i] = usageString.toString().trim();
			}
		}
		return usages;
	}

	void updateHelpForCommands(List<RegisteredCommand> commands) {
		Map<String, HelpTopic> helpTopicsToAdd = new HashMap<>();

		for (RegisteredCommand command : commands) {
			if (isPaperBrigAPI && !command.shouldGenerateHelpTopic()) continue;

			// Don't override the plugin help topic
			String commandPrefix = generateCommandHelpPrefix(command.commandName());
			
			StringBuilder aliasSb = new StringBuilder();
			final String shortDescription;
			
			// Must be empty string, not null as defined by OBC::CustomHelpTopic
			final String permission = command.permission().getPermission().orElse("");
			
			HelpTopic helpTopic;
			final Optional<Object> commandHelpTopic = command.helpTopic();
			if (commandHelpTopic.isPresent()) {
				helpTopic = (HelpTopic) commandHelpTopic.get();
				shortDescription = "";
			} else {
				// Generate short description
				final Optional<String> shortDescriptionOptional = command.shortDescription();
				final Optional<String> fullDescriptionOptional = command.fullDescription();
				if (shortDescriptionOptional.isPresent()) {
					shortDescription = shortDescriptionOptional.get();
				} else if (fullDescriptionOptional.isPresent()) {
					shortDescription = fullDescriptionOptional.get();
				} else {
					shortDescription = "A command by the " + plugin.getName() + " plugin.";
				}
	
				// Generate full description
				StringBuilder sb = new StringBuilder();
				if (fullDescriptionOptional.isPresent()) {
					sb.append(ChatColor.GOLD).append("Description: ").append(ChatColor.WHITE).append(fullDescriptionOptional.get()).append("\n");
				}
	
				generateHelpUsage(sb, command);
				sb.append("\n");
	
				// Generate aliases. We make a copy of the StringBuilder because we
				// want to change the output when we register aliases
				aliasSb = new StringBuilder(sb.toString());
				if (command.aliases().length > 0) {
					sb.append(ChatColor.GOLD).append("Aliases: ").append(ChatColor.WHITE).append(String.join(", ", command.aliases()));
				}

				helpTopic = nms.generateHelpTopic(commandPrefix, shortDescription, sb.toString().trim(), permission);
			}
			helpTopicsToAdd.put(commandPrefix, helpTopic);

			for (String alias : command.aliases()) {
				if (commandHelpTopic.isPresent()) {
					helpTopic = (HelpTopic) commandHelpTopic.get();
				} else {
					StringBuilder currentAliasSb = new StringBuilder(aliasSb.toString());
					currentAliasSb.append(ChatColor.GOLD).append("Aliases: ").append(ChatColor.WHITE);
	
					// We want to get all aliases (including the original command name),
					// except for the current alias
					List<String> aliases = new ArrayList<>(Arrays.asList(command.aliases()));
					aliases.add(command.commandName());
					aliases.remove(alias);
	
					currentAliasSb.append(String.join(", ", aliases));
	
					// Don't override the plugin help topic
					commandPrefix = generateCommandHelpPrefix(alias);

					helpTopic = nms.generateHelpTopic(commandPrefix, shortDescription, currentAliasSb.toString().trim(), permission);
				}
				helpTopicsToAdd.put(commandPrefix, helpTopic);
			}
		}

		// We have to use helpTopics.put (instead of .addTopic) because we're overwriting an existing help topic, not adding a new help topic
		nms.getHelpMap().putAll(helpTopicsToAdd);
	}

	@Override
	public void onDisable() {
		// Nothing to do
	}

	@Override
	public abstract BukkitCommandSender<? extends CommandSender> wrapCommandSender(CommandSender sender);

	@Override
	public void registerPermission(String string) {
		try {
			Bukkit.getPluginManager().addPermission(new Permission(string));
		} catch (IllegalArgumentException e) {
			assert true; // nop, not an error.
		}
	}

	@Override
	public void preCommandRegistration(String commandName) {
		if (Bukkit.getServer() == null) {
			return; // and pray that nothing happens later
		}
		// Warn if the command we're registering already exists in this plugin's
		// plugin.yml file
		final PluginCommand pluginCommand = Bukkit.getPluginCommand(commandName);
		if (pluginCommand == null) {
			return;
		}
		String pluginName = pluginCommand.getPlugin().getName();
		if (plugin.getName().equals(pluginName)) {
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
	public void postCommandRegistration(RegisteredCommand registeredCommand, LiteralCommandNode<Source> resultantNode, List<LiteralCommandNode<Source>> aliasNodes) {
		commandRegistrationStrategy.postCommandRegistration(registeredCommand, resultantNode, aliasNodes);

		if (!CommandAPI.canRegister()) {
			// Adding the command to the help map usually happens in `CommandAPIBukkit#onEnable`
			updateHelpForCommands(List.of(registeredCommand));

			// Sending command dispatcher packets usually happens when Players join the server
			for (Player p : Bukkit.getOnlinePlayers()) {
				p.updateCommands();
			}
		}
	}

	@Override
	public LiteralCommandNode<Source> registerCommandNode(LiteralArgumentBuilder<Source> node, String namespace) {
		return commandRegistrationStrategy.registerCommandNode(node, namespace);
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
		CommandAPIBukkit.get().unregisterInternal(commandName, unregisterNamespaces, unregisterBukkit);
	}

	private void unregisterInternal(String commandName, boolean unregisterNamespaces, boolean unregisterBukkit) {
		CommandAPI.logInfo("Unregistering command /" + commandName);

		commandRegistrationStrategy.unregister(commandName, unregisterNamespaces, unregisterBukkit);

		if (!CommandAPI.canRegister()) {
			// Help topics (from Bukkit and CommandAPI) are only setup after plugins enable, so we only need to worry
			//  about removing them once the server is loaded.
			nms.getHelpMap().remove("/" + commandName);

			// Notify players
			for (Player p : Bukkit.getOnlinePlayers()) {
				p.updateCommands();
			}
		}
	}

	@Override
	public BukkitCommandAPIMessenger setupMessenger() {
		messenger = new BukkitCommandAPIMessenger(plugin);
		return messenger;
	}

	@Override
	public BukkitCommandAPIMessenger getMessenger() {
		return messenger;
	}

	@Override
	public final CommandDispatcher<Source> getBrigadierDispatcher() {
		return commandRegistrationStrategy.getBrigadierDispatcher();
	}

	@Override
	public CommandAPILogger getLogger() {
		if (logger == null) {
			logger = new DefaultLogger();
		}
		return logger;
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
	public void updateRequirements(AbstractPlayer<?> player) {
		((Player) player.getSource()).updateCommands();
	}

	@Override
	public Argument<String> newConcreteMultiLiteralArgument(String nodeName, String[] literals) {
		return new MultiLiteralArgument(nodeName, literals);
	}

	@Override
	public Argument<String> newConcreteLiteralArgument(String nodeName, String literal) {
		return new LiteralArgument(nodeName, literal);
	}

	@Override
	public CommandAPICommand newConcreteCommandAPICommand(CommandMetaData<CommandSender> meta) {
		return new CommandAPICommand(meta);
	}
	
	/**
	 * Initializes the CommandAPI's implementation of an NBT API. If you are shading
	 * the CommandAPI, you should be using
	 * {@link CommandAPIBukkitConfig#initializeNBTAPI(Class, Function)} in your
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

	public void registerBukkitRecipesSafely(Iterator<Recipe> recipes) {
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

	boolean isInvalidNamespace(String commandName, String namespace) {
		if (namespace == null) {
			throw new NullPointerException("Parameter 'namespace' was null when registering command /" + commandName + "!");
		}
		if (namespace.isEmpty()) {
			CommandAPI.logNormal("Registering command '" + commandName + "' using the default namespace because an empty namespace was given!");
			return true;
		}
		if (!CommandAPIHandler.NAMESPACE_PATTERN.matcher(namespace).matches()) {
			CommandAPI.logNormal("Registering comand '" + commandName + "' using the default namespace because an invalid namespace (" + namespace + ") was given. Only 0-9, a-z, underscores, periods and hyphens are allowed!");
			return true;
		}
		return false;
	}

	/*
	 * Implement several methods from CommandAPIPlatform that just need to delegate to NMS
	 */

	@Override
	public BukkitCommandSender<? extends CommandSender> getSenderForCommand(CommandContext<Source> cmdCtx, boolean forceNative) {
		return nms.getSenderForCommand(cmdCtx, forceNative);
	}

	@Override
	public Source getBrigadierSourceFromCommandSender(AbstractCommandSender<? extends CommandSender> sender) {
		return nms.getBrigadierSourceFromCommandSender(sender);
	}

	@Override
	public SuggestionProvider<Source> getSuggestionProvider(SuggestionProviders suggestionProvider) {
		return nms.getSuggestionProvider(suggestionProvider);
	}

	@Override
	public BukkitCommandSender<? extends CommandSender> getCommandSenderFromCommandSource(Source css) {
		return nms.getCommandSenderFromCommandSource(css);
	}

	@Override
	public void createDispatcherFile(File file, CommandDispatcher<Source> dispatcher) throws IOException {
		nms.createDispatcherFile(file, dispatcher);
	}

	@Override
	public void reloadDataPacks() {
		nms.reloadDataPacks();
	}
}
