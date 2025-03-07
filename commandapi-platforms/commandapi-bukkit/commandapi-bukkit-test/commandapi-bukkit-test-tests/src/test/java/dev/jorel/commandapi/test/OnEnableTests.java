package dev.jorel.commandapi.test;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.mojang.brigadier.tree.RootCommandNode;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.SpigotCommandRegistration;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.help.HelpTopic;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Run CommandAPI.onEnable()'s scheduler
 */
class OnEnableTests extends TestBase {

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
	void testOnEnableExecution() {
		assertDoesNotThrow(() -> enableServer());
	}

	@Test
	void testOnEnableRegisterAndUnregisterCommand() {
		enableServer();

		// Spy a PlayerMock to listen for calls to `updateCommands()`
		PlayerMock updateCommandsPlayer = Mockito.spy(new PlayerMock(server, "player"));
		server.addPlayer(updateCommandsPlayer);

		// Get a CraftPlayer for running VanillaCommandWrapper commands
		Player runCommandsPlayer = MockPlatform.getInstance().wrapPlayerMockIntoCraftPlayer(updateCommandsPlayer);
		// Give player permission to run command
		Mockito.when(runCommandsPlayer.hasPermission("permission")).thenReturn(true);

		// register command while server is enabled
		Mut<String> results = Mut.of();
		new CommandAPICommand("command")
			.withArguments(new StringArgument("argument"))
			.withAliases("alias1", "alias2")
			.withPermission("permission")
			.withHelp("short description", "full description")
			.executes((sender, args) -> {
				results.set(args.getUnchecked(0));
			})
			.register();

		// Update commands should have been called for all players on the server
		Mockito.verify(updateCommandsPlayer, Mockito.times(1)).updateCommands();

		// Make sure command tree built correctly
		assertEquals("""
			{
			  "type": "root",
			  "children": {
			    "command": {
			      "type": "literal",
			      "children": {
			        "argument": {
			          "type": "argument",
			          "argumentType": "com.mojang.brigadier.arguments.StringArgumentType",
			          "properties": {
			            "type": "word"
			          },
			          "executable": true
			        }
			      }
			    },
			    "alias1": {
			      "type": "literal",
			      "redirect": [
			        "command"
			      ]
			    },
			    "alias2": {
			      "type": "literal",
			      "redirect": [
			        "command"
			      ]
			    }
			  }
			}""", getDispatcherString());

		// Make sure command and its aliases exist in the Bukkit CommandMap
		SimpleCommandMap commandMap = CommandAPIBukkit.get().getSimpleCommandMap();
		SpigotCommandRegistration<?> spigotCommandRegistration = (SpigotCommandRegistration<?>) CommandAPIBukkit.get().getCommandRegistrationStrategy();

		Command mainCommand = commandMap.getCommand("command");
		assertNotNull(mainCommand);
		assertTrue(spigotCommandRegistration.isVanillaCommandWrapper(mainCommand));

		Command alias1Command = commandMap.getCommand("alias1");
		assertNotNull(alias1Command);
		assertTrue(spigotCommandRegistration.isVanillaCommandWrapper(alias1Command));

		Command alias2Command = commandMap.getCommand("alias2");
		assertNotNull(alias2Command);
		assertTrue(spigotCommandRegistration.isVanillaCommandWrapper(alias2Command));

		// Make sure namespaces were set up as well
		assertEquals(mainCommand, commandMap.getCommand("minecraft:command"));
		assertEquals(alias1Command, commandMap.getCommand("minecraft:alias1"));
		assertEquals(alias2Command, commandMap.getCommand("minecraft:alias2"));

		// Make sure permissions were added to Bukkit commands
		assertEquals("permission", mainCommand.getPermission());
		assertEquals("permission", alias1Command.getPermission());
		assertEquals("permission", alias2Command.getPermission());


		// Make sure commands were added to 'resources' dispatcher
		RootCommandNode<?> resourcesRoot = MockPlatform.getInstance().getMockResourcesDispatcher().getRoot();

		assertNotNull(resourcesRoot.getChild("command"));
		assertNotNull(resourcesRoot.getChild("alias1"));
		assertNotNull(resourcesRoot.getChild("alias2"));

		// Namespaces should be in the resources dispatcher too
		assertNotNull(resourcesRoot.getChild("minecraft:command"));
		assertNotNull(resourcesRoot.getChild("minecraft:alias1"));
		assertNotNull(resourcesRoot.getChild("minecraft:alias2"));


		// Check the help topic was added
		HelpTopic topic = server.getHelpMap().getHelpTopic("/command");
		assertNotNull(topic);

		// Check the short description
		assertEquals("short description", topic.getShortText());

		// Check the full description
		assertEquals(ChatColor.translateAlternateColorCodes('&', """
			short description
			&6Description: &ffull description
			&6Usage: &f/command <argument>
			&6Aliases: &falias1, alias2"""), topic.getFullText(runCommandsPlayer));


		// Make sure commands run correctly
		assertStoresResult(runCommandsPlayer, "command argument", results, "argument");
		assertStoresResult(runCommandsPlayer, "alias1 argument", results, "argument");
		assertStoresResult(runCommandsPlayer, "alias2 argument", results, "argument");
		assertStoresResult(runCommandsPlayer, "minecraft:command argument", results, "argument");
		assertStoresResult(runCommandsPlayer, "minecraft:alias1 argument", results, "argument");
		assertStoresResult(runCommandsPlayer, "minecraft:alias2 argument", results, "argument");


		// Unregister just the main command
		CommandAPI.unregister("command");

		// Update commands should have been called for all players on the server
		Mockito.verify(updateCommandsPlayer, Mockito.times(2)).updateCommands();

		// Make sure main command was removed from tree
		//  While the "command" node is no longer in the tree, the alias nodes still have a reference to it
		//  as their redirect, so they still function perfectly fine as commands.
		assertEquals("""
			{
			  "type": "root",
			  "children": {
			    "alias1": {
			      "type": "literal",
			      "redirect": {
			        "type": "literal",
			        "children": {
			          "argument": {
			            "type": "argument",
			            "argumentType": "com.mojang.brigadier.arguments.StringArgumentType",
			            "properties": {
			              "type": "word"
			            },
			            "executable": true
			          }
			        }
			      }
			    },
			    "alias2": {
			      "type": "literal",
			      "redirect": [
			        "alias1",
			        "redirect command"
			      ]
			    }
			  }
			}""", getDispatcherString());


		// Make sure command is removed from Bukkit CommandMap
		assertNull(commandMap.getCommand("command"));

		// Namespace should still be there
		assertEquals(mainCommand, commandMap.getCommand("minecraft:command"));


		// Command should be removed from resources dispatcher
		assertNull(resourcesRoot.getChild("command"));

		// Namespace should still be there
		assertNotNull(resourcesRoot.getChild("minecraft:command"));


		// Help topic should be gone
		assertNull(server.getHelpMap().getHelpTopic("/command"));


		// Command should fail
		assertCommandFailsWith(runCommandsPlayer, "command argument",
				"Unknown or incomplete command, see below for error at position 0: <--[HERE]");

		// You would expect namespace to succeed since it is in the CommandMap
		// However, running that command simply tells the Brig dispatcher to run the original command
		// The command was removed from the Brig dispacter, so it doesn't actually know how to do that
		// This behavior is documented at the bottom of the docs page for Command unregistration
		// As a result, this test doesn't actually pass
//		// Namespace command should still work
//		assertStoresResult(runCommandsPlayer, "minecraft:command argument", results, "argument");


		// Unregister the namespaced version of the command
		CommandAPI.unregister("command", true);

		// Update commands should have been called for all players on the server
		Mockito.verify(updateCommandsPlayer, Mockito.times(3)).updateCommands();

		// Namespace should be gone from Bukkit's map
		assertNull(commandMap.getCommand("minecraft:command"));

		// Namespace should be gone from resources dispatcher
		assertNull(resourcesRoot.getChild("minecraft:command"));

		// Namespace should fail
		assertCommandFailsWith(runCommandsPlayer, "minecraft:command argument",
				"Unknown or incomplete command, see below for error at position 0: <--[HERE]");

		assertNoMoreResults(results);
	}
}
