package dev.jorel.commandapi.test.arguments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ItemStackPredicateArgument;
import dev.jorel.commandapi.test.MockPlatform;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;

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

		PlayerMock player = server.addPlayer();

		// /test dirt
		server.dispatchCommand(player, "test dirt");
		assertTrue(results.get().test(new ItemStack(Material.DIRT)));

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

		PlayerMock player = server.addPlayer();
		
		// Identical to the ItemStackArgument tests, except ItemStackPredicates have a slightly
		// different list of possible values because it can also accept * and tags (starting with #)
		List<String> itemPredicateNames = new ArrayList<>(MockPlatform.getInstance().getAllItemNames());
		itemPredicateNames.add(0, "#");
		itemPredicateNames.add(1, "*");
		
		// /test
		// All items should be suggested
		assertEquals(itemPredicateNames, server.getSuggestions(player, "test "));
	
		// /test x
		// All items starting with 'a' should be suggested, as well as items which
		// are underscore-separated and start with 'a', such as 'wooden_axe'
		assertEquals(itemPredicateNames.stream().filter(s -> s.contains(":a") || s.contains("_a")).toList(), server.getSuggestions(player, "test a"));
		
		// /test dirt
		// Completed item names should suggest open curly braces (or square brackets in 1.20.5+)
		assertEquals(List.of("["), server.getSuggestions(player, "test dirt"));
		
		// /test dirt{
		// /test dirt[ (1.20.5+)
		// Components (and component predicates) are suggested, as well as ! to negate one and ] to close the list
		List<String> suggestions = server.getSuggestions(player, "test dirt[");
		assertEquals(List.of("!", "]"), suggestions.subList(0, 2));
		assertTrue(suggestions.contains("minecraft:damage"));
		
		// /test dirt{}
		// /test dirt[] (1.20.5+)
		// Nothing is suggested after the NBT or component list
		assertEquals(List.of(), server.getSuggestions(player, "test dirt[]"));
	}

}