package dev.jorel.commandapi;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for using {@link CommandAPIVersionHandler#usePlatformImplementation(Function)}
 */
class CommandAPIVersionHandlerOverridingTests extends CommandAPITestUtilities {
	// Setup
	@BeforeEach
	public void setUp() {
		MockBukkit.mock();
	}

	@AfterEach
	public void tearDown() {
		MockBukkit.unmock();
	}

	private static class CustomMockCommandAPISpigot extends MockCommandAPISpigot {
		public CustomMockCommandAPISpigot(InternalSpigotConfig config) {
			super(config);
		}
	}

	// Tests
	@Test
	void testDefaultPlatform() {
		MockCommandAPIPlugin.load();

		assertEquals(MockCommandAPISpigot.class, getMockCommandAPI().getCommandAPIBukkit().getClass());
	}

	@Test
	void testChangingPlatform() {
		CommandAPIVersionHandler.usePlatformImplementation(CustomMockCommandAPISpigot::new);
		MockCommandAPIPlugin.load();

		assertEquals(CustomMockCommandAPISpigot.class, getMockCommandAPI().getCommandAPIBukkit().getClass());
	}

	@Test
	void testPlatformDoesNotPersist() {
		testChangingPlatform();

		MockBukkit.unmock();
		MockBukkit.mock();

		testDefaultPlatform();
	}
}
