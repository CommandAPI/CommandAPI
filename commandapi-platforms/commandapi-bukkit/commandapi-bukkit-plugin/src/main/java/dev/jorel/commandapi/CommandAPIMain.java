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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;

/**
 * Main CommandAPI plugin entrypoint
 */
public class CommandAPIMain extends JavaPlugin {

	@Override
	public void onLoad() {
		// Read config file
		saveDefaultConfig();
		FileConfiguration fileConfig = getConfig();
		CommandAPIBukkitConfig config = new CommandAPIBukkitConfig(this)
			.verboseOutput(fileConfig.getBoolean("verbose-outputs"))
			.silentLogs(fileConfig.getBoolean("silent-logs"))
			.useLatestNMSVersion(fileConfig.getBoolean("use-latest-nms-version"))
			.missingExecutorImplementationMessage(fileConfig.getString("messages.missing-executor-implementation"))
			.dispatcherFile(fileConfig.getBoolean("create-dispatcher-json") ? new File(getDataFolder(), "command_registration.json") : null)
			.initializeNBTAPI(NBTContainer.class, NBTContainer::new)
			.shouldHookPaperReload(fileConfig.getBoolean("hook-paper-reload"));

		for (String pluginName : fileConfig.getStringList("skip-sender-proxy")) {
			if (Bukkit.getPluginManager().getPlugin(pluginName) != null) {
				config.addSkipSenderProxy(pluginName);
			} else {
				new InvalidPluginException("Could not find a plugin " + pluginName + "! Has it been loaded properly?")
					.printStackTrace();
			}
		}

		// Main CommandAPI loading
		CommandAPI.setLogger(CommandAPILogger.fromJavaLogger(getLogger()));
		CommandAPI.onLoad(config);

		// Configure the NBT API - we're not allowing tracking at all, according
		// to the CommandAPI's design principles. The CommandAPI isn't used very
		// much, so this tiny proportion of servers makes very little impact to
		// the NBT API's stats.
		MinecraftVersion.disableBStats();
		MinecraftVersion.disableUpdateCheck();

		// Convert all plugins to be converted
		if (!fileConfig.getList("plugins-to-convert").isEmpty()
			&& fileConfig.getMapList("plugins-to-convert").isEmpty()) {
			CommandAPI.logError("plugins-to-convert has an invalid type. Did you miss a colon (:) after a plugin name?");
		}

		// Load all plugins at the same time
		Map<JavaPlugin, String[]> pluginsToConvert = new HashMap<>();
		for (Map<?, ?> map : fileConfig.getMapList("plugins-to-convert")) {
			String[] pluginCommands;
			if (map.values().size() == 1 && map.values().iterator().next() == null) {
				pluginCommands = new String[0];
			} else {
				@SuppressWarnings("unchecked")
				List<String> commands = (List<String>) map.values().iterator().next();
				pluginCommands = commands.toArray(new String[0]);
			}

			String pluginName = (String) map.keySet().iterator().next();
			Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
			if (plugin != null) {
				if (plugin instanceof JavaPlugin javaPlugin) {
					pluginsToConvert.put(javaPlugin, pluginCommands);
				} else {
					new InvalidPluginException("Plugin " + pluginName + " is not a JavaPlugin!").printStackTrace();
				}
			} else {
				new InvalidPluginException("Could not find a plugin " + pluginName + "! Has it been loaded properly?")
					.printStackTrace();
			}
		}
		// Convert plugin commands
		for (Entry<JavaPlugin, String[]> pluginToConvert : pluginsToConvert.entrySet()) {
			if (pluginToConvert.getValue().length == 0) {
				Converter.convert(pluginToConvert.getKey());
			} else {
				for (String command : pluginToConvert.getValue()) {
					new AdvancedConverter(pluginToConvert.getKey(), command).convert();
				}
			}
		}

		// Convert all arbitrary commands
		for (String commandName : fileConfig.getStringList("other-commands-to-convert")) {
			new AdvancedConverter(commandName).convertCommand();
		}
	}

	@Override
	public void onEnable() {
		CommandAPI.onEnable();

		// TODO: Example command, remove before merging
		new CommandAPICommand("rate")
			.withArguments(
				new StringArgument("topic").setOptional(true),
				new IntegerArgument("rating", 0, 10),
				new PlayerArgument("target").setOptional(true)
			)
			.executes(info -> {
				String topic = (String) info.args().get("topic");
				if(topic == null) {
					// Honestly not sure how to justify topic being optional here,
					// causing there to be a required argument after an optional one,
					// but I'm sure an example exists, right?
					info.sender().sendMessage("You didn't give a rating");
					return;
				}

				// We know this is not null because rating is required if topic is given
				int rating = (int) info.args().get("rating");

				// The target player is optional, so give it a default here
				CommandSender target = (CommandSender) info.args().getOrDefault("target", info.sender());

				target.sendMessage("Your " + topic + " was rated: " + rating + "/10");
			})
			.register();
	}
}
