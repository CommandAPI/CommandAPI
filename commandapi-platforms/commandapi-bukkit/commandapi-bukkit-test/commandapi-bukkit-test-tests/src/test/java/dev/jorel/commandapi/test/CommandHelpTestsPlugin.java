package dev.jorel.commandapi.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

public class CommandHelpTestsPlugin extends JavaPlugin {
	// Additional constructors required for MockBukkit
	public CommandHelpTestsPlugin() {
		super();
	}

	public CommandHelpTestsPlugin(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
		super(loader, description, dataFolder, file);
	}

	public static InputStream pluginYaml() {
		return new ByteArrayInputStream("""
			name: CommandHelpTestsPlugin
			main: dev.jorel.commandapi.test.CommandHelpTestsPlugin
			version: 0.0.1
			description: A mock Bukkit plugin for CommandAPI testing
			author: Will Kroboth
			website: https://www.jorel.dev/CommandAPI/
			api-version: 1.13
			commands:
			  registeredCommand:
			  registeredAlias:
			""".getBytes());
	}
}
