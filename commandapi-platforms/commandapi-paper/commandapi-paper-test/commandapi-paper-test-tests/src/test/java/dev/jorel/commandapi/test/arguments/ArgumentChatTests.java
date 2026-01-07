package dev.jorel.commandapi.test.arguments;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ChatArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link ChatArgument}
 */
class ArgumentChatTests extends TestBase {

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
	void executionTestWithChatArgument() {
		Mut<Component> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new ChatArgument("chat"))
			.executesPlayer((player, args) -> {
				results.set((Component) args.get("chat"));
			})
			.register();

		Player player = addPlayer("APlayer");
		player.setOp(true);

		// /test Hello, @p!
		assertStoresResult(player, "test Hello, @p!", results, GsonComponentSerializer.gson().deserialize("[\"Hello, \",\"APlayer\",\"!\"]"));

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithChatArgumentNoOp() {
		Mut<Component> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new ChatArgument("chat"))
			.executesPlayer((player, args) -> {
				results.set((Component) args.get("chat"));
			})
			.register();

		Player player = addPlayer("APlayer");

		// /test Hello, APlayer!
		assertStoresResult(player, "test Hello, APlayer!", results, GsonComponentSerializer.gson().deserialize("[\"Hello, APlayer!\"]"));

		// /test Hello, @p!
		assertStoresResult(player, "test Hello, @p!", results, GsonComponentSerializer.gson().deserialize("[\"Hello, @p!\"]"));

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithChatArgument() {
		new CommandAPICommand("test")
			.withArguments(new ChatArgument("chat"))
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		// /test
		// Should suggest nothing
		assertNoSuggestions(player, "test ");
	}
}
