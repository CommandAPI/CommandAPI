package dev.jorel.commandapi.test;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIPaperConfig;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.function.Function;

public class Main extends JavaPlugin {

	public static Class nbtContainerClass = null;
	public static Function nbtContainerConstructor = null;

	@Override
	public void onLoad() {
		CommandAPIPaperConfig config = new CommandAPIPaperConfig(this)
			.silentLogs(true)
			.dispatcherFile(new File(getDataFolder(), "command_registration.json"));

		if (nbtContainerClass != null && nbtContainerConstructor != null) {
			config = config.initializeNBTAPI(nbtContainerClass, nbtContainerConstructor);
		}

		CommandAPI.onLoad(config);
	}

	@Override
	public void onEnable() {
		CommandAPI.onEnable();
	}

	@Override
	public void onDisable() {
		CommandAPI.onDisable();
	}
}
