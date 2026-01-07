package dev.jorel.commandapi.test.arguments;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.AxisArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import org.bukkit.Axis;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

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

		Player player = addPlayer();

		// /test x
		assertStoresResult(player, "test x", results, EnumSet.of(Axis.X));

		// /test xy
		assertStoresResult(player, "test xy", results, EnumSet.of(Axis.X, Axis.Y));

		// /test xyz
		assertStoresResult(player, "test xyz", results, EnumSet.of(Axis.X, Axis.Y, Axis.Z));

		// /test xyz
		assertStoresResult(player, "test zyx", results, EnumSet.of(Axis.X, Axis.Y, Axis.Z));

		// /test w
		assertCommandFailsWith(player, "test w", "Invalid swizzle, expected combination of 'x', 'y' and 'z' at position 6: test w<--[HERE]");

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

		Player player = addPlayer();

		// /test
		// The axis argument doesn't have any suggestions
		assertNoSuggestions(player, "test ");

		// /test x
		// The axis argument doesn't have any suggestions
		assertNoSuggestions(player, "test x");
	}
}
