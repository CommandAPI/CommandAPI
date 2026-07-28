package dev.jorel.commandapi.arguments;

import org.mockbukkit.mockbukkit.command.ConsoleCommandSenderMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jorel.commandapi.CommandTestBase;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.MockCommandSource;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class EntitySelectorArgumentTests extends CommandTestBase {
	// Setup
	@BeforeEach
	public void setUp() {
		super.setUp();

		new CommandTree("test")
			.then(new LiteralArgument("multiple")
				.then(new LiteralArgument("player")
					.then(new EntitySelectorArgument.ManyPlayers("players").executes(DEFAULT_EXECUTOR))
				)
				.then(new LiteralArgument("entity")
					.then(new EntitySelectorArgument.ManyEntities("entities").executes(DEFAULT_EXECUTOR))
				)
				.then(new LiteralArgument("notEmpty")
					.then(new LiteralArgument("player")
						.then(new EntitySelectorArgument.ManyPlayers("players", false)
							.executes(DEFAULT_EXECUTOR)
						)
					)
					.then(new LiteralArgument("entity")
						.then(new EntitySelectorArgument.ManyEntities("entities", false)
							.executes(DEFAULT_EXECUTOR)
						)
					)
				)
			)
			.then(new LiteralArgument("single")
				.then(new LiteralArgument("player")
					.then(new EntitySelectorArgument.OnePlayer("player").executes(DEFAULT_EXECUTOR))
				)
				.then(new LiteralArgument("entity")
					.then(new EntitySelectorArgument.OneEntity("entity").executes(DEFAULT_EXECUTOR))
				)
			)
			.register();
	}

	@AfterEach
	public void tearDown() {
		super.tearDown();
	}

	// Tests
	@Test
	void testEmptyInput() {
		PlayerMock player = server.addPlayer();

		assertThrowsWithMessage(
			CommandSyntaxException.class,
			() -> createContextWithParser(player, "entities", EntitySelectorParser.parser, ""),
			"Invalid name or UUID at position 0: <--[HERE]"
		);
	}

	@Test
	void testSuggestions_EntitySelectorParser_suggestNameOrSelector() {
		server.addPlayer("Player1");
		server.addPlayer("Player2");

		ConsoleCommandSenderMock console = server.getConsoleSender();
		assertCommandSuggestsTooltips(
			console, "test multiple entity ",
			21,
			makeTooltip("@a", "All players"),
			makeTooltip("@e", "All entities"),
			makeTooltip("@n", "Nearest entity"),
			makeTooltip("@p", "Nearest player"),
			makeTooltip("@r", "Random player"),
			makeTooltip("@s", "Current entity"),
			makeTooltip("Player1", null),
			makeTooltip("Player2", null)
		);
	}

	@Test
	void testErrors_EntitySelectorArgumentType_parse() {
		// Yes, these errors do start at position 0 on a real server, Minecraft's code specifically tells them to do that
		PlayerMock player = server.addPlayer();
		Entity entity = server.getWorlds().get(0).spawn(new Location(null, 0, 0, 0), Pig.class);

		// Single selector required, but multiple targets allowed
		assertCommandFails(
			player, "test single player @a",
			"Only one player is allowed, but the provided selector allows more than one at position 0: <--[HERE]"
		);
		assertCommandFails(
			player, "test single entity @e",
			"Only one entity is allowed, but the provided selector allows more than one at position 0: <--[HERE]"
		);

		// Entities included, but only players allowed
		assertCommandFails(
			player, "test single player @n",
			"Only players may be affected by this command, but the provided selector includes entities at position 0: <--[HERE]"
		);
		assertCommandFails(
			player, "test multiple player @e",
			"Only players may be affected by this command, but the provided selector includes entities at position 0: <--[HERE]"
		);

		// Although, note that player-only selectors parse when using @s, even if the source is not a player
		assertCommandSucceedsWithArguments(
			player, "test single player @s",
			player
		);
		assertCommandFails(
			entity, "test single player @s",
			"No player was found" // Not a parse error, but a retrieval error
		);

		assertCommandSucceedsWithArguments(
			player, "test multiple player @s",
			List.of(player)
		);
		assertCommandSucceedsWithArguments(
			entity, "test multiple player @s",
			List.of() // This one is goofy.
		);
	}

	@Test
	void test_EntitySelectorArgumentType_noEntitiesFound() {
		ConsoleCommandSenderMock console = server.getConsoleSender();

		// Many entities
		assertCommandFails(
			console, "test multiple notEmpty entity @e",
			"No entity was found"
		);
		assertCommandSucceedsWithArguments(
			console, "test multiple entity @e",
			List.of()
		);

		// Single entity
		assertCommandFails(
			console, "test single entity @n",
			"No entity was found"
		);

		// Many players
		assertCommandFails(
			console, "test multiple notEmpty player @a",
			"No player was found"
		);
		assertCommandSucceedsWithArguments(
			console, "test multiple player @a",
			List.of()
		);

		// Single player
		assertCommandFails(
			console, "test single player @r",
			"No player was found"
		);
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	void test_EntitySelectorArgumentType_tooManyEntitiesFound(boolean twoEntities) throws CommandSyntaxException {
		// If you tried to use the EntitySelectorArgument directly to do this, the ArgumentType wouldn't
		//  parse, but you can manually ask to have a single entity when multiple are selected
		PlayerMock player1 = server.addPlayer();

		if (twoEntities) {
			// Success vs failure here depends on whether the selector actually finds too many entities,
			//  not just if it could potentially find too many entities.
			server.addPlayer();
		}

		// Entities
		CommandContext<MockCommandSource> entitiesContext = createContextWithParser(
			player1, "entities", EntitySelectorParser.parser, "@e"
		);

		if (twoEntities) {
			assertThrowsWithMessage(
				CommandSyntaxException.class,
				() -> EntitySelectorArgumentType.findSingleEntity(entitiesContext, "entities"),
				"Only one entity is allowed, but the provided selector allows more than one"
			);
		} else {
			assertEquals(player1, EntitySelectorArgumentType.findSingleEntity(entitiesContext, "entities"));
		}

		// Players
		CommandContext<MockCommandSource> playersContext = createContextWithParser(
			player1, "players", EntitySelectorParser.parser, "@a"
		);

		if (twoEntities) {
			assertThrowsWithMessage(
				CommandSyntaxException.class,
				() -> EntitySelectorArgumentType.findSinglePlayer(playersContext, "players"),
				"No player was found" // This is the way it works. When it finds too many players, it says it found none.
			);
		} else {
			assertEquals(player1, EntitySelectorArgumentType.findSinglePlayer(playersContext, "players"));
		}
	}

	@Test
	void testErrors_EntitySelectorParser_parseSelector() {
		PlayerMock player = server.addPlayer();

		// Missing selector type
		assertCommandFails(
			player, "test multiple entity @",
			"Missing selector type at position 22: ...e entity @<--[HERE]"
		);

		// Unknown selector type
		assertCommandFails(
			player, "test multiple entity @h",
			"Unknown selector type '@h' at position 22: ...e entity @<--[HERE]"
		);
	}

	@Test
	void testSuggestions_EntitySelectorParser_suggestSelector() {
		server.addPlayer("Player1");
		server.addPlayer("Player2");

		ConsoleCommandSenderMock console = server.getConsoleSender();
		assertCommandSuggestsTooltips(
			console, "test multiple entity @",
			21,
			makeTooltip("@a", "All players"),
			makeTooltip("@e", "All entities"),
			makeTooltip("@n", "Nearest entity"),
			makeTooltip("@p", "Nearest player"),
			makeTooltip("@r", "Random player"),
			makeTooltip("@s", "Current entity")
		);
	}

	@Test
	void testSelectorP() {
		// Selects nearest player
		// Layout: p1 e    p2
		PlayerMock p1 = server.addPlayer();
		p1.teleport(p1.getLocation().set(0, 0, 0));

		Entity e = server.getWorlds().get(0).spawn(new Location(null, 1, 0, 0), Pig.class);

		PlayerMock p2 = server.addPlayer();
		p2.teleport(p2.getLocation().set(5, 0, 0));

		assertCommandSucceedsWithArguments(
			p1, "test multiple entity @p",
			List.of(p1)
		);
		assertCommandSucceedsWithArguments(
			e, "test multiple entity @p",
			List.of(p1)
		);
		assertCommandSucceedsWithArguments(
			p2, "test multiple entity @p",
			List.of(p2)
		);
	}

	@Test
	void testSelectorA() {
		// Selects all players in arbitrary order
		//  This will be in order added to the server because it references a LinkedHashSet
		// Layout: p1 e    p2
		PlayerMock p1 = server.addPlayer();
		p1.teleport(p1.getLocation().set(0, 0, 0));

		Entity e = server.getWorlds().get(0).spawn(new Location(null, 1, 0, 0), Pig.class);

		PlayerMock p2 = server.addPlayer();
		p2.teleport(p2.getLocation().set(5, 0, 0));

		assertCommandSucceedsWithArguments(
			p1, "test multiple entity @a",
			List.of(p1, p2)
		);
		assertCommandSucceedsWithArguments(
			e, "test multiple entity @a",
			List.of(p1, p2)
		);
		assertCommandSucceedsWithArguments(
			p2, "test multiple entity @a",
			List.of(p1, p2)
		);
	}

	@Test
	void testSelectorR() {
		// Selects random player
		// Layout: p1 e    p2
		PlayerMock p1 = server.addPlayer();
		p1.teleport(p1.getLocation().set(0, 0, 0));

		Entity e = server.getWorlds().get(0).spawn(new Location(null, 1, 0, 0), Pig.class);

		PlayerMock p2 = server.addPlayer();
		p2.teleport(p2.getLocation().set(5, 0, 0));

		assertAllPlayersRandomlyPossible(p1, Set.of(p1, p2));
		assertAllPlayersRandomlyPossible(e, Set.of(p1, p2));
		assertAllPlayersRandomlyPossible(p2, Set.of(p1, p2));
	}

	private void assertAllPlayersRandomlyPossible(CommandSender source, Set<Player> players) {
		// It feels a little weird to have a test that could randomly fail, but if players are
		//  actually being selected randomly it seems very unlikely that this would give a false negative
		Set<Player> seenSoFar = new HashSet<>();
		for (int i = 0; i < 100; i++) {
			List<Entity> entities = getExecutionInfoOfSuccessfulCommand(source, "test multiple entity @r")
				.args().getUnchecked("entities");

			assertNotNull(entities, "Could not get entities list");
			assertEquals(1, entities.size(), "Expected only one entity, but found: " + entities);

			Entity entity = entities.get(0);
			assertInstanceOf(Player.class, entity, "Expected to find player, but was: " + entity);
			Player player = (Player) entity;

			assertTrue(players.contains(player), "Expected player to be in set " + players + ", but found " + player);
			seenSoFar.add(player);

			if (seenSoFar.equals(players)) return;
		}
		fail("Expected to find all players <" + players + ">, but only saw <" + seenSoFar + ">");
	}

	@Test
	void testSelectorS() {
		// Selects current sender
		// Layout: p1 e    p2
		PlayerMock p1 = server.addPlayer();
		p1.teleport(p1.getLocation().set(0, 0, 0));

		Entity e = server.getWorlds().get(0).spawn(new Location(null, 1, 0, 0), Pig.class);

		PlayerMock p2 = server.addPlayer();
		p2.teleport(p2.getLocation().set(5, 0, 0));

		assertCommandSucceedsWithArguments(
			p1, "test multiple entity @s",
			List.of(p1)
		);
		assertCommandSucceedsWithArguments(
			e, "test multiple entity @s",
			List.of(e)
		);
		assertCommandSucceedsWithArguments(
			p2, "test multiple entity @s",
			List.of(p2)
		);

		// Not an entity, so returns empty list
		ConsoleCommandSenderMock console = server.getConsoleSender();
		assertCommandSucceedsWithArguments(
			console, "test multiple entity @s",
			List.of()
		);
	}

	@Test
	void testSelectorE() {
		// Selects all entities in arbitrary order
		//  This is a little weird, because entities are stored in a HashSet, and their hashes change each time
		//  the test runs because they have random UUIDs. You could bother defining consistent UUIDs to get a random
		//  but consistent order, but I opted to convert the List returned by the argument into a Set and check that.
		// Layout: p1 e    p2
		PlayerMock p1 = server.addPlayer();
		p1.teleport(p1.getLocation().set(0, 0, 0));

		Entity e = server.getWorlds().get(0).spawn(new Location(null, 1, 0, 0), Pig.class);

		PlayerMock p2 = server.addPlayer();
		p2.teleport(p2.getLocation().set(5, 0, 0));

		assertEquals(
			Set.of(p1, e, p2),
			new HashSet<>(Objects.requireNonNull(
				getExecutionInfoOfSuccessfulCommand(p1, "test multiple entity @e")
					.args()
					.<List<Entity>>getUnchecked("entities")
			))
		);
		assertEquals(
			Set.of(p1, e, p2),
			new HashSet<>(Objects.requireNonNull(
				getExecutionInfoOfSuccessfulCommand(e, "test multiple entity @e")
					.args()
					.<List<Entity>>getUnchecked("entities")
			))
		);
		assertEquals(
			Set.of(p1, e, p2),
			new HashSet<>(Objects.requireNonNull(
				getExecutionInfoOfSuccessfulCommand(p2, "test multiple entity @e")
					.args()
					.<List<Entity>>getUnchecked("entities")
			))
		);
	}

	@Test
	void testSelectorN() {
		// Selects nearest entity
		// Layout: p1 e    p2
		PlayerMock p1 = server.addPlayer();
		p1.teleport(p1.getLocation().set(0, 0, 0));

		Entity e = server.getWorlds().get(0).spawn(new Location(null, 1, 0, 0), Pig.class);

		PlayerMock p2 = server.addPlayer();
		p2.teleport(p2.getLocation().set(5, 0, 0));

		assertCommandSucceedsWithArguments(
			p1, "test single entity @n",
			p1
		);
		assertCommandSucceedsWithArguments(
			e, "test single entity @n",
			e
		);
		assertCommandSucceedsWithArguments(
			p2, "test single entity @n",
			p2
		);
	}

	@Test
	void testSuggestions_EntitySelectorParser_suggestOpenOptions() {
		ConsoleCommandSenderMock console = server.getConsoleSender();

		// Valid selector starts options
		assertCommandSuggests(
			console, "test multiple entity @a",
			23, "["
		);

		// Invalid selector suggests overriding with correct selectors
		assertCommandSuggestsTooltips(
			console, "test multiple entity @h",
			21,
			makeTooltip("@a", "All players"),
			makeTooltip("@e", "All entities"),
			makeTooltip("@n", "Nearest entity"),
			makeTooltip("@p", "Nearest player"),
			makeTooltip("@r", "Random player"),
			makeTooltip("@s", "Current entity")
		);
	}

	@Test
	void testErrors_EntitySelectorParser_parseNameOrUUID() {
		PlayerMock player = server.addPlayer();

		// Empty string
		assertCommandFails(
			player, "test multiple entity  ",
			"Invalid name or UUID at position 21: ...le entity <--[HERE]"
		);

		// Too long to be a player name
		assertCommandFails(
			player, "test multiple entity 12345678901234567",
			"Invalid name or UUID at position 21: ...le entity <--[HERE]"
		);
	}

	@Test
	void testSuggestions_EntitySelectorParser_suggestName() {
		server.addPlayer("alice");
		server.addPlayer("allan");
		server.addPlayer("bob");

		ConsoleCommandSenderMock console = server.getConsoleSender();

		// Only suggests names that match remaining
		assertCommandSuggests(
			console, "test multiple entity a",
			21, "alice", "allan"
		);
		assertCommandSuggests(
			console, "test multiple entity all",
			21, "allan"
		);
		assertCommandSuggests(
			console, "test multiple entity b",
			21, "bob"
		);

		// Suggestions are not case-sensitive
		assertCommandSuggests(
			console, "test multiple entity AL",
			21, "alice", "allan"
		);
	}

	@Test
	void testSelectorName() {
		PlayerMock player1 = server.addPlayer("player1");
		PlayerMock player2 = server.addPlayer("player2");
		ConsoleCommandSenderMock console = server.getConsoleSender();

		// Try entity selector
		assertCommandSucceedsWithArguments(
			console, "test multiple entity player1",
			List.of(player1)
		);
		assertCommandSucceedsWithArguments(
			console, "test multiple entity player2",
			List.of(player2)
		);
		assertCommandSucceedsWithArguments(
			console, "test multiple entity invalid",
			List.of()
		);

		// Try player selector
		assertCommandSucceedsWithArguments(
			console, "test multiple player player1",
			List.of(player1)
		);
		assertCommandSucceedsWithArguments(
			console, "test multiple player player2",
			List.of(player2)
		);
		assertCommandSucceedsWithArguments(
			console, "test multiple player invalid",
			List.of()
		);
	}

	@Test
	void testSelectorUUID() throws CommandSyntaxException {
		UUID playerUUID = UUID.randomUUID();
		PlayerMock player = new PlayerMock(server, "player", playerUUID);
		server.addPlayer(player);

		Entity entity = server.getWorlds().get(0).spawn(new Location(null, 0, 0, 0), Pig.class);
		UUID entityUUID = entity.getUniqueId();

		UUID otherUUID = UUID.randomUUID();

		ConsoleCommandSenderMock console = server.getConsoleSender();

		// Try entity selector
		assertCommandSucceedsWithArguments(
			console, "test multiple entity " + playerUUID,
			List.of(player)
		);
		assertCommandSucceedsWithArguments(
			console, "test multiple entity " + entityUUID,
			List.of(entity)
		);
		assertCommandSucceedsWithArguments(
			console, "test multiple entity " + otherUUID,
			List.of()
		);

		// Try player selector
		//  Note that a command like `test multiple player <uuid>` will fail b/c uuid selector includes entities,
		//  but you can still ask an uuid selector to find players directly
		CommandContext<MockCommandSource> playerUUIDContext = createContextWithParser(
			console, "players", EntitySelectorParser.parser, playerUUID.toString()
		);
		assertEquals(
			List.of(player),
			EntitySelectorArgumentType.findManyPlayers(playerUUIDContext, "players", true)
		);

		CommandContext<MockCommandSource> entityUUIDContext = createContextWithParser(
			console, "players", EntitySelectorParser.parser, entityUUID.toString()
		);
		assertEquals(
			List.of(),
			EntitySelectorArgumentType.findManyPlayers(entityUUIDContext, "players", true)
		);

		CommandContext<MockCommandSource> otherUUIDContext = createContextWithParser(
			console, "players", EntitySelectorParser.parser, otherUUID.toString()
		);
		assertEquals(
			List.of(),
			EntitySelectorArgumentType.findManyPlayers(otherUUIDContext, "players", true)
		);
	}
}
