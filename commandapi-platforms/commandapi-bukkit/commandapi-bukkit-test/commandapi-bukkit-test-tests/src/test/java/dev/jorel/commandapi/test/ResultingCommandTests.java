package dev.jorel.commandapi.test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.exceptions.GreedyArgumentException;
import dev.jorel.commandapi.exceptions.InvalidCommandNameException;
import dev.jorel.commandapi.exceptions.MissingCommandExecutorException;

/**
 * Tests for commands with resulting command executors
 */
@SuppressWarnings("unused")
class ResultingCommandTests extends TestBase {
	
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
	void testResultingWorkSuccess() {
		new CommandAPICommand("test")
			.executes((sender, args) -> {
				return 10;
			})
			.register();
		
		PlayerMock player = server.addPlayer();
		
		int result = server.dispatchBrigadierCommand(player, "test");
		
		assertEquals(10, result);
	}

}
