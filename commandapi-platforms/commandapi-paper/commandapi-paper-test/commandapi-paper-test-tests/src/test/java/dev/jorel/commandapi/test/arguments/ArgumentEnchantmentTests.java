package dev.jorel.commandapi.test.arguments;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EnchantmentArgument;
import dev.jorel.commandapi.test.MockPlatform;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * Tests for the {@link EnchantmentArgument}
 */
class ArgumentEnchantmentTests extends TestBase {

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
	void executionTestWithEnchantmentArgument() {
		Mut<Enchantment> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new EnchantmentArgument("enchantment"))
			.executesPlayer((player, args) -> {
				results.set((Enchantment) args.get("enchantment"));
			})
			.register();

		Player player = addPlayer();

		// /test sharpness
		assertStoresResult(player, "test sharpness", results, Enchantment.SHARPNESS);

		// /test minecraft:sharpness
		assertStoresResult(player, "test minecraft:sharpness", results, Enchantment.SHARPNESS);

		// /test blah
		// Unknown enchantment, blah is not a valid enchantment
		assertCommandFailsWith(player, "test blah", "Can't find element 'minecraft:blah' of type 'minecraft:enchantment'");

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithEnchantmentArgumentAllEnchantments() {
		Mut<Enchantment> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new EnchantmentArgument("enchantment"))
			.executesPlayer((player, args) -> {
				results.set((Enchantment) args.get("enchantment"));
			})
			.register();

		Player player = addPlayer();

		for (Enchantment enchantment : MockPlatform.getInstance().getEnchantments()) {
			assertStoresResult(player, "test " + enchantment.getKey().getKey(), results, enchantment);
		}

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithEnchantmentArgument() {
		new CommandAPICommand("test")
			.withArguments(new EnchantmentArgument("enchantment"))
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		// /test
		// All enchantments should be suggested
		assertCommandSuggests(player, "test minecraft:",
			Arrays.stream(MockPlatform.getInstance().getEnchantments())
				.map(e -> e.getKey())
				.map(NamespacedKey::toString)
				.sorted()
				.toList()
		);

		// /test p
		// All enchantments starting with p should be suggested
		assertCommandSuggests(player, "test p",
			Arrays.stream(MockPlatform.getInstance().getEnchantments())
				.map(e -> e.getKey())
				.filter(s -> s.toString().contains(":p") || s.toString().contains("_p"))
				.map(NamespacedKey::toString)
				.sorted()
				.toList()
		);

		// /test x
		// No enchantments should be suggested
		assertNoSuggestions(player, "test x");
	}
}
