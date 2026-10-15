package dev.jorel.commandapi;

import dev.jorel.commandapi.test.MockPlatform;
import dev.jorel.commandapi.test.TestSpigotNMS_26_3;

/**
 * This file handles loading the correct platform implementation. The CommandAPIVersionHandler
 * file within the commandapi-core module is NOT used at run time. Instead, the platform modules
 * replace this class with their own version that handles loads the correct class for their version
 */
public abstract class CommandAPIVersionHandler {

	/**
	 * Used by the shared tests in commandapi-bukkit-test-tests for behavior that differs between platforms
	 */
	public static final boolean IS_PAPER = false;

	// Minecraft isn't obfuscated since 26.1
	public static final boolean IS_MOJANG_MAPPED = true;

	/**
	 * @return the Minecraft version being tested, used by the shared tests in commandapi-bukkit-test-tests
	 */
	public static MCVersion getVersion() {
		return MCVersion.V26_3;
	}

	static LoadContext getPlatform(CommandAPIConfig<?> config) {
		if (!(config instanceof CommandAPISpigotConfig spigotConfig)) {
			throw new IllegalArgumentException("CommandAPISpigot was loaded with non-Spigot config!");
		}

		// Mirrors commandapi-spigot-vh, with the SpigotNMS implementation swapped for the mocked one
		CommandAPIPlatform<?, ?, ?> platform = new TestSpigotNMS_26_3(new InternalSpigotConfig(spigotConfig));

		// MockBukkit's paper-api has Paper's Brigadier API, so CommandAPIBukkit thinks it's running on Paper
		MockPlatform.setField(CommandAPIBukkit.class, "isPaperBrigAPI", platform, false);
		return new LoadContext(platform);
	}
}
