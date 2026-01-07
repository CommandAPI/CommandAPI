package dev.jorel.commandapi.test.arguments;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ChatComponentArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link ChatComponentArgument}
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
	void executionTestWithChatComponentArgument() {
		Mut<Component> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new ChatComponentArgument("text"))
			.executesPlayer((player, args) -> {
				results.set((Component) args.get(0));
			})
			.register();

		Player player = addPlayer("Skepter");

		// /test [\"[\\\"\\\",{\\\"text ... /CommandAPI\\\"}}]\"]
		// Dev note: See comment at top of file
		assertStoresResult(player, "test " + json, results, GsonComponentSerializer.gson().deserialize(json));

		// /test []
		// Fails due to invalid JSON for a chat component
		assertCommandFailsWith(player, "test []", "Invalid chat component: Not a JSON object: [] at position 8: test []<--[HERE]");

		// /test [\"[\"\",{\"text\":\"Some text with bad quote escaping\"}\"]
		// Fails due to inner quotes not being escaped with a \ character
		assertCommandFailsWith(player, "test [\"[\"\",{\"text\":\"Some text with bad quote escaping\"}\"]", "Invalid chat component: Unterminated array at line 1 column 6 path $[1] at position 11: ...est [\"[\"\",<--[HERE]");

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithChatComponentArgument() {
		new CommandAPICommand("test")
			.withArguments(new ChatComponentArgument("text"))
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		// /test
		// The ChatComponentArgument doesn't have any suggestions
		assertNoSuggestions(player, "test ");

		// /test [
		// The ChatComponentArgument doesn't have any suggestions
		assertNoSuggestions(player, "test [");
	}
}
