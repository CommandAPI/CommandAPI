package dev.jorel.commandapi.test.arguments;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.TeamArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link TeamArgument}
 */
class ArgumentTeamTests extends TestBase {

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
	void executionTestWithTeamArgument() {
		Mut<Team> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new TeamArgument("team"))
			.executesPlayer((player, args) -> {
				results.set((Team) args.get("team"));
			})
			.register();

		Player player = addPlayer();

		Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam("myteam");
		Team myTeam = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("myteam");

		assertStoresResult(player, "test myteam", results, myTeam);

		// /test blah
		// Fails because 'blah' is not a valid Team
		assertCommandFailsWith(player, "test blah", "Unknown team 'blah'");

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithTeamArgument() {
		new CommandAPICommand("test")
			.withArguments(new TeamArgument("team"))
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam("myteam");
		Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam("my_other_team");

		// /test
		// Should suggest a list of all teams
		assertCommandSuggests(player, "test ", "my_other_team", "myteam");
	}
}
