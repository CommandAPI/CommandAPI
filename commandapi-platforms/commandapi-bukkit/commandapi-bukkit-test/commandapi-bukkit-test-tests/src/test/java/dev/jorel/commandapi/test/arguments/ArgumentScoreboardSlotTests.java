package dev.jorel.commandapi.test.arguments;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.bukkit.ChatColor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ScoreboardSlotArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import dev.jorel.commandapi.wrappers.ScoreboardSlot;

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

		PlayerMock player = server.addPlayer();

		// /test below_name
		server.dispatchCommand(player, "test below_name");
		assertEquals(ScoreboardSlot.BELOW_NAME, results.get());

		// /test list
		server.dispatchCommand(player, "test list");
		assertEquals(ScoreboardSlot.PLAYER_LIST, results.get());

		// /test sidebar
		server.dispatchCommand(player, "test sidebar");
		assertEquals(ScoreboardSlot.SIDEBAR, results.get());

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

		PlayerMock player = server.addPlayer();

		for (ChatColor color : ChatColor.values()) {
			if (color.isColor()) {
				ScoreboardSlot expected = ScoreboardSlot.ofTeamColor(color);

				server.dispatchCommand(player, "test sidebar.team." + color.name().toLowerCase());
				assertEquals(expected, results.get());
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

		PlayerMock player = server.addPlayer();

		List<String> expectedSuggestions = Stream.concat(
				Arrays.stream(ChatColor.values())
				.filter(ChatColor::isColor)
				.map(c -> "sidebar.team." + c.name().toLowerCase()),
			List.of("below_name", "list", "sidebar").stream()).sorted().toList();
		
		// /test
		assertEquals(expectedSuggestions, server.getSuggestions(player, "test "));
	}

}
