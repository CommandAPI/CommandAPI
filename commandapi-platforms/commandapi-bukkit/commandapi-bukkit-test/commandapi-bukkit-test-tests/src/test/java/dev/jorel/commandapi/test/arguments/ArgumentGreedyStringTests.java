package dev.jorel.commandapi.test.arguments;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.exceptions.GreedyArgumentException;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the {@link GreedyStringArgument}
 */
class ArgumentGreedyStringTests extends TestBase {

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
	void executionTestWithGreedyStringArgument() {
		Mut<String> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new GreedyStringArgument("value"))
			.executesPlayer((player, args) -> {
				results.set((String) args.get(0));
			})
			.register();

		PlayerMock player = server.addPlayer();

		// /test Hello World
		server.dispatchCommand(player, "test Hello World");
		assertEquals("Hello World", results.get());

		// /test Hello
		server.dispatchCommand(player, "test Hello");
		assertEquals("Hello", results.get());

		// /test ""*`?#+*!"§
		server.dispatchCommand(player, "test \"\"*`?#+*!\"§");
		assertEquals("\"\"*`?#+*!\"§", results.get());

		// /test こんにちは
		server.dispatchCommand(player, "test こんにちは");
		assertEquals("こんにちは", results.get());

		assertNoMoreResults(results);

	}

	@Test
	void exceptionTestWithGreedyStringArgument() {

		assertThrows(GreedyArgumentException.class, () -> new CommandAPICommand("test")
			.withArguments(new GreedyStringArgument("greedy"))
			.withArguments(new EntitySelectorArgument.OnePlayer("player"))
			.executesPlayer(P_EXEC)
			.register());

	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithGreedyStringArgument() {
		new CommandAPICommand("test")
			.withArguments(new GreedyStringArgument("value"))
			.executesPlayer(P_EXEC)
			.register();

		PlayerMock player = server.addPlayer();

		// /test
		assertEquals(List.of(), server.getSuggestions(player, "test "));
	}

}
