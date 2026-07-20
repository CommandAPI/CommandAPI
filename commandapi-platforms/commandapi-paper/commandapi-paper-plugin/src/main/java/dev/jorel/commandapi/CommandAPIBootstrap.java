package dev.jorel.commandapi;

import dev.jorel.commandapi.config.BukkitConfigurationAdapter;
import dev.jorel.commandapi.config.DefaultBukkitConfig;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.InvalidPluginException;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@SuppressWarnings("UnstableApiUsage") // We know we are using new Paper Bootstrap stuff
public class CommandAPIBootstrap implements PluginBootstrap {
	@Override
	public void bootstrap(@NotNull BootstrapContext context) {
		// Create default configuration file if necessary
		File dataFolder = context.getDataDirectory().toFile();
		File configFile = new File(dataFolder, "config.yml");
		BukkitConfigurationAdapter.createMinimalInstance(configFile).saveDefaultConfig(
			DefaultBukkitConfig.createDefaultPaperConfig(),
			dataFolder,
			context.getLogger()::error
		);

		// Read config options
		YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(configFile);
		CommandAPIPaperConfig config = new CommandAPIPaperConfig(context)
			.verboseOutput(yamlConfig.getBoolean("verbose-outputs"))
			.silentLogs(yamlConfig.getBoolean("silent-logs"))
			.fallbackToLatestNMS(yamlConfig.getBoolean("fallback-to-latest-nms"))
			.missingExecutorImplementationMessage(yamlConfig.getString("messages.missing-executor-implementation"))
			.dispatcherFile(yamlConfig.getBoolean("create-dispatcher-json") ? new File(dataFolder, "command_registration.json") : null)
			.hookPaperReload(yamlConfig.getBoolean("hook-paper-reload")) // TODO: Remove once this utilizes the bootstrapper
			.skipInitialDatapackReload(yamlConfig.getBoolean("skip-initial-datapack-reload")) // TODO: Remove once this utilizes the bootstrapper
			.enableNetworking(yamlConfig.getBoolean("enable-networking"))
			.makeNetworkingExceptionsWarnings(yamlConfig.getBoolean("make-networking-exceptions-warnings"));

		for (String pluginName : yamlConfig.getStringList("skip-sender-proxy")) {
			// TODO: Do we need to do this later during onLoad in order to properly see other plugins?
			//  This seems to only be used by `Converter`, so maybe this can be handled directly like the
			//  other command-conversion config options rather than being in the `onLoad` config.
			if (Bukkit.getPluginManager().getPlugin(pluginName) != null) {
				config.addSkipSenderProxy(pluginName);
			} else {
				new InvalidPluginException("Could not find a plugin " + pluginName + "! Has it been loaded properly?")
					.printStackTrace();
			}
		}

		// Main CommandAPI loading
		CommandAPI.setLogger(CommandAPILogger.fromSlf4jLogger(context.getLogger()));
		CommandAPI.onLoad(config);
	}
}
