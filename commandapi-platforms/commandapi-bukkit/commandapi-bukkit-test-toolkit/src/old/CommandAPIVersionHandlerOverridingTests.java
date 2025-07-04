package dev.jorel.commandapi;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for using {@link CommandAPIVersionHandler#usePlatformImplementation(CommandAPIPlatform)}
 */
class CommandAPIVersionHandlerOverridingTests {
	// Setup
	@BeforeEach
	public void setUp() {
		MockBukkit.mock();
	}

	@AfterEach
	public void tearDown() {
		MockBukkit.unmock();
	}

	private static class CustomMockCommandAPIBukkit extends MockCommandAPI {

	}

	// Tests
	@Test
	void testDefaultPlatform() {
		MockCommandAPIPlugin.load();

		assertEquals(MockCommandAPI.class, CommandAPITestUtilities.getMockCommandAPI().getClass());
	}

	@Test
	void testChangingPlatform() {
		CommandAPIVersionHandler.usePlatformImplementation(new CustomMockCommandAPIBukkit());
		MockCommandAPIPlugin.load();

		assertEquals(CustomMockCommandAPIBukkit.class, CommandAPITestUtilities.getMockCommandAPI().getClass());
	}

	@Test
	void testPlatformDoesNotPersist() {
		CommandAPIVersionHandler.usePlatformImplementation(new CustomMockCommandAPIBukkit());
		MockCommandAPIPlugin.load();

		assertEquals(CustomMockCommandAPIBukkit.class, CommandAPITestUtilities.getMockCommandAPI().getClass());

		MockBukkit.unmock();
		MockBukkit.mock();

		MockCommandAPIPlugin.load();
		assertEquals(MockCommandAPI.class, CommandAPITestUtilities.getMockCommandAPI().getClass());
	}
}
