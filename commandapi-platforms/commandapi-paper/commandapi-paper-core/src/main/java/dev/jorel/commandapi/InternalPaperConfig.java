package dev.jorel.commandapi;

import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;

@SuppressWarnings("UnstableApiUsage")
public class InternalPaperConfig extends InternalBukkitConfig {

	private final PluginMeta pluginMeta;
	private final LifecycleEventOwner lifecycleEventOwner;

	public InternalPaperConfig(CommandAPIPaperConfig config) {
		super(config);
		this.pluginMeta = config.pluginMeta;
		this.lifecycleEventOwner = config.lifecycleEventOwner;
	}

	/**
	 * @return the {@link LifecycleEventOwner} loading the CommandAPI
	 */
	public LifecycleEventOwner getLifecycleEventOwner() {
		return lifecycleEventOwner;
	}

	/**
	 * @return The {@link PluginMeta} of the plugin loading the CommandAPI
	 */
	public PluginMeta getPluginMeta() {
		return pluginMeta;
	}

}
