package dev.jorel.commandapi.test.arguments;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ChatColorArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

		Player player = addPlayer();

		// /test <color>
		// The list of 16 chat colors
		for (String str : new String[]{
			"black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple",
			"gold", "gray", "dark_gray", "blue", "green", "aqua", "red", "light_purple",
			"yellow", "white"
		}) {
			assertStoresResult(player, "test " + str, results, NamedTextColor.NAMES.value(str));
		}

		// /test reset
		assertStoresResult(player, "test reset", results, NamedTextColor.WHITE);

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

		Player player = addPlayer();

		// /test d
		assertCommandSuggests(player, "test d", "dark_aqua", "dark_blue", "dark_gray", "dark_green", "dark_purple", "dark_red");
	}
}
