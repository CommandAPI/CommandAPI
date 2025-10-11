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

	protected CommandAPIPaperConfig(LifecycleEventOwner lifecycleEventOwner) {
		super(lifecycleEventOwner.getPluginMeta().getName());
		this.pluginMeta = lifecycleEventOwner.getPluginMeta();
		this.lifecycleEventOwner = lifecycleEventOwner;
		fallbackToLatestNMS(true);
	}

	/**
	 * Creates a new {@code CommandAPIPaperConfig} object
	 *
	 * @param plugin The {@link JavaPlugin} loading the CommandAPI during `onLoad`
	 */
	public static CommandAPIPaperConfig forPlugin(JavaPlugin plugin) {
		return new CommandAPIPaperConfig(plugin);
	}

	/**
	 * Creates a new {@code CommandAPIPaperConfig} object
	 *
	 * @param bootstrapContext The {@link BootstrapContext} loading the CommandAPI during `bootstrap`
	 */
	public static CommandAPIPaperConfig forBootstrap(BootstrapContext bootstrapContext) {
		return new CommandAPIPaperConfig(bootstrapContext);
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
