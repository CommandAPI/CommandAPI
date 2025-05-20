package dev.jorel.commandapi;

import io.papermc.paper.plugin.configuration.PluginMeta;

@SuppressWarnings("UnstableApiUsage")
public class CommandAPIPaperConfig extends CommandAPIBukkitConfig<CommandAPIPaperConfig> {

	PluginMeta pluginMeta;
	boolean shouldHookPaperReload = false;

	public CommandAPIPaperConfig(PluginMeta pluginMeta) {
		super(pluginMeta.getName());
		this.pluginMeta = pluginMeta;
	}

	/**
	 * Sets whether the CommandAPI should skip its datapack reload step after the server
	 * has finished loading. This does not skip reloading of datapacks when invoked manually
	 * when {@link #shouldHookPaperReload(boolean)} is set.
	 * @param skip whether the CommandAPI should skip reloading datapacks when the server has finished loading
	 * @return this CommandAPIPaperConfig
	 */
	public CommandAPIPaperConfig skipReloadDatapacks(boolean skip) {
		this.skipReloadDatapacks = skip;
		return this;
	}

	/**
	 * Sets the CommandAPI to hook into Paper's {@link io.papermc.paper.event.server.ServerResourcesReloadedEvent} when available
	 * if true. This helps CommandAPI commands to work in datapacks after {@code /minecraft:reload}
	 * is run.
	 *
	 * @param hooked whether the CommandAPI should hook into Paper's {@link io.papermc.paper.event.server.ServerResourcesReloadedEvent}
	 * @return this CommandAPIPaperConfig
	 */
	public CommandAPIPaperConfig shouldHookPaperReload(boolean hooked) {
		this.shouldHookPaperReload = hooked;
		return this;
	}

	@Override
	public CommandAPIPaperConfig instance() {
		return this;
	}
}
