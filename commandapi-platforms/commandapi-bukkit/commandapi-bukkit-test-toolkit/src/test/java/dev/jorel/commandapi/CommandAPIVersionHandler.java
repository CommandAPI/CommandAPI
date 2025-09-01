package dev.jorel.commandapi;

public abstract class CommandAPIVersionHandler {
	static LoadContext getPlatform(CommandAPIConfig<?> config) {
		MockInternalBukkitConfig mockInternalBukkitConfig;
		if (config instanceof MockCommandAPIBukkitConfig bukkitConfig) {
			mockInternalBukkitConfig = new MockInternalBukkitConfig(bukkitConfig);
		} else {
			throw new IllegalStateException("MockCommandAPIBukkit was loaded with non-MockBukkit config!");
		}

		return new LoadContext(new MockCommandAPIBukkit(mockInternalBukkitConfig));
	}
}
