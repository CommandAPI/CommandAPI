package dev.jorel.commandapi;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.function.Function;

/**
 * Configuration wrapper class for Bukkit. The config.yml file used by the CommandAPI is
 * only ever read from, nothing is ever written to it. That's why there's only
 * getter methods.
 */
public abstract class InternalBukkitConfig extends InternalConfig {
	// The name of the plugin that is loading the CommandAPI
	private final String pluginName;

	// Whether we should use the latest NMS version (which may not be compatible)
	private final boolean fallbackToLatestNMS;

	private final boolean skipReloadDatapacks;

	// List of plugins which should ignore proxied senders
	private final List<String> skipSenderProxy;

	// NBT API configuration
	private Class<?> nbtContainerClass;
	private Function<Object, ?> nbtContainerConstructor;

	/**
	 * Creates an {@link InternalBukkitConfig} from a {@link CommandAPIBukkitConfig}
	 *
	 * @param config The configuration to use to set up this internal configuration
	 */
	public InternalBukkitConfig(CommandAPIBukkitConfig<? extends CommandAPIBukkitConfig<?>> config) {
		super(config);
		this.pluginName = config.pluginName;
		this.fallbackToLatestNMS = config.fallbackToLatestNMS;
		this.skipReloadDatapacks = config.skipReloadDatapacks;
		this.skipSenderProxy = config.skipSenderProxy;
		this.nbtContainerClass = config.nbtContainerClass;
		this.nbtContainerConstructor = config.nbtContainerConstructor;
	}

	/**
	 * @return The name of the {@link JavaPlugin} that is loading the CommandAPI
	 */
	public String getPluginName() {
		return pluginName;
	}

	/**
	 * @return Whether the CommandAPI should fall back to the latest version if no specific
	 * 	implementation for the current version was found
	 */
	public boolean fallbackToLatestNMS() {
		return fallbackToLatestNMS;
	}

	/**
	 * @return Whether the CommandAPI should skip reloading datapacks when the server has finished loading
	 */
	public boolean skipReloadDatapacks() {
		return skipReloadDatapacks;
	}

	/**
	 * @param commandName A command where sender proxying should be skipped
	 * @return Whether sender proxying should be skipped for a given command
	 */
	public boolean shouldSkipSenderProxy(String commandName) {
		return this.skipSenderProxy.contains(commandName);
	}

	/**
	 * @return The NBT Tag Compound implementation class
	 */
	public Class<?> getNBTContainerClass() {
		return this.nbtContainerClass;
	}

	/**
	 * @return A function that takes in an Object (NMS NBTTagCompound) and returns
	 *         an implementation of an NBT Tag Compound
	 */
	public Function<Object, ?> getNBTContainerConstructor() {
		return this.nbtContainerConstructor;
	}

	public void lateInitializeNBT(Class<?> nbtContainerClass, Function<Object, ?> nbtContainerConstructor) {
		this.nbtContainerClass = nbtContainerClass;
		this.nbtContainerConstructor = nbtContainerConstructor;
	}

}
