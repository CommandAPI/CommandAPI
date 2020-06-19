package dev.jorel.commandapi;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.permissions.Permission;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.CustomArgument.CustomArgumentException;
import dev.jorel.commandapi.arguments.CustomArgument.MessageBuilder;
import dev.jorel.commandapi.arguments.CustomProvidedArgument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.Location2DArgument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import dev.jorel.commandapi.arguments.ScoreHolderArgument;
import dev.jorel.commandapi.nms.NMS;
import dev.jorel.commandapi.nms.NMS_1_13;
import dev.jorel.commandapi.nms.NMS_1_13_1;
import dev.jorel.commandapi.nms.NMS_1_13_2;
import dev.jorel.commandapi.nms.NMS_1_14;
import dev.jorel.commandapi.nms.NMS_1_14_3;
import dev.jorel.commandapi.nms.NMS_1_14_4;
import dev.jorel.commandapi.nms.NMS_1_15_R1;

/**
 * Handles the main backend of the CommandAPI. This constructs brigadier Command
 * objects, applies and generates arguments and handles suggestions. This also
 * handles permission registration for Bukkit, interactions for NMS and the
 * registration and unregistration of commands. 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public final class CommandAPIHandler {
	private TreeMap<String, CommandPermission> permissionsToFix;

	// Cache maps
	private static Map<ClassCache, Field> fields;
	private static Map<ClassCache, Method> methods;

	// NMS variables
	private static String packageName = null;
	private CommandDispatcher dispatcher;
	private static NMS nms;
	private Object nmsServer;

	/**
	 * Returns an instance of NMS.
	 * @return an instance of NMS
	 */
	public static NMS getNMS() {
		return nms;
	}

	/**
	 * Initialise the CommandAPIHandler. Hooks into the right version of NMS using
	 * the different versions, hooks into the NBTAPI if possible and hooks into
	 * Spigot's API if possible. Also initialises reflection caches and the NMS
	 * command dispatcher.
	 * 
	 * @throws ClassNotFoundException if brigadier is not found or cannot properly
	 *                                hook into NMS
	 */
	protected CommandAPIHandler() throws ClassNotFoundException, InstantiationException {
		// Package checks

		try {
			Class.forName("com.mojang.brigadier.CommandDispatcher");
		} catch (ClassNotFoundException e) {
			throw new ClassNotFoundException("Cannot hook into Brigadier (Are you running Minecraft 1.13 or above?)");
		}

		// Setup NMS
		try {
			this.nmsServer = Bukkit.getServer().getClass().getDeclaredMethod("getServer").invoke(Bukkit.getServer());
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			CommandAPIMain.getLog().severe("Unable to hook into NMS properly!");
		}
		CommandAPIHandler.packageName = nmsServer.getClass().getPackage().getName();

		// Load NMS version
		String version = null;
		try {
			version = (String) Class.forName(packageName + ".MinecraftServer").getDeclaredMethod("getVersion")
					.invoke(nmsServer);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			CommandAPIMain.getLog().severe("Failed to retrieve current Minecraft version!");
		}

		switch (version) {
			case "1.13":
				nms = new NMS_1_13();
				break;
			case "1.13.1":
				nms = new NMS_1_13_1();
				break;
			case "1.13.2":
				nms = new NMS_1_13_2();
				break;
			case "1.14":
			case "1.14.1":
			case "1.14.2":
				nms = new NMS_1_14();
				break;
			case "1.14.3":
				nms = new NMS_1_14_3();
				break;
			case "1.14.4":
				nms = new NMS_1_14_4();
				break;
			case "1.15":
			case "1.15.1":
			case "1.15.2":
				nms = new NMS_1_15_R1();
				break;
			default:
				throw new UnsupportedClassVersionError("This version of Minecraft is unsupported: " + version);
		}

		// Log successful hooks
		if (CommandAPIMain.getConfiguration().hasVerboseOutput()) {
			String compatibleVersions = Arrays.toString(nms.compatibleVersions());
			compatibleVersions = compatibleVersions.substring(1, compatibleVersions.length() - 1);
			CommandAPIMain.getLog()
					.info("Hooked into " + nms.getClass().getName() + " (compatible with " + compatibleVersions + ")");
		}

		// Checks other dependencies
		if (Bukkit.getPluginManager().getPlugin("NBTAPI") != null) {
			CommandAPIMain.getLog().info("Hooked into the NBTAPI successfully.");
		} else {
			CommandAPIMain.getLog().warning(
					"Couldn't hook into the NBTAPI for NBT support. See https://www.spigotmc.org/resources/nbt-api.7939/");
		}

		try {
			Class.forName("org.spigotmc.SpigotConfig");
			CommandAPIMain.getLog().info("Hooked into Spigot successfully for Chat/ChatComponents");
		} catch (ClassNotFoundException e) {
			CommandAPIMain.getLog().warning("Couldn't hook into Spigot for chat/chatcomponents");
		}

		// Everything from this line will use getNMSClass(), so we initialize our cache
		// here
		fields = new HashMap<>();
		methods = new HashMap<>();
		permissionsToFix = new TreeMap<>();

		this.dispatcher = nms.getBrigadierDispatcher(nmsServer);
	}

	/**
	 * Unregisters a command from the NMS command graph.
	 * 
	 * @param commandName the name of the command to unregister
	 * @param force       whether the unregistration system should attempt to remove
	 *                    all instances of the command, regardless of whether they
	 *                    have been registered by Minecraft, Bukkit or Spigot etc.
	 */
	protected void unregister(String commandName, boolean force) {
		try {
			if (CommandAPIMain.getConfiguration().hasVerboseOutput()) {
				CommandAPIMain.getLog().info("Unregistering command /" + commandName);
			}

			// Get the child nodes from the loaded dispatcher class
			Field children = getField(CommandNode.class, "children");
			Map<String, CommandNode<?>> commandNodeChildren = (Map<String, CommandNode<?>>) children
					.get(dispatcher.getRoot());

			if (force) {
				// Remove them by force
				List<String> keysToRemove = new ArrayList<>();
				commandNodeChildren.keySet().stream().filter(s -> s.contains(":"))
						.filter(s -> s.split(":")[1].equalsIgnoreCase(commandName)).forEach(keysToRemove::add);
				keysToRemove.forEach(commandNodeChildren::remove);
			}

			// Otherwise, just remove them normally
			commandNodeChildren.remove(commandName);
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Generates a command to be registered by the CommandAPI.
	 * @param args set of ordered argument pairs which contain the tooltip text and their argument types 
	 * @param executor code to be ran when the command is executed
	 * @return a brigadier command which is registered internally
	 * @throws CommandSyntaxException if an error occurs when the command is ran
	 */
	private Command generateCommand(LinkedHashMap<String, Argument> args, CustomCommandExecutor executor)
			throws CommandSyntaxException {

		// Generate our command from executor
		return (cmdCtx) -> {

			// Get the CommandSender via NMS
			CommandSender sender = nms.getSenderForCommand(cmdCtx);

			// Array for arguments for executor
			List<Object> argList = new ArrayList<>();

			// Populate array
			for (Entry<String, Argument> entry : args.entrySet()) {
				switch (entry.getValue().getArgumentType()) {
				case ADVANCEMENT:
					argList.add(nms.getAdvancement(cmdCtx, entry.getKey()));
					break;
				case AXIS:
					argList.add(nms.getAxis(cmdCtx, entry.getKey()));
					break;				
				case CHAT:
					argList.add(nms.getChat(cmdCtx, entry.getKey()));
					break;
				case CHATCOLOR:
					argList.add(nms.getChatColor(cmdCtx, entry.getKey()));
					break;
				case CHAT_COMPONENT:
					argList.add(nms.getChatComponent(cmdCtx, entry.getKey()));
					break;
				case CUSTOM:
					CustomArgument arg = (CustomArgument) entry.getValue();
					String customresult = (String) cmdCtx.getArgument(entry.getKey(), String.class);
					try {
						argList.add(arg.getParser().apply(customresult));
					} catch (CustomArgumentException e) {
						throw e.toCommandSyntax(customresult, cmdCtx);
					} catch (Exception e) {
						String errorMsg = new MessageBuilder("Error in executing command ").appendFullInput()
								.append(" - ").appendArgInput().appendHere().toString().replace("%input%", customresult)
								.replace("%finput%", cmdCtx.getInput());
						throw new SimpleCommandExceptionType(() -> {
							return errorMsg;
						}).create();
					}
					break;
				case ENCHANTMENT:
					argList.add(nms.getEnchantment(cmdCtx, entry.getKey()));
					break;
				case ENTITY_SELECTOR:
					EntitySelectorArgument argument = (EntitySelectorArgument) entry.getValue();
					argList.add(nms.getEntitySelector(cmdCtx, entry.getKey(), argument.getEntitySelector()));
					break;
				case ENTITY_TYPE:
					argList.add(nms.getEntityType(cmdCtx, entry.getKey(), sender));
					break;
				case ENVIRONMENT:
					argList.add(nms.getDimension(cmdCtx, entry.getKey()));
					break;
				case FLOAT_RANGE:
					argList.add(nms.getFloatRange(cmdCtx, entry.getKey()));
					break;
				case FUNCTION:
					argList.add(nms.getFunction(cmdCtx, entry.getKey()));
					break;
				case INT_RANGE:
					argList.add(nms.getIntRange(cmdCtx, entry.getKey()));
					break;
				case ITEMSTACK:
					argList.add(nms.getItemStack(cmdCtx, entry.getKey()));
					break;
				case LITERAL:
					break;
				case LOCATION:
					LocationType locationType = ((LocationArgument) entry.getValue()).getLocationType();
					argList.add(nms.getLocation(cmdCtx, entry.getKey(), locationType, sender));
					break;
				case LOCATION_2D:
					LocationType locationType2d = ((Location2DArgument) entry.getValue()).getLocationType();
					argList.add(nms.getLocation2D(cmdCtx, entry.getKey(), locationType2d, sender));
					break;
				case LOOT_TABLE:
					argList.add(nms.getLootTable(cmdCtx, entry.getKey()));
					break;
				case MATH_OPERATION:
					argList.add(nms.getMathOperation(cmdCtx, entry.getKey()));
					break;
				case NBT_COMPOUND:
					argList.add(nms.getNBTCompound(cmdCtx, entry.getKey()));
					break;
				case OBJECTIVE:
					argList.add(nms.getObjective(cmdCtx, entry.getKey(), sender));
					break;
				case OBJECTIVE_CRITERIA:
					argList.add(nms.getObjectiveCriteria(cmdCtx, entry.getKey()));
					break;
				case PARTICLE:
					argList.add(nms.getParticle(cmdCtx, entry.getKey()));
					break;
				case PLAYER:
					argList.add(nms.getPlayer(cmdCtx, entry.getKey()));
					break;
				case POTION_EFFECT:
					argList.add(nms.getPotionEffect(cmdCtx, entry.getKey()));
					break;
				case RECIPE:
					argList.add(nms.getRecipe(cmdCtx, entry.getKey()));
					break;
				case ROTATION:
					argList.add(nms.getRotation(cmdCtx, entry.getKey()));
					break;
				case SCORE_HOLDER:
					ScoreHolderArgument scoreHolderArgument = (ScoreHolderArgument) entry.getValue();
					argList.add(scoreHolderArgument.isSingle() ? nms.getScoreHolderSingle(cmdCtx, entry.getKey())
							: nms.getScoreHolderMultiple(cmdCtx, entry.getKey()));
					break;
				case SCOREBOARD_SLOT:
					argList.add(nms.getScoreboardSlot(cmdCtx, entry.getKey()));
					break;
				case SIMPLE_TYPE:
					argList.add(cmdCtx.getArgument(entry.getKey(), entry.getValue().getPrimitiveType()));
					break;
				case SOUND:
					argList.add(nms.getSound(cmdCtx, entry.getKey()));
					break;
				case TEAM:
					argList.add(nms.getTeam(cmdCtx, entry.getKey(), sender));
					break;
				case TIME:
					argList.add(nms.getTime(cmdCtx, entry.getKey()));
					break;
				}
			}

			return executor.execute(sender, argList.toArray());
		};
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// SECTION: Permissions //
	//////////////////////////////////////////////////////////////////////////////////////////////////////

	private Predicate generatePermissions(String commandName, CommandPermission permission) {

		// If we've already registered a permission, set it to the "parent" permission.
		/*
		 * This permission generation setup ONLY works iff: - You register the parent
		 * permission node FIRST. - Example: /mycmd - permission node my.perm /mycmd
		 * <arg> - permission node my.perm.other
		 *
		 * the my.perm.other permission node is revoked for the COMMAND REGISTRATION,
		 * however: - The permission node IS REGISTERED. - The permission node, if used
		 * for an argument (as in this case), will be used for suggestions for said
		 * argument
		 */
		if (permissionsToFix.containsKey(commandName.toLowerCase())) {
			if (!permissionsToFix.get(commandName.toLowerCase()).equals(permission)) {
				permission = permissionsToFix.get(commandName.toLowerCase());
			}
		} else {
			// Add permission to a list to fix conflicts with minecraft:permissions
			permissionsToFix.put(commandName.toLowerCase(), permission);
		}

		final CommandPermission finalPermission = permission;

		// Register it to the Bukkit permissions registry
		if (finalPermission.getPermission() != null) {
			try {
				Bukkit.getPluginManager().addPermission(new Permission(finalPermission.getPermission()));
			} catch (IllegalArgumentException e) {
			}
		}

		return (Object clw) -> permissionCheck(nms.getCommandSenderForCLW(clw), finalPermission);
	}

	/**
	 * Checks if a sender has a given permission.
	 * @param sender the sender to check permissions of
	 * @param permission the CommandAPI CommandPermission permission to check
	 * @return true if the sender satisfies the provided permission
	 */
	private boolean permissionCheck(CommandSender sender, CommandPermission permission) {
		if (sender == null) {
			return true;
		}
		if (permission.equals(CommandPermission.NONE)) {
			return true;
		} else if (permission.equals(CommandPermission.OP)) {
			return sender.isOp();
		} else {
			return sender.hasPermission(permission.getPermission());
		}
	}

	protected void fixPermissions() {
		/*
		 * Makes permission checks more "Bukkit" like and less "Vanilla Minecraft" like
		 */
		try {

			// Get the command map to find registered commands
			SimpleCommandMap map = nms.getSimpleCommandMap();
			Field f = getField(SimpleCommandMap.class, "knownCommands");
			Map<String, org.bukkit.command.Command> knownCommands = (Map<String, org.bukkit.command.Command>) f
					.get(map);

			CommandAPIMain.getLog().info("Linking permissions to commands:");
			permissionsToFix.forEach((cmdName, perm) -> {

				if (perm.equals(CommandPermission.NONE)) {
					if (CommandAPIMain.getConfiguration().hasVerboseOutput()) {
						CommandAPIMain.getLog().info("NONE -> /" + cmdName);
					}
					// Set the command permission to empty string (Minecraft standard for "no
					// permission required")
					if (nms.isVanillaCommandWrapper(knownCommands.get(cmdName))) {
						knownCommands.get(cmdName).setPermission("");
					}
					if (nms.isVanillaCommandWrapper(knownCommands.get("minecraft:" + cmdName))) {
						knownCommands.get(cmdName).setPermission("");
					}
				} else {
					if (perm.getPermission() != null) {
						if (CommandAPIMain.getConfiguration().hasVerboseOutput()) {
							CommandAPIMain.getLog().info(perm.getPermission() + " -> /" + cmdName);
						}
						// Set the command permission to the (String) permission node
						if (nms.isVanillaCommandWrapper(knownCommands.get(cmdName))) {
							knownCommands.get(cmdName).setPermission(perm.getPermission());
						}
						if (nms.isVanillaCommandWrapper(knownCommands.get("minecraft:" + cmdName))) {
							knownCommands.get(cmdName).setPermission(perm.getPermission());
						}
					} else {
						// Dafaq?
					}
				}
			});
		} catch (IllegalAccessException | IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// SECTION: Registration //
	//////////////////////////////////////////////////////////////////////////////////////////////////////

	// Builds our NMS command using the given arguments for this method, then
	// registers it
	protected void register(String commandName, CommandPermission permissions, String[] aliases,
			final LinkedHashMap<String, Argument> args, CustomCommandExecutor executor) throws Exception {
		if (CommandAPIMain.getConfiguration().hasVerboseOutput()) {
			// Create a list of argument names
			StringBuilder builder = new StringBuilder();
			args.values().forEach(arg -> builder.append("<").append(arg.getClass().getSimpleName()).append("> "));
			CommandAPIMain.getLog().info("Registering command /" + commandName + " " + builder.toString());
		}

		Command command = generateCommand(args, executor);

		/*
		 * The innermost argument needs to be connected to the executor. Then that
		 * argument needs to be connected to the previous argument etc. Then the first
		 * argument needs to be connected to the command name
		 *
		 * CommandName -> Args1 -> Args2 -> ... -> ArgsN -> Executor
		 */

		LiteralCommandNode resultantNode;
		if (args.isEmpty()) {
			// Link command name to the executor
			resultantNode = this.dispatcher.register((LiteralArgumentBuilder) getLiteralArgumentBuilder(commandName)
					.requires(generatePermissions(commandName, permissions)).executes(command));

			// Register aliases
			for (String alias : aliases) {
				if (CommandAPIMain.getConfiguration().hasVerboseOutput()) {
					CommandAPIMain.getLog().info("Registering alias /" + alias + " -> " + resultantNode.getName());
				}
				// TODO: What if we just do
				// getLiteralArgumentBuilder(alias).redirect(resultantNode)?
				this.dispatcher.register((LiteralArgumentBuilder) getLiteralArgumentBuilder(alias)
						.requires(generatePermissions(alias, permissions)).executes(command));
			}
		} else {

			// List of keys for reverse iteration
			ArrayList<String> keys = new ArrayList<>(args.keySet());

			// Link the last element to the executor
			ArgumentBuilder inner;
			// New scope used here to prevent innerArg accidentally being used below
			{
				Argument innerArg = args.get(keys.get(keys.size() - 1));
				if (innerArg instanceof LiteralArgument) {
					String str = ((LiteralArgument) innerArg).getLiteral();
					inner = getLiteralArgumentBuilderArgument(str, innerArg.getArgumentPermission()).executes(command);
				} else if (innerArg instanceof CustomProvidedArgument) {
					inner = getRequiredArgumentBuilderWithProvider(keys.get(keys.size() - 1), innerArg.getRawType(),
							innerArg.getArgumentPermission(),
							nms.getSuggestionProvider(((CustomProvidedArgument) innerArg).getSuggestionProvider()))
									.executes(command);
				} else {
					inner = getRequiredArgumentBuilderDynamic(keys.get(keys.size() - 1), innerArg,
							innerArg.getArgumentPermission()).executes(command);
				}
			}

			// Link everything else up, except the first
			ArgumentBuilder outer = inner;
			for (int i = keys.size() - 2; i >= 0; i--) {
				Argument outerArg = args.get(keys.get(i));
				if (outerArg instanceof LiteralArgument) {
					String str = ((LiteralArgument) outerArg).getLiteral();
					outer = getLiteralArgumentBuilderArgument(str, outerArg.getArgumentPermission()).then(outer);
				} else if (outerArg instanceof CustomProvidedArgument) {
					outer = getRequiredArgumentBuilderWithProvider(keys.get(i), outerArg.getRawType(),
							outerArg.getArgumentPermission(),
							nms.getSuggestionProvider(((CustomProvidedArgument) outerArg).getSuggestionProvider()))
									.then(outer);
				} else {
					outer = getRequiredArgumentBuilderDynamic(keys.get(i), outerArg, outerArg.getArgumentPermission())
							.then(outer);
				}
			}

			// Link command name to first argument and register
			resultantNode = this.dispatcher.register((LiteralArgumentBuilder) getLiteralArgumentBuilder(commandName)
					.requires(generatePermissions(commandName, permissions)).then(outer));

			// Register aliases
			for (String alias : aliases) {
				if (CommandAPIMain.getConfiguration().hasVerboseOutput()) {
					CommandAPIMain.getLog().info("Registering alias /" + alias + " -> " + resultantNode.getName());
				}
				this.dispatcher.register((LiteralArgumentBuilder) getLiteralArgumentBuilder(alias)
						.requires(generatePermissions(alias, permissions)).then(outer));
			}
		}

		// Produce the commandDispatch.json file for debug purposes
		if (CommandAPIMain.getConfiguration().willCreateDispatcherFile()) {
			File file = CommandAPIMain.getDispatcherFile();
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace(System.out);
			}

			nms.createDispatcherFile(nmsServer, file, dispatcher);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// SECTION: SuggestionProviders //
	//////////////////////////////////////////////////////////////////////////////////////////////////////

	// NMS ICompletionProvider.a()
	private CompletableFuture<Suggestions> getSuggestionsBuilder(SuggestionsBuilder builder, String[] array) {
		String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
		for (int i = 0; i < array.length; i++) {
			String str = array[i];
			if (str.toLowerCase(Locale.ROOT).startsWith(remaining)) {
				builder.suggest(str);
			}
		}
		return builder.buildFuture();
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// SECTION: Argument Builders //
	//////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a literal for a given name.
	 * @param commandName the name of the literal to create
	 * @return a brigadier LiteralArgumentBuilder representing a literal
	 */
	private LiteralArgumentBuilder<?> getLiteralArgumentBuilder(String commandName) {
		return LiteralArgumentBuilder.literal(commandName);
	}

	/**
	 * Creates a literal for a given name that requires a specified permission.
	 * @param commandName the name fo the literal to create
	 * @param permission the permission required to use this literal
	 * @return a brigadier LiteralArgumentBuilder representing a literal
	 */
	private LiteralArgumentBuilder<?> getLiteralArgumentBuilderArgument(String commandName,
			CommandPermission permission) {
		return LiteralArgumentBuilder.literal(commandName)
				.requires(clw -> permissionCheck(nms.getCommandSenderForCLW(clw), permission));
	}

	// Gets a RequiredArgumentBuilder for a DynamicSuggestedStringArgument
	private <T> RequiredArgumentBuilder<?, T> getRequiredArgumentBuilderDynamic(String argumentName, Argument type,
			CommandPermission permission) {
		if(type.getOverriddenSuggestions() == null) {
			return RequiredArgumentBuilder.argument(argumentName, (ArgumentType<T>) type.getRawType()).requires(clw -> 
	             permissionCheck(nms.getCommandSenderForCLW(clw), permission)
	        );
		} else {
			return getRequiredArgumentBuilderWithProvider(argumentName, type.getRawType(), permission,
					(CommandContext context, SuggestionsBuilder builder) -> getSuggestionsBuilder(builder,
							type.getOverriddenSuggestions().apply(nms.getCommandSenderForCLW(context.getSource()))));
		}
	}

	// Gets a RequiredArgumentBuilder for an argument, given a SuggestionProvider
	private <T> RequiredArgumentBuilder<?, T> getRequiredArgumentBuilderWithProvider(String argumentName,
			ArgumentType<T> type, CommandPermission permission, SuggestionProvider provider) {
		return RequiredArgumentBuilder.argument(argumentName, type)
				.requires(clw -> permissionCheck(nms.getCommandSenderForCLW(clw), permission)).suggests(provider);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// SECTION: Reflection //
	//////////////////////////////////////////////////////////////////////////////////////////////////////

	// Gets a field using reflection and caches it
	public static Field getField(Class<?> clazz, String name) {
		ClassCache key = new ClassCache(clazz, name);
		if (fields.containsKey(key)) {
			return fields.get(key);
		} else {
			Field result = null;
			try {
				result = clazz.getDeclaredField(name);
			} catch (NoSuchFieldException | SecurityException e) {
				e.printStackTrace();
			}
			result.setAccessible(true);
			fields.put(key, result);
			return result;
		}
	}

	// Gets a field using reflection and caches it
	public static Method getMethod(Class<?> clazz, String name) {
		ClassCache key = new ClassCache(clazz, name);
		if (methods.containsKey(key)) {
			return methods.get(key);
		} else {
			Method result = null;
			try {
				result = clazz.getDeclaredMethod(name);
			} catch (SecurityException | NoSuchMethodException e) {
				e.printStackTrace();
			}
			result.setAccessible(true);
			methods.put(key, result);
			return result;
		}
	}

	/**
	 * Class to store cached methods and fields
	 * <p>
	 * This is required because each key is made up of a class and a field or method
	 * name
	 */
	@SuppressWarnings("unused")
	private static class ClassCache {

		private final Class<?> clazz;
		private final String name;

		public ClassCache(Class<?> clazz, String name) {
			this.clazz = clazz;
			this.name = name;
		}
	}
}
