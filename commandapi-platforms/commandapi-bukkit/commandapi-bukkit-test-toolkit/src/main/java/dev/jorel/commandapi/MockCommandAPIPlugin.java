package dev.jorel.commandapi;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;

/**
 * A {@link JavaPlugin} that loads the CommandAPI in a testing environment. If your plugin depends on the CommandAPI
 * plugin when running on a server (you aren't shading the CommandAPI), load this class before loading your own plugin
 * with {@link MockBukkit}. You can do this directly using {@code MockBukkit.load(MockCommandAPIPlugin.class);}, or with
 * {@link MockCommandAPIPlugin#load()}. If you need to configure settings usually located in the CommandAPI's plugin.yml,
 * use {@link MockCommandAPIPlugin#load(Consumer)}.
 */
public class MockCommandAPIPlugin extends JavaPlugin {
	// Allow loading with settings,
	//  Default to none if `MockBukkit.load(MockCommandAPIPlugin.class)` is used directly
	private static Consumer<CommandAPIPaperConfig> configureSettings = null;

	/**
	 * Loads the CommandAPI plugin using {@link MockBukkit#load(Class)}.
	 *
	 * @return The {@link MockCommandAPIPlugin} instance that was loaded.
	 */
	public static MockCommandAPIPlugin load() {
		return load(null);
	}

	/**
	 * Loads the CommandAPI plugin using {@link MockBukkit#load(Class)}.
	 *
	 * @param configureSettings A {@link Consumer} that can configure the settings of the {@link dev.jorel.commandapi.CommandAPIPaperConfig}
	 *                          before it is used to load the CommandAPI plugin.
	 * @return The {@link MockCommandAPIPlugin} instance that was loaded.
	 */
	public static MockCommandAPIPlugin load(Consumer<CommandAPIPaperConfig> configureSettings) {
		MockCommandAPIPlugin.configureSettings = configureSettings;
		return MockBukkit.load(MockCommandAPIPlugin.class);
	}

	@Override
	public void onLoad() {
		CommandAPIPaperConfig config = new CommandAPIPaperConfig(this.getPluginMeta());

		if (configureSettings != null) {
			configureSettings.accept(config);
		}
		configureSettings = null; // Reset to avoid configs persisting between tests

		CommandAPI.onLoad(config);
	}

	@Override
	public void onEnable() {
		CommandAPIPaper.onEnable(this);
	}

	@Override
	public void onDisable() {
		CommandAPI.onDisable();
	}
}
