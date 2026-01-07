package dev.jorel.commandapi.test.arguments;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.exceptions.GreedyArgumentException;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

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

		Player player = addPlayer();

		// /test Hello World
		assertStoresResult(player, "test Hello World", results, "Hello World");

		// /test Hello
		assertStoresResult(player, "test Hello", results, "Hello");

		// /test ""*`?#+*!"§
		assertStoresResult(player, "test \"\"*`?#+*!\"§", results, "\"\"*`?#+*!\"§");

		// /test こんにちは
		assertStoresResult(player, "test こんにちは", results, "こんにちは");

		assertNoMoreResults(results);
	}

	@RepeatedTest(10)
	void executionTestWithRandomInput() {
		Mut<String> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new GreedyStringArgument("value"))
			.executesPlayer((player, args) -> {
				results.set((String) args.get(0));
			})
			.register();

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 100; i++) {
			builder.append((char) ThreadLocalRandom.current().nextInt(32, 127));
		}
		String stringArgValue = builder.toString();

		Player player = addPlayer("APlayer");
		assertStoresResult(player, "test " + stringArgValue, results, stringArgValue);
	}

	@Test
	void exceptionTestWithGreedyStringArgument() {

		assertThrows(GreedyArgumentException.class, () -> new CommandAPICommand("test")
			.withArguments(new GreedyStringArgument("greedy"))
			.withArguments(new StringArgument("player"))
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

		Player player = addPlayer();

		// /test
		assertNoSuggestions(player, "test ");
	}
}
