package dev.jorel.commandapi;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * A class that contains information needed to configure the CommandAPI on Bukkit-based servers.
 */
public abstract class CommandAPIBukkitConfig<T extends CommandAPIBukkitConfig<T>> extends CommandAPIConfig<T> {

	JavaPlugin plugin;

	// Default configuration
	boolean skipReloadDatapacks = true;

	/**
	 * Creates a new CommandAPIBukkitConfig object. Variables in this
	 * constructor are required to load the CommandAPI on Bukkit properly.
	 *
	 * @param plugin The {@link JavaPlugin} that is loading the CommandAPI
	 *               This is used when registering events.
	 */
	public CommandAPIBukkitConfig(JavaPlugin plugin) {
		this.plugin = plugin;
		super.setNamespace(plugin.getName().toLowerCase());
	}

	/**
	 * @deprecated The plugin namespace is now the default namespace
	 * @return this CommandAPIBukkitConfig
	 */
	@Deprecated(since = "10.0.0", forRemoval = true)
	public T usePluginNamespace() {
		super.setNamespace(plugin.getName().toLowerCase());
		super.usePluginNamespace = true;
		return instance();
	}

	@Override
	public T setNamespace(String namespace) {
		if (namespace == null) {
			throw new NullPointerException("Default namespace can't be null!");
		}
		if (namespace.isEmpty()) {
			CommandAPI.logNormal("Did not set namespace to an empty value! Namespace '" + super.namespace + "' is used as the default namespace!");
			return instance();
		}
		if (!CommandAPIHandler.NAMESPACE_PATTERN.matcher(namespace).matches()) {
			CommandAPI.logNormal("Did not set namespace to the provided '" + namespace + "' namespace because only 0-9, a-z, underscores, periods and hyphens are allowed!");
			return instance();
		}
		return super.setNamespace(namespace);
	}

	@Override
	public abstract T instance();
}
