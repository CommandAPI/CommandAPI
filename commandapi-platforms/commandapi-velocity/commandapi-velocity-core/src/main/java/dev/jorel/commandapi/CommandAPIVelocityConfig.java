package dev.jorel.commandapi;

import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.Optional;

/**
 * A class that contains information needed to configure the CommandAPI on Velocity-based servers.
 */
public class CommandAPIVelocityConfig extends CommandAPIConfig<CommandAPIVelocityConfig> {
	ProxyServer server;
	Object plugin;

	/**
	 * Creates a new CommandAPIVelocityConfig object. Variables in this
	 * constructor are required to load the CommandAPI on Velocity properly.
	 *
	 * @param server The {@link ProxyServer} that the CommandAPI is running on.
	 * @param plugin The plugin object (annotated by {@link Plugin}) loading the CommandAPI.
	 *               This is used when registering events.
	 */
	public CommandAPIVelocityConfig(ProxyServer server, Object plugin) {
		this.server = server;
		this.plugin = plugin;
		super.setNamespace("");
	}

	/**
	 * Sets whether the CommandAPI should use the plugin's name as the default namespace
	 * <p>
	 * If called, any call to {@link CommandAPIConfig#setNamespace(String)} will be ignored
	 *
	 * @return this CommandAPIVelocityConfig
	 */
	public CommandAPIVelocityConfig usePluginNamespace() {
		Optional<PluginContainer> pluginContainerOptional = server.getPluginManager().fromInstance(plugin);
		if (pluginContainerOptional.isEmpty()) {
			CommandAPI.logInfo("Cannot use plugin namespace because " + plugin.getClass().getSimpleName() + " is not a Velocity plugin! The currently set namespace wasn't changed.");
			return instance();
		}
		super.setNamespace(pluginContainerOptional.get().getDescription().getId());
		super.usePluginNamespace = true;
		return instance();
	}

	@Override
	public CommandAPIVelocityConfig setNamespace(String namespace) {
		if (namespace == null) {
			throw new NullPointerException("Default namespace can't be null!");
		}
		if (!CommandAPIHandler.NAMESPACE_PATTERN.matcher(namespace).matches()) {
			CommandAPI.logNormal("Did not set namespace to the provided '" + namespace + "' namespace because only 0-9, a-z, underscores, periods and hyphens are allowed!");
			return this;
		}
		return super.setNamespace(namespace);
	}

	@Override
	public CommandAPIVelocityConfig instance() {
		return this;
	}
}
