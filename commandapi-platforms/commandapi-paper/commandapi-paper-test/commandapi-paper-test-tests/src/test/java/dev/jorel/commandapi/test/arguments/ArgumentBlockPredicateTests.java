package dev.jorel.commandapi.test.arguments;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.BlockPredicateArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the {@link BlockPredicateArgument}
 * <p>
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
		getServer().getWorlds().get(0).getBlockAt(0, 0, 0).setType(type);
	}

	private Block getBlock() {
		return getServer().getWorlds().get(0).getBlockAt(0, 0, 0);
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

		Player player = addPlayer();

		setBlock(Material.DIRT);

		// /test dirt
		getServer().dispatchCommand(player, "test dirt");
		assertTrue(results.get().test(getBlock()));

		setBlock(Material.OAK_LEAVES);

		// /test #leaves
		getServer().dispatchCommand(player, "test #leaves");
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

		Player player = addPlayer();

		// /test
		assertCommandSuggests(player, "test ", Arrays
			.stream(Material.values())
			.filter(Material::isBlock)
			.filter(Predicate.not(Material::isLegacy))
			.map(Material::getKey)
			.map(NamespacedKey::toString)
			.sorted()
			.toList());

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
