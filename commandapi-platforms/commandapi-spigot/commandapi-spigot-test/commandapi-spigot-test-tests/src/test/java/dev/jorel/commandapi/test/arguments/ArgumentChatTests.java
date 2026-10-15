package dev.jorel.commandapi.test.arguments;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ChatArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

/**
 * Tests for the Spigot {@link ChatArgument}
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
	void executionTestWithSpigotChatArgument() {
		Mut<BaseComponent[]> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new ChatArgument("chat"))
			.executesPlayer((player, args) -> {
				results.set((BaseComponent[]) args.get("chat"));
			})
			.register();

		PlayerMock player = server.addPlayer("APlayer");
		player.setOp(true);

		BaseComponent expected = new TextComponent("Hello, ");
		expected.addExtra(new TextComponent("APlayer"));
		expected.addExtra(new TextComponent("!"));

		// /test Hello, @p!
		server.dispatchCommand(player, "test Hello, @p!");
		assertArrayEquals(new BaseComponent[] { expected }, results.get());

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithSpigotChatArgumentNoOp() {
		Mut<BaseComponent[]> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new ChatArgument("chat"))
			.executesPlayer((player, args) -> {
				results.set((BaseComponent[]) args.get("chat"));
			})
			.register();

		PlayerMock player = server.addPlayer("APlayer");

		// /test Hello, APlayer!
		server.dispatchCommand(player, "test Hello, APlayer!");
		assertArrayEquals(ComponentSerializer.parse("[\"Hello, APlayer!\"]"), results.get());

		// /test Hello, @p!
		server.dispatchCommand(player, "test Hello, @p!");
		assertArrayEquals(ComponentSerializer.parse("[\"Hello, @p!\"]"), results.get());

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithSpigotChatArgument() {
		new CommandAPICommand("test")
			.withArguments(new ChatArgument("chat"))
			.executesPlayer(P_EXEC)
			.register();

		PlayerMock player = server.addPlayer();

		// /test
		// Should suggest nothing
		assertEquals(List.of(), server.getSuggestions(player, "test "));
	}

}
