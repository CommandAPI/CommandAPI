package dev.jorel.commandapi.test.arguments;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntityTypeArgument;
import dev.jorel.commandapi.test.MockPlatform;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Tests for the {@link EntityTypeArgument}
 */
class ArgumentEntityTypeTests extends TestBase {

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

	/**
	 * @return A sorted list of entity types in the Minecraft namespace. These
	 * start with {@code minecraft:}. The {@code player} and {@code fishing_bobber}
	 * entities are NOT in this list
	 */
	private List<String> getAllEntityTypes() {
		return Arrays.stream(MockPlatform.getInstance().getEntityTypes())
			.filter(e -> e != EntityType.UNKNOWN)
			.filter(e -> e != EntityType.PLAYER)
			.filter(e -> e != EntityType.FISHING_BOBBER)
			.map(e -> e.getKey().toString())
			.sorted()
			.toList();
	}

	/*********
	 * Tests *
	 *********/

	@Test
	void executionTestWithEntityTypeArgument() {
		Mut<EntityType> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new EntityTypeArgument("entity"))
			.executesPlayer((player, args) -> {
				results.set((EntityType) args.get(0));
			})
			.register();

		Player player = addPlayer();

		// /test pig
		assertStoresResult(player, "test pig", results, EntityType.PIG);

		// /test minecraft:pig
		assertStoresResult(player, "test minecraft:pig", results, EntityType.PIG);

		// /test giraffe
		// Unknown entity, giraffe is not a valid entity type
		assertCommandFailsWith(player, "test giraffe", "Can't find element 'minecraft:giraffe' of type 'minecraft:entity_type' at position 12: ...st giraffe<--[HERE]");

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithEntityTypeArgumentAllEntityTypes() {
		Mut<EntityType> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new EntityTypeArgument("entity"))
			.executesPlayer((player, args) -> {
				results.set((EntityType) args.get(0));
			})
			.register();

		Player player = addPlayer();

		for (String entityType : getAllEntityTypes()) {
			assertStoresResult(player, "test " + entityType, results,
				Arrays.stream(EntityType.values()).filter(e -> e.getKey().toString().equals(entityType)).findFirst().get());
		}

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithEntityTypeArgument() {
		new CommandAPICommand("test")
			.withArguments(new EntityTypeArgument("entity"))
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		// /test
		// All entities should be suggested
		assertCommandSuggests(player, "test ", getAllEntityTypes());

		// /test p
		// All entities starting with 'p' should be suggested as well as entities which
		// are underscore-separated and start with 'p', such as 'ender_pearl'
		assertCommandSuggests(player, "test p",
			getAllEntityTypes().stream().filter(s -> s.contains(":p") || s.contains("_p")).toList());

		// /test x
		// No entities should be suggested
		assertNoSuggestions(player, "test x");
	}
}
