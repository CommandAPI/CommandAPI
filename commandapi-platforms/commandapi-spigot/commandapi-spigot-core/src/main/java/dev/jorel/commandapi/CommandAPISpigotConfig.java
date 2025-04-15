package dev.jorel.commandapi;

import org.bukkit.plugin.java.JavaPlugin;

public class CommandAPISpigotConfig extends CommandAPIBukkitConfig<CommandAPISpigotConfig> {

	public CommandAPISpigotConfig(JavaPlugin plugin) {
		super(plugin);
	}

	/**
	 * Sets whether the CommandAPI should skip its datapack reload step after the server
	 * has finished loading.
	 * @param skip whether the CommandAPI should skip reloading datapacks when the server has finished loading
	 * @return this CommandAPIBukkitConfig
	 */
	public CommandAPISpigotConfig skipReloadDatapacks(boolean skip) {
		this.skipReloadDatapacks = skip;
		return this;
	}

	@Override
	public CommandAPISpigotConfig instance() {
		return this;
	}
}
