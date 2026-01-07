package dev.jorel.commandapi;

import dev.jorel.commandapi.test.MockPlatform;

public abstract class CommandAPIVersionHandler {
	static LoadContext getPlatform(CommandAPIConfig<?> config) {
		InternalPaperConfig internalPaperConfig;
		if (config instanceof CommandAPIPaperConfig paperConfig) {
			internalPaperConfig = new InternalPaperConfig(paperConfig);
		} else {
			throw new IllegalArgumentException("CommandAPIPaper was loaded with non-Paper config!");
		}

		return new LoadContext(MockPlatform.getInstance().createPlatform(internalPaperConfig));
	}
}
