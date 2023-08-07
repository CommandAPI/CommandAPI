package dev.jorel.commandapi.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.Converter;

/**
 * Tests for converted commands
 */
class CommandConvertedTests extends TestBase {

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
	void test1() {
		JavaPlugin plugin = MockBukkit.loadWith(CommandConvertedTestsPlugin.class, CommandConvertedTestsPlugin.pluginYaml());
		
		Converter.convert(plugin, "mycommand");
		
		PlayerMock player = server.addPlayer();

		server.dispatchBrigadierCommand(player, "mycommand");

		assertEquals("hello", player.nextMessage());
	}
}
