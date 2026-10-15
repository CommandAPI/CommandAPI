package dev.jorel.commandapi.test.arguments;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.TimeArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;

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

		PlayerMock player = server.addPlayer();
		
		// /test 120
		server.dispatchCommand(player, "test 120");
		assertEquals(120, results.get());
		
		// /test 120t
		server.dispatchCommand(player, "test 120t");
		assertEquals(120, results.get());
		
		// /test 120s
		server.dispatchCommand(player, "test 120s");
		assertEquals(2400, results.get());
		
		// /test 0.82s
		server.dispatchCommand(player, "test 0.82s");
		assertEquals(16, results.get());

		// /test .5d
		server.dispatchCommand(player, "test .5d");
		assertEquals(12000, results.get());
		
		// /test 0
		server.dispatchCommand(player, "test 0");
		assertEquals(0, results.get());

		// /test -2
		// Fails because -2 is negative (ticks can only be 0 or greater)
		assertCommandFailsWith(player, "test -2", "The tick count must not be less than 0: found -2 at position 7: test -2<--[HERE]");
		
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

		PlayerMock player = server.addPlayer();

		// /test
		assertEquals(List.of(), server.getSuggestions(player, "test "));
		
		// /test 1
		assertEquals(List.of("d", "s", "t"), server.getSuggestions(player, "test 1"));
	}

}
