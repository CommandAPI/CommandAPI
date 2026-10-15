package dev.jorel.commandapi.test;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.*;
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

		PlayerMock player = server.addPlayer();
		// Paper checks the minecraft.command.selector permission when selectors are used (Spigot doesn't)
		player.setOp(true);

		server.dispatchCommand(player, "test @e");
		assertEquals("@e", results.get());

		server.dispatchCommand(player, "test @a");
		assertEquals("@a", results.get());

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

		PlayerMock player = server.addPlayer();

		server.dispatchCommand(player, "test 15.34 \"This is interesting text\"");
		assertEquals("15.34", results.get());
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

		PlayerMock player = server.addPlayer();
		// Paper checks the minecraft.command.selector permission when selectors are used (Spigot doesn't)
		player.setOp(true);

		server.dispatchCommand(player, "test 15.34 \"This is interesting text\"");
		assertEquals("15.34", results.get());
		assertEquals("\"This is interesting text\"", results.get());
		assertEquals("<no-entity-selector-given>", results.get());

		server.dispatchCommand(player, "test 15.34 \"This is interesting text\" @e");
		assertEquals("15.34", results.get());
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

		PlayerMock player = server.addPlayer();
		// Paper checks the minecraft.command.selector permission when selectors are used (Spigot doesn't)
		player.setOp(true);

		server.dispatchCommand(player, "test 15.34 \"This is interesting text\"");
		assertEquals("15.34", results.get());
		assertEquals("\"This is interesting text\"", results.get());
		assertEquals("<no-entity-selector-given>", results.get());
		assertEquals("<no-message-given>", results.get());

		server.dispatchCommand(player, "test 15.34 \"This is interesting text\" @e");
		assertEquals("15.34", results.get());
		assertEquals("\"This is interesting text\"", results.get());
		assertEquals("@e", results.get());
		assertEquals("<no-message-given>", results.get());

		server.dispatchCommand(player, "test 15.34 \"This is interesting text\" @e Hello, everyone! This is a test which passes and doesn't throw any error!");
		assertEquals("15.34", results.get());
		assertEquals("\"This is interesting text\"", results.get());
		assertEquals("@e", results.get());
		assertEquals("Hello, everyone! This is a test which passes and doesn't throw any error!", results.get());

		assertNoMoreResults(results);
	}

}
