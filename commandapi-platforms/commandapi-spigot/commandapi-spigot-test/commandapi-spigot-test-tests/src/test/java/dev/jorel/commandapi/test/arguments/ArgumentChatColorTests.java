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
		Mut<ChatColor> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new ChatColorArgument("color"))
			.executesPlayer((player, args) -> {
				results.set((ChatColor) args.get(0));
			})
			.register();

		PlayerMock player = server.addPlayer();
		
		// /test <color>
		// The list of 16 chat colors
		all16ChatColors: {
			int numberOfColors = 0;
			for(ChatColor color : ChatColor.values()) {
				if(color.isColor()) {
					server.dispatchCommand(player, "test " + color.name().toLowerCase());
					assertEquals(color, results.get());
					numberOfColors++;
				}
			}
			
			assertEquals(16, numberOfColors);
		}
		
		// /test reset
		server.dispatchCommand(player, "test reset");
		assertEquals(ChatColor.RESET, results.get());
		
		// /test <format>
		// Fails because only 'reset' and the 16 chat colors are permitted
		for(ChatColor color : ChatColor.values()) {
			if(color.isFormat() && !color.equals(ChatColor.RESET)) {
				String colorName = color.name().toLowerCase();
				final String command = "test " + colorName;
				final int errorPosition = command.length();
				final String truncatedCommand;
				if (command.length() > 10) {
					truncatedCommand = "..." + command.substring(command.length() - 10, command.length());
				} else {
					truncatedCommand = command;
				}
				assertCommandFailsWith(player, command, String.format("Unknown color '%s' at position %d: %s<--[HERE]", colorName, errorPosition, truncatedCommand));
			}
		}

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
