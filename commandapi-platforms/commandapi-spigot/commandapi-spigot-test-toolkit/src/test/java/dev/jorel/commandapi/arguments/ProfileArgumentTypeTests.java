package dev.jorel.commandapi.arguments;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandTestBase;
import dev.jorel.commandapi.MockCommandSource;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerProfile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.command.ConsoleCommandSenderMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.ArrayList;
import java.util.List;

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

	private void registerCommand() {
		new CommandAPICommand("test")
			.withArguments(new PlayerProfileArgument("player"))
			.executes(DEFAULT_EXECUTOR)
			.register();
	}

	private List<PlayerProfile> profileList(Player... players) {
		List<PlayerProfile> profileList = new ArrayList<>(players.length);
		for (Player player : players) {
			profileList.add(player.getPlayerProfile());
		}

		return profileList;
	}

	// Test
	@Test
	void testEmptyInputs() throws CommandSyntaxException {
		registerCommand();

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

	@Test
	void testDefaultSuggestions() {
		registerCommand();

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

	@Test
	void testEntitySelectorErrors() {
		registerCommand();
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

	@Test
	void testEntitySelectorSuccess() {
		registerCommand();

		// Layout: p1  p2
		PlayerMock p1 = server.addPlayer();
		p1.teleport(p1.getLocation().set(0, 0, 0));

		PlayerMock p2 = server.addPlayer();
		p2.teleport(p2.getLocation().set(1, 0, 0));

		// Arbitrary order uses creation order, since it references a LinkedHashSet
		assertCommandSucceedsWithArguments(
			p1, "test @a",
			profileList(p1, p2)
		);
		assertCommandSucceedsWithArguments(
			p2, "test @a",
			profileList(p1, p2)
		);

		// @p selects nearest
		assertCommandSucceedsWithArguments(
			p1, "test @p",
			profileList(p1)
		);
		assertCommandSucceedsWithArguments(
			p2, "test @p",
			profileList(p2)
		);
	}

	@Test
	void testEntitySelectorSuggestions() {
		registerCommand();

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

	@Test
	void testNameSelectorErrors() {
		registerCommand();

		PlayerMock playerA = server.addPlayer("PlayerA");

		assertCommandFails(
			playerA, "test invalid",
			"That player does not exist"
		);
	}

	@Test
	void testNameSelectorSuccess() {
		registerCommand();

		PlayerMock playerA = server.addPlayer("PlayerA");
		PlayerMock playerB = server.addPlayer("PlayerB");

		ConsoleCommandSenderMock console = server.getConsoleSender();

		assertCommandSucceedsWithArguments(
			console, "test PlayerA",
			profileList(playerA)
		);
		assertCommandSucceedsWithArguments(
			console, "test PlayerB",
			profileList(playerB)
		);

		// Not case-sensitive
		assertCommandSucceedsWithArguments(
			console, "test pLAyeRa",
			profileList(playerA)
		);
	}

	@Test
	void testNameSuggestions() {
		registerCommand();

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
