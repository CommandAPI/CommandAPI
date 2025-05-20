package dev.jorel.commandapi;

import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import com.google.inject.Inject;

// TODO: Add plugin meta data, see: https://docs.spongepowered.org/stable/en/plugin/plugin-meta.html
/**
 * Main CommandAPI plugin entrypoint
 */
@Plugin("CommandAPI")
public class CommandAPIMain {

	@Inject
	private Logger logger;
	private Server server;

	@Listener
	public void onServerStarting(final StartingEngineEvent<Server> event) {
		server = event.engine();
		CommandAPI.setLogger(CommandAPILogger.fromApacheLog4jLogger(logger));

		// TODO: Save default config file if it doesn't exist then load config and apply settings to CommandAPIConfig()
		//  See: https://docs.spongepowered.org/stable/en/plugin/configuration/index.html
		CommandAPI.onLoad(new CommandAPISpongeConfig(server));
	}

	@Listener
	public void onServerStart(final StartedEngineEvent<Server> event) {
		CommandAPISponge.onEnable();
	}

	@Listener
	public void onServerStopping(final StoppingEngineEvent<Server> event) {
		CommandAPI.onDisable();
	}
}
