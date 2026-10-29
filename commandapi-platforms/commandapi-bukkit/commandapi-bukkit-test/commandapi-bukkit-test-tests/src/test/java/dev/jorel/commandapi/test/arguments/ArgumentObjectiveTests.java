package dev.jorel.commandapi.test.arguments;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ObjectiveArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Objective;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@link ObjectiveArgument}
 */
class ArgumentObjectiveTests extends TestBase {

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
	void executionTestWithObjectiveArgument() {
		Mut<Objective> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new ObjectiveArgument("objective"))
			.executesPlayer((player, args) -> {
				results.set((Objective) args.get(0));
			})
			.register();

		PlayerMock player = server.addPlayer();

		Bukkit.getScoreboardManager().getMainScoreboard().registerNewObjective("test-objective", "dummy");
		Objective testObjective = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("test-objective");

		// /test test-objective
		server.dispatchCommand(player, "test test-objective");
		assertEquals(testObjective, results.get());

		// /test my-objective
		assertCommandFailsWith(player, "test my-objective", "Unknown scoreboard objective 'my-objective'");

		assertNoMoreResults(results);
	}

}
