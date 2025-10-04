package io.github.jorelali;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

// Import helper methods from CommandAPITestUtilities
import static dev.jorel.commandapi.CommandAPITestUtilities.assertCommandSucceeds;

// Import MockBukkit assertions
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockbukkit.mockbukkit.matcher.command.MessageTargetReceivedAnyMessageMatcher.hasNotReceivedAny;
import static org.mockbukkit.mockbukkit.matcher.command.MessageTargetReceivedMessageMatcher.hasReceived;

class PingPongCommandTests {
	// Setup
	private ServerMock server;

	@BeforeEach
	public void setUp() {
		// Set up MockBukkit server
		server = MockBukkit.mock();

		// Load our plugin
		MockBukkit.load(Main.class);
	}

	@AfterEach
	public void tearDown() {
		// Reset for a clean slate next test
		MockBukkit.unmock();
	}

	// Tests
	@Test
	void runCommand() {
		PlayerMock player = server.addPlayer();

		assertCommandSucceeds(player, "ping");

		assertThat(player, hasReceived("pong!"));
		assertThat(player, hasNotReceivedAny());
	}
}
