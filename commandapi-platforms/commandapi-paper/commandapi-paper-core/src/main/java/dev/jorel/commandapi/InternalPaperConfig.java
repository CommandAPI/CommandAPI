package dev.jorel.commandapi;

import io.papermc.paper.plugin.configuration.PluginMeta;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

@SuppressWarnings("UnstableApiUsage")
public class InternalPaperConfig extends InternalBukkitConfig {

	private final PluginMeta pluginMeta;

	// Whether to hook into paper's reload event to reload datapacks when /minecraft:reload is run
	private final boolean shouldHookPaperReload;

	public InternalPaperConfig(CommandAPIPaperConfig config) {
		super(config);
		this.pluginMeta = config.pluginMeta;
		this.shouldHookPaperReload = config.shouldHookPaperReload;
	}

	/**
	 * @return The {@link PluginMeta} of the plugin loading the CommandAPI
	 */
	public PluginMeta getPluginMeta() {
		return pluginMeta;
	}

	/**
	 * @return Whether the CommandAPI should hook into Paper's {@link io.papermc.paper.event.server.ServerResourcesReloadedEvent}
	 * when available to perform the CommandAPI's custom datapack reload when {@code /minecraft:reload}
	 * is run.
	 */
	public boolean shouldHookPaperReload() {
		return shouldHookPaperReload;
	}

}
