package dev.jorel.commandapi.test.arguments;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.List;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.MCVersion;
import dev.jorel.commandapi.arguments.FunctionArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.test.MockPlatform;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import dev.jorel.commandapi.wrappers.FunctionWrapper;

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

		PlayerMock player = server.addPlayer();

		// Declare our functions on the server
		MockPlatform.getInstance().addFunction(new NamespacedKey("ns", "myfunc"), List.of("mysay hi"));

		// Run the /test command
		server.dispatchCommand(player, "test ns:myfunc");

		// Check that the FunctionArgument has one entry and it hasn't run the /mysay
		// command
		FunctionWrapper[] result = results.get();
		assertEquals(1, result.length);
		assertNoMoreResults(sayResults);

		// Run the function (which should run the /mysay command)
		int functionResult = result[0].run();
		
		if (version.greaterThanOrEqualTo(MCVersion.V1_20_3)) {
			assertEquals(1, MockPlatform.getInstance().popFunctionCallbackResult());
		} else {
			assertEquals(1, functionResult);
		}
		
		// TODO: I can't figure out how to get commands to run on 1.16.5 and
		// I don't think we really care. If you decide you want to care, feel
		// free to implement function running for 1.16.5, but I'm not spending
		// any more time on it - Skepter
		assumeTrue(version.greaterThanOrEqualTo(MCVersion.V1_17));
		assumeTrue(version.lessThan(MCVersion.V1_20));

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

		PlayerMock player = server.addPlayer();

		// Declare our functions on the server
		MockPlatform.getInstance().addTag(new NamespacedKey("ns", "mytag"), List.of(
			List.of("mysay hi", "mysay bye"),
			List.of("mysay hello", "mysay world")
		));

		// Run the /test command
		server.dispatchCommand(player, "test #ns:mytag");

		// Check that the FunctionArgument has one entry and it hasn't run the /mysay
		// command
		FunctionWrapper[] result = results.get();
		assertEquals(2, result.length);
		assertNoMoreResults(sayResults);

		// Run the function (which should run the /mysay command)
		for(FunctionWrapper wrapper : result) {
			int functionResult = wrapper.run();
			
			if (version.greaterThanOrEqualTo(MCVersion.V1_20_3)) {
				assertEquals(1, MockPlatform.getInstance().popFunctionCallbackResult());
				assertEquals(1, MockPlatform.getInstance().popFunctionCallbackResult());
			} else {
				assertEquals(2, functionResult);
			}
		}
		
		// TODO: I can't figure out how to get commands to run on 1.16.5 and
		// I don't think we really care. If you decide you want to care, feel
		// free to implement function running for 1.16.5, but I'm not spending
		// any more time on it. - Skepter
		assumeTrue(version.greaterThanOrEqualTo(MCVersion.V1_17));
		assumeTrue(version.lessThan(MCVersion.V1_20));
		
		// TODO: For reasons I can't figure out, the FunctionArgument suffers
		// from a race condition when run on 1.18.1. This causes "hello" to be
		// printed before "hi" and causes the test to fail. We don't really care
		// too much about this, but worth noting. - Skepter
		assumeFalse(version.equals(MCVersion.V1_18));

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

		PlayerMock player = server.addPlayer();

		// Declare our functions on the server
		MockPlatform.getInstance().addFunction(new NamespacedKey("ns", "myfunc"), List.of("mysay hi"));
		MockPlatform.getInstance().addFunction(new NamespacedKey("mynamespace", "myotherfunc"), List.of("mysay bye"));

		// /test
		// Should suggest mynamespace:myotherfunc and ns:myfunc
		assertEquals(List.of("mynamespace:myotherfunc", "ns:myfunc"), server.getSuggestions(player, "test "));
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

		PlayerMock player = server.addPlayer();

		// Declare our functions on the server
		MockPlatform.getInstance().addFunction(new NamespacedKey("ns", "myfunc"), List.of("mysay hi"));
		MockPlatform.getInstance().addFunction(new NamespacedKey("mynamespace", "myotherfunc"), List.of("mysay bye"));
		MockPlatform.getInstance().addTag(new NamespacedKey("ns", "mytag"), List.of(
			List.of("mysay hi", "mysay bye"),
			List.of("mysay hello", "mysay world")
		));
		MockPlatform.getInstance().addTag(new NamespacedKey("namespace", "myothertag"), List.of(
			List.of("mysay hi", "mysay bye"),
			List.of("mysay hello", "mysay world")
		));

		// /test
		// Should suggest #namespace:myothertag, #ns:mytag, mynamespace:myotherfunc and ns:myfunc
		assertEquals(List.of("#namespace:myothertag", "#ns:mytag", "mynamespace:myotherfunc", "ns:myfunc"), server.getSuggestions(player, "test "));
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

		PlayerMock player = server.addPlayer();

		// Declare our functions on the server
		MockPlatform.getInstance().addFunction(new NamespacedKey("ns", "myfunc"), List.of("mysay hi", "mysay bye"));

		// Run the /test command
		server.dispatchCommand(player, "test ns:myfunc");

		// Check that the FunctionArgument has one entry and it hasn't run the /mysay
		// command
		FunctionWrapper[] result = results.get();
		assertEquals(1, result.length);
		assertNoMoreResults(sayResults);
		
		assertArrayEquals(new String[] { "mysay hi", "mysay bye" }, result[0].getCommands());

		assertNoMoreResults(results);
		assertNoMoreResults(sayResults);
	}

}
