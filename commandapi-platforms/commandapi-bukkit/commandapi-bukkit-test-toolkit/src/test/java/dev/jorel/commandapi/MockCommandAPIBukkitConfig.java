package dev.jorel.commandapi;

import org.bukkit.plugin.java.JavaPlugin;

public class MockCommandAPIBukkitConfig extends CommandAPIBukkitConfig<MockCommandAPIBukkitConfig> {
	JavaPlugin plugin;

	public MockCommandAPIBukkitConfig(JavaPlugin plugin) {
		super(plugin.getName());
		this.plugin = plugin;
	}

	@Override
	public MockCommandAPIBukkitConfig instance() {
		return this;
	}
}
