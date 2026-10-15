package dev.jorel.commandapi;

import dev.jorel.commandapi.nms.APITypeProvider;
import dev.jorel.commandapi.test.PaperMockNMS;

/**
 * This file handles loading the correct platform implementation. The CommandAPIVersionHandler
 * file within the commandapi-core module is NOT used at run time. Instead, the platform modules
 * replace this class with their own version that handles loads the correct class for their version
 */
public abstract class CommandAPIVersionHandler {

	/**
	 * Used by the shared tests in commandapi-bukkit-test-tests for behavior that differs between platforms
	 */
	public static final boolean IS_PAPER = true;

	// Paper is Mojang-mapped since 1.20.5
	public static final boolean IS_MOJANG_MAPPED = true;

	static {
		// MockBukkit's ServerMock uses Bukkit's registries when it's created, which on Paper are backed by the
		//  Minecraft registries. This also initializes the game version, which Paper's ServerBuildInfo (used by
		//  Bukkit#setServer) needs. The shared TestBase uses this class before mocking the server.
		PaperMockNMS.setupRegistries();
	}

	/**
	 * @return the Minecraft version being tested, used by the shared tests in commandapi-bukkit-test-tests
	 */
	public static MCVersion getVersion() {
		return MCVersion.V26_3;
	}

	static LoadContext getPlatform(CommandAPIConfig<?> config) {
		if (!(config instanceof CommandAPIPaperConfig paperConfig)) {
			throw new IllegalArgumentException("CommandAPIPaper was loaded with non-Paper config!");
		}

		// Mirrors commandapi-paper-vh, with the PaperNMS implementation swapped for the mocked one
		return new LoadContext(new TestCommandAPIPaper(new InternalPaperConfig(paperConfig), new APITypeProvider(new PaperMockNMS())));
	}
}
