package dev.jorel.commandapi.test.arguments;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.Axis;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.AxisArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;

/**
 * Tests for the {@link AxisArgument}
 */
class ArgumentAxisTests extends TestBase {

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

	@SuppressWarnings("unchecked")
	@Test
	void executionTestWithAxisArgument() {
		Mut<EnumSet<Axis>> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new AxisArgument("axes"))
			.executesPlayer((player, args) -> {
				results.set((EnumSet<Axis>) args.get(0));
			})
			.register();

		PlayerMock player = server.addPlayer();

		// /test x
		server.dispatchCommand(player, "test x");
		assertEquals(EnumSet.of(Axis.X), results.get());

		// /test xy
		server.dispatchCommand(player, "test xy");
		assertEquals(EnumSet.of(Axis.X, Axis.Y), results.get());

		// /test xyz
		server.dispatchCommand(player, "test xyz");
		assertEquals(EnumSet.of(Axis.X, Axis.Y, Axis.Z), results.get());

		// /test xyz
		server.dispatchCommand(player, "test zyx");
		assertEquals(EnumSet.of(Axis.X, Axis.Y, Axis.Z), results.get());

		// /test w
		assertCommandFailsWith(player, "test w", "Invalid swizzle: expected combination of 'x', 'y' and 'z' at position 6: test w<--[HERE]");

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithAxisArgument() {
		new CommandAPICommand("test")
			.withArguments(new AxisArgument("axis"))
			.executesPlayer(P_EXEC)
			.register();

		PlayerMock player = server.addPlayer();

		// /test
		// The axis argument doesn't have any suggestions
		assertEquals(List.of(), server.getSuggestions(player, "test "));

		// /test x
		// The axis argument doesn't have any suggestions
		assertEquals(List.of(), server.getSuggestions(player, "test x"));
	}

}
