package dev.jorel.commandapi.test.arguments;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.ItemStackArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@link MultiLiteralArgument}
 */
public class ArgumentMultiLiteralTests extends TestBase {

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
			.register("minecraft");

		new CommandTree("command2")
			.then(
				new MultiLiteralArgument("literal1", "option1", "option2", "option3").then(
					new MultiLiteralArgument("literal2", "option1", "option2", "option3")
						.executesPlayer(P_EXEC)
				)
			)
			.register("minecraft");

		// Combining arguments shouldn't affect unpacking
		new CommandAPICommand("command3")
			.withArguments(
				new MultiLiteralArgument("literal1", "option1", "option2").combineWith(
					new MultiLiteralArgument("literal2", "option1", "option2"),
					new MultiLiteralArgument("literal3", "option1", "option2")
				)
			)
			.executesPlayer(P_EXEC)
			.register("minecraft");

		new CommandTree("command4")
			.then(
				new MultiLiteralArgument("literal1", "option1", "option2").combineWith(
						new MultiLiteralArgument("literal2", "option1", "option2"),
						new MultiLiteralArgument("literal3", "option1", "option2")
					)
					.executesPlayer(P_EXEC)
			)
			.register("minecraft");

		// Make sure all the commands were set up in the tree correctly
		assertEquals("""
				{
				  "type": "root",
				  "children": {
				    "command1": {
				      "type": "literal",
				      "children": {
				        "option1": {
				          "type": "literal",
				          "children": {
				            "option1": {
				              "type": "literal",
				              "executable": true
				            },
				            "option2": {
				              "type": "literal",
				              "executable": true
				            },
				            "option3": {
				              "type": "literal",
				              "executable": true
				            }
				          }
				        },
				        "option2": {
				          "type": "literal",
				          "children": {
				            "option1": {
				              "type": "literal",
				              "executable": true
				            },
				            "option2": {
				              "type": "literal",
				              "executable": true
				            },
				            "option3": {
				              "type": "literal",
				              "executable": true
				            }
				          }
				        },
				        "option3": {
				          "type": "literal",
				          "children": {
				            "option1": {
				              "type": "literal",
				              "executable": true
				            },
				            "option2": {
				              "type": "literal",
				              "executable": true
				            },
				            "option3": {
				              "type": "literal",
				              "executable": true
				            }
				          }
				        }
				      }
				    },
				    "command2": {
				      "type": "literal",
				      "children": {
				        "option1": {
				          "type": "literal",
				          "children": {
				            "option1": {
				              "type": "literal",
				              "executable": true
				            },
				            "option2": {
				              "type": "literal",
				              "executable": true
				            },
				            "option3": {
				              "type": "literal",
				              "executable": true
				            }
				          }
				        },
				        "option2": {
				          "type": "literal",
				          "children": {
				            "option1": {
				              "type": "literal",
				              "executable": true
				            },
				            "option2": {
				              "type": "literal",
				              "executable": true
				            },
				            "option3": {
				              "type": "literal",
				              "executable": true
				            }
				          }
				        },
				        "option3": {
				          "type": "literal",
				          "children": {
				            "option1": {
				              "type": "literal",
				              "executable": true
				            },
				            "option2": {
				              "type": "literal",
				              "executable": true
				            },
				            "option3": {
				              "type": "literal",
				              "executable": true
				            }
				          }
				        }
				      }
				    },
				    "command3": {
				      "type": "literal",
				      "children": {
				        "option1": {
				          "type": "literal",
				          "children": {
				            "option1": {
				              "type": "literal",
				              "children": {
				                "option1": {
				                  "type": "literal",
				                  "executable": true
				                },
				                "option2": {
				                  "type": "literal",
				                  "executable": true
				                }
				              }
				            },
				            "option2": {
				              "type": "literal",
				              "children": {
				                "option1": {
				                  "type": "literal",
				                  "executable": true
				                },
				                "option2": {
				                  "type": "literal",
				                  "executable": true
				                }
				              }
				            }
				          }
				        },
				        "option2": {
				          "type": "literal",
				          "children": {
				            "option1": {
				              "type": "literal",
				              "children": {
				                "option1": {
				                  "type": "literal",
				                  "executable": true
				                },
				                "option2": {
				                  "type": "literal",
				                  "executable": true
				                }
				              }
				            },
				            "option2": {
				              "type": "literal",
				              "children": {
				                "option1": {
				                  "type": "literal",
				                  "executable": true
				                },
				                "option2": {
				                  "type": "literal",
				                  "executable": true
				                }
				              }
				            }
				          }
				        }
				      }
				    },
				    "command4": {
				      "type": "literal",
				      "children": {
				        "option1": {
				          "type": "literal",
				          "children": {
				            "option1": {
				              "type": "literal",
				              "children": {
				                "option1": {
				                  "type": "literal",
				                  "executable": true
				                },
				                "option2": {
				                  "type": "literal",
				                  "executable": true
				                }
				              }
				            },
				            "option2": {
				              "type": "literal",
				              "children": {
				                "option1": {
				                  "type": "literal",
				                  "executable": true
				                },
				                "option2": {
				                  "type": "literal",
				                  "executable": true
				                }
				              }
				            }
				          }
				        },
				        "option2": {
				          "type": "literal",
				          "children": {
				            "option1": {
				              "type": "literal",
				              "children": {
				                "option1": {
				                  "type": "literal",
				                  "executable": true
				                },
				                "option2": {
				                  "type": "literal",
				                  "executable": true
				                }
				              }
				            },
				            "option2": {
				              "type": "literal",
				              "children": {
				                "option1": {
				                  "type": "literal",
				                  "executable": true
				                },
				                "option2": {
				                  "type": "literal",
				                  "executable": true
				                }
				              }
				            }
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

		Player player = addPlayer();

		// /test literal
		assertStoresResult(player, "test literal", results, "literal");

		// /test literal1
		assertStoresResult(player, "test literal1", results, "literal1");

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

		Player player = addPlayer();

		// /test literal
		assertStoresResult(player, "test literal", results, "literal");

		// /test literal1
		assertStoresResult(player, "test literal1", results, "literal1");

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

		Player player = addPlayer();

		// /test lit1 lit1
		assertStoresResult(player, "test lit1 lit1", results, "lit1");
		assertEquals("lit1", results.get());

		// /test lit1 lit3
		assertStoresResult(player, "test lit1 lit3", results, "lit1");
		assertEquals("lit3", results.get());

		// /test lit2 lit1
		assertStoresResult(player, "test lit2 lit1", results, "lit2");
		assertEquals("lit1", results.get());

		// /test lit2 lit3
		assertStoresResult(player, "test lit2 lit3", results, "lit2");
		assertEquals("lit3", results.get());

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithSubcommands() {
		// Doing these because subcommands are converted into MultiLiteralArguments
		Mut<Object> results = Mut.of();

		new CommandAPICommand("test")
			.withSubcommand(new CommandAPICommand("hello")
				.withArguments(new ItemStackArgument("hello"))
				.executesPlayer(info -> {
					results.set(info.args().get("hello"));
				})
			)
			.withSubcommand(new CommandAPICommand("bye")
				.withArguments(new IntegerArgument("bye"))
				.executesPlayer(info -> {
					results.set(info.args().get("bye"));
				})
			)
			.register();

		Player player = addPlayer();

		// /test hello minecraft:stick
		ItemStack item = new ItemStack(Material.STICK);
		assertStoresResult(player, "test hello minecraft:stick", results, item);

		// /test bye 5
		assertStoresResult(player, "test bye 5", results, 5);
	}

	@Test
	void executionTestWithArrayConstructor() {
		Mut<String> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new MultiLiteralArgument("literals", "lit1", "lit2", "lit3"))
			.executesPlayer(info -> {
				results.set((String) info.args().get(0));
			})
			.register();

		Player player = addPlayer();

		assertStoresResult(player, "test lit1", results, "lit1");

		assertStoresResult(player, "test lit2", results, "lit2");

		assertStoresResult(player, "test lit3", results, "lit3");

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

		Player player = addPlayer();

		assertCommandSuggests(player, "test ", "literal", "literal1", "literal2");
	}
}
