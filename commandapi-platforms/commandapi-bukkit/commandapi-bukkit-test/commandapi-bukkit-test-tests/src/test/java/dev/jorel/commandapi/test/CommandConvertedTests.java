package dev.jorel.commandapi.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.LocationArgument;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.Converter;

/**
 * Tests for converted commands
 */
class CommandConvertedTests extends TestBase {

	/*********
	 * Setup *
	 *********/

	private CommandConvertedTestsPlugin plugin;

	@BeforeEach
	public void setUp() {
		super.setUp();
		plugin = CommandConvertedTestsPlugin.load();
	}

	@AfterEach
	public void tearDown() {
		super.tearDown();
	}

	/*********
	 * Tests *
	 *********/

	@Test
	void testConvertedExecution() {
		Converter.convert(plugin, "mycommand");

		PlayerMock player = server.addPlayer();
		server.dispatchBrigadierCommand(player, "mycommand");

		assertEquals("hello", player.nextMessage());
	}

	@Test
	void testSpaceInArguments() {
		Mut<String[]> results = plugin.getCaptureArgsResults();
		Converter.convert(plugin, "captureargs",
			new LocationArgument("location"),
			new EntitySelectorArgument.OnePlayer("player"),
			new GreedyStringArgument("string")
		);

		// Enable server to get `minecraft` namespace
		enableServer();
		Player player = server.addCraftPlayer("Player");

		// Make sure arguments are fully flattened and split
		assertStoresArrayResult(player, "minecraft:captureargs 1 2 3 @p hello world", results,
			"1", "2", "3", "Player", "hello", "world");

		assertNoMoreResults(results);
	}

	@Test
	void testFlatteningCombinations() {
		Mut<String[]> results = plugin.getCaptureArgsResults();
		Converter.convert(plugin, "captureargs",
			new EntitySelectorArgument.ManyPlayers("player1"),
			new EntitySelectorArgument.ManyPlayers("player2")
		);

		// Enable server to get `minecraft` namespace
		enableServer();
		Player playerA = server.addCraftPlayer("PlayerA");
		server.addPlayer("PlayerB");

		// Make sure all combinations of flattened arguments are run
		server.dispatchCommand(playerA, "minecraft:captureargs @a @a");
		assertArrayEquals(new String[]{"PlayerA", "PlayerA"}, results.get());
		assertArrayEquals(new String[]{"PlayerA", "PlayerB"}, results.get());
		assertArrayEquals(new String[]{"PlayerB", "PlayerA"}, results.get());
		assertArrayEquals(new String[]{"PlayerB", "PlayerB"}, results.get());

		assertNoMoreResults(results);
	}

	@Test
	void testSuccessCounting() {
		Converter.convert(plugin, "alldifferent",
			new EntitySelectorArgument.ManyPlayers("player1"),
			new EntitySelectorArgument.ManyPlayers("player2"),
			new EntitySelectorArgument.ManyPlayers("player3")
		);

		// Add 3 players
		Player sender = server.addPlayer("PlayerA");
		server.addPlayer("PlayerB");
		server.addPlayer("PlayerC");


		plugin.resetAllDifferentRunCount();
		// There are 3! = 6 permutations of 3 players
		assertEquals(6, server.dispatchBrigadierCommand(sender, "alldifferent @a @a @a"));

		// Command should run 3x3x3 = 27 times total
		assertEquals(27, plugin.resetAllDifferentRunCount());
	}
}
