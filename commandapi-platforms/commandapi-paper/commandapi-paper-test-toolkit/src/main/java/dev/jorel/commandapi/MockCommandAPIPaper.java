package dev.jorel.commandapi;

import dev.jorel.commandapi.nms.BundledNMS;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * An implementation of {@link CommandAPIPaper} that is compatible with a MockBukkit testing environment.
 * Does not rely on any version-specific Minecraft code to (ideally) support testing in any version.
 */
public class MockCommandAPIPaper extends CommandAPIPaper<MockCommandSource> {
	// References to utility classes
	private final MockCommandAPI mockCommandAPI;

	public MockCommandAPIPaper(InternalPaperConfig config, BundledNMS<MockCommandSource> nms) {
		super(config, nms);
		this.mockCommandAPI = new MockCommandAPI(this);
	}

	// Load
	// Not calling super implementations of `onLoad` and `onEnable` to avoid casting
	//  our MockCommandRegistrationStrategy to PaperCommandRegistration.
	//  Seems kinda weird that CommandAPIPaper is even doing that...
	//  We probably don't need SpigotCommandRegistration and PaperCommandRegistration
	//  now that we have CommandAPISpigot and CommandAPIPaper.
	@Override
	public void onLoad() {
		mockCommandAPI.onLoad();
		super.checkDependencies();
	}

	@Override
	public void onEnable() {
		super.plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin(getConfiguration().getPluginName());
	}

	// Test environment-specific
	@Override
	public CommandAPILogger getLogger() {
		return MockCommandAPI.ENABLE_LOGGING ? super.getLogger() : CommandAPILogger.silent();
	}
}
