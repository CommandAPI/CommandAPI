package dev.jorel.commandapi;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;

/**
 * 'Simple' conversion of Plugin commands
 */
public class Converter {

	/**
	 * Convert all commands stated in Plugin's plugin.yml file into CommandAPI-compatible commands
	 * @param plugin The plugin which commands are to be converted
	 */
	public static void convert(Plugin plugin) {
		CommandAPI.getLog().info("Converting commands for " + plugin.getName());
		plugin.getDescription().getCommands().keySet().forEach(commandName -> convertPluginCommand((JavaPlugin) plugin, commandName));
	}
	
	/**
	 * Convert a command stated in Plugin's plugin.yml file into CommandAPI-compatible commands
	 * @param p The plugin where the command is registered
	 * @param cmdName The command to convert
	 */
	public static void convert(Plugin p, String cmdName) {
		JavaPlugin plugin = (JavaPlugin) p;
		convertPluginCommand(plugin, cmdName);
	}
	
	private static void convertPluginCommand(JavaPlugin plugin, String commandName) {
		CommandAPI.getLog().info("Converting " + plugin.getName() + " command /" + commandName);
		
		/* Parse the commands */
		Map<String, Object> cmdData = plugin.getDescription().getCommands().get(commandName);
		
		if(cmdData == null) {
			CommandAPI.getLog().severe("Couldn't find /" + commandName + " in " + plugin.getName() + "'s plugin.yml. Are you sure you're not confusing it with an alias?");
			return;
		}

		//Convert stupid YAML aliases to a String[] for CommandAPI
		String[] aliases = null;
		Object aliasObj = cmdData.get("aliases");
		if(aliasObj == null) {
			aliases = new String[0];
		} else if(aliasObj instanceof String) {
			aliases = new String[] {(String) aliasObj};
		} else if(aliasObj instanceof List) {
			@SuppressWarnings("unchecked")
			List<String> list = (List<String>) aliasObj;
			aliases = list.toArray(new String[0]);
		}
		if(aliases.length != 0) {
			CommandAPI.getLog().info("Aliases for command /" + commandName + " found. Using aliases " + Arrays.deepToString(aliases));
		}
		 
		//Convert YAML to CommandPermission
		CommandPermission permissionNode = null;
		String permission = (String) cmdData.get("permission");
		if(permission == null) {
			permissionNode = CommandPermission.NONE;
		} else {
			CommandAPI.getLog().info("Permission for command /" + commandName + " found. Using " + permission);
			permissionNode = CommandPermission.fromString(permission);
		}
		
		//No arguments
		new CommandAPICommand(commandName)
			.withPermission(permissionNode)
			.withAliases(aliases)
			.executes((sender, args) -> { plugin.getCommand(commandName).execute(sender, commandName, new String[0]); })
			.register();
		
		//Multiple arguments
		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
		arguments.put("args", new GreedyStringArgument());
		
		new CommandAPICommand(commandName)
			.withPermission(permissionNode)
			.withAliases(aliases)
			.withArguments(arguments)
			.executes((sender, args) -> { plugin.getCommand(commandName).execute(sender, commandName, ((String) args[0]).split(" ")); })
			.register();
	}
	
}
