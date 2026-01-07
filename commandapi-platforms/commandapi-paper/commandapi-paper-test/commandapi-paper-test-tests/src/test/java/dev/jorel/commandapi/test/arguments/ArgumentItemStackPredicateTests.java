package dev.jorel.commandapi.test.arguments;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ItemStackPredicateArgument;
import dev.jorel.commandapi.test.MockPlatform;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the {@link ItemStackPredicateArgument}
 */
class ArgumentItemStackPredicateTests extends TestBase {

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
	void executionTestWithItemStackPredicateArgument() {
		Mut<Predicate<ItemStack>> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new ItemStackPredicateArgument("predicate"))
			.executesPlayer((player, args) -> {
				results.set(args.getUnchecked(0));
			})
			.register();

		Player player = addPlayer();

		// /test #axes
		getServer().dispatchCommand(player, "test #axes");
		assertTrue(results.get().test(new ItemStack(Material.DIAMOND_AXE)));

		// TODO: Not sure why this fails, but it does!
		// /test dirt
//		server.dispatchCommand(player, "test dirt{Count:3b}");
//		assertTrue(results.get().test(new ItemStack(Material.DIRT)));
//		assertTrue(results.get().test(new ItemStack(Material.DIRT, 3)));

		// TODO: For some reason, the item registry doesn't have any tags in the
		// testing environment! I'm not sure why, but it would be very nice if
		// we could get tag testing here as well
		// /test #minecraft:planks
//		{
//			server.dispatchCommand(player, "test #minecraft:planks");
//			Predicate<ItemStack> predicate = results.get();
//			assertTrue(predicate.test(new ItemStack(Material.ACACIA_PLANKS)));
//			assertFalse(predicate.test(new ItemStack(Material.DIRT)));
//		}

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithItemStackPredicateArgument() {
		new CommandAPICommand("test")
			.withArguments(new ItemStackPredicateArgument("predicate"))
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		// Identical to the ItemStackArgument tests, except ItemStackPredicates have a slightly
		// different list of possible values because it can also accept * and tags (starting with #)
		List<String> itemPredicateNames = new ArrayList<>(MockPlatform.getInstance().getAllItemNames());
		itemPredicateNames.add(0, "#");
		itemPredicateNames.add(1, "*");

		// /test
		// All items should be suggested
		assertCommandSuggests(player, "test ", itemPredicateNames);

		// /test x
		// All items starting with 'a' should be suggested, as well as items which
		// are underscore-separated and start with 'a', such as 'wooden_axe'
		assertCommandSuggests(player, "test a",
			itemPredicateNames.stream().filter(s -> s.contains(":a") || s.contains("_a")).toList());

		// /test dirt
		// Completed item names should suggest square brackets
		assertCommandSuggests(player, "test dirt", "[");

		// /test dirt[
		// NBT has no suggestions
		assertNoSuggestions(player, "test dirt[");

		// /test dirt[]
		// NBT has no suggestions
		assertNoSuggestions(player, "test dirt[]");
	}
}