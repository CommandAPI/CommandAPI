package dev.jorel.commandapi.arguments;

import org.mockbukkit.mockbukkit.command.ConsoleCommandSenderMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandTestBase;
import dev.jorel.commandapi.MockCommandSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ProfileArgumentTypeTests extends CommandTestBase {
	// Setup
	@BeforeEach
	public void setUp() {
		super.setUp();
	}

	@AfterEach
	public void tearDown() {
		super.tearDown();
	}

	private void registerCommand(boolean offlinePlayer) {
		// Both arguments reference ProfileArgumentType
		new CommandAPICommand("test")
			.withArguments(
				offlinePlayer ?
					new OfflinePlayerArgument("offline") :
					new PlayerProfileArgument("player")
			)
			.executes(DEFAULT_EXECUTOR)
			.register();
	}

	// Test
	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	void testEmptyInputs(boolean offlinePlayer) throws CommandSyntaxException {
		registerCommand(offlinePlayer);

		PlayerMock player = server.addPlayer();

		// Fully empty input, cannot read
		CommandContext<MockCommandSource> emptyNameContext = createContextWithParser(
			player, "player", ProfileArgumentType.INSTANCE::parse, ""
		);
		assertThrowsWithMessage(
			CommandSyntaxException.class,
			() -> ProfileArgumentType.getProfiles(emptyNameContext, "player"),
			"That player does not exist"
		);

		// Empty input, but can read
		CommandContext<MockCommandSource> spaceNameContext = createContextWithParser(
			player, "player", ProfileArgumentType.INSTANCE::parse, " "
		);
		assertThrowsWithMessage(
			CommandSyntaxException.class,
			() -> ProfileArgumentType.getProfiles(spaceNameContext, "player"),
			"That player does not exist"
		);
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	void testDefaultSuggestions(boolean offlinePlayer) {
		registerCommand(offlinePlayer);

		server.addPlayer("Player1");
		server.addPlayer("Player2");

		ConsoleCommandSenderMock console = server.getConsoleSender();
		assertCommandSuggestsTooltips(
			console, "test ",
			5,
			makeTooltip("@a", "All players"),
			makeTooltip("@e", "All entities"),
			makeTooltip("@n", "Nearest entity"),
			makeTooltip("@p", "Nearest player"),
			makeTooltip("@r", "Random player"),
			makeTooltip("@s", "Current entity"),
			makeTooltip("Player1", null),
			makeTooltip("Player2", null)
		);
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	void testEntitySelectorErrors(boolean offlinePlayer) {
		registerCommand(offlinePlayer);
		ConsoleCommandSenderMock console = server.getConsoleSender();

		assertCommandFails(
			console, "test @",
			"Missing selector type at position 6: test @<--[HERE]"
		);

		assertCommandFails(
			console, "test @e",
			"Only players may be affected by this command, but the provided selector includes entities"
		);

		assertCommandFails(
			console, "test @a",
			"No player was found"
		);
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	void testEntitySelectorSuccess(boolean offlinePlayer) {
		registerCommand(offlinePlayer);

		// Layout: p1  p2
		PlayerMock p1 = server.addPlayer();
		p1.teleport(p1.getLocation().set(0, 0, 0));

		PlayerMock p2 = server.addPlayer();
		p2.teleport(p2.getLocation().set(1, 0, 0));

		// When multiple are selected, defaults to first in order
		// Arbitrary order ends up selecting first player, since it references a LinkedHashSet
		assertCommandSucceedsWithArguments(
			p1, "test @a",
			p1
		);
		assertCommandSucceedsWithArguments(
			p2, "test @a",
			p1
		);

		// @p selects nearest
		assertCommandSucceedsWithArguments(
			p1, "test @p",
			p1
		);
		assertCommandSucceedsWithArguments(
			p2, "test @p",
			p2
		);
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	void testEntitySelectorSuggestions(boolean offlinePlayer) {
		registerCommand(offlinePlayer);

		server.addPlayer("Player1");
		server.addPlayer("Player2");

		ConsoleCommandSenderMock console = server.getConsoleSender();
		assertCommandSuggestsTooltips(
			console, "test @",
			5,
			makeTooltip("@a", "All players"),
			makeTooltip("@e", "All entities"),
			makeTooltip("@n", "Nearest entity"),
			makeTooltip("@p", "Nearest player"),
			makeTooltip("@r", "Random player"),
			makeTooltip("@s", "Current entity")
		);
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	void testNameSelectorErrors(boolean offlinePlayer) {
		registerCommand(offlinePlayer);

		PlayerMock playerA = server.addPlayer("PlayerA");

		assertCommandFails(
			playerA, "test invalid",
			"That player does not exist"
		);
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	void testNameSelectorSuccess(boolean offlinePlayer) {
		registerCommand(offlinePlayer);

		PlayerMock playerA = server.addPlayer("PlayerA");
		PlayerMock playerB = server.addPlayer("PlayerB");

		ConsoleCommandSenderMock console = server.getConsoleSender();

		assertCommandSucceedsWithArguments(
			console, "test PlayerA",
			playerA
		);
		assertCommandSucceedsWithArguments(
			console, "test PlayerB",
			playerB
		);

		// Not case-sensitive
		assertCommandSucceedsWithArguments(
			console, "test pLAyeRa",
			playerA
		);
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	void testNameSuggestions(boolean offlinePlayer) {
		registerCommand(offlinePlayer);

		server.addPlayer("alice");
		server.addPlayer("allan");
		server.addPlayer("bob");

		ConsoleCommandSenderMock console = server.getConsoleSender();

		// Only suggests names that match remaining
		assertCommandSuggests(
			console, "test a",
			5, "alice", "allan"
		);
		assertCommandSuggests(
			console, "test all",
			5, "allan"
		);
		assertCommandSuggests(
			console, "test b",
			5, "bob"
		);

		// Suggestions are not case-sensitive
		assertCommandSuggests(
			console, "test AL",
			5, "alice", "allan"
		);
	}
}
