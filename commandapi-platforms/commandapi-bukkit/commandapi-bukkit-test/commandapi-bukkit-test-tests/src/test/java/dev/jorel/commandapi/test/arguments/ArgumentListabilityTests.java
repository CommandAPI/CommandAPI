package dev.jorel.commandapi.test.arguments;

import com.mojang.brigadier.tree.CommandNode;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Tests for the effects of the {@link AbstractArgument#setListed(boolean)} method.
 * Arguments may use different {@link CommandNode} implementation that implement this with separate code.
 */
class ArgumentListabilityTests extends TestBase {

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
	void testUnlistedRequiredArguments() {
		Mut<Integer> results = Mut.of();
		Player sender = server.addPlayer();

		// Unlisted arguments before or after should not interfere with parsing
		new CommandAPICommand("test")
			.withArguments(
				new StringArgument("number").setListed(false),
				new IntegerArgument("number"),
				new StringArgument("number").setListed(false)
			)
			.executes(info -> {
				results.set(info.args().getUnchecked("number"));
			})
			.register();

		assertStoresResult(sender, "test abc 10 def", results, 10);
		assertStoresResult(sender, "test 0 1 2", results, 1);

		assertNoMoreResults(results);
	}

	@Test
	void testUnlistedLiterals() {
		Mut<Integer> results = Mut.of();

		// Default unlisted literals should not overwrite parsed results
		new CommandAPICommand("test")
			.withArguments(
				new LiteralArgument("number"),
				new IntegerArgument("number"),
				new LiteralArgument("number")
			)
			.executesPlayer(info -> {
				results.set(info.args().getUnchecked("number"));
			})
			.register();

		Player player = server.addPlayer();

		// Unlisted MultiLiterals before or after should not affect the result given
		assertStoresResult(player, "test number 0 number", results, 0);
		assertStoresResult(player, "test number 10 number", results, 10);

		assertNoMoreResults(results);
	}

	@Test
	void testUnlistedMultiLiterals() {
		Mut<String> results = Mut.of();

		// This command is valid because the listed arguments do not repeat names
		new CommandAPICommand("test")
			.withArguments(
				new MultiLiteralArgument("literal", "a", "b", "c").setListed(false),
				new MultiLiteralArgument("literal", "c", "d", "e"),
				new MultiLiteralArgument("literal", "f", "g", "h").setListed(false)
			)
			.executesPlayer(info -> {
				results.set(info.args().getUnchecked("literal"));
			})
			.register();

		Player player = server.addPlayer();

		// Unlisted MultiLiterals before or after should not affect the result given
		assertStoresResult(player, "test a c f", results, "c");
		assertStoresResult(player, "test b d g", results, "d");
		assertStoresResult(player, "test c e h", results, "e");

		assertNoMoreResults(results);
	}

	@Test
	void testUnlistedPreviewableArguments() {
		Mut<Integer> results = Mut.of();
		Player sender = server.addPlayer();

		// Unlisted arguments before or after should not interfere with parsing
		//  Though all current Previewable arguments are also Greedy, so you can't
		//  have an argument after it anyway ¯\_(ツ)_/¯
		new CommandAPICommand("test")
			.withArguments(
				new IntegerArgument("number"),
				new ChatArgument("number").setListed(false)
			)
			.executes(info -> {
				results.set(info.args().getUnchecked("number"));
			})
			.register();

		assertStoresResult(sender, "test 10 def", results, 10);
		assertStoresResult(sender, "test 1 2", results, 1);

		assertNoMoreResults(results);
	}

	@Test
	void testUnlistedDynamicMultiLiteralArguments() {
		Mut<String> results = Mut.of();
		Player sender = server.addPlayer();

		new CommandAPICommand("test")
			.withArguments(
				new DynamicMultiLiteralArgument("literal", s -> List.of("a", "b", "c")).setListed(false),
				new DynamicMultiLiteralArgument("literal", s -> List.of("d", "e", "f")),
				new DynamicMultiLiteralArgument("literal", s -> List.of("g", "h", "i")).setListed(false)
			)
			.executes(info -> {
				results.set(info.args().getUnchecked("literal"));
			})
			.register();

		assertStoresResult(sender, "test a d g", results, "d");
		assertStoresResult(sender, "test b e h", results, "e");
		assertStoresResult(sender, "test c f i", results, "f");

		assertNoMoreResults(results);
	}
}
