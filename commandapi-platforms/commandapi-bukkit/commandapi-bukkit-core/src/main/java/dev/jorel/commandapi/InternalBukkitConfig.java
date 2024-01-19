package dev.jorel.commandapi;

import io.papermc.paper.event.server.ServerResourcesReloadedEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Configuration wrapper class for Bukkit. The config.yml file used by the CommandAPI is
 * only ever read from, nothing is ever written to it. That's why there's only
 * getter methods.
 */
public abstract class InternalBukkitConfig extends InternalConfig {
	// The plugin that is loading the CommandAPI
	private final JavaPlugin plugin;

	/**
	 * Creates an {@link InternalBukkitConfig} from a {@link CommandAPIBukkitConfig}
	 *
	 * @param config The configuration to use to set up this internal configuration
	 */
	public InternalBukkitConfig(CommandAPIBukkitConfig config) {
		super(config);
		this.plugin = config.plugin;
	}

	/**
	 * @return The {@link JavaPlugin} that is loading the CommandAPI
	 */
	public JavaPlugin getPlugin() {
		return plugin;
	}

}
