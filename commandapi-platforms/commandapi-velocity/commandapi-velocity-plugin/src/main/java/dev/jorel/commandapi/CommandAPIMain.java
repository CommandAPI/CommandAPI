package dev.jorel.commandapi;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.jorel.commandapi.config.DefaultVelocityConfig;
import dev.jorel.commandapi.config.VelocityConfigurationAdapter;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Main CommandAPI plugin entrypoint
 */
@Plugin(
	id = "commandapi",
	name = "CommandAPI",
	version = "${project.version}", // Hopefully Maven is smart enough to substitute this TODO: Maven is not that smart
	url = "https://commandapi.jorel.dev",
	description = "An API to use Minecraft 1.13s new command UI",
	authors = {"Skepter"}
)
public class CommandAPIMain {
	@Inject
	public CommandAPIMain(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
		// Try to find the config file
		Path configFile = dataDirectory.resolve("config.yml");

		YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
			.nodeStyle(NodeStyle.BLOCK)
			.path(configFile)
			.build();

		// Create or update config
		VelocityConfigurationAdapter.createMinimalInstance(loader).saveDefaultConfig(
			DefaultVelocityConfig.createDefault(),
			configFile.getParent().toFile(),
			logger
		);

		// Load the file as a yaml node
		ConfigurationNode configYAML;
		try {
			configYAML = loader.load();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// Configure the CommandAPI
		CommandAPIVelocityConfig config = new CommandAPIVelocityConfig(server, this)
			.verboseOutput(configYAML.node("verbose-outputs").getBoolean())
			.silentLogs(configYAML.node("silent-logs").getBoolean())
			.missingExecutorImplementationMessage(configYAML.node("messages", "missing-executor-implementation").getString())
			.dispatcherFile(configYAML.node("create-dispatcher-json").getBoolean() ? new File(dataDirectory.toFile(), "command_registration.json") : null)
			.errorOnFailedPacketSends(configYAML.node("error-on-failed-packet-sends").getBoolean());

		// Load
		CommandAPI.setLogger(CommandAPILogger.fromJavaLogger(logger));
		CommandAPI.onLoad(config);
	}

	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent event) {
		// Enable
		CommandAPI.onEnable();
	}

	@Subscribe
	public void onProxyShutdown(ProxyShutdownEvent event) {
		// Shut down
		CommandAPI.onDisable();
	}

	// On /velocity reload
	@Subscribe
	public void onProxyReload(ProxyReloadEvent event) {
		// Handle quirky stuff
	}
}
