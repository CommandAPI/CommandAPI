package dev.jorel.commandapi;

import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;

@SuppressWarnings("UnstableApiUsage")
public class CommandAPIPaperConfig<T extends LifecycleEventOwner> extends CommandAPIBukkitConfig<CommandAPIPaperConfig<T>> {

	PluginMeta pluginMeta;
	LifecycleEventOwner lifecycleEventOwner;
	boolean shouldHookPaperReload = false;

	/**
	 * Creates a new {@code CommandAPIPaperConfig} object
	 *
	 * @param pluginMeta the {@link io.papermc.paper.plugin.configuration.PluginMeta} of the plugin loading the CommandAPI
	 * @param lifecycleEventOwner a {@link io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner}.
	 *                           Can be a {@link org.bukkit.plugin.java.JavaPlugin} or a {@link io.papermc.paper.plugin.bootstrap.BootstrapContext}
	 */
	public CommandAPIPaperConfig(PluginMeta pluginMeta, T lifecycleEventOwner) {
		super(pluginMeta.getName());
		this.pluginMeta = pluginMeta;
		this.lifecycleEventOwner = lifecycleEventOwner;
	}

	/**
	 * Sets whether the CommandAPI should skip its datapack reload step after the server
	 * has finished loading. This does not skip reloading of datapacks when invoked manually
	 * when {@link #shouldHookPaperReload(boolean)} is set.
	 * @param skip whether the CommandAPI should skip reloading datapacks when the server has finished loading
	 * @return this CommandAPIPaperConfig
	 */
	public CommandAPIPaperConfig<T> skipReloadDatapacks(boolean skip) {
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
	public CommandAPIPaperConfig<T> shouldHookPaperReload(boolean hooked) {
		this.shouldHookPaperReload = hooked;
		return this;
	}

	@Override
	public CommandAPIPaperConfig<T> instance() {
		return this;
	}
}
