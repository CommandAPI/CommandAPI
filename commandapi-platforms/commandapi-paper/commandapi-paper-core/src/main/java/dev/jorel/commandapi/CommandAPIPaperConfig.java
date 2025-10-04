package dev.jorel.commandapi;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("UnstableApiUsage")
public class CommandAPIPaperConfig extends CommandAPIBukkitConfig<CommandAPIPaperConfig> {

	PluginMeta pluginMeta;
	LifecycleEventOwner lifecycleEventOwner;
	boolean hookPaperReload = false;

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

	CommandAPIPaperConfig hookPaperReload(boolean hookPaperReload) {
		this.hookPaperReload = hookPaperReload;
		return this;
	}

	CommandAPIPaperConfig skipInitialDatapackReload(boolean skipInitialDatapackReload) {
		this.skipReloadDatapacks = skipInitialDatapackReload;
		return this;
	}

	@Override
	public CommandAPIPaperConfig instance() {
		return this;
	}
}
