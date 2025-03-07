package dev.jorel.commandapi;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.jorel.commandapi.arguments.Argument;

import java.util.Optional;

public class CommandAPICommand extends AbstractCommandAPICommand<CommandAPICommand, Argument<?>, CommandSource> implements VelocityExecutable<CommandAPICommand> {
	/**
	 * Creates a new command builder
	 *
	 * @param commandName The name of the command to create
	 */
	public CommandAPICommand(String commandName) {
		super(commandName);
	}

	/**
	 * Registers the command with a given namespace
	 *
	 * @param namespace The namespace of this command. This cannot be null and can only contain 0-9, a-z, underscores, periods, and hyphens.
	 */
	@Override
	public void register(String namespace) {
		super.register(namespace);
	}

	/**
	 * Registers the command with the given plugin object
	 *
	 * @param plugin The plugin instance used to determine this command's namespace
	 */
	public void register(Object plugin) {
		if (plugin == null) {
			throw new NullPointerException("Parameter 'plugin' was null while trying to register command /" + this.getName() + "!");
		}
		ProxyServer server = CommandAPIVelocity.getConfiguration().getServer();
		Optional<PluginContainer> pluginContainerOptional = server.getPluginManager().fromInstance(plugin);
		if (pluginContainerOptional.isEmpty()) {
			CommandAPI.logInfo("Using the default namespace to register commands since " + plugin.getClass().getSimpleName() + " is not a Velocity plugin!");
			super.register();
			return;
		}
		super.register(pluginContainerOptional.get().getDescription().getId());
	}

	@Override
	public CommandAPICommand instance() {
		return this;
	}
}
