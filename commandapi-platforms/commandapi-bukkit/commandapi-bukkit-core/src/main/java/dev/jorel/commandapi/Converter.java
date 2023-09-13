/*******************************************************************************
 * Copyright 2018, 2021 Jorel Ali (Skepter) - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package dev.jorel.commandapi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.executors.NativeCommandExecutor;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;

/**
 * 'Simple' conversion of Plugin commands
 */
public final class Converter {

	// Cannot be instantiated
	private Converter() {
	}

	private static final List<Argument<?>> PLAIN_ARGUMENTS = List.of(new GreedyStringArgument("args"));
	private static final Set<String> CALLER_METHODS = Set.of("isPermissionSet", "hasPermission",
			"addAttachment", "removeAttachment", "recalculatePermissions", "getEffectivePermissions", "isOp", "setOp");

	/**
	 * Convert all commands stated in Plugin's plugin.yml file into
	 * CommandAPI-compatible commands
	 * 
	 * @param plugin The plugin which commands are to be converted
	 */
	public static void convert(JavaPlugin plugin) {
		CommandAPI.logInfo("Converting commands for " + plugin.getName() + ":");
		for (String commandName : plugin.getDescription().getCommands().keySet()) {
			convertPluginCommand(plugin, commandName, PLAIN_ARGUMENTS);
		}
	}

	/**
	 * Convert a command stated in Plugin's plugin.yml file into
	 * CommandAPI-compatible commands
	 * 
	 * @param plugin  The plugin where the command is registered
	 * @param cmdName The command to convert
	 */
	public static void convert(JavaPlugin plugin, String cmdName) {
		convertPluginCommand(plugin, cmdName, PLAIN_ARGUMENTS);
	}

	/**
	 * Convert a command stated in Plugin's plugin.yml file into
	 * CommandAPI-compatible commands
	 * 
	 * @param plugin    The plugin where the command is registered
	 * @param cmdName   The command to convert
	 * @param arguments The arguments that should be used to parse this command
	 */
	public static void convert(JavaPlugin plugin, String cmdName, Argument<?>... arguments) {
		convertPluginCommand(plugin, cmdName, Arrays.asList(arguments));
	}

	/**
	 * Convert a command stated in Plugin's plugin.yml file into
	 * CommandAPI-compatible commands
	 * 
	 * @param plugin    The plugin where the command is registered
	 * @param cmdName   The command to convert
	 * @param arguments The arguments that should be used to parse this command
	 */
	public static void convert(JavaPlugin plugin, String cmdName, List<Argument<?>> arguments) {
		convertPluginCommand(plugin, cmdName, arguments);
	}

	/**
	 * Convert the provided command name into a CommandAPI-compatible command
	 * 
	 * @param cmdName The name of the command (without the leading /). For commands
	 *                such as //set in WorldEdit, this parameter should be "/set"
	 */
	public static void convert(String cmdName) {
		convertCommand(cmdName, PLAIN_ARGUMENTS);
	}

	/**
	 * Convert the provided command name with its list of arguments into a
	 * CommandAPI-compatible command
	 * 
	 * @param cmdName   The name of the command (without the leading /). For
	 *                  commands such as //set in WorldEdit, this parameter should
	 *                  be "/set"
	 * @param arguments The arguments that should be used to parse this command
	 */
	public static void convert(String cmdName, List<Argument<?>> arguments) {
		convertCommand(cmdName, arguments);
	}

	private static void convertCommand(String commandName, List<Argument<?>> arguments) {
		CommandAPI.logInfo("Converting command /" + commandName);

		// No arguments
		new CommandAPICommand(commandName).withPermission(CommandPermission.NONE).executesNative((sender, args) -> {
			Bukkit.dispatchCommand(mergeProxySender(sender), commandName);
			return;
		}).register();

		// Multiple arguments
		CommandAPICommand multiArgs = new CommandAPICommand(commandName).withPermission(CommandPermission.NONE)
				.withArguments(arguments).executesNative((sender, args) -> {
					// We know the args are a String[] because that's how converted things are
					// handled in generateCommand()
					CommandSender proxiedSender = mergeProxySender(sender);
					Bukkit.dispatchCommand(proxiedSender, commandName + " " + String.join(" ", (String[]) args.args()));
				});

		multiArgs.setConverted(true);
		multiArgs.register();
	}
	
	private static String[] unpackAliases(Object aliasObj) {
		if (aliasObj == null) {
			return new String[0];
		} else if (aliasObj instanceof String aliasStr) {
			return new String[] { aliasStr };
		} else if (aliasObj instanceof List<?> aliasList) {
			return aliasList.toArray(new String[0]);
		} else {
			throw new IllegalStateException("Invalid type for aliases. Expected String or List, but got " + aliasObj.getClass().getSimpleName());
		}
	}

	private static void convertPluginCommand(JavaPlugin plugin, String commandName, List<Argument<?>> arguments) {
		CommandAPI.logInfo("Converting " + plugin.getName() + " command /" + commandName);
		/* Parse the commands */
		Map<String, Object> cmdData = plugin.getDescription().getCommands().get(commandName);

		if (cmdData == null) {
			CommandAPI.logError("Couldn't find /" + commandName + " in " + plugin.getName()
					+ "'s plugin.yml. Are you sure you're not confusing it with an alias?");
			return;
		}

		// Convert stupid YAML aliases to a String[] for CommandAPI
		String[] aliases = unpackAliases(cmdData.get("aliases"));
		if (aliases.length != 0) {
			CommandAPI.logInfo("Aliases for command /" + commandName + " found. Using aliases " + Arrays.deepToString(aliases));
		}

		// Convert YAML to description
		String fullDescription = null;
		Object descriptionObj = cmdData.get("description");
		if (descriptionObj != null && descriptionObj instanceof String descriptionStr) {
			fullDescription = descriptionStr;
		}

		// Convert YAML to CommandPermission
		CommandPermission permissionNode = null;
		String permission = (String) cmdData.get("permission");
		if (permission == null) {
			permissionNode = CommandPermission.NONE;
		} else {
			CommandAPI.logInfo("Permission for command /" + commandName + " found. Using " + permission);
			permissionNode = CommandPermission.fromString(permission);
		}
		
		NativeCommandExecutor executor = (sender, args) -> {
			org.bukkit.command.Command command = plugin.getCommand(commandName);
			
			if (command == null) {
				command = CommandAPIBukkit.get().getSimpleCommandMap()
						.getCommand(commandName);
			}

			CommandSender proxiedSender = CommandAPI.getConfiguration().shouldSkipSenderProxy(plugin.getName())
					? sender.getCallee()
					: mergeProxySender(sender);

			if (args.args() instanceof String[] argsArr) {
				command.execute(proxiedSender, commandName, argsArr);
			} else {
				command.execute(proxiedSender, commandName, new String[0]);
			}
		};

		// No arguments
		new CommandAPICommand(commandName)
			.withPermission(permissionNode)
			.withAliases(aliases)
			.withFullDescription(fullDescription)
			.executesNative(executor)
			.register();

		// Multiple arguments
		new CommandAPICommand(commandName)
			.withPermission(permissionNode)
			.withAliases(aliases)
			.withArguments(arguments)
			.withFullDescription(fullDescription)
			.executesNative(executor)
			.setConverted(true)
			.register();
	}

	/*
	 * https://www.jorel.dev/blog/posts/Simplifying-Bukkit-CommandSenders/
	 */
	private static CommandSender mergeProxySender(NativeProxyCommandSender proxySender) {
		// Add all interfaces
		Set<Class<?>> calleeInterfacesList = new HashSet<>();
		Class<?> currentClass = proxySender.getCallee().getClass();
		if (currentClass.isInterface()) {
			calleeInterfacesList.add(currentClass);
		}
		while (currentClass != null) {
			calleeInterfacesList.addAll(Arrays.asList(currentClass.getInterfaces()));
			currentClass = currentClass.getSuperclass();
		}
		Class<?>[] calleeInterfaces = calleeInterfacesList.toArray(new Class<?>[0]);

		InvocationHandler handler = (Object p, Method method, Object[] args) -> {
			switch(method.getName()) {
				case "getLocation":
					return proxySender.getLocation();
				case "getBlock":
					return proxySender.getLocation().getBlock();
				case "getEyeLocation":
					if(proxySender.getCallee() instanceof LivingEntity livingEntity) {
						Location loc = proxySender.getLocation();
						loc.setY(loc.getY() + livingEntity.getEyeHeight());
						return loc;
					} else {
						// This case should never happen. If it does, please let me know!
						return proxySender.getLocation();
					}
				case "getWorld":
					return proxySender.getWorld();
				default:
					return method.invoke(
							CALLER_METHODS.contains(method.getName()) ? proxySender.getCaller() : proxySender.getCallee(),
							args);
			}
		};

		return (CommandSender) Proxy.newProxyInstance(CommandSender.class.getClassLoader(), calleeInterfaces, handler);
	}

}
