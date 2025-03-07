package dev.jorel.commandapi.test.arguments;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;
import java.util.List;

import dev.jorel.commandapi.Converter;
import dev.jorel.commandapi.test.CommandConvertedTestsPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;

/**
 * Tests for the {@link EntitySelectorArgument}
 */
class ArgumentEntitySelectorTests extends TestBase {

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
	void executionTestWithEntitySelectorArgumentOnePlayer() {
		Mut<Player> results = Mut.of();
		
		new CommandAPICommand("test")
			.withArguments(new EntitySelectorArgument.OnePlayer("value"))
			.executesPlayer((player, args) -> {
				results.set((Player) args.get(0));
			})
			.register();
		
		PlayerMock player = server.addPlayer("APlayer");
		
		// /test APlayer
		server.dispatchCommand(player, "test APlayer");
		assertEquals("APlayer", results.get().getName());
		
		// /test @p
		server.dispatchCommand(player, "test @p");
		assertEquals("APlayer", results.get().getName());
		
		// /test @a[limit=1]
		server.dispatchCommand(player, "test @a[limit=1]");
		assertEquals("APlayer", results.get().getName());
		
		// /test @e[limit=1]
		// Fails because @e can accept any entity, but we only want one player
		assertCommandFailsWith(player, "test @e[limit=1]", "Only players may be affected by this command, but the provided selector includes entities at position 0: <--[HERE]");
		
		// /test @e[limit=1,type=player]
		// Should NOT fail because we've specified that the entity type is a player
		// Dev note: This command fails because no entities are found, most likely due to entity lookups not working in the test environment.
		assertNotCommandFailsWith(player, "test @e[limit=1,type=player]", "Only players may be affected by this command, but the provided selector includes entities at position 0: <--[HERE]");
		
		// /test @a
		// Fails because @a can allow more than one player, but we've specified only one player
		assertCommandFailsWith(player, "test @a", "Only one player is allowed, but the provided selector allows more than one at position 0: <--[HERE]");

		assertNoMoreResults(results);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	void executionTestWithEntitySelectorArgumentManyPlayers() {
		Mut<Collection<Player>> results = Mut.of();
		
		new CommandAPICommand("test")
			.withArguments(new EntitySelectorArgument.ManyPlayers("value"))
			.executesPlayer((player, args) -> {
				results.set((Collection<Player>) args.get(0));
			})
			.register();
		
		PlayerMock player = server.addPlayer("APlayer");
		server.addPlayer("APlayer1");
		server.addPlayer("APlayer2");
		server.addPlayer("APlayer3");
		server.addPlayer("APlayer4");
		
		// /test @a
		server.dispatchCommand(player, "test @a");
		assertEquals(List.of("APlayer", "APlayer1", "APlayer2", "APlayer3", "APlayer4"), results.get().stream().map(Player::getName).toList());
		
		assertNoMoreResults(results);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	void executionTestWithEntitySelectorArgumentManyPlayersProhibitEmpty() {
		Mut<Collection<Player>> results = Mut.of();
		
		new CommandAPICommand("test")
			.withArguments(new EntitySelectorArgument.ManyPlayers("value", false))
			.executesPlayer((player, args) -> {
				results.set((Collection<Player>) args.get(0));
			})
			.register();
		
		PlayerMock player = server.addPlayer("APlayer");
		
		// /test @e[gamemode=creative]
		assertCommandFailsWith(player, "test @e[gamemode=creative]", "No player was found");
		
		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithEntitySelectorArgumentOneEntity() {
		Mut<Entity> results = Mut.of();
		
		new CommandAPICommand("test")
			.withArguments(new EntitySelectorArgument.OneEntity("value"))
			.executesPlayer((player, args) -> {
				results.set((Entity) args.get(0));
			})
			.register();
		
		PlayerMock player = server.addPlayer("APlayer");
		
		// /test APlayer
		server.dispatchCommand(player, "test APlayer");
		assertEquals("APlayer", results.get().getName());
		
		// /test @p
		server.dispatchCommand(player, "test @p");
		assertEquals("APlayer", results.get().getName());
		
		// /test @e
		// Fails because @e can allow more than one entity, but we've specified only one entity
		assertCommandFailsWith(player, "test @e", "Only one entity is allowed, but the provided selector allows more than one at position 0: <--[HERE]");
		
		// /test @e[limit=1]
		// Should NOT fail with "Only one entity is allowed" because we've added a single entity limiter to it.
		server.dispatchCommand(player, "test @e[limit=1]");
		assertEquals("APlayer", results.get().getName());
		
		// /test @a
		// Fails because @a can allow more than one entity (player), but we've specified only one entity
		assertCommandFailsWith(player, "test @a", "Only one entity is allowed, but the provided selector allows more than one at position 0: <--[HERE]");

		assertNoMoreResults(results);
	}

	@SuppressWarnings("unchecked")
	@Test
	void executionTestWithEntitySelectorArgumentManyEntities() {
		Mut<Collection<Entity>> results = Mut.of();
		
		new CommandAPICommand("test")
			.withArguments(new EntitySelectorArgument.ManyEntities("value"))
			.executesPlayer((player, args) -> {
				results.set((Collection<Entity>) args.get(0));
			})
			.register();
		
		PlayerMock player = server.addPlayer("APlayer");
		server.addPlayer("APlayer1");
		server.addPlayer("APlayer2");
		server.addPlayer("APlayer3");
		server.addPlayer("APlayer4");
		
		// /test @a
		server.dispatchCommand(player, "test @a");
		assertEquals(List.of("APlayer", "APlayer1", "APlayer2", "APlayer3", "APlayer4"), results.get().stream().map(Entity::getName).toList());
		
		// /test APlayer
		server.dispatchCommand(player, "test APlayer");
		assertEquals(1, results.get().size());
		
		// /test @p
		server.dispatchCommand(player, "test @p");
		assertEquals(1, results.get().size());

		assertNoMoreResults(results);
	}

	@SuppressWarnings("unchecked")
	@Test
	void executionTestWithEntitySelectorArgumentManyEntitiesProhibitEmpty() {
		Mut<Collection<Entity>> results = Mut.of();
		
		new CommandAPICommand("test")
			.withArguments(new EntitySelectorArgument.ManyEntities("value", false))
			.executesPlayer((player, args) -> {
				results.set((Collection<Entity>) args.get(0));
			})
			.register();
		
		PlayerMock player = server.addPlayer("APlayer");
		
		// /test @e[type=pig]
		assertCommandFailsWith(player, "test @e[type=pig]", "No entity was found");

		assertNoMoreResults(results);
	}

	// Converted tests
	@Test
	void convertedTestWithOnePlayerSelector() {
		CommandConvertedTestsPlugin plugin = CommandConvertedTestsPlugin.load();
		Mut<String[]> results = plugin.getCaptureArgsResults();

		Converter.convert(plugin, "captureargs", new EntitySelectorArgument.OnePlayer("player"));

		// Enable server so `minecraft` namespace is accessible by assertStoresArrayResult
		//  This does mean we have to use CraftPlayer mocks to run the command
		enableServer();
		Player playerA = server.addCraftPlayer("PlayerA");
		Player playerB = server.addCraftPlayer("PlayerB");

		// Selected player should unpack to the player's name
		assertStoresArrayResult(playerA, "minecraft:captureargs @s", results, "PlayerA");
		assertStoresArrayResult(playerB, "minecraft:captureargs @s", results, "PlayerB");

		assertNoMoreResults(results);
	}

	@Test
	void convertedTestWithOneEntitySelector() {
		CommandConvertedTestsPlugin plugin = CommandConvertedTestsPlugin.load();
		Mut<String[]> results = plugin.getCaptureArgsResults();

		Converter.convert(plugin, "captureargs", new EntitySelectorArgument.OneEntity("entity"));

		// Enable server so `minecraft` namespace is accessible by assertStoresArrayResult
		//  This does mean we have to use CraftPlayer mocks to run the command
		enableServer();
		Player playerA = server.addCraftPlayer("PlayerA");
		Player playerB = server.addCraftPlayer("PlayerB");

		// Selected entity should unpack to the player's name
		assertStoresArrayResult(playerA, "minecraft:captureargs @s", results, "PlayerA");
		assertStoresArrayResult(playerB, "minecraft:captureargs @s", results, "PlayerB");

		assertNoMoreResults(results);
	}

	@Test
	void convertedTestWithManyPlayerSelector() {
		CommandConvertedTestsPlugin plugin = CommandConvertedTestsPlugin.load();
		Mut<String[]> results = plugin.getCaptureArgsResults();

		Converter.convert(plugin, "captureargs", new EntitySelectorArgument.ManyPlayers("entities"));

		// Enable server so `minecraft` namespace exists
		//  This does mean we have to use CraftPlayer mocks to run the command
		enableServer();
		Player player = server.addCraftPlayer("Player1");
		server.addPlayer("Player2");
		server.addPlayer("Player3");
		server.addPlayer("Player4");
		server.addPlayer("Player5");

		// Command runs once for each selected entity
		server.dispatchCommand(player, "minecraft:captureargs @a");
		assertArrayEquals(new String[]{"Player1"}, results.get());
		assertArrayEquals(new String[]{"Player2"}, results.get());
		assertArrayEquals(new String[]{"Player3"}, results.get());
		assertArrayEquals(new String[]{"Player4"}, results.get());
		assertArrayEquals(new String[]{"Player5"}, results.get());

		assertNoMoreResults(results);
	}

	@Test
	void convertedTestWithManyEntitySelector() {
		CommandConvertedTestsPlugin plugin = CommandConvertedTestsPlugin.load();
		Mut<String[]> results = plugin.getCaptureArgsResults();

		Converter.convert(plugin, "captureargs", new EntitySelectorArgument.ManyEntities("entities"));

		// Enable server so `minecraft` namespace exists
		//  This does mean we have to use CraftPlayer mocks to run the command
		enableServer();
		Player player = server.addCraftPlayer("Player1");
		server.addPlayer("Player2");
		server.addPlayer("Player3");
		server.addPlayer("Player4");
		server.addPlayer("Player5");

		// Command runs once for each selected entity
		server.dispatchCommand(player, "minecraft:captureargs @e");
		assertArrayEquals(new String[]{"Player1"}, results.get());
		assertArrayEquals(new String[]{"Player2"}, results.get());
		assertArrayEquals(new String[]{"Player3"}, results.get());
		assertArrayEquals(new String[]{"Player4"}, results.get());
		assertArrayEquals(new String[]{"Player5"}, results.get());

		assertNoMoreResults(results);
	}
}
