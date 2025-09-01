package dev.jorel.commandapi;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Loads the CommandAPI in this abstract Bukkit environment.
 */
public class MockCommandAPIPlugin extends JavaPlugin {
	@Override
	public void onLoad() {
		CommandAPI.onLoad(new MockCommandAPIBukkitConfig(this));
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
