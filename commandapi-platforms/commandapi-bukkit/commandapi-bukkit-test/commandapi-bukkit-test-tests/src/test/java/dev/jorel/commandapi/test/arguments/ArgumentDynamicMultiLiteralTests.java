package dev.jorel.commandapi.test.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.tree.RootCommandNode;
import dev.jorel.commandapi.Brigadier;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.DynamicMultiLiteralArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.commandnodes.DifferentClientNode;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import dev.jorel.commandapi.test.commandnodes.NodeTypeSerializerTests;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@link DynamicMultiLiteralArgument}.
 */
class ArgumentDynamicMultiLiteralTests extends TestBase {

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

	@ParameterizedTest
	@ValueSource(strings = {"PlayerA", "PlayerB"})
	void testClientRewriting(String playerName) {
		Argument<?> argument = new DynamicMultiLiteralArgument("option", sender ->
			sender == null ? List.of("default") : List.of("default", sender.getName())
		).combineWith(new LiteralArgument("following"));

		RootCommandNode<Object> node = NodeTypeSerializerTests.getArgumentNodes(argument);
		Object client = Brigadier.getBrigadierSourceFromCommandSender(server.addPlayer(playerName));

		// DynamicMultiLiteralCommandNode should be used in structure
		JsonObject originalStructure = NodeTypeSerializerTests.serialize(node);
		assertEquals("""
			{
			  "type": "root",
			  "children": {
			    "option": {
			      "type": "dynamicMultiLiteral",
			      "isListed": true,
			      "defaultLiterals": [
			        "default"
			      ],
			      "children": {
			        "following": {
			          "type": "literal"
			        }
			      }
			    }
			  }
			}""", NodeTypeSerializerTests.prettyJsonString(originalStructure));

		// DynamicMultiLiteral should not change when registered
		DifferentClientNode.rewriteAllChildren(client, node, true);

		JsonObject registerRewrite = NodeTypeSerializerTests.serialize(node);
		assertEquals("""
			{
			  "type": "root",
			  "children": {
			    "option": {
			      "type": "dynamicMultiLiteral",
			      "isListed": true,
			      "defaultLiterals": [
			        "default"
			      ],
			      "children": {
			        "following": {
			          "type": "literal"
			        }
			      }
			    }
			  }
			}""", NodeTypeSerializerTests.prettyJsonString(registerRewrite));

		// DynamicMultiLiteral should be replaced with appropriate literal nodes when sent to client
		DifferentClientNode.rewriteAllChildren(client, node, false);

		JsonObject clientRewrite = NodeTypeSerializerTests.serialize(node);
		assertEquals(String.format("""
			{
			  "type": "root",
			  "children": {
			    "default": {
			      "type": "literal",
			      "children": {
			        "following": {
			          "type": "literal"
			        }
			      }
			    },
			    "%s": {
			      "type": "literal",
			      "children": {
			        "following": [
			          "default",
			          "following"
			        ]
			      }
			    }
			  }
			}""", playerName), NodeTypeSerializerTests.prettyJsonString(clientRewrite));
	}

	@Test
	void testMultiLiteralBehavior() {
		Mut<String> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(
				new DynamicMultiLiteralArgument("option", sender -> List.of("a", "b", "c"))
			)
			.executes(info -> {
				results.set(info.args().getUnchecked("option"));
			})
			.register();

		Player sender = server.addPlayer();

		// Defined literals should be accepted
		assertStoresResult(sender, "test a", results, "a");
		assertStoresResult(sender, "test b", results, "b");
		assertStoresResult(sender, "test c", results, "c");

		// Other input should be rejected
		assertCommandFailsWith(
			sender, "test d",
			"Expected literal [a, b, c] at position 5: test <--[HERE]"
		);
		assertCommandFailsWith(
			sender, "test hello",
			"Expected literal [a, b, c] at position 5: test <--[HERE]"
		);

		assertNoMoreResults(results);
	}

	@Test
	void testDynamicBehavior() {
		Mut<String> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(
				new DynamicMultiLiteralArgument("option", sender ->
					sender == null ? List.of("default") : List.of("default", sender.getName())
				)
			)
			.executes(info -> {
				results.set(info.args().getUnchecked("option"));
			})
			.register();

		Player playerA = server.addPlayer("PlayerA");
		Player playerB = server.addPlayer("PlayerB");

		CommandSender console = server.getConsoleSender();

		// Senders should only be able to use their own name option
		assertStoresResult(playerA, "test default", results, "default");
		assertStoresResult(playerA, "test PlayerA", results, "PlayerA");
		assertCommandFailsWith(
			playerA, "test PlayerB",
			"Expected literal [default, PlayerA] at position 5: test <--[HERE]"
		);
		assertCommandFailsWith(
			playerA, "test CONSOLE",
			"Expected literal [default, PlayerA] at position 5: test <--[HERE]"
		);

		assertStoresResult(playerB, "test default", results, "default");
		assertCommandFailsWith(
			playerB, "test PlayerA",
			"Expected literal [default, PlayerB] at position 5: test <--[HERE]"
		);
		assertStoresResult(playerB, "test PlayerB", results, "PlayerB");
		assertCommandFailsWith(
			playerB, "test CONSOLE",
			"Expected literal [default, PlayerB] at position 5: test <--[HERE]"
		);

		assertStoresResult(console, "test default", results, "default");
		assertCommandFailsWith(
			console, "test PlayerA",
			"Expected literal [default, CONSOLE] at position 5: test <--[HERE]"
		);
		assertCommandFailsWith(
			console, "test PlayerB",
			"Expected literal [default, CONSOLE] at position 5: test <--[HERE]"
		);
		assertStoresResult(console, "test CONSOLE", results, "CONSOLE");

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void testMultiLiteralSuggestions() {
		new CommandAPICommand("test")
			.withArguments(
				new DynamicMultiLiteralArgument("option", sender -> List.of("alice", "apple", "bob", "banana"))
			)
			.executesPlayer(P_EXEC)
			.register();

		Player sender = server.addPlayer();

		assertCommandSuggests(
			sender, "test ",
			"alice", "apple", "banana", "bob"
		);

		assertCommandSuggests(
			sender, "test a",
			"alice", "apple"
		);
		assertCommandSuggests(
			sender, "test b",
			"banana", "bob"
		);

		assertCommandSuggests(
			sender, "test al",
			"alice"
		);

		assertCommandSuggests(
			sender, "test c"
		);
	}

	@Test
	void testDynamicSuggestions() {
		new CommandAPICommand("test")
			.withArguments(
				new DynamicMultiLiteralArgument("option", sender ->
					sender == null ? List.of("default") : List.of("default", sender.getName())
				)
			)
			.executesPlayer(P_EXEC)
			.register();

		Player playerA = server.addPlayer("PlayerA");
		Player playerB = server.addPlayer("PlayerB");

		CommandSender console = server.getConsoleSender();

		// Senders should only be suggested their own name option
		assertCommandSuggests(
			playerA, "test ",
			"default", "PlayerA"
		);

		assertCommandSuggests(
			playerB, "test ",
			"default", "PlayerB"
		);

		assertCommandSuggests(
			console, "test ",
			"CONSOLE", "default"
		);
	}
}
