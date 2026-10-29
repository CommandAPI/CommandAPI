package dev.jorel.commandapi.test.arguments;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ChatComponentArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

/**
 * Tests for the Spigot {@link ChatComponentArgument}
 */
class ArgumentChatComponentTests extends TestBase {

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
	
	final String json = "[\"%s\"]".formatted("""
		["", {
		    "text": "Once upon a time, there was a guy "
		}, {
		    "text": "Skepter",
		    "color": "light_purple",
		    "hoverEvent": {
		        "action": "show_entity",
		        "value": "Skepter"
		    }
		}, {
		    "text": " and he created the "
		}, {
		    "text": "CommandAPI",
		    "underlined": true,
		    "clickEvent": {
		        "action": "open_url",
		        "value": "https://github.com/JorelAli/CommandAPI"
		    }
		}]
		""".stripIndent().replace("\n", "").replace("\r", "").replace("\"", "\\\""));
	
	// The above, in normal human-readable JSON gets turned into this for command purposes:
	// [\"[\\\"\\\",{\\\"text\\\":\\\"Once upon a time, there was a guy call \\\"},{\\\"text\\\":\\\"Skepter\\\",\\\"color\\\":\\\"light_purple\\\",\\\"hoverEvent\\\":{\\\"action\\\":\\\"show_entity\\\",\\\"value\\\":\\\"Skepter\\\"}},{\\\"text\\\":\\\" and he created the \\\"},{\\\"text\\\":\\\"CommandAPI\\\",\\\"underlined\\\":true,\\\"clickEvent\\\":{\\\"action\\\":\\\"open_url\\\",\\\"value\\\":\\\"https://github.com/JorelAli/CommandAPI\\\"}}]\"]

	/*********
	 * Tests *
	 *********/
	
	@Test
	void executionTestWithSpigotChatComponentArgument() {
		Mut<BaseComponent[]> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new ChatComponentArgument("text"))
			.executesPlayer((player, args) -> {
				results.set((BaseComponent[]) args.get(0));
			})
			.register();

		PlayerMock player = server.addPlayer("Skepter");

		// /test [\"[\\\"\\\",{\\\"text ... /CommandAPI\\\"}}]\"]
		// Dev note: See comment at top of file
		server.dispatchCommand(player, "test " + json);
		assertArrayEquals(ComponentSerializer.parse(json), results.get());
		
		// /test []
		// Fails due to invalid JSON for a chat component
		// Components are parsed as SNBT
		assertCommandFailsWith(player, "test []", "Invalid chat component: Failed to parse either. First: Failed to parse either. First: Not a string; Second: List must have contents; Second: Not a map: [] at position 5: test <--[HERE]");
		
		// /test [\"[\"\",{\"text\":\"Some text with bad quote escaping\"}\"]
		// Fails due to inner quotes not being escaped with a \ character
		assertCommandFailsWith(player, "test [\"[\"\",{\"text\":\"Some text with bad quote escaping\"}\"]", "Expected literal , at position 9: test [\"[\"<--[HERE]");
		
		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithSpigotChatComponentArgument() {
		new CommandAPICommand("test")
			.withArguments(new ChatComponentArgument("text"))
			.executesPlayer(P_EXEC)
			.register();

		PlayerMock player = server.addPlayer();

		// /test
		// The ChatComponentArgument doesn't have any suggestions
		assertEquals(List.of(), server.getSuggestions(player, "test "));

		// /test [
		// Components are parsed as SNBT, so this suggests how to continue the list
		assertEquals(List.of("]", "B", "I", "L"), server.getSuggestions(player, "test ["));
	}

}
