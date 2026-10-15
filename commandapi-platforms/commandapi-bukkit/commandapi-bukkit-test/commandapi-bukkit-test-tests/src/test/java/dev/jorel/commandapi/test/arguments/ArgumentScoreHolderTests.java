package dev.jorel.commandapi.test.arguments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ScoreHolderArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;

/**
 * Tests for the {@link ScoreHolderArgument}
 */
class ArgumentScoreHolderTests extends TestBase {

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
	void executionTestWithScoreHolderArgumentSingle() {
		Mut<String> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new ScoreHolderArgument.Single("holder"))
			.executesPlayer((player, args) -> {
				results.set(args.getUnchecked(0));
			})
			.register();

		PlayerMock player = server.addPlayer("APlayer");

		// /test APlayer
		server.dispatchCommand(player, "test APlayer");
		assertEquals("APlayer", results.get());
		
		UUID uuid = UUID.randomUUID();

		// /test <uuid>
		server.dispatchCommand(player, "test " + uuid);
		assertEquals(uuid.toString(), results.get());

		// /test @e[type=player,scores={deaths=..9},limit=1]
		// This should fail because the user doesn't have permission
		assertCommandFailsWith(player, "test @e[type=player,scores={deaths=..9},limit=1]", "Selector not allowed at position 5: test <--[HERE]");

		// /test @e[type=player,scores={deaths=..9}]
		// The selector permission is checked before the number of entities
		assertCommandFailsWith(player, "test @e[type=player,scores={deaths=..9}]", "Selector not allowed at position 5: test <--[HERE]");

		player.setOp(true);
		
		// /test @e[type=player,scores={deaths=..9},limit=1]
		// TODO: Set an entity
		assertCommandFailsWith(player, "test @e[type=player,scores={deaths=..9},limit=1]", "No entity was found");

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithScoreHolderArgumentMultiple() {
		Mut<Collection<String>> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new ScoreHolderArgument.Multiple("holder"))
			.executesPlayer((player, args) -> {
				results.set(args.getUnchecked(0));
			})
			.register();

		PlayerMock player = server.addPlayer("APlayer");

		// /test APlayer
		server.dispatchCommand(player, "test APlayer");
		assertIterableEquals(Set.of("APlayer"), results.get());
		
		UUID uuid = UUID.randomUUID();

		// /test <uuid>
		server.dispatchCommand(player, "test " + uuid);
		assertIterableEquals(Set.of(uuid.toString()), results.get());

		// /test @e[type=player,scores={deaths=..9},limit=1]
		// This should fail because the user doesn't have permission
		assertCommandFailsWith(player, "test @e[type=player,scores={deaths=..9},limit=1]", "Selector not allowed at position 5: test <--[HERE]");

		// /test @e[type=player,scores={deaths=..9}]
		// This should fail because the user doesn't have permission
		assertCommandFailsWith(player, "test @e[type=player,scores={deaths=..9}]", "Selector not allowed at position 5: test <--[HERE]");

		player.setOp(true);

		// /test @e[type=player,scores={deaths=..9},limit=1]
		// TODO: Set an entity
		assertCommandFailsWith(player, "test @e[type=player,scores={deaths=..9},limit=1]", "No entity was found");

		// /test @e[type=player,scores={deaths=..9}]
		// TODO: Set an entity
		assertCommandFailsWith(player, "test @e[type=player,scores={deaths=..9}]", "No entity was found");

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithScoreHolderArgumentSingle() {
		new CommandAPICommand("test")
		.withArguments(new ScoreHolderArgument.Single("holder"))
			.executesPlayer(P_EXEC)
			.register();

		PlayerMock player = server.addPlayer();

		// /test
		assertIterableEquals(Set.of(), server.getSuggestions(player, "test "));
	}

	@Test
	void suggestionTestWithScoreHolderArgumentMultiple() {
		new CommandAPICommand("test")
		.withArguments(new ScoreHolderArgument.Multiple("holder"))
			.executesPlayer(P_EXEC)
			.register();

		PlayerMock player = server.addPlayer();

		// /test
		assertIterableEquals(Set.of(), server.getSuggestions(player, "test "));
	}

}
