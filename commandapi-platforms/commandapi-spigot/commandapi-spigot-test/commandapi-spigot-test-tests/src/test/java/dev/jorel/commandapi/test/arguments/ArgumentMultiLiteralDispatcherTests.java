package dev.jorel.commandapi.test.arguments;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.ItemStackArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Dispatcher tests for the {@link MultiLiteralArgument}
 */
public class ArgumentMultiLiteralDispatcherTests extends TestBase {

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

	/********************
	 * Suggestion tests *
	 ********************/

}
