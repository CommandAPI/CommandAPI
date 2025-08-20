package dev.jorel.commandapi;

import dev.jorel.commandapi.nms.*;

import java.util.function.Function;

public abstract class CommandAPIVersionHandler {
	// Allow loading a different platform implementation (most likely to implement something `MockCommandAPISpigot` doesn't)
	private static Function<InternalPaperConfig, CommandAPIPlatform<?, ?, ?>> alternativePlatform = null;

	/**
	 * Configures the test kit to use the given {@link CommandAPIPlatform} when the CommandAPI is loaded.
	 *
	 * @param platform A {@link Function} that uses a {@link InternalPaperConfig} to create the
	 *                 {@link CommandAPIPlatform} to use for the next test. This will likely be a custom
	 *                 implementation of {@link MockCommandAPIPaper} that overrides a method you need to run
	 *                 tests that doesn't have a proper implementation in {@link MockCommandAPIPaper}.
	 */
	public static void usePlatformImplementation(Function<InternalPaperConfig, CommandAPIPlatform<?, ?, ?>> platform) {
		alternativePlatform = platform;
	}

	static LoadContext getPlatform(CommandAPIConfig<?> config) {
		InternalPaperConfig internalPaperConfig;
		if (config instanceof CommandAPIPaperConfig paperConfig) {
			internalPaperConfig = new InternalPaperConfig(paperConfig);
		} else {
			throw new IllegalArgumentException("CommandAPIPaper was loaded with non-Paper config!");
		}

		// Default to MockCommandAPIBukkit if not given
		CommandAPIPlatform<?, ?, ?> platform = alternativePlatform == null ?
			new MockCommandAPIPaper(internalPaperConfig, new MockPaperNMS()) :
			alternativePlatform.apply(internalPaperConfig);


		// Reset to avoid platform persisting between tests
		alternativePlatform = null;

		return new LoadContext(platform);
	}
}
