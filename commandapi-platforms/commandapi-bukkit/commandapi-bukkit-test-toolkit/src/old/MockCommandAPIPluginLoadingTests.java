package dev.jorel.commandapi;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for loading the {@link MockCommandAPIPlugin}.
 */
class MockCommandAPIPluginLoadingTests {
	// Setup
	@BeforeEach
	public void setUp() {
		MockBukkit.mock();
	}

	@AfterEach
	public void tearDown() {
		MockBukkit.unmock();
	}

	// Tests
	@Test
	void testMockBukkitLoad() {
		MockCommandAPIPlugin plugin = MockBukkit.load(MockCommandAPIPlugin.class);

		assertTrue(plugin.isEnabled());
	}

	@Test
	void testLoadMethod() {
		MockCommandAPIPlugin plugin = MockCommandAPIPlugin.load();

		assertTrue(plugin.isEnabled());
	}

	@Test
	void testLoadWithConfiguration() {
		MockCommandAPIPlugin plugin = MockCommandAPIPlugin.load(config -> config.verboseOutput(true));

		assertTrue(plugin.isEnabled());
		assertTrue(CommandAPIBukkit.getConfiguration().hasVerboseOutput());
	}

	@Test
	void testConfigurationDoesNotPersist() {
		MockCommandAPIPlugin plugin = MockCommandAPIPlugin.load(config -> config.verboseOutput(true));

		assertTrue(plugin.isEnabled());
		assertTrue(CommandAPIBukkit.getConfiguration().hasVerboseOutput());

		MockBukkit.unmock();
		MockBukkit.mock();

		plugin = MockBukkit.load(MockCommandAPIPlugin.class);

		assertTrue(plugin.isEnabled());
		assertFalse(CommandAPIBukkit.getConfiguration().hasVerboseOutput());
	}
}
