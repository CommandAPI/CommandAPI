package dev.jorel.commandapi.test.arguments;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.TimeArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link TimeArgument}
 */
@SuppressWarnings("null")
class ArgumentTimeTests extends TestBase {

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
	void executionTestWithTimeArgument() {
		Mut<Integer> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new TimeArgument("time"))
			.executesPlayer((player, args) -> {
				results.set((int) args.get(0));
			})
			.register();

		Player player = addPlayer();

		// /test 120
		assertStoresResult(player, "test 120", results, 120);

		// /test 120t
		assertStoresResult(player, "test 120t", results, 120);

		// /test 120s
		assertStoresResult(player, "test 120s", results, 2400);

		// /test 0.82s
		assertStoresResult(player, "test 0.82s", results, 16);

		// /test .5d
		assertStoresResult(player, "test .5d", results, 12000);

		// /test 0
		assertStoresResult(player, "test 0", results, 0);

		// /test -2
		// Fails because -2 is negative (ticks can only be 0 or greater)
		assertCommandFailsWith(player, "test -2", "The tick count must not be less than 0, found -2 at position 7: test -2<--[HERE]");

		// /test 2x
		// Fails because 'x' is not a valid unit
		assertCommandFailsWith(player, "test 2x", "Invalid unit at position 7: test 2x<--[HERE]");

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithTimeArgument() {
		new CommandAPICommand("test")
			.withArguments(new TimeArgument("color"))
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		// /test
		assertNoSuggestions(player, "test ");

		// /test 1
		assertCommandSuggests(player, "test 1", "d", "s", "t");
	}
}
