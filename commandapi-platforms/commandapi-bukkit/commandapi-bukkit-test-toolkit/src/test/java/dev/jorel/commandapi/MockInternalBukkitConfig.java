package dev.jorel.commandapi;

import org.bukkit.plugin.java.JavaPlugin;

public class MockInternalBukkitConfig extends InternalBukkitConfig {

	private final JavaPlugin plugin;

	/**
	 * Creates an {@link InternalBukkitConfig} from a {@link CommandAPIBukkitConfig}
	 *
	 * @param config The configuration to use to set up this internal configuration
	 */
	public MockInternalBukkitConfig(MockCommandAPIBukkitConfig config) {
		super(config);
		this.plugin = config.plugin;
	}

	/**
	 * @return the {@link JavaPlugin} loading the CommandAPI
	 */
	public JavaPlugin getPlugin() {
		return plugin;
	}
}
