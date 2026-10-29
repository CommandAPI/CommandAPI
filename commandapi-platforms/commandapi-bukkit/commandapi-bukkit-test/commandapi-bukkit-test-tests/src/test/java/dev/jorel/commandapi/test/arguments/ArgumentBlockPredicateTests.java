package dev.jorel.commandapi.test.arguments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.function.Predicate;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.BlockPredicateArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;

/**
 * Tests for the {@link BlockPredicateArgument}
 * 
 * TODO: Screw this argument, it's too complicated to test without having a
 * properly loaded Minecraft world
 */
@Disabled
class ArgumentBlockPredicateTests extends TestBase {

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
	
	private void setBlock(Material type) {
		server.getWorlds().get(0).getBlockAt(0, 0, 0).setType(type);
	}
	
	private Block getBlock() {
		return server.getWorlds().get(0).getBlockAt(0, 0, 0);
	}

	/*********
	 * Tests *
	 *********/

	@Test
	void executionTestWithBlockPredicateArgument() {
		Mut<Predicate<Block>> results = Mut.of();

		new CommandAPICommand("test")
		.withArguments(new BlockPredicateArgument("blockpredicate"))
		.executesPlayer((player, args) -> {
			results.set(args.getUnchecked(0));
		})
		.register();

		PlayerMock player = server.addPlayer();
		
		setBlock(Material.DIRT);

		// /test dirt
		server.dispatchCommand(player, "test dirt");
		assertTrue(results.get().test(getBlock()));
		
		setBlock(Material.OAK_LEAVES);
		
		// /test #leaves
		server.dispatchCommand(player, "test #leaves");
		assertTrue(results.get().test(getBlock()));

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithBlockPredicateArgument() {
		new CommandAPICommand("test")
		.withArguments(new BlockPredicateArgument("blockpredicate"))
		.executesPlayer(P_EXEC)
		.register();

		PlayerMock player = server.addPlayer();

		// /test
		assertEquals(Arrays
			.stream(Material.values())
			.filter(Material::isBlock)
			.filter(Predicate.not(Material::isLegacy))
			.map(Material::getKey)
			.map(NamespacedKey::toString)
			.sorted()
			.toList(), server.getSuggestions(player, "test "));
		
		// /test #
//		assertEquals(Arrays
//			.stream(Material.values())
//			.filter(Material::isBlock)
//			.filter(Predicate.not(Material::isLegacy))
//			.map(Material::getKey)
//			.map(NamespacedKey::toString)
//			.sorted()
//			.toList(), server.getSuggestions(player, "test #"));
	}

}
