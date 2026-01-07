package dev.jorel.commandapi.test.arguments;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ScoreHolderArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

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

		Player player = addPlayer("APlayer");

		// /test APlayer
		assertStoresResult(player, "test APlayer", results, "APlayer");

		UUID uuid = UUID.randomUUID();

		// /test <uuid>
		assertStoresResult(player, "test " + uuid, results, uuid.toString());

		// /test @e[type=player,scores={deaths=..9},limit=1]
		// This should fail because the user doesn't have permission
		assertCommandFailsWith(player, "test @e[type=player,scores={deaths=..9},limit=1]", "Selector not allowed");

		// /test @e[type=player,scores={deaths=..9}]
		assertCommandFailsWith(player, "test @e[type=player,scores={deaths=..9}]", "Only one entity is allowed, but the provided selector allows more than one at position 40: ...aths=..9}]<--[HERE]");

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

		Player player = addPlayer("APlayer");

		// /test APlayer
		getServer().dispatchCommand(player, "test APlayer");
		assertIterableEquals(Set.of("APlayer"), results.get());

		UUID uuid = UUID.randomUUID();

		// /test <uuid>
		getServer().dispatchCommand(player, "test " + uuid);
		assertIterableEquals(Set.of(uuid.toString()), results.get());

		// /test @e[type=player,scores={deaths=..9},limit=1]
		// This should fail because the user doesn't have permission
		assertCommandFailsWith(player, "test @e[type=player,scores={deaths=..9},limit=1]", "Selector not allowed");

		// /test @e[type=player,scores={deaths=..9}]
		// This should fail because the user doesn't have permission
		assertCommandFailsWith(player, "test @e[type=player,scores={deaths=..9}]", "Selector not allowed");

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

		Player player = addPlayer();

		// /test
		assertNoSuggestions(player, "test ");
	}

	@Test
	void suggestionTestWithScoreHolderArgumentMultiple() {
		new CommandAPICommand("test")
			.withArguments(new ScoreHolderArgument.Multiple("holder"))
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		// /test
		assertNoSuggestions(player, "test ");
	}
}
