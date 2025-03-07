package dev.jorel.commandapi.test.exceptions;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.exceptions.InvalidCommandNameException;
import dev.jorel.commandapi.test.TestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link InvalidCommandNameException}.
 */
class InvalidCommandNameExceptionTests extends TestBase {

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
	void testCommandAPICommandInvalidCommandNameException() {
		assertThrowsWithMessage(
			InvalidCommandNameException.class,
			"Invalid command with name 'null' cannot be registered!",
			() -> new CommandAPICommand(null)
		);

		assertThrowsWithMessage(
			InvalidCommandNameException.class,
			"Invalid command with name '' cannot be registered!",
			() -> new CommandAPICommand("")
		);

		assertThrowsWithMessage(
			InvalidCommandNameException.class,
			"Invalid command with name 'my command' cannot be registered!",
			() -> new CommandAPICommand("my command")
		);
	}

	@Test
	void testCommandTreeInvalidCommandNameException() {
		assertThrowsWithMessage(
			InvalidCommandNameException.class,
			"Invalid command with name 'null' cannot be registered!",
			() -> new CommandTree(null)
		);

		assertThrowsWithMessage(
			InvalidCommandNameException.class,
			"Invalid command with name '' cannot be registered!",
			() -> new CommandTree("")
		);

		assertThrowsWithMessage(
			InvalidCommandNameException.class,
			"Invalid command with name 'my command' cannot be registered!",
			() -> new CommandTree("my command")
		);
	}
}
