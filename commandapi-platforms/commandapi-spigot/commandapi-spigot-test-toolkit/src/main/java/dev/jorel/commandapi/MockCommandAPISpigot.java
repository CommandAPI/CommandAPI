package dev.jorel.commandapi;

import dev.jorel.commandapi.nms.MockNMS;
import dev.jorel.commandapi.nms.MockSpigotNMS;
import dev.jorel.commandapi.nms.NMS;

/**
 * An implementation of {@link CommandAPISpigot} that is compatible with a MockBukkit testing environment.
 * Does not rely on any version-specific Minecraft code to (ideally) support testing in any version.
 */
public class MockCommandAPISpigot extends CommandAPISpigot<MockCommandSource> implements MockSpigotNMS {
	// References to utility classes
	private MockNMS mockNMS;
	private final MockCommandAPI mockCommandAPI;

	public MockCommandAPISpigot(InternalSpigotConfig config) {
		super(config);
		this.mockCommandAPI = new MockCommandAPI(this);
	}

	// Load
	@Override
	public void onLoad() {
		mockCommandAPI.onLoad();

		super.onLoad();
	}

	// Test environment-specific
	@Override
	public CommandRegistrationStrategy<MockCommandSource> createCommandRegistrationStrategy() {
		return mockCommandAPI.getCommandRegistrationStrategy();
	}

	@Override
	public NMS<MockCommandSource> bukkitNMS() {
		if (mockNMS == null) {
			this.mockNMS = new MockNMS();
		}
		return mockNMS;
	}

	@Override
	public CommandAPILogger getLogger() {
		return MockCommandAPI.ENABLE_LOGGING ? super.getLogger() : CommandAPILogger.silent();
	}
}
