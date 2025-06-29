package dev.jorel.commandapi;

import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

@SuppressWarnings("UnstableApiUsage")
public class InternalPaperConfig extends InternalBukkitConfig {

	private final PluginMeta pluginMeta;
	private final boolean isCommandAPIPlugin;

	public InternalPaperConfig(CommandAPIPaperConfig<? extends LifecycleEventOwner> config) {
		super(config);
		this.pluginMeta = config.pluginMeta;
		this.isCommandAPIPlugin = config.isCommandAPIPlugin;
	}

	boolean isCommandAPIPlugin() {
		return isCommandAPIPlugin;
	}

	/**
	 * @return The {@link PluginMeta} of the plugin loading the CommandAPI
	 */
	public PluginMeta getPluginMeta() {
		return pluginMeta;
	}

}
