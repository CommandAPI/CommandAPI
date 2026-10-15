package dev.jorel.commandapi.test.arguments;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.world.WorldMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.Location2DArgument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import dev.jorel.commandapi.wrappers.Location2D;

/**
 * Dispatcher tests for the 40+ arguments in dev.jorel.commandapi.arguments
 */
class ArgumentDispatcherTests extends TestBase {
	
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
	void executionTestWithStringArgument() {
		new CommandAPICommand("test")
			.withArguments(new StringArgument("value"))
			.executesPlayer((player, args) -> {
				String value = (String) args.get(0);
				player.sendMessage("success " + value);
			})
			.register("minecraft");

		PlayerMock player = server.addPlayer();
		boolean commandResult = server.dispatchCommand(player, "test myvalue");
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
		server.dispatchCommand(player, "test myvalue");
		assertNotEquals("success blah", player.nextMessage());
		
		// Tests from the documentation
		assertDoesNotThrow(() -> assertTrue(server.dispatchThrowableCommand(player, "test Hello")));
		assertDoesNotThrow(() -> assertTrue(server.dispatchThrowableCommand(player, "test 123")));
		assertDoesNotThrow(() -> assertTrue(server.dispatchThrowableCommand(player, "test hello123")));
		assertDoesNotThrow(() -> assertTrue(server.dispatchThrowableCommand(player, "test Hello_world")));
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

		PlayerMock sender = server.addPlayer("APlayer");

		server.dispatchCommand(sender, "test");
		server.dispatchCommand(sender, "test 1");
		server.dispatchCommand(sender, "test 1 1");
		server.dispatchCommand(sender, "test 1 1 1");
		server.dispatchCommand(sender, "test 1 1 2");
		server.dispatchCommand(sender, "test 1 2");
		server.dispatchCommand(sender, "test 1 2 1");
		server.dispatchCommand(sender, "test 1 2 2");
		server.dispatchCommand(sender, "test 2");
		server.dispatchCommand(sender, "test 2 1");
		server.dispatchCommand(sender, "test 2 1 1");
		server.dispatchCommand(sender, "test 2 1 2");
		server.dispatchCommand(sender, "test 2 2");
		server.dispatchCommand(sender, "test 2 2 1");
		server.dispatchCommand(sender, "test 2 2 2");

		assertEquals("", result.get());
		assertEquals("1", result.get());
		assertEquals("11", result.get());
		assertEquals("111", result.get());
		assertEquals("112", result.get());
		assertEquals("12", result.get());
		assertEquals("121", result.get());
		assertEquals("122", result.get());
		assertEquals("2", result.get());
		assertEquals("21", result.get());
		assertEquals("211", result.get());
		assertEquals("212", result.get());
		assertEquals("22", result.get());
		assertEquals("221", result.get());
		assertEquals("222", result.get());
	}

	private CommandExecutor givePosition(String pos, Mut<String> result) {
		return (sender, args) -> result.set(pos);
	}
	
	
	
	
	
}
