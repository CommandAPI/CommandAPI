package dev.jorel.commandapi.test.arguments;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import dev.jorel.commandapi.test.mockbukkit.CommandAPIPlayerMock;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the 40+ arguments in dev.jorel.commandapi.arguments
 */
class ArgumentTests extends TestBase {

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
	void executionTest() {
		new CommandAPICommand("test")
			.executesPlayer((player, args) -> {
				player.sendMessage("success");
			})
			.register();

		CommandAPIPlayerMock player = addPlayer();
		boolean commandResult = getServer().dispatchCommand(player, "test");
		assertTrue(commandResult);
		assertEquals("success", player.nextMessage());
	}

	@Test
	void executionTestWithStringArgument() {
		new CommandAPICommand("test")
			.withArguments(new StringArgument("value"))
			.executesPlayer((player, args) -> {
				String value = (String) args.get(0);
				player.sendMessage("success " + value);
			})
			.register("minecraft");

		CommandAPIPlayerMock player = addPlayer();
		boolean commandResult = getServer().dispatchCommand(player, "test myvalue");
		assertTrue(commandResult);
		assertEquals("success myvalue", player.nextMessage());
		assertEquals("""
			{
			  "type": "root",
			  "children": {
			    "test": {
			      "type": "literal",
			      "children": {
			        "value": {
			          "type": "argument",
			          "parser": "brigadier:string",
			          "properties": {
			            "type": "word"
			          },
			          "executable": true
			        }
			      }
			    }
			  }
			}""", getDispatcherString());

		// Negative test
		getServer().dispatchCommand(player, "test myvalue");
		assertNotEquals("success blah", player.nextMessage());

		// Tests from the documentation
		assertDoesNotThrow(() -> assertTrue(getServer().dispatchThrowableCommand(player, "test Hello")));
		assertDoesNotThrow(() -> assertTrue(getServer().dispatchThrowableCommand(player, "test 123")));
		assertDoesNotThrow(() -> assertTrue(getServer().dispatchThrowableCommand(player, "test hello123")));
		assertDoesNotThrow(() -> assertTrue(getServer().dispatchThrowableCommand(player, "test Hello_world")));
		assertEquals("success Hello", player.nextMessage());
		assertEquals("success 123", player.nextMessage());
		assertEquals("success hello123", player.nextMessage());
		assertEquals("success Hello_world", player.nextMessage());

		// Negative tests from the documentation
		assertCommandFailsWith(player, "test hello@email.com", "Expected whitespace to end one argument, but found trailing data at position 10: test hello<--[HERE]");
		assertCommandFailsWith(player, "test yesn't", "Expected whitespace to end one argument, but found trailing data at position 9: test yesn<--[HERE]");
	}

	@Test
	void executionTestWithCommandTree() {
		Mut<String> result = Mut.of();
		new CommandTree("test").executes(givePosition("", result))
			.then(new LiteralArgument("1").executes(givePosition("1", result))
				.then(new LiteralArgument("1").executes(givePosition("11", result))
					.then(new LiteralArgument("1").executes(givePosition("111", result)))
					.then(new LiteralArgument("2").executes(givePosition("112", result)))
				)
				.then(new LiteralArgument("2").executes(givePosition("12", result))
					.then(new LiteralArgument("1").executes(givePosition("121", result)))
					.then(new LiteralArgument("2").executes(givePosition("122", result)))
				)
			)
			.then(new LiteralArgument("2").executes(givePosition("2", result))
				.then(new LiteralArgument("1").executes(givePosition("21", result))
					.then(new LiteralArgument("1").executes(givePosition("211", result)))
					.then(new LiteralArgument("2").executes(givePosition("212", result)))
				)
				.then(new LiteralArgument("2").executes(givePosition("22", result))
					.then(new LiteralArgument("1").executes(givePosition("221", result)))
					.then(new LiteralArgument("2").executes(givePosition("222", result)))
				)
			).register("minecraft");

		assertEquals("""
			{
			  "type": "root",
			  "children": {
			    "test": {
			      "type": "literal",
			      "children": {
			        "1": {
			          "type": "literal",
			          "children": {
			            "1": {
			              "type": "literal",
			              "children": {
			                "1": {
			                  "type": "literal",
			                  "executable": true
			                },
			                "2": {
			                  "type": "literal",
			                  "executable": true
			                }
			              },
			              "executable": true
			            },
			            "2": {
			              "type": "literal",
			              "children": {
			                "1": {
			                  "type": "literal",
			                  "executable": true
			                },
			                "2": {
			                  "type": "literal",
			                  "executable": true
			                }
			              },
			              "executable": true
			            }
			          },
			          "executable": true
			        },
			        "2": {
			          "type": "literal",
			          "children": {
			            "1": {
			              "type": "literal",
			              "children": {
			                "1": {
			                  "type": "literal",
			                  "executable": true
			                },
			                "2": {
			                  "type": "literal",
			                  "executable": true
			                }
			              },
			              "executable": true
			            },
			            "2": {
			              "type": "literal",
			              "children": {
			                "1": {
			                  "type": "literal",
			                  "executable": true
			                },
			                "2": {
			                  "type": "literal",
			                  "executable": true
			                }
			              },
			              "executable": true
			            }
			          },
			          "executable": true
			        }
			      },
			      "executable": true
			    }
			  }
			}""", getDispatcherString());

		Player sender = addPlayer("APlayer");

		assertStoresResult(sender, "test",       result, "");
		assertStoresResult(sender, "test 1",     result, "1");
		assertStoresResult(sender, "test 1 1",   result, "11");
		assertStoresResult(sender, "test 1 1 1", result, "111");
		assertStoresResult(sender, "test 1 1 2", result, "112");
		assertStoresResult(sender, "test 1 2",   result, "12");
		assertStoresResult(sender, "test 1 2 1", result, "121");
		assertStoresResult(sender, "test 1 2 2", result, "122");
		assertStoresResult(sender, "test 2",     result, "2");
		assertStoresResult(sender, "test 2 1",   result, "21");
		assertStoresResult(sender, "test 2 1 1", result, "211");
		assertStoresResult(sender, "test 2 1 2", result, "212");
		assertStoresResult(sender, "test 2 2",   result, "22");
		assertStoresResult(sender, "test 2 2 1", result, "221");
		assertStoresResult(sender, "test 2 2 2", result, "222");
	}

	private CommandExecutor givePosition(String pos, Mut<String> result) {
		return (sender, args) -> result.set(pos);
	}

	@Test
		// Pre-#321
	void executionTwoCommandsSameArgumentDifferentName() {
		Mut<String> str1 = Mut.of();
		Mut<String> str2 = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new StringArgument("str_1"))
			.executesPlayer((player, args) -> {
				str1.set((String) args.get(0));
			})
			.register();

		new CommandAPICommand("test")
			.withArguments(new StringArgument("str_2"))
			.executesPlayer((player, args) -> {
				str2.set((String) args.get(0));
			})
			.register();

		Player player = addPlayer("APlayer");
		getServer().dispatchCommand(player, "test hello");
		getServer().dispatchCommand(player, "test world");
		assertEquals("hello", str1.get());
		assertEquals("world", str1.get());

		assertNoMoreResults(str1);
		assertNoMoreResults(str2);
	}

	@Test
		// Pre-#321
	void executionTwoCommandsSameArgumentDifferentNameDifferentImplementation() {
		Mut<Integer> int1 = Mut.of();
		Mut<Integer> int2 = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new IntegerArgument("int_1", 1, 10))
			.executesPlayer((player, args) -> {
				int1.set((int) args.get(0));
			})
			.register();

		new CommandAPICommand("test")
			.withArguments(new IntegerArgument("str_2", 50, 100))
			.executesPlayer((player, args) -> {
				int2.set((int) args.get(0));
			})
			.register();

		Player player = addPlayer("APlayer");
		getServer().dispatchCommand(player, "test 5");
		getServer().dispatchCommand(player, "test 60");
		assertEquals(5, int1.get());
		assertEquals(60, int2.get());

		assertNoMoreResults(int1);
		assertNoMoreResults(int2);
	}

	@Test
		// Pre-#321
	void executionTwoCommandsSameArgumentDifferentNameDifferentImplementation2() {
		Mut<Integer> int1 = Mut.of();
		Mut<Integer> int2 = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new IntegerArgument("int_1", 1, 100))
			.executesPlayer((player, args) -> {
				int1.set((int) args.get(0));
			})
			.register();

		new CommandAPICommand("test")
			.withArguments(new IntegerArgument("str_2", 50, 100))
			.executesPlayer((player, args) -> {
				int2.set((int) args.get(0));
			})
			.register();

		Player player = addPlayer("APlayer");
		getServer().dispatchCommand(player, "test 5");
		getServer().dispatchCommand(player, "test 60");
		assertEquals(5, int1.get());
		assertEquals(60, int1.get());

		assertNoMoreResults(int1);
		assertNoMoreResults(int2);
	}
}
