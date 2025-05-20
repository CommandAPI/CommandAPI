package dev.jorel.commandapi;

import org.bukkit.plugin.java.JavaPlugin;

public class InternalSpigotConfig extends InternalBukkitConfig {

	private final JavaPlugin plugin;

	public InternalSpigotConfig(CommandAPISpigotConfig config) {
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
