package dev.jorel.commandapi.test.wrappers;

import dev.jorel.commandapi.wrappers.ScoreboardSlot;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ScoreboardSlotWrapperTests {

	private DisplaySlot getNamedDisplaySlot(String name) {
		final String nameNoTeam = name.replace("TEAM_", "");
		for (DisplaySlot slot : DisplaySlot.values()) {
			if (slot.name().equals(name)) {
				return slot;
			} else if (slot.name().equals(nameNoTeam)) {
				return slot;
			}
		}
		// Should never occur in the testing environment
		throw new IllegalStateException("Can't find " + name + " in " + Arrays.stream(DisplaySlot.values()).map(Enum::name).collect(Collectors.joining(", ")));
	}

	@Test
	void testScoreboardSlotInternalMinecraft() {
		assertEquals(ScoreboardSlot.PLAYER_LIST, ScoreboardSlot.ofMinecraft(0));
		assertEquals(ScoreboardSlot.SIDEBAR, ScoreboardSlot.ofMinecraft(1));
		assertEquals(ScoreboardSlot.BELOW_NAME, ScoreboardSlot.ofMinecraft(2));
		assertEquals(ScoreboardSlot.SIDEBAR_TEAM_BLACK, ScoreboardSlot.ofMinecraft(3));
		assertEquals(ScoreboardSlot.SIDEBAR_TEAM_DARK_BLUE, ScoreboardSlot.ofMinecraft(4));
		assertEquals(ScoreboardSlot.SIDEBAR_TEAM_DARK_GREEN, ScoreboardSlot.ofMinecraft(5));
		assertEquals(ScoreboardSlot.SIDEBAR_TEAM_DARK_AQUA, ScoreboardSlot.ofMinecraft(6));
		assertEquals(ScoreboardSlot.SIDEBAR_TEAM_DARK_RED, ScoreboardSlot.ofMinecraft(7));
		assertEquals(ScoreboardSlot.SIDEBAR_TEAM_DARK_PURPLE, ScoreboardSlot.ofMinecraft(8));
		assertEquals(ScoreboardSlot.SIDEBAR_TEAM_GOLD, ScoreboardSlot.ofMinecraft(9));
		assertEquals(ScoreboardSlot.SIDEBAR_TEAM_GRAY, ScoreboardSlot.ofMinecraft(10));
		assertEquals(ScoreboardSlot.SIDEBAR_TEAM_DARK_GRAY, ScoreboardSlot.ofMinecraft(11));
		assertEquals(ScoreboardSlot.SIDEBAR_TEAM_BLUE, ScoreboardSlot.ofMinecraft(12));
		assertEquals(ScoreboardSlot.SIDEBAR_TEAM_GREEN, ScoreboardSlot.ofMinecraft(13));
		assertEquals(ScoreboardSlot.SIDEBAR_TEAM_AQUA, ScoreboardSlot.ofMinecraft(14));
		assertEquals(ScoreboardSlot.SIDEBAR_TEAM_RED, ScoreboardSlot.ofMinecraft(15));
		assertEquals(ScoreboardSlot.SIDEBAR_TEAM_LIGHT_PURPLE, ScoreboardSlot.ofMinecraft(16));
		assertEquals(ScoreboardSlot.SIDEBAR_TEAM_YELLOW, ScoreboardSlot.ofMinecraft(17));
		assertEquals(ScoreboardSlot.SIDEBAR_TEAM_WHITE, ScoreboardSlot.ofMinecraft(18));
	}

	@Test
	void testScoreboardSlotOf() {
		assertEquals(ScoreboardSlot.PLAYER_LIST, ScoreboardSlot.of(DisplaySlot.PLAYER_LIST));
		assertEquals(ScoreboardSlot.SIDEBAR, ScoreboardSlot.of(DisplaySlot.SIDEBAR));
		assertEquals(ScoreboardSlot.BELOW_NAME, ScoreboardSlot.of(DisplaySlot.BELOW_NAME));

		if (DisplaySlot.values().length > 3) {
			assertEquals(ScoreboardSlot.SIDEBAR_TEAM_BLACK, ScoreboardSlot.of(getNamedDisplaySlot("SIDEBAR_TEAM_BLACK")));
			assertEquals(ScoreboardSlot.SIDEBAR_TEAM_DARK_BLUE, ScoreboardSlot.of(getNamedDisplaySlot("SIDEBAR_TEAM_DARK_BLUE")));
			assertEquals(ScoreboardSlot.SIDEBAR_TEAM_DARK_GREEN, ScoreboardSlot.of(getNamedDisplaySlot("SIDEBAR_TEAM_DARK_GREEN")));
			assertEquals(ScoreboardSlot.SIDEBAR_TEAM_DARK_AQUA, ScoreboardSlot.of(getNamedDisplaySlot("SIDEBAR_TEAM_DARK_AQUA")));
			assertEquals(ScoreboardSlot.SIDEBAR_TEAM_DARK_RED, ScoreboardSlot.of(getNamedDisplaySlot("SIDEBAR_TEAM_DARK_RED")));
			assertEquals(ScoreboardSlot.SIDEBAR_TEAM_DARK_PURPLE, ScoreboardSlot.of(getNamedDisplaySlot("SIDEBAR_TEAM_DARK_PURPLE")));
			assertEquals(ScoreboardSlot.SIDEBAR_TEAM_GOLD, ScoreboardSlot.of(getNamedDisplaySlot("SIDEBAR_TEAM_GOLD")));
			assertEquals(ScoreboardSlot.SIDEBAR_TEAM_GRAY, ScoreboardSlot.of(getNamedDisplaySlot("SIDEBAR_TEAM_GRAY")));
			assertEquals(ScoreboardSlot.SIDEBAR_TEAM_DARK_GRAY, ScoreboardSlot.of(getNamedDisplaySlot("SIDEBAR_TEAM_DARK_GRAY")));
			assertEquals(ScoreboardSlot.SIDEBAR_TEAM_BLUE, ScoreboardSlot.of(getNamedDisplaySlot("SIDEBAR_TEAM_BLUE")));
			assertEquals(ScoreboardSlot.SIDEBAR_TEAM_GREEN, ScoreboardSlot.of(getNamedDisplaySlot("SIDEBAR_TEAM_GREEN")));
			assertEquals(ScoreboardSlot.SIDEBAR_TEAM_AQUA, ScoreboardSlot.of(getNamedDisplaySlot("SIDEBAR_TEAM_AQUA")));
			assertEquals(ScoreboardSlot.SIDEBAR_TEAM_RED, ScoreboardSlot.of(getNamedDisplaySlot("SIDEBAR_TEAM_RED")));
			assertEquals(ScoreboardSlot.SIDEBAR_TEAM_LIGHT_PURPLE, ScoreboardSlot.of(getNamedDisplaySlot("SIDEBAR_TEAM_LIGHT_PURPLE")));
			assertEquals(ScoreboardSlot.SIDEBAR_TEAM_YELLOW, ScoreboardSlot.of(getNamedDisplaySlot("SIDEBAR_TEAM_YELLOW")));
			assertEquals(ScoreboardSlot.SIDEBAR_TEAM_WHITE, ScoreboardSlot.of(getNamedDisplaySlot("SIDEBAR_TEAM_WHITE")));
		}
	}

	@Test
	void testScoreboardSlotOfTeamColor() {
		assertEquals(ScoreboardSlot.SIDEBAR_TEAM_BLACK, ScoreboardSlot.ofTeamColor(ChatColor.BLACK));
		assertEquals(ScoreboardSlot.SIDEBAR_TEAM_DARK_BLUE, ScoreboardSlot.ofTeamColor(ChatColor.DARK_BLUE));
		assertEquals(ScoreboardSlot.SIDEBAR_TEAM_DARK_GREEN, ScoreboardSlot.ofTeamColor(ChatColor.DARK_GREEN));
		assertEquals(ScoreboardSlot.SIDEBAR_TEAM_DARK_AQUA, ScoreboardSlot.ofTeamColor(ChatColor.DARK_AQUA));
		assertEquals(ScoreboardSlot.SIDEBAR_TEAM_DARK_RED, ScoreboardSlot.ofTeamColor(ChatColor.DARK_RED));
		assertEquals(ScoreboardSlot.SIDEBAR_TEAM_DARK_PURPLE, ScoreboardSlot.ofTeamColor(ChatColor.DARK_PURPLE));
		assertEquals(ScoreboardSlot.SIDEBAR_TEAM_GOLD, ScoreboardSlot.ofTeamColor(ChatColor.GOLD));
		assertEquals(ScoreboardSlot.SIDEBAR_TEAM_GRAY, ScoreboardSlot.ofTeamColor(ChatColor.GRAY));
		assertEquals(ScoreboardSlot.SIDEBAR_TEAM_DARK_GRAY, ScoreboardSlot.ofTeamColor(ChatColor.DARK_GRAY));
		assertEquals(ScoreboardSlot.SIDEBAR_TEAM_BLUE, ScoreboardSlot.ofTeamColor(ChatColor.BLUE));
		assertEquals(ScoreboardSlot.SIDEBAR_TEAM_GREEN, ScoreboardSlot.ofTeamColor(ChatColor.GREEN));
		assertEquals(ScoreboardSlot.SIDEBAR_TEAM_AQUA, ScoreboardSlot.ofTeamColor(ChatColor.AQUA));
		assertEquals(ScoreboardSlot.SIDEBAR_TEAM_RED, ScoreboardSlot.ofTeamColor(ChatColor.RED));
		assertEquals(ScoreboardSlot.SIDEBAR_TEAM_LIGHT_PURPLE, ScoreboardSlot.ofTeamColor(ChatColor.LIGHT_PURPLE));
		assertEquals(ScoreboardSlot.SIDEBAR_TEAM_YELLOW, ScoreboardSlot.ofTeamColor(ChatColor.YELLOW));
		assertEquals(ScoreboardSlot.SIDEBAR_TEAM_WHITE, ScoreboardSlot.ofTeamColor(ChatColor.WHITE));

		assertEquals(ScoreboardSlot.SIDEBAR, ScoreboardSlot.ofTeamColor(ChatColor.MAGIC));
		assertEquals(ScoreboardSlot.SIDEBAR, ScoreboardSlot.ofTeamColor(ChatColor.BOLD));
		assertEquals(ScoreboardSlot.SIDEBAR, ScoreboardSlot.ofTeamColor(ChatColor.STRIKETHROUGH));
		assertEquals(ScoreboardSlot.SIDEBAR, ScoreboardSlot.ofTeamColor(ChatColor.UNDERLINE));
		assertEquals(ScoreboardSlot.SIDEBAR, ScoreboardSlot.ofTeamColor(ChatColor.ITALIC));
		assertEquals(ScoreboardSlot.SIDEBAR, ScoreboardSlot.ofTeamColor(ChatColor.RESET));
	}

	@Test
	void testScoreboardSlotHasTeamColor() {
		assertFalse(ScoreboardSlot.PLAYER_LIST.hasTeamColor());
		assertFalse(ScoreboardSlot.SIDEBAR.hasTeamColor());
		assertFalse(ScoreboardSlot.BELOW_NAME.hasTeamColor());

		assertTrue(ScoreboardSlot.SIDEBAR_TEAM_BLACK.hasTeamColor());
		assertTrue(ScoreboardSlot.SIDEBAR_TEAM_DARK_BLUE.hasTeamColor());
		assertTrue(ScoreboardSlot.SIDEBAR_TEAM_DARK_GREEN.hasTeamColor());
		assertTrue(ScoreboardSlot.SIDEBAR_TEAM_DARK_AQUA.hasTeamColor());
		assertTrue(ScoreboardSlot.SIDEBAR_TEAM_DARK_RED.hasTeamColor());
		assertTrue(ScoreboardSlot.SIDEBAR_TEAM_DARK_PURPLE.hasTeamColor());
		assertTrue(ScoreboardSlot.SIDEBAR_TEAM_GOLD.hasTeamColor());
		assertTrue(ScoreboardSlot.SIDEBAR_TEAM_GRAY.hasTeamColor());
		assertTrue(ScoreboardSlot.SIDEBAR_TEAM_DARK_GRAY.hasTeamColor());
		assertTrue(ScoreboardSlot.SIDEBAR_TEAM_BLUE.hasTeamColor());
		assertTrue(ScoreboardSlot.SIDEBAR_TEAM_GREEN.hasTeamColor());
		assertTrue(ScoreboardSlot.SIDEBAR_TEAM_AQUA.hasTeamColor());
		assertTrue(ScoreboardSlot.SIDEBAR_TEAM_RED.hasTeamColor());
		assertTrue(ScoreboardSlot.SIDEBAR_TEAM_LIGHT_PURPLE.hasTeamColor());
		assertTrue(ScoreboardSlot.SIDEBAR_TEAM_YELLOW.hasTeamColor());
		assertTrue(ScoreboardSlot.SIDEBAR_TEAM_WHITE.hasTeamColor());
	}

	@Test
	void testScoreboardSlotGetDisplaySlot() {
		assertEquals(DisplaySlot.PLAYER_LIST, ScoreboardSlot.PLAYER_LIST.getDisplaySlot());
		assertEquals(DisplaySlot.SIDEBAR, ScoreboardSlot.SIDEBAR.getDisplaySlot());
		assertEquals(DisplaySlot.BELOW_NAME, ScoreboardSlot.BELOW_NAME.getDisplaySlot());

		if (DisplaySlot.values().length > 3) {
			assertEquals(getNamedDisplaySlot("SIDEBAR_TEAM_BLACK"), ScoreboardSlot.SIDEBAR_TEAM_BLACK.getDisplaySlot());
			assertEquals(getNamedDisplaySlot("SIDEBAR_TEAM_DARK_BLUE"), ScoreboardSlot.SIDEBAR_TEAM_DARK_BLUE.getDisplaySlot());
			assertEquals(getNamedDisplaySlot("SIDEBAR_TEAM_DARK_GREEN"), ScoreboardSlot.SIDEBAR_TEAM_DARK_GREEN.getDisplaySlot());
			assertEquals(getNamedDisplaySlot("SIDEBAR_TEAM_DARK_AQUA"), ScoreboardSlot.SIDEBAR_TEAM_DARK_AQUA.getDisplaySlot());
			assertEquals(getNamedDisplaySlot("SIDEBAR_TEAM_DARK_RED"), ScoreboardSlot.SIDEBAR_TEAM_DARK_RED.getDisplaySlot());
			assertEquals(getNamedDisplaySlot("SIDEBAR_TEAM_DARK_PURPLE"), ScoreboardSlot.SIDEBAR_TEAM_DARK_PURPLE.getDisplaySlot());
			assertEquals(getNamedDisplaySlot("SIDEBAR_TEAM_GOLD"), ScoreboardSlot.SIDEBAR_TEAM_GOLD.getDisplaySlot());
			assertEquals(getNamedDisplaySlot("SIDEBAR_TEAM_GRAY"), ScoreboardSlot.SIDEBAR_TEAM_GRAY.getDisplaySlot());
			assertEquals(getNamedDisplaySlot("SIDEBAR_TEAM_DARK_GRAY"), ScoreboardSlot.SIDEBAR_TEAM_DARK_GRAY.getDisplaySlot());
			assertEquals(getNamedDisplaySlot("SIDEBAR_TEAM_BLUE"), ScoreboardSlot.SIDEBAR_TEAM_BLUE.getDisplaySlot());
			assertEquals(getNamedDisplaySlot("SIDEBAR_TEAM_GREEN"), ScoreboardSlot.SIDEBAR_TEAM_GREEN.getDisplaySlot());
			assertEquals(getNamedDisplaySlot("SIDEBAR_TEAM_AQUA"), ScoreboardSlot.SIDEBAR_TEAM_AQUA.getDisplaySlot());
			assertEquals(getNamedDisplaySlot("SIDEBAR_TEAM_RED"), ScoreboardSlot.SIDEBAR_TEAM_RED.getDisplaySlot());
			assertEquals(getNamedDisplaySlot("SIDEBAR_TEAM_LIGHT_PURPLE"), ScoreboardSlot.SIDEBAR_TEAM_LIGHT_PURPLE.getDisplaySlot());
			assertEquals(getNamedDisplaySlot("SIDEBAR_TEAM_YELLOW"), ScoreboardSlot.SIDEBAR_TEAM_YELLOW.getDisplaySlot());
			assertEquals(getNamedDisplaySlot("SIDEBAR_TEAM_WHITE"), ScoreboardSlot.SIDEBAR_TEAM_WHITE.getDisplaySlot());
		}
	}
}
