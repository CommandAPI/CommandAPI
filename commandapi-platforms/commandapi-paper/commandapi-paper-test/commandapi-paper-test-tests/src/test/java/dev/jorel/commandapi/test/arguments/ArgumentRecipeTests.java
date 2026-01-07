package dev.jorel.commandapi.test.arguments;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.RecipeArgument;
import dev.jorel.commandapi.test.MockPlatform;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@link RecipeArgument}
 */
class ArgumentRecipeTests extends TestBase {

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
	void executionTestWithRecipeArgument() {
		Mut<Recipe> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new RecipeArgument("recipe"))
			.executesPlayer((player, args) -> {
				results.set((Recipe) args.get(0));
			})
			.register();

		Player player = addPlayer();

		// /test diamond_pickaxe
		getServer().dispatchCommand(player, "test diamond_pickaxe");
		assertEquals(new ItemStack(Material.DIAMOND_PICKAXE), results.get().getResult());

		// /test unknownRecipe
		assertCommandFailsWith(player, "test unknownrecipe", "Unknown recipe: minecraft:unknownrecipe");

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithRecipeArgumentKeyed() {
		Mut<Keyed> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new RecipeArgument("recipe"))
			.executesPlayer((player, args) -> {
				results.set((Keyed) args.get(0));
			})
			.register();

		Player player = addPlayer();

		for (NamespacedKey str : MockPlatform.getInstance().getAllRecipes()) {
			getServer().dispatchCommand(player, "test " + str.toString());
			assertEquals(str, results.get().getKey());
		}

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithRecipeArgument() {
		new CommandAPICommand("test")
			.withArguments(new RecipeArgument("recipe"))
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		// /test
		assertCommandSuggests(player, "test ",
			MockPlatform.getInstance().getAllRecipes().stream()
				.map(NamespacedKey::toString)
				.sorted()
				.toList()
		);

		// /test minecraft:s
		assertCommandSuggests(player, "test minecraft:s",
			MockPlatform.getInstance().getAllRecipes().stream()
				.map(NamespacedKey::toString)
				.filter(s -> s.startsWith("minecraft:s"))
				.sorted()
				.toList()
		);
	}
}
