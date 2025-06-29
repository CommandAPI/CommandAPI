package dev.jorel.commandapi;

import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;

@SuppressWarnings("UnstableApiUsage")
public class CommandAPIPaperConfig<T extends LifecycleEventOwner> extends CommandAPIBukkitConfig<CommandAPIPaperConfig<T>> {

	PluginMeta pluginMeta;
	LifecycleEventOwner lifecycleEventOwner;
	boolean isCommandAPIPlugin = false;

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

	CommandAPIPaperConfig<T> isCommandAPIPlugin(boolean isCommandAPIPlugin) {
		this.isCommandAPIPlugin = isCommandAPIPlugin;
		return this;
	}

	@Override
	public CommandAPIPaperConfig<T> instance() {
		return this;
	}
}
