package dev.jorel.commandapi.test;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for command permissions
 */
class CommandPermissionTests extends TestBase {

	/*********
	 * Setup *
	 *********/

	@BeforeEach
	public void setUp() {
		super.setUp();
	}

	@AfterEach
	public void tearDown() {
		super.tearDown();
	}

	/*********
	 * Tests *
	 *********/

	@Test
	void executionTestWithPermissionOnCommand() {
		Mut<Boolean> results = Mut.of();

		new CommandAPICommand("test")
			.withPermission("permission.node")
			.executesPlayer((player, args) -> {
				results.set(true);
			})
			.register();

		Player player = addPlayer();

		// Player doesn't have permission //

		// /test
		assertCommandFailsWith(player, "test", "Unknown or incomplete command, see below for error at position 0: <--[HERE]");

		// Player has permission //

		player.addAttachment(mockPlatform.getPlugin(), "permission.node", true);

		// /test
		assertTrue(getServer().dispatchCommand(player, "test"));
		assertTrue(results.get());

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithPermissionOnArgument() {
		Mut<Boolean> results1 = Mut.of();
		Mut<Boolean> results2 = Mut.of();

		new CommandAPICommand("test")
			.executesPlayer((player, args) -> {
				results1.set(true);
			})
			.register();

		new CommandAPICommand("test")
			.withArguments(new StringArgument("str").withPermission("permission.node"))
			.executesPlayer((player, args) -> {
				results2.set(true);
			})
			.register();

		Player player = addPlayer();

		// Player doesn't have permission //

		// /test
		// (The player should still be able to run /test with no problems)
		assertTrue(getServer().dispatchCommand(player, "test"));
		assertTrue(results1.get());

		// /test hello
		assertCommandFailsWith(player, "test hello", "Incorrect argument for command at position 5: test <--[HERE]");

		// Player has permission //

		player.addAttachment(mockPlatform.getPlugin(), "permission.node", true);

		// /test
		// (The player should still be able to run /test with no problems)
		assertTrue(getServer().dispatchCommand(player, "test"));
		assertTrue(results1.get());

		// /test hello
		assertTrue(getServer().dispatchCommand(player, "test hello"));
		assertTrue(results2.get());

		assertNoMoreResults(results1);
		assertNoMoreResults(results2);
	}

	// Todo: Command trees, subcommands, literal arguments, multiple arguments on the same level etc.
	// Possibly also to-do: check suggestions only show for what you have permission to see?
}
