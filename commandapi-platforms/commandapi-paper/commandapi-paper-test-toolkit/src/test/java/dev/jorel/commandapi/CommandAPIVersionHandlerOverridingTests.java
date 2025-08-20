package dev.jorel.commandapi;

import dev.jorel.commandapi.nms.MockPaperNMS;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

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

	private static class CustomMockCommandAPIPaper extends MockCommandAPIPaper {
		public CustomMockCommandAPIPaper(InternalPaperConfig config) {
			super(config, new MockPaperNMS());
		}
	}

	// Tests
	@Test
	void testDefaultPlatform() {
		MockCommandAPIPlugin.load();

		assertEquals(MockCommandAPIPaper.class, getMockCommandAPI().getCommandAPIBukkit().getClass());
	}

	@Test
	void testChangingPlatform() {
		CommandAPIVersionHandler.usePlatformImplementation(CustomMockCommandAPIPaper::new);
		MockCommandAPIPlugin.load();

		assertEquals(CustomMockCommandAPIPaper.class, getMockCommandAPI().getCommandAPIBukkit().getClass());
	}

	@Test
	void testPlatformDoesNotPersist() {
		testChangingPlatform();

		MockBukkit.unmock();
		MockBukkit.mock();

		testDefaultPlatform();
	}
}
