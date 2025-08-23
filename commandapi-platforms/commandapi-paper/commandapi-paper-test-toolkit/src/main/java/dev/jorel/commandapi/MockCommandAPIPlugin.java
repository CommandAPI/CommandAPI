package dev.jorel.commandapi;

import org.bukkit.plugin.java.JavaPlugin;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.function.Consumer;

/**
 * Loads the CommandAPI in this mock Paper environment.
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
	 * @param configureSettings A {@link Consumer} that can configure the settings of the {@link CommandAPIPaperConfig}
	 *                          before it is used to load the CommandAPI plugin.
	 * @return The {@link MockCommandAPIPlugin} instance that was loaded.
	 */
	public static MockCommandAPIPlugin load(Consumer<CommandAPIPaperConfig> configureSettings) {
		MockCommandAPIPlugin.configureSettings = configureSettings;
		return MockBukkit.load(MockCommandAPIPlugin.class);
	}

	@Override
	public void onLoad() {
		CommandAPIPaperConfig config = new CommandAPIPaperConfig(this);

		if (configureSettings != null) {
			configureSettings.accept(config);
		}
		configureSettings = null; // Reset to avoid configs persisting between tests

		CommandAPI.onLoad(config);
	}

	@Override
	public void onEnable() {
		CommandAPI.onEnable();
	}

	@Override
	public void onDisable() {
		CommandAPI.onDisable();
	}
}
