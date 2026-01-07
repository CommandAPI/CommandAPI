package dev.jorel.commandapi.test.arguments;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.FunctionArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import dev.jorel.commandapi.wrappers.FunctionWrapper;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@link FunctionArgument}
 */
class ArgumentFunctionTests extends TestBase {

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
	void executionTestWithFunctionArgument() {
		Mut<FunctionWrapper[]> results = Mut.of();
		Mut<String> sayResults = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new FunctionArgument("function"))
			.executesPlayer((player, args) -> {
				results.set((FunctionWrapper[]) args.get("function"));
			})
			.register();

		new CommandAPICommand("mysay")
			.withArguments(new GreedyStringArgument("message"))
			.executesPlayer((player, args) -> {
				sayResults.set(args.getUnchecked("message"));
			})
			.register();

		Player player = addPlayer();

		// Declare our functions on the server
		getServer().addFunction(new NamespacedKey("ns", "myfunc"), List.of("mysay hi"));

		// Run the /test command
		getServer().dispatchCommand(player, "test ns:myfunc");

		// Check that the FunctionArgument has one entry and it hasn't run the /mysay
		// command
		FunctionWrapper[] result = results.get();
		assertEquals(1, result.length);
		assertNoMoreResults(sayResults);

		// Run the function (which should run the /mysay command)
		result[0].run();
		assertEquals(1, getServer().popFunctionCallbackResult());

		// Check that /mysay was run successfully...
		assertEquals("hi", sayResults.get());

		assertNoMoreResults(results);
		assertNoMoreResults(sayResults);
	}

	@Test
	void executionTestWithFunctionArgumentTag() {
		Mut<FunctionWrapper[]> results = Mut.of();
		Mut<String> sayResults = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new FunctionArgument("function"))
			.executesPlayer((player, args) -> {
				results.set((FunctionWrapper[]) args.get("function"));
			})
			.register();

		new CommandAPICommand("mysay")
			.withArguments(new GreedyStringArgument("message"))
			.executesPlayer((player, args) -> {
				sayResults.set(args.getUnchecked("message"));
			})
			.register();

		Player player = addPlayer();

		// Declare our functions on the server
		getServer().addTag(new NamespacedKey("ns", "mytag"), List.of(
			List.of("mysay hi", "mysay bye"),
			List.of("mysay hello", "mysay world")
		));

		// Run the /test command
		getServer().dispatchCommand(player, "test #ns:mytag");

		// Check that the FunctionArgument has one entry and it hasn't run the /mysay
		// command
		FunctionWrapper[] result = results.get();
		assertEquals(2, result.length);
		assertNoMoreResults(sayResults);

		// Run the function (which should run the /mysay command)
		for (FunctionWrapper wrapper : result) {
			wrapper.run();

			assertEquals(1, getServer().popFunctionCallbackResult());
			assertEquals(1, getServer().popFunctionCallbackResult());
		}

		// Check that /mysay was run successfully...
		assertEquals("hi", sayResults.get());
		assertEquals("bye", sayResults.get());
		assertEquals("hello", sayResults.get());
		assertEquals("world", sayResults.get());

		assertNoMoreResults(results);
		assertNoMoreResults(sayResults);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithFunctionArgument() {
		new CommandAPICommand("test")
			.withArguments(new FunctionArgument("function"))
			.executesPlayer(P_EXEC)
			.register();

		new CommandAPICommand("mysay")
			.withArguments(new GreedyStringArgument("message"))
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		// Declare our functions on the server
		getServer().addFunction(new NamespacedKey("ns", "myfunc"), List.of("mysay hi"));
		getServer().addFunction(new NamespacedKey("mynamespace", "myotherfunc"), List.of("mysay bye"));

		// /test
		// Should suggest mynamespace:myotherfunc and ns:myfunc
		assertCommandSuggests(player, "test ", "mynamespace:myotherfunc", "ns:myfunc");
	}

	@Test
	void suggestionTestWithFunctionArgumentTag() {
		new CommandAPICommand("test")
			.withArguments(new FunctionArgument("function"))
			.executesPlayer(P_EXEC)
			.register();

		new CommandAPICommand("mysay")
			.withArguments(new GreedyStringArgument("message"))
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		// Declare our functions on the server
		getServer().addFunction(new NamespacedKey("ns", "myfunc"), List.of("mysay hi"));
		getServer().addFunction(new NamespacedKey("mynamespace", "myotherfunc"), List.of("mysay bye"));
		getServer().addTag(new NamespacedKey("ns", "mytag"), List.of(
			List.of("mysay hi", "mysay bye"),
			List.of("mysay hello", "mysay world")
		));
		getServer().addTag(new NamespacedKey("namespace", "myothertag"), List.of(
			List.of("mysay hi", "mysay bye"),
			List.of("mysay hello", "mysay world")
		));

		// /test
		// Should suggest #namespace:myothertag, #ns:mytag, mynamespace:myotherfunc and ns:myfunc
		assertCommandSuggests(player, "test ", "#namespace:myothertag", "#ns:mytag", "mynamespace:myotherfunc", "ns:myfunc");
	}

	/********************************
	 * Function commands list tests *
	 ********************************/

	@Test
	void commandListTestWithFunctionArgument() {
		Mut<FunctionWrapper[]> results = Mut.of();
		Mut<String> sayResults = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new FunctionArgument("function"))
			.executesPlayer((player, args) -> {
				results.set((FunctionWrapper[]) args.get("function"));
			})
			.register();

		new CommandAPICommand("mysay")
			.withArguments(new GreedyStringArgument("message"))
			.executesPlayer((player, args) -> {
				sayResults.set(args.getUnchecked("message"));
			})
			.register();

		Player player = addPlayer();

		// Declare our functions on the server
		getServer().addFunction(new NamespacedKey("ns", "myfunc"), List.of("mysay hi", "mysay bye"));

		// Run the /test command
		getServer().dispatchCommand(player, "test ns:myfunc");

		// Check that the FunctionArgument has one entry and it hasn't run the /mysay
		// command
		FunctionWrapper[] result = results.get();
		assertEquals(1, result.length);
		assertNoMoreResults(sayResults);

		assertArrayEquals(new String[]{"mysay hi", "mysay bye"}, result[0].getCommands());

		assertNoMoreResults(results);
		assertNoMoreResults(sayResults);
	}
}
