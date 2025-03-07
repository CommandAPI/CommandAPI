package dev.jorel.commandapi.test;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandUnregisterTests extends TestBase {

	/*********
	 * Setup *
	 *********/
	static class BRIG_TREE {
		static String FULL = """
			{
			  "type": "root",
			  "children": {
			    "test": {
			      "type": "literal",
			      "children": {
			        "string": {
			          "type": "argument",
			          "argumentType": "com.mojang.brigadier.arguments.StringArgumentType",
			          "properties": {
			            "type": "word"
			          },
			          "executable": true
			        }
			      }
			    },
			    "minecraft:test": {
			      "type": "literal",
			      "children": {
			        "string": {
			          "type": "argument",
			          "argumentType": "com.mojang.brigadier.arguments.StringArgumentType",
			          "properties": {
			            "type": "word"
			          },
			          "executable": true
			        }
			      }
			    }
			  }
			}""";

		static String NO_MAIN = """
			{
			  "type": "root",
			  "children": {
			    "minecraft:test": {
			      "type": "literal",
			      "children": {
			        "string": {
			          "type": "argument",
			          "argumentType": "com.mojang.brigadier.arguments.StringArgumentType",
			          "properties": {
			            "type": "word"
			          },
			          "executable": true
			        }
			      }
			    }
			  }
			}""";

		static String NO_NAMESPACE = """
			{
			  "type": "root",
			  "children": {
			    "test": {
			      "type": "literal",
			      "children": {
			        "string": {
			          "type": "argument",
			          "argumentType": "com.mojang.brigadier.arguments.StringArgumentType",
			          "properties": {
			            "type": "word"
			          },
			          "executable": true
			        }
			      }
			    }
			  }
			}""";

		static String EMPTY = """
			{
			  "type": "root"
			}""";
	}

	PlayerMock player;
	Mut<String> vanillaResults;
	CommandMap commandMap;
	Mut<String> bukkitResults;
	Command bukkitCommand;

	@BeforeEach
	public void setUp() {
		super.setUp();
		disablePaperImplementations();

		player = server.addPlayer();

		// Set up a Vanilla command
		vanillaResults = Mut.of();
		new CommandAPICommand("test")
			.withArguments(new StringArgument("string"))
			.executes((sender, args) -> {
				vanillaResults.set(args.getUnchecked(0));
			})
			.register();
		// Pretend the command exists in a namespace version
		//  Namespaces usually only exist in the Bukkit CommandMap, but CommandAPIBukkit can
		//  check for and remove namespaces, so we'll test it
		new CommandAPICommand("minecraft:test")
			.withArguments(new StringArgument("string"))
			.executes((sender, args) -> {
				vanillaResults.set(args.getUnchecked(0));
			})
			.register();

		assertEquals(BRIG_TREE.FULL, getDispatcherString());

		assertStoresResult(player, "test word", vanillaResults, "word");
		assertStoresResult(player, "minecraft:test word", vanillaResults, "word");

		// Set up a Bukkit command
		commandMap = CommandAPIBukkit.get().getSimpleCommandMap();

		bukkitResults = Mut.of();
		bukkitCommand = new Command("test") {
			@Override
			public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] arguments) {
				if(arguments.length == 0) return false;
				bukkitResults.set(arguments[0]);
				return true;
			}
		};
		commandMap.register("bukkit", bukkitCommand);

		assertEquals(bukkitCommand, commandMap.getCommand("test"));
		assertEquals(bukkitCommand, commandMap.getCommand("bukkit:test"));

		assertStoresResult(player, "test word", bukkitResults, "word");
		assertStoresResult(player, "bukkit:test word", bukkitResults, "word");


		assertNoMoreResults(vanillaResults);
		assertNoMoreResults(bukkitResults);

		assertTrue(CommandAPI.canRegister());
	}

	@AfterEach
	public void tearDown() {
		super.tearDown();
	}

	/*********
	 * Tests *
	 *********/

	@Test
	void testUnregister() {
		// unregisterNamespaces: false, unregisterBukkit: false
		CommandAPI.unregister("test");

		// Only minecraft:test left in the tree
		assertEquals(BRIG_TREE.NO_MAIN, getDispatcherString());

		// test goes to bukkit:test
		assertStoresResult(player, "test word", bukkitResults, "word");
		// minecraft:test passes
		assertStoresResult(player, "minecraft:test word", vanillaResults, "word");


		// Bukkit unchanged
		assertEquals(bukkitCommand, commandMap.getCommand("test"));
		assertEquals(bukkitCommand, commandMap.getCommand("bukkit:test"));

		assertStoresResult(player, "test word", bukkitResults, "word");
		assertStoresResult(player, "bukkit:test word", bukkitResults, "word");


		assertNoMoreResults(vanillaResults);
		assertNoMoreResults(bukkitResults);
	}

	@Test
	void testUnregisterNamespace() {
		// unregisterNamespaces: false, unregisterBukkit: false
		CommandAPI.unregister("minecraft:test");

		// Only test left in the tree
		assertEquals(BRIG_TREE.NO_NAMESPACE, getDispatcherString());

		// test goes to bukkit:test
		assertStoresResult(player, "test word", bukkitResults, "word");
		// minecraft:test fails
		assertCommandFailsWith(player, "minecraft:test word",
			"Unknown or incomplete command, see below for error at position 0: <--[HERE]");


		// Bukkit unchanged
		assertEquals(bukkitCommand, commandMap.getCommand("test"));
		assertEquals(bukkitCommand, commandMap.getCommand("bukkit:test"));

		// Both pass
		assertStoresResult(player, "test word", bukkitResults, "word");
		assertStoresResult(player, "bukkit:test word", bukkitResults, "word");


		assertNoMoreResults(vanillaResults);
		assertNoMoreResults(bukkitResults);
	}

	@Test
	void testUnregisterBoth() {
		// unregisterNamespaces: true, unregisterBukkit: false
		CommandAPI.unregister("test", true);

		// No test command in tree
		assertEquals(BRIG_TREE.EMPTY, getDispatcherString());

		// test goes to bukkit:test
		assertStoresResult(player, "test word", bukkitResults, "word");
		// minecraft:test fails
		assertCommandFailsWith(player, "minecraft:test word",
			"Unknown or incomplete command, see below for error at position 0: <--[HERE]");


		// Bukkit unchanged
		assertEquals(bukkitCommand, commandMap.getCommand("test"));
		assertEquals(bukkitCommand, commandMap.getCommand("bukkit:test"));

		// Both pass
		assertStoresResult(player, "test word", bukkitResults, "word");
		assertStoresResult(player, "bukkit:test word", bukkitResults, "word");


		assertNoMoreResults(vanillaResults);
		assertNoMoreResults(bukkitResults);
	}

	@Test
	void testUnregisterBukkit() {
		// unregisterNamespaces: false, unregisterBukkit: true
		CommandAPIBukkit.unregister("test", false, true);

		// Vanilla unchanged
		assertEquals(BRIG_TREE.FULL, getDispatcherString());

		// Both pass
		assertStoresResult(player, "test word", vanillaResults, "word");
		assertStoresResult(player, "minecraft:test word", vanillaResults, "word");


		// Only bukkit:test left in the map
		assertNull(commandMap.getCommand("test"));
		assertEquals(bukkitCommand, commandMap.getCommand("bukkit:test"));

		// test goes to minecraft:test
		assertStoresResult(player, "test word", vanillaResults, "word");
		// bukkit:test passes
		assertStoresResult(player, "bukkit:test word", bukkitResults, "word");


		assertNoMoreResults(vanillaResults);
		assertNoMoreResults(bukkitResults);
	}

	@Test
	void testUnregisterBukkitNamespace() {
		// unregisterNamespaces: false, unregisterBukkit: true
		CommandAPIBukkit.unregister("bukkit:test", false, true);

		// Vanilla unchanged
		assertEquals(BRIG_TREE.FULL, getDispatcherString());

		// test goes to bukkit:test
		assertStoresResult(player, "test word", bukkitResults, "word");
		// minecraft:test passes
		assertStoresResult(player, "minecraft:test word", vanillaResults, "word");


		// Only test left in the map
		assertEquals(bukkitCommand, commandMap.getCommand("test"));
		assertNull(commandMap.getCommand("bukkit:test"));


		// test passes
		assertStoresResult(player, "test word", bukkitResults, "word");
		// bukkit:test fails
		assertCommandFailsWith(player, "bukkit:test word",
			"Unknown or incomplete command, see below for error at position 0: <--[HERE]");


		assertNoMoreResults(vanillaResults);
		assertNoMoreResults(bukkitResults);
	}

	@Test
	void testUnregisterBukkitBoth() {
		// unregisterNamespaces: true, unregisterBukkit: true
		CommandAPIBukkit.unregister("test", true, true);

		// Vanilla unchanged
		assertEquals(BRIG_TREE.FULL, getDispatcherString());

		// Both pass
		assertStoresResult(player, "test word", vanillaResults, "word");
		assertStoresResult(player, "minecraft:test word", vanillaResults, "word");


		// No test command in the map
		assertNull(commandMap.getCommand("test"));
		assertNull(commandMap.getCommand("bukkit:test"));

		// test goes to minecraft:test
		assertStoresResult(player, "test word", vanillaResults, "word");
		// bukkit:test fails
		assertCommandFailsWith(player, "bukkit:test word",
			"Unknown or incomplete command, see below for error at position 0: <--[HERE]");


		assertNoMoreResults(vanillaResults);
		assertNoMoreResults(bukkitResults);
	}

	@Test
	void testUnregisterWithHangingColons() {
		commandMap.registerAll("plugin", List.of(
			new Command(":command") {
				@Override
				public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings) {
					return true;
				}
			},
			new Command("namespace:") {
				@Override
				public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings) {
					return true;
				}
			}
		));

		testUnregisterBukkitBoth();
	}
}
