package dev.jorel.commandapi;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A class that contains information needed to configure the CommandAPI on Bukkit-based servers.
 */
public abstract class CommandAPIBukkitConfig<T extends CommandAPIBukkitConfig<T>> extends CommandAPIConfig<T> {

	String pluginName;

	// Default configuration
	boolean fallbackToLatestNMS = false;
	boolean skipReloadDatapacks = true;

	List<String> skipSenderProxy = new ArrayList<>();

	// NBT API
	Class<?> nbtContainerClass = null;
	Function<Object, ?> nbtContainerConstructor = null;

	/**
	 * Creates a new CommandAPIBukkitConfig object. Variables in this
	 * constructor are required to load the CommandAPI on Bukkit properly.
	 *
	 * @param pluginName The name of the {@link JavaPlugin} that is loading the CommandAPI
	 */
	public CommandAPIBukkitConfig(String pluginName) {
		this.pluginName = pluginName;
		super.setNamespace(pluginName.toLowerCase());
	}

	/**
	 * Sets whether the CommandAPI should fall back to the latest nms version
	 * if no implementation for the current version was found.
	 *
	 * @param fallbackToLatestNMS whether to fall back to the latest versions
	 * @return this CommandAPIConfig
	 */
	public T fallbackToLatestNMS(boolean fallbackToLatestNMS) {
		this.fallbackToLatestNMS = fallbackToLatestNMS;
		return instance();
	}

	public T addSkipSenderProxy(String... names) {
		this.skipSenderProxy.addAll(List.of(names));
		return instance();
	}

	public T addSkipSenderProxy(List<String> names) {
		this.skipSenderProxy.addAll(names);
		return instance();
	}

	/**
	 * Initializes the CommandAPI's implementation of an NBT API.
	 *
	 * @param <NBT>                     the type that the NBT compound container class
	 *                                is
	 * @param nbtContainerClass       the NBT compound container class. For example,
	 *                                {@code NBTContainer.class}
	 * @param nbtContainerConstructor a function that takes an Object (NMS
	 *                                {@code NBTTagCompound}) and returns an
	 *                                instance of the provided NBT compound
	 *                                container. For example,
	 *                                {@code NBTContainer::new}.
	 * @return this CommandAPIConfig
	 */
	public <NBT> T initializeNBTAPI(Class<NBT> nbtContainerClass,
									 Function<Object, NBT> nbtContainerConstructor) {
		this.nbtContainerClass = nbtContainerClass;
		this.nbtContainerConstructor = nbtContainerConstructor;
		return instance();
	}

	/**
	 * Sets the default namespace to use when register commands
	 *
	 * @param namespace the namespace to use when register commands
	 * @return this CommandAPIConfig
	 */
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
