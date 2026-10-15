package dev.jorel.commandapi.test.arguments;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.bukkit.ChatColor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ChatColorArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Tests for the {@link ChatColorArgument}
 */
class ArgumentChatColorTests extends TestBase {

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

	@SuppressWarnings("unused")
	@Test
	void executionTestWithChatColorArgument() {
		Mut<NamedTextColor> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new ChatColorArgument("color"))
			.executesPlayer((player, args) -> {
				results.set((NamedTextColor) args.get(0));
			})
			.register();

		PlayerMock player = server.addPlayer();

		// /test <color>
		// The list of 16 chat colors
		for (String str : new String[] {
			"black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple",
			"gold", "gray", "dark_gray", "blue", "green", "aqua", "red", "light_purple",
			"yellow", "white"
		}) {
			server.dispatchCommand(player, "test " + str);
			assertEquals(NamedTextColor.NAMES.value(str), results.get());
		}

		// /test reset
		// Paper's NamedTextColor argument type has no color for reset
		assertCommandFailsWith(player, "test reset", "Unknown color 'reset' at position 10: test reset<--[HERE]");

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithChatColorArgument() {
		new CommandAPICommand("test")
			.withArguments(new ChatColorArgument("color"))
			.executesPlayer(P_EXEC)
			.register();

		PlayerMock player = server.addPlayer();

		// /test d
		assertEquals(List.of("dark_aqua", "dark_blue", "dark_gray", "dark_green", "dark_purple", "dark_red"), server.getSuggestions(player, "test d"));
	}

}
