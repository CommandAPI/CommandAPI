package dev.jorel.commandapi;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("UnstableApiUsage")
public class CommandAPIPaperConfig extends CommandAPIBukkitConfig<CommandAPIPaperConfig> {

	PluginMeta pluginMeta;
	LifecycleEventOwner lifecycleEventOwner;
	boolean isCommandAPIPlugin = false;

	/**
	 * Creates a new {@code CommandAPIPaperConfig} object
	 *
	 * @param lifecycleEventOwner The {@link LifecycleEventOwner} loading the CommandAPI.
	 *                            Can be a {@link JavaPlugin} or a {@link BootstrapContext}.
	 */
	public CommandAPIPaperConfig(LifecycleEventOwner lifecycleEventOwner) {
		super(lifecycleEventOwner.getPluginMeta().getName());
		this.pluginMeta = lifecycleEventOwner.getPluginMeta();
		this.lifecycleEventOwner = lifecycleEventOwner;
		fallbackToLatestNMS(true);
	}

	CommandAPIPaperConfig isCommandAPIPlugin() {
		this.isCommandAPIPlugin = true;
		return this;
	}

	@Override
	public CommandAPIPaperConfig instance() {
		return this;
	}
}
