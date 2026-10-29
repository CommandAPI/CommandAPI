package dev.jorel.commandapi.test.arguments;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.exceptions.BadLiteralException;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;

/**
 * Dispatcher tests for the {@link LiteralArgument}
 */
class ArgumentLiteralDispatcherTests extends TestBase {

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

	
	/********************
	 * Dispatcher tests *
	 ********************/
	
	@Test
	void dispatcherTestWithLiteralArgument() {
		new CommandAPICommand("test")
			.withArguments(new LiteralArgument("literal"))
			.executesPlayer(P_EXEC)
			.register("minecraft");

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
			    },
			    "minecraft:test": {
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

	/********************
	 * Suggestion tests *
	 ********************/

	
	// TODO: Some tests revolving around case sensitivity would be good!

}
