package dev.jorel.commandapi.arguments;

import be.seeseemelk.mockbukkit.command.ConsoleCommandSenderMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
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
					new PlayerArgument("player")
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
}
