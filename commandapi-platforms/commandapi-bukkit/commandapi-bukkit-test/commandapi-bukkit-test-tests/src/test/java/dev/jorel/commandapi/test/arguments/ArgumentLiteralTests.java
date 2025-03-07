package dev.jorel.commandapi.test.arguments;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.exceptions.BadLiteralException;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;

/**
 * Tests for the {@link LiteralArgument}
 */
class ArgumentLiteralTests extends TestBase {

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
	void executionTestWithLiteralArgument() {
		Mut<String> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new LiteralArgument("literal"))
			.executesPlayer((player, args) -> {
				assertEquals(0, args.args().length);
				results.set((String) args.get(0));
			})
			.register();

		PlayerMock player = server.addPlayer();

		// /test literal
		server.dispatchCommand(player, "test literal");
		assertEquals(null, results.get());

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithLiteralArgumentListed() {
		Mut<String> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new LiteralArgument("literal").setListed(true))
			.executesPlayer((player, args) -> {
				assertEquals(1, args.args().length);
				results.set((String) args.get(0));
			})
			.register();

		PlayerMock player = server.addPlayer();

		// /test literal
		server.dispatchCommand(player, "test literal");
		assertEquals("literal", results.get());
		
		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithLiteralArgumentListedAndNodeName() {
		Mut<String> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new LiteralArgument("literal").setListed(true))
			.executesPlayer((player, args) -> {
				assertEquals(1, args.args().length);
				results.set((String) args.get("literal"));
			})
			.register();

		PlayerMock player = server.addPlayer();

		// /test literal
		server.dispatchCommand(player, "test literal");
		assertEquals("literal", results.get());

		assertNoMoreResults(results);
	}
	
	/********************
	 * Dispatcher tests *
	 ********************/
	
	@Test
	void dispatcherTestWithLiteralArgument() {
		new CommandAPICommand("test")
			.withArguments(new LiteralArgument("literal"))
			.executesPlayer(P_EXEC)
			.register();

		assertEquals("""
			{
			  "type": "root",
			  "children": {
			    "test": {
			      "type": "literal",
			      "children": {
			        "literal": {
			          "type": "literal",
			          "executable": true
			        }
			      }
			    }
			  }
			}""", getDispatcherString());
	}

	/*********************************
	 * Instantiation exception tests *
	 *********************************/

	@Test
	void exceptionTestWithLiteralArgumentInvalid() {
		// Test literal argument with null
		assertThrows(BadLiteralException.class, () -> new LiteralArgument(null));
		
		// Test literal argument with empty string
		assertThrows(BadLiteralException.class, () -> new LiteralArgument(""));
		
		// Test literal argument with a space in it
		// TODO: This test fails. Do commands actually support spaces in
		// literal arguments? If so, this may be something worth documenting.
		// If not, we need to make this test pass!
//		assertThrows(BadLiteralException.class, () -> new LiteralArgument("spaced literal"));
		
		// Test literal argument with non-ASCII characters.
		// These are allowed in literal arguments, so it doesn't throw
		assertDoesNotThrow(() -> new LiteralArgument("こんにちは"));
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithLiteralArgument() {
		new CommandAPICommand("test")
			.withArguments(new LiteralArgument("literal"))
			.executesPlayer(P_EXEC)
			.register();

		PlayerMock player = server.addPlayer();

		// /test
		assertEquals(List.of("literal"), server.getSuggestions(player, "test "));
	}

	@Test
	void suggestionTestWithTwoLiteralArguments() {
		new CommandAPICommand("test")
			.withArguments(new LiteralArgument("literal1"))
			.executesPlayer(P_EXEC)
			.register();
		
		new CommandAPICommand("test")
			.withArguments(new LiteralArgument("literal2"))
			.executesPlayer(P_EXEC)
			.register();

		PlayerMock player = server.addPlayer();

		// /test
		assertEquals(List.of("literal1", "literal2"), server.getSuggestions(player, "test "));
	}
	
	// TODO: Some tests revolving around case sensitivity would be good!

}
