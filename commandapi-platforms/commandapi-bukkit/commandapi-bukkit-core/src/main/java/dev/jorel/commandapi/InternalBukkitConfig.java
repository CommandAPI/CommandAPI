package dev.jorel.commandapi;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Configuration wrapper class for Bukkit. The config.yml file used by the CommandAPI is
 * only ever read from, nothing is ever written to it. That's why there's only
 * getter methods.
 */
public abstract class InternalBukkitConfig extends InternalConfig {
	// The plugin that is loading the CommandAPI
	private final JavaPlugin plugin;

	private final boolean skipReloadDatapacks;

	/**
	 * Creates an {@link InternalBukkitConfig} from a {@link CommandAPIBukkitConfig}
	 *
	 * @param config The configuration to use to set up this internal configuration
	 */
	public InternalBukkitConfig(CommandAPIBukkitConfig<? extends CommandAPIBukkitConfig<?>> config) {
		super(config);
		this.plugin = config.plugin;
		this.skipReloadDatapacks = config.skipReloadDatapacks;
	}

	/**
	 * @return The {@link JavaPlugin} that is loading the CommandAPI
	 */
	public JavaPlugin getPlugin() {
		return plugin;
	}

	/**
	 * @return Whether the CommandAPI should skip reloading datapacks when the server has finished loading
	 */
	public boolean skipReloadDatapacks() {
		return skipReloadDatapacks;
	}

}
