package dev.jorel.commandapi;

import org.bukkit.plugin.java.JavaPlugin;

public class MockInternalBukkitConfig extends InternalBukkitConfig {
	private final JavaPlugin plugin;

	public MockInternalBukkitConfig(MockCommandAPIBukkitConfig config) {
		super(config);
		this.plugin = config.plugin;
	}

	public JavaPlugin getPlugin() {
		return plugin;
	}
}
