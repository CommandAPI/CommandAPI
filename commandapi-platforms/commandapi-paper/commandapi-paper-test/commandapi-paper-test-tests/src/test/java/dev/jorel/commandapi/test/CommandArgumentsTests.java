package dev.jorel.commandapi.test;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.TextArgument;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommandArgumentsTests extends TestBase {

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
	public void executionTestForRawArgumentsWithOnlyOneArgumentAndRequiredArgumentOnly() {
		Mut<String> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new EntitySelectorArgument.ManyEntities("entities"))
			.executesPlayer(info -> {
				results.set(info.args().getRaw("entities"));
			})
			.register();

		Player player = addPlayer();

		assertStoresResult(player, "test @e", results, "@e");

		assertStoresResult(player, "test @a", results, "@a");

		assertNoMoreResults(results);
	}

	@Test
	public void executionTestForRawArgumentsWithMultipleArgumentsAndRequiredArgumentsOnly() {
		Mut<String> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new DoubleArgument("double"))
			.withArguments(new TextArgument("text"))
			.executesPlayer(info -> {
				results.set(info.args().getRaw("double"));
				results.set(info.args().getRaw("text"));
			})
			.register();

		Player player = addPlayer();

		assertStoresResult(player, "test 15.34 \"This is interesting text\"", results, "15.34");
		assertEquals("\"This is interesting text\"", results.get());

		assertNoMoreResults(results);
	}

	@Test
	public void executionTestForRawArgumentsWithMultipleArgumentsWithMultipleRequiredAndOneOptionalArguments() {
		Mut<String> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new DoubleArgument("double"))
			.withArguments(new TextArgument("text"))
			.withOptionalArguments(new EntitySelectorArgument.ManyEntities("entities"))
			.executesPlayer(info -> {
				results.set(info.args().getRaw("double"));
				results.set(info.args().getRaw("text"));
				results.set(info.args().getRawOptional("entities").orElse("<no-entity-selector-given>"));
			})
			.register();

		Player player = addPlayer();

		assertStoresResult(player, "test 15.34 \"This is interesting text\"", results, "15.34");
		assertEquals("\"This is interesting text\"", results.get());
		assertEquals("<no-entity-selector-given>", results.get());

		assertStoresResult(player, "test 15.34 \"This is interesting text\" @e", results, "15.34");
		assertEquals("\"This is interesting text\"", results.get());
		assertEquals("@e", results.get());

		assertNoMoreResults(results);
	}

	@Test
	public void executionTestForRawArgumentsWithMultipleArgumentsWithMultipleRequiredAndMultipleOptionalArguments() {
		Mut<String> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new DoubleArgument("double"))
			.withArguments(new TextArgument("text"))
			.withOptionalArguments(new EntitySelectorArgument.ManyEntities("entities"))
			.withOptionalArguments(new GreedyStringArgument("message"))
			.executesPlayer(info -> {
				results.set(info.args().getRaw("double"));
				results.set(info.args().getRaw("text"));
				results.set(info.args().getRawOptional("entities").orElse("<no-entity-selector-given>"));
				results.set(info.args().getRawOptional("message").orElse("<no-message-given>"));
			})
			.register();

		Player player = addPlayer();

		assertStoresResult(player, "test 15.34 \"This is interesting text\"", results, "15.34");
		assertEquals("\"This is interesting text\"", results.get());
		assertEquals("<no-entity-selector-given>", results.get());
		assertEquals("<no-message-given>", results.get());

		assertStoresResult(player, "test 15.34 \"This is interesting text\" @e", results, "15.34");
		assertEquals("\"This is interesting text\"", results.get());
		assertEquals("@e", results.get());
		assertEquals("<no-message-given>", results.get());

		assertStoresResult(player, "test 15.34 \"This is interesting text\" @e Hello, everyone! This is a test which passes and doesn't throw any error!", results, "15.34");
		assertEquals("\"This is interesting text\"", results.get());
		assertEquals("@e", results.get());
		assertEquals("Hello, everyone! This is a test which passes and doesn't throw any error!", results.get());

		assertNoMoreResults(results);
	}
}
