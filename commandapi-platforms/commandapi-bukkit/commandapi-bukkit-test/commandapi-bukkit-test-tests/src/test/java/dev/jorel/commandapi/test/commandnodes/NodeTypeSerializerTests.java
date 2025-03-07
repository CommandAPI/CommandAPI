package dev.jorel.commandapi.test.commandnodes;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import dev.jorel.commandapi.CommandAPIHandler;
import dev.jorel.commandapi.arguments.AbstractArgument.NodeInformation;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.commandnodes.NodeTypeSerializer;
import dev.jorel.commandapi.test.TestBase;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link NodeTypeSerializer#addTypeInformation(JsonObject, CommandNode)}
 * via {@link CommandAPIHandler#serializeNodeToJson(CommandNode)}
 */
public class NodeTypeSerializerTests extends TestBase {

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

	public static <Source> RootCommandNode<Source> getArgumentNodes(Argument<?> argument) {
		RootCommandNode<Source> root = new RootCommandNode<>();
		NodeInformation<CommandSender, ?> previousNodeInformation = new NodeInformation<>(
			List.of(root), children -> {}
		);
		argument.addArgumentNodes(
			previousNodeInformation, new ArrayList<>(), new ArrayList<>(), (builder, args) -> builder.build()
		);
		return root;
	}

	public static <Source> JsonObject serialize(CommandNode<Source> node) {
		CommandAPIHandler<?, ?, Source> handler = CommandAPIHandler.getInstance();
		return handler.serializeNodeToJson(node);
	}

	public static String prettyJsonString(JsonObject object) {
		return new GsonBuilder().setPrettyPrinting().create().toJson(object);
	}

	/*********
	 * Tests *
	 *********/
	@Test
	void testUnknownNodeSerialization() {
		CommandNode<?> node = new UnknownCommandNode();
		JsonObject actual = serialize(node);

		JsonObject expected = new JsonObject();
		expected.addProperty("type", "unknown");
		expected.addProperty("typeClassName", "dev.jorel.commandapi.test.commandnodes.UnknownCommandNode");

		assertEquals(expected, actual);
	}

	@Test
	void testFlagsArgumentSerialization() {
		FlagsArgument argument = new FlagsArgument("flags")
			.loopingBranch(new LiteralArgument("literalLoop"))
			.loopingBranch(new StringArgument("argumentLoop"))
			.terminalBranch(new LiteralArgument("literalTerminal"))
			.terminalBranch(new StringArgument("argumentTerminal"));

		RootCommandNode<?> node = getArgumentNodes(argument);
		JsonObject actual = serialize(node);

		assertEquals("""
			{
			  "type": "root",
			  "children": {
			    "flags": {
			      "type": "flagsArgumentRootNode",
			      "children": {
			        "literalLoop": {
			          "type": "flagArgumentEndingLiteral",
			          "flagsArgumentName": "flags",
			          "wrappedNode": {
			            "name": "literalLoop",
			            "type": "literal"
			          },
			          "children": {
			            "commandapi:hiddenRootNode": {
			              "type": "flagArgumentHiddenRedirect",
			              "children": {
			                "flags": [
			                  "flags"
			                ]
			              }
			            },
			            "literalLoop": [
			              "flags",
			              "literalLoop"
			            ],
			            "argumentLoop": [
			              "flags",
			              "argumentLoop"
			            ],
			            "literalTerminal": [
			              "flags",
			              "literalTerminal"
			            ],
			            "argumentTerminal": [
			              "flags",
			              "argumentTerminal"
			            ]
			          }
			        },
			        "argumentLoop": {
			          "type": "flagArgumentEndingArgument",
			          "flagsArgumentName": "flags",
			          "wrappedNode": {
			            "name": "argumentLoop",
			            "type": "argument",
			            "argumentType": "com.mojang.brigadier.arguments.StringArgumentType",
			            "properties": {
			              "type": "word"
			            }
			          },
			          "children": {
			            "commandapi:hiddenRootNode": [
			              "flags",
			              "literalLoop",
			              "commandapi:hiddenRootNode"
			            ],
			            "literalLoop": [
			              "flags",
			              "literalLoop"
			            ],
			            "argumentLoop": [
			              "flags",
			              "argumentLoop"
			            ],
			            "literalTerminal": [
			              "flags",
			              "literalTerminal"
			            ],
			            "argumentTerminal": [
			              "flags",
			              "argumentTerminal"
			            ]
			          }
			        },
			        "literalTerminal": {
			          "type": "flagArgumentEndingLiteral",
			          "flagsArgumentName": "flags",
			          "wrappedNode": {
			            "name": "literalTerminal",
			            "type": "literal"
			          }
			        },
			        "argumentTerminal": {
			          "type": "flagArgumentEndingArgument",
			          "flagsArgumentName": "flags",
			          "wrappedNode": {
			            "name": "argumentTerminal",
			            "type": "argument",
			            "argumentType": "com.mojang.brigadier.arguments.StringArgumentType",
			            "properties": {
			              "type": "word"
			            }
			          }
			        }
			      }
			    }
			  }
			}""", prettyJsonString(actual));
	}

	@Test
	void testNamedLiteralSerialization() {
		// Unlisted literals use Brigadier literal node
		// Listed literals use custom NamedLiteral node
		Argument<?> argument = new MultiLiteralArgument("listedMulti", "option1", "option2")
			.combineWith(
				new MultiLiteralArgument("unlistedMulti", "option1", "option2").setListed(false),
				new LiteralArgument("unlistedLiteral", "option3"),
				new LiteralArgument("listedLiteral", "option4").setListed(true)
			);

		RootCommandNode<?> node = getArgumentNodes(argument);
		JsonObject actual = serialize(node);

		assertEquals("""
			{
			  "type": "root",
			  "children": {
			    "option1": {
			      "type": "namedLiteral",
			      "nodeName": "listedMulti",
			      "children": {
			        "option1": {
			          "type": "literal",
			          "children": {
			            "option3": {
			              "type": "literal",
			              "children": {
			                "option4": {
			                  "type": "namedLiteral",
			                  "nodeName": "listedLiteral"
			                }
			              }
			            }
			          }
			        },
			        "option2": {
			          "type": "literal",
			          "children": {
			            "option3": [
			              "option1",
			              "option1",
			              "option3"
			            ]
			          }
			        }
			      }
			    },
			    "option2": {
			      "type": "namedLiteral",
			      "nodeName": "listedMulti",
			      "children": {
			        "option1": [
			          "option1",
			          "option1"
			        ],
			        "option2": [
			          "option1",
			          "option2"
			        ]
			      }
			    }
			  }
			}""", prettyJsonString(actual));
	}

	@Test
	void testUnnamedArgumentSerialization() {
		// Listed arguments use Brigadier argument node
		// Unlisted arguments use custom UnnamedArgument
		Argument<?> argument = new StringArgument("namedArgument")
			.combineWith(new StringArgument("unnamedArgument").setListed(false));

		RootCommandNode<?> node = getArgumentNodes(argument);
		JsonObject actual = serialize(node);

		assertEquals("""
			{
			  "type": "root",
			  "children": {
			    "namedArgument": {
			      "type": "argument",
			      "argumentType": "com.mojang.brigadier.arguments.StringArgumentType",
			      "properties": {
			        "type": "word"
			      },
			      "children": {
			        "unnamedArgument": {
			          "type": "unnamedArgument",
			          "argumentType": "com.mojang.brigadier.arguments.StringArgumentType",
			          "properties": {
			            "type": "word"
			          }
			        }
			      }
			    }
			  }
			}""", prettyJsonString(actual));
	}

	@ParameterizedTest
	@ValueSource(bytes = {0b000, 0b001, 0b010, 0b011, 0b100, 0b101, 0b110, 0b111})
	void testPreviewableSerialization(byte parameter) {
		// Test all 8 combinations of the 3 boolean parameters
		boolean hasPreview = (parameter & 0b001) == 0;
		boolean legacy = (parameter & 0b010) == 0;
		boolean listed = (parameter & 0b100) == 0;

		// Create argument according to parameters
		Argument<?> argument = (legacy ? new ChatArgument("argument") : new AdventureChatArgument("argument"))
			.withPreview(hasPreview ? PreviewInfo::parsedInput : null);
		argument.setListed(listed);

		RootCommandNode<?> node = getArgumentNodes(argument);
		JsonObject actual = serialize(node);

		assertEquals(String.format("""
			{
			  "type": "root",
			  "children": {
			    "argument": {
			      "type": "previewableArgument",
			      "hasPreview": %s,
			      "legacy": %s,
			      "listed": %s,
			      "argumentType": "net.minecraft.commands.arguments.ArgumentChat"
			    }
			  }
			}""", hasPreview, legacy, listed), prettyJsonString(actual));
	}

	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	void testDynamicMultiLiteralSerialization(boolean listed) {
		DynamicMultiLiteralArgument argument = new DynamicMultiLiteralArgument("argument", sender -> {
			List<String> literals = new ArrayList<>(List.of("first", "second", "third"));
			// For serialization, the sender is unknown, so only the default literals should be included
			if (sender != null) literals.add(sender.getName());

			return literals;
		});
		argument.setListed(listed);

		RootCommandNode<?> node = getArgumentNodes(argument);
		JsonObject actual = serialize(node);

		assertEquals(String.format("""
			{
			  "type": "root",
			  "children": {
			    "argument": {
			      "type": "dynamicMultiLiteral",
			      "isListed": %s,
			      "defaultLiterals": [
			        "first",
			        "second",
			        "third"
			      ]
			    }
			  }
			}""", listed), prettyJsonString(actual));
	}
}
