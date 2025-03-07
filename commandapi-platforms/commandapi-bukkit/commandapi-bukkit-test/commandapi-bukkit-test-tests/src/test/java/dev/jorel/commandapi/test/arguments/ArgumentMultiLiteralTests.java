package dev.jorel.commandapi.test.arguments;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@link MultiLiteralArgument}
 */
class ArgumentMultiLiteralTests extends TestBase {

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
	void commandBuildingTestWithMultiLiteralArgument() {
		// 2 MultiLiterals in a row should unpack into 9 commands
		new CommandAPICommand("command1")
			.withArguments(
				new MultiLiteralArgument("literal1", "option1", "option2", "option3"),
				new MultiLiteralArgument("literal2", "option1", "option2", "option3")
			)
			.executesPlayer(P_EXEC)
			.register();

		new CommandTree("command2")
			.then(
				new MultiLiteralArgument("literal1", "option1", "option2", "option3").then(
					new MultiLiteralArgument("literal2", "option1", "option2", "option3")
						.executesPlayer(P_EXEC)
				)
			)
			.register();

		// Combining arguments shouldn't affect unpacking
		new CommandAPICommand("command3")
			.withArguments(
				new MultiLiteralArgument("literal1", "option1", "option2").combineWith(
					new MultiLiteralArgument("literal2", "option1", "option2"),
					new MultiLiteralArgument("literal3", "option1", "option2")
				)
			)
			.executesPlayer(P_EXEC)
			.register();

		new CommandTree("command4")
			.then(
				new MultiLiteralArgument("literal1", "option1", "option2").combineWith(
						new MultiLiteralArgument("literal2", "option1", "option2"),
						new MultiLiteralArgument("literal3", "option1", "option2")
					)
					.executesPlayer(P_EXEC)
			)
			.register();

		// Make sure all the commands were set up in the tree correctly
		assertEquals("""
				{
				  "type": "root",
				  "children": {
				    "command1": {
				      "type": "literal",
				      "children": {
				        "option1": {
				          "type": "namedLiteral",
				          "nodeName": "literal1",
				          "children": {
				            "option1": {
				              "type": "namedLiteral",
				              "nodeName": "literal2",
				              "executable": true
				            },
				            "option2": {
				              "type": "namedLiteral",
				              "nodeName": "literal2",
				              "executable": true
				            },
				            "option3": {
				              "type": "namedLiteral",
				              "nodeName": "literal2",
				              "executable": true
				            }
				          }
				        },
				        "option2": {
				          "type": "namedLiteral",
				          "nodeName": "literal1",
				          "children": {
				            "option1": [
				              "command1",
				              "option1",
				              "option1"
				            ],
				            "option2": [
				              "command1",
				              "option1",
				              "option2"
				            ],
				            "option3": [
				              "command1",
				              "option1",
				              "option3"
				            ]
				          }
				        },
				        "option3": {
				          "type": "namedLiteral",
				          "nodeName": "literal1",
				          "children": {
				            "option1": [
				              "command1",
				              "option1",
				              "option1"
				            ],
				            "option2": [
				              "command1",
				              "option1",
				              "option2"
				            ],
				            "option3": [
				              "command1",
				              "option1",
				              "option3"
				            ]
				          }
				        }
				      }
				    },
				    "command2": {
				      "type": "literal",
				      "children": {
				        "option1": {
				          "type": "namedLiteral",
				          "nodeName": "literal1",
				          "children": {
				            "option1": {
				              "type": "namedLiteral",
				              "nodeName": "literal2",
				              "executable": true
				            },
				            "option2": {
				              "type": "namedLiteral",
				              "nodeName": "literal2",
				              "executable": true
				            },
				            "option3": {
				              "type": "namedLiteral",
				              "nodeName": "literal2",
				              "executable": true
				            }
				          }
				        },
				        "option2": {
				          "type": "namedLiteral",
				          "nodeName": "literal1",
				          "children": {
				            "option1": [
				              "command2",
				              "option1",
				              "option1"
				            ],
				            "option2": [
				              "command2",
				              "option1",
				              "option2"
				            ],
				            "option3": [
				              "command2",
				              "option1",
				              "option3"
				            ]
				          }
				        },
				        "option3": {
				          "type": "namedLiteral",
				          "nodeName": "literal1",
				          "children": {
				            "option1": [
				              "command2",
				              "option1",
				              "option1"
				            ],
				            "option2": [
				              "command2",
				              "option1",
				              "option2"
				            ],
				            "option3": [
				              "command2",
				              "option1",
				              "option3"
				            ]
				          }
				        }
				      }
				    },
				    "command3": {
				      "type": "literal",
				      "children": {
				        "option1": {
				          "type": "namedLiteral",
				          "nodeName": "literal1",
				          "children": {
				            "option1": {
				              "type": "namedLiteral",
				              "nodeName": "literal2",
				              "children": {
				                "option1": {
				                  "type": "namedLiteral",
				                  "nodeName": "literal3",
				                  "executable": true
				                },
				                "option2": {
				                  "type": "namedLiteral",
				                  "nodeName": "literal3",
				                  "executable": true
				                }
				              }
				            },
				            "option2": {
				              "type": "namedLiteral",
				              "nodeName": "literal2",
				              "children": {
				                "option1": [
				                  "command3",
				                  "option1",
				                  "option1",
				                  "option1"
				                ],
				                "option2": [
				                  "command3",
				                  "option1",
				                  "option1",
				                  "option2"
				                ]
				              }
				            }
				          }
				        },
				        "option2": {
				          "type": "namedLiteral",
				          "nodeName": "literal1",
				          "children": {
				            "option1": [
				              "command3",
				              "option1",
				              "option1"
				            ],
				            "option2": [
				              "command3",
				              "option1",
				              "option2"
				            ]
				          }
				        }
				      }
				    },
				    "command4": {
				      "type": "literal",
				      "children": {
				        "option1": {
				          "type": "namedLiteral",
				          "nodeName": "literal1",
				          "children": {
				            "option1": {
				              "type": "namedLiteral",
				              "nodeName": "literal2",
				              "children": {
				                "option1": {
				                  "type": "namedLiteral",
				                  "nodeName": "literal3",
				                  "executable": true
				                },
				                "option2": {
				                  "type": "namedLiteral",
				                  "nodeName": "literal3",
				                  "executable": true
				                }
				              }
				            },
				            "option2": {
				              "type": "namedLiteral",
				              "nodeName": "literal2",
				              "children": {
				                "option1": [
				                  "command4",
				                  "option1",
				                  "option1",
				                  "option1"
				                ],
				                "option2": [
				                  "command4",
				                  "option1",
				                  "option1",
				                  "option2"
				                ]
				              }
				            }
				          }
				        },
				        "option2": {
				          "type": "namedLiteral",
				          "nodeName": "literal1",
				          "children": {
				            "option1": [
				              "command4",
				              "option1",
				              "option1"
				            ],
				            "option2": [
				              "command4",
				              "option1",
				              "option2"
				            ]
				          }
				        }
				      }
				    }
				  }
				}""",
			getDispatcherString()
		);
	}

	@Test
	void executionTestWithMultiLiteralArgument() {
		Mut<String> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new MultiLiteralArgument("literals", "literal", "literal1", "literal2"))
			.executesPlayer((player, args) -> {
				results.set((String) args.get(0));
			})
			.register();

		PlayerMock player = server.addPlayer();

		// /test literal
		server.dispatchCommand(player, "test literal");
		assertEquals("literal", results.get());

		// /test literal1
		server.dispatchCommand(player, "test literal1");
		assertEquals("literal1", results.get());

		// /test literal3
		assertCommandFailsWith(player, "test literal3", "Incorrect argument for command at position 5: test <--[HERE]");

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithMultiLiteralArgumentNodeName() {
		Mut<String> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new MultiLiteralArgument("literals", "literal", "literal1", "literal2"))
			.executesPlayer((player, args) -> {
				results.set((String) args.get("literals"));
			})
			.register();

		PlayerMock player = server.addPlayer();

		// /test literal
		server.dispatchCommand(player, "test literal");
		assertEquals("literal", results.get());

		// /test literal1
		server.dispatchCommand(player, "test literal1");
		assertEquals("literal1", results.get());

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithMultipleMultiLiteralArguments() {
		Mut<String> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new MultiLiteralArgument("literals1", "lit1", "lit2"))
			.withArguments(new MultiLiteralArgument("literals2", "lit1", "lit3"))
			.executes((player, args) -> {
				results.set(args.getUnchecked("literals1"));
				results.set(args.getUnchecked("literals2"));
			})
			.register();

		PlayerMock player = server.addPlayer();

		// /test lit1 lit1
		server.dispatchCommand(player, "test lit1 lit1");
		assertEquals("lit1", results.get());
		assertEquals("lit1", results.get());

		// /test lit1 lit3
		server.dispatchCommand(player, "test lit1 lit3");
		assertEquals("lit1", results.get());
		assertEquals("lit3", results.get());

		// /test lit2 lit1
		server.dispatchCommand(player, "test lit2 lit1");
		assertEquals("lit2", results.get());
		assertEquals("lit1", results.get());

		// /test lit2 lit3
		server.dispatchCommand(player, "test lit2 lit3");
		assertEquals("lit2", results.get());
		assertEquals("lit3", results.get());

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithMultiLiteralArgument() {
		new CommandAPICommand("test")
			.withArguments(new MultiLiteralArgument("literals", "literal", "literal1", "literal2"))
			.executesPlayer(P_EXEC)
			.register();

		PlayerMock player = server.addPlayer();

		assertEquals(List.of("literal", "literal1", "literal2"), server.getSuggestions(player, "test "));
	}
}
