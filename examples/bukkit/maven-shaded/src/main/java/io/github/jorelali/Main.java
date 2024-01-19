package io.github.jorelali;

import java.io.File;

import de.tr7zw.changeme.nbtapi.NBTContainer;
import dev.jorel.commandapi.CommandAPIPaperConfig;
import dev.jorel.commandapi.CommandAPILogger;
import org.bukkit.plugin.java.JavaPlugin;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIConfig;

public class Main extends JavaPlugin {
	@Override
	public void onLoad() {
		// Set CommandAPI to use this plugin's logger
		CommandAPI.setLogger(CommandAPILogger.fromJavaLogger(getLogger()));

		// Load the CommandAPI
		CommandAPI.onLoad(
			// Configure the CommandAPI
			new CommandAPIPaperConfig(this)
				// Turn on verbose output for command registration logs
				.verboseOutput(true)
				// Give file where Brigadier's command registration tree should be dumped
				.dispatcherFile(new File(getDataFolder(), "command_registration.json"))
				// Point to the NBT API we want to use
				.initializeNBTAPI(NBTContainer.class, NBTContainer::new)
		);
	}

	@Override
	public void onEnable() {
		// Enable the CommandAPI
		CommandAPI.onEnable();

		// Register commands using our MyCommands class
		MyCommands myCommands = new MyCommands(this);
		myCommands.registerAllCommands();
		myCommands.registerAllCommandTrees();
	}
}
