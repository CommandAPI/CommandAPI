package dev.jorel.commandapi.test.arguments;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ScoreboardSlotArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import dev.jorel.commandapi.wrappers.ScoreboardSlot;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Tests for the {@link ScoreboardSlotArgument}
 */
class ArgumentScoreboardSlotTests extends TestBase {

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
	void executionTestWithScoreboardSlotArgument() {
		Mut<ScoreboardSlot> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new ScoreboardSlotArgument("slot"))
			.executesPlayer((player, args) -> {
				results.set(args.getUnchecked(0));
			})
			.register();

		Player player = addPlayer();

		// /test below_name
		assertStoresResult(player, "test below_name", results, ScoreboardSlot.BELOW_NAME);

		// /test list
		assertStoresResult(player, "test list", results, ScoreboardSlot.PLAYER_LIST);

		// /test sidebar
		assertStoresResult(player, "test sidebar", results, ScoreboardSlot.SIDEBAR);

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithScoreboardSlotTeamsArgument() {
		Mut<ScoreboardSlot> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new ScoreboardSlotArgument("slot"))
			.executesPlayer((player, args) -> {
				results.set(args.getUnchecked(0));
			})
			.register();

		Player player = addPlayer();

		for (ChatColor color : ChatColor.values()) {
			if (color.isColor()) {
				ScoreboardSlot expected = ScoreboardSlot.ofTeamColor(color);

				assertStoresResult(player, "test sidebar.team." + color.name().toLowerCase(), results, expected);
			}
		}

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithScoreboardSlotArgument() {
		new CommandAPICommand("test")
			.withArguments(new ScoreboardSlotArgument("slot"))
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		List<String> expectedSuggestions = Stream.concat(
			Arrays.stream(ChatColor.values())
				.filter(ChatColor::isColor)
				.map(c -> "sidebar.team." + c.name().toLowerCase()),
			List.of("below_name", "list", "sidebar").stream()).sorted().toList();

		// /test
		assertCommandSuggests(player, "test ", expectedSuggestions);
	}
}
