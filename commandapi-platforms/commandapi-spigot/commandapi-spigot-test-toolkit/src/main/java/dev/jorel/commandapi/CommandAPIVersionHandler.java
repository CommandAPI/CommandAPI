package dev.jorel.commandapi;

import java.util.function.Function;

public abstract class CommandAPIVersionHandler {
	// Allow loading a different platform implementation (most likely to implement something `MockCommandAPISpigot` doesn't)
	private static Function<InternalSpigotConfig, CommandAPIPlatform<?, ?, ?>> alternativePlatform = null;

	/**
	 * Configures the test kit to use the given {@link CommandAPIPlatform} when the CommandAPI is loaded.
	 *
	 * @param platform A {@link Function} that uses a {@link InternalSpigotConfig} to create the
	 *                 {@link CommandAPIPlatform} to use for the next test. This will likely be a custom
	 *                 implementation of {@link MockCommandAPISpigot} that overrides a method you need to run
	 *                 tests that doesn't have a proper implementation in {@link MockCommandAPISpigot}.
	 */
	public static void usePlatformImplementation(Function<InternalSpigotConfig, CommandAPIPlatform<?, ?, ?>> platform) {
		alternativePlatform = platform;
	}

	static LoadContext getPlatform(CommandAPIConfig<?> config) {
		InternalSpigotConfig internalSpigotConfig;
		if (config instanceof CommandAPISpigotConfig spigotConfig) {
			internalSpigotConfig = new InternalSpigotConfig(spigotConfig);
		} else {
			throw new IllegalArgumentException("CommandAPISpigot was loaded with non-Spigot config!");
		}

		// Default to MockCommandAPISpigot if not given
		CommandAPIPlatform<?, ?, ?> platform = alternativePlatform == null ?
			new MockCommandAPISpigot(internalSpigotConfig) :
			alternativePlatform.apply(internalSpigotConfig);

		// Reset to avoid platform persisting between tests
		alternativePlatform = null;

		return new LoadContext(platform);
	}
}
