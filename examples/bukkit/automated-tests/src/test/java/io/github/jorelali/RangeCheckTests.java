package io.github.jorelali;

import dev.jorel.commandapi.CommandAPITestUtilities;
import dev.jorel.commandapi.MockCommandAPIPlugin;
import dev.jorel.commandapi.wrappers.IntegerRange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.List;
import java.util.Map;

// Import MockBukkit assertions
import static org.mockbukkit.mockbukkit.matcher.command.MessageTargetReceivedAnyMessageMatcher.hasNotReceivedAny;
import static org.mockbukkit.mockbukkit.matcher.command.MessageTargetReceivedMessageMatcher.hasReceived;

// Access helper methods by extending CommandAPITestUtilities
public class RangeCheckTests extends CommandAPITestUtilities {
	// Setup
	private PlayerMock player;

	@BeforeEach
	public void setUp() {
		// Set up MockBukkit server
		ServerMock server = MockBukkit.mock();

		// Load the CommandAPI plugin
		MockBukkit.load(MockCommandAPIPlugin.class);

		// Load our plugin
		MockBukkit.load(Main.class);

		// Get player for running commands
		player = server.addPlayer();
	}

	@AfterEach
	public void tearDown() {
		MockBukkit.unmock();
	}

	// Tests
	@Test
	void testCommandExecution() {
		// Testing successful usage of command
		assertCommandSucceeds(player, "rangecheck 10..15 13");
		hasReceived("Number 13 is within range 10..15").matches(player);

		assertCommandSucceedsWithArguments(
			player, "rangecheck 10..15 10",
			// Verifying argument array
			new IntegerRange(10, 15), 10
		);
		hasReceived("Number 10 is within range 10..15").matches(player);

		assertCommandSucceedsWithArguments(
			player, "rangecheck -3..-3 -3",
			// Verifying argument map
			Map.of(
				"range", new IntegerRange(-3, -3),
				"number", -3
			)
		);
		hasReceived("Number -3 is within range -3..-3").matches(player);

		// Test unsuccessful usage of command
		assertCommandFails(
			player, "rangecheck 10..15 5",
			"Number 5 is outside of range 10..15"
		);

		assertCommandFailsWithArguments(
			player, "rangecheck 10..15 20",
			"Number 20 is outside of range 10..15",
			// Verifying argument array
			new IntegerRange(10, 15), 20
		);

		assertCommandFailsWithArguments(
			player, "rangecheck -3..-3 10",
			"Number 10 is outside of range -3..-3",
			// Verifying argument map
			Map.of(
				"range", new IntegerRange(-3, -3),
				"number", 10
			)
		);

		// Make sure no extra messages were sent
		hasNotReceivedAny().matches(player);
	}

	@Test
	void testNumberSuggestions() {
		// Verifying just suggestion text
		assertCommandSuggests(
			player, "rangecheck 0..10 ",
			// Give array of suggestions
			"-5", "15", "5"
		);
		assertCommandSuggests(
			player, "rangecheck -10..0 ",
			// Give list of suggestions
			List.of("-15", "-5", "5")
		);

		// Verifying text and tooltips
		assertCommandSuggestsTooltips(
			player, "rangecheck 10..11 ",
			// Give array of suggestions
			makeTooltip("10", "Middle"),
			makeTooltip("12", "Too high"),
			makeTooltip("8", "Too low")
		);
		assertCommandSuggestsTooltips(
			player, "rangecheck 10..10 ",
			// Give list of suggestions
			List.of(
				makeTooltip("10", "Middle"),
				makeTooltip("15", "Too high"),
				makeTooltip("5", "Too low")
			)
		);
	}
}
