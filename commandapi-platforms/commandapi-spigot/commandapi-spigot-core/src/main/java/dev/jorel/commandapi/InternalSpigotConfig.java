package dev.jorel.commandapi;

public class InternalSpigotConfig extends InternalBukkitConfig {

	private final boolean skipReloadDatapacks;

	public InternalSpigotConfig(CommandAPISpigotConfig config) {
		super(config);

		this.skipReloadDatapacks = config.skipReloadDatapacks;
	}

	/**
	 * @return Whether the CommandAPI should skip reloading datapacks when the server has finished loading
	 */
	public boolean skipReloadDatapacks() {
		return skipReloadDatapacks;
	}

}
