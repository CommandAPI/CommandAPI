package dev.jorel.commandapi.test;

import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for commands with resulting command executors
 */
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

		Player player = addPlayer();

		int result = getServer().dispatchBrigadierCommand(player, "test");

		assertEquals(10, result);
	}
}
