package dev.jorel.commandapi.test.arguments;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EnchantmentArgument;
import dev.jorel.commandapi.test.MockPlatform;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;

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

		PlayerMock player = server.addPlayer();

		// /test sharpness
		server.dispatchCommand(player, "test sharpness");
		assertEquals(Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft("sharpness")), results.get());

		// /test minecraft:sharpness
		server.dispatchCommand(player, "test minecraft:sharpness");
		assertEquals(Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft("sharpness")), results.get());

		// /test blah
		// Unknown enchantment, blah is not a valid enchantment
		assertCommandFailsWith(player, "test blah", "Can't find element 'minecraft:blah' of type 'minecraft:enchantment' at position 9: test blah<--[HERE]");

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

		PlayerMock player = server.addPlayer();

		for (Enchantment enchantment : MockPlatform.getInstance().getEnchantments()) {
			server.dispatchCommand(player, "test " + enchantment.getKey().getKey());
			assertEquals(enchantment, results.get());
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

		PlayerMock player = server.addPlayer();
		
		// /test
		// All enchantments should be suggested
		assertEquals(
			Arrays.stream(MockPlatform.getInstance().getEnchantments())
				.map(e -> e.getKey())
				.map(NamespacedKey::toString)
				.sorted()
				.toList(),
			server.getSuggestions(player, "test minecraft:")
		);
		
		// /test p
		// All enchantments starting with p should be suggested
		assertEquals(
			Arrays.stream(MockPlatform.getInstance().getEnchantments())
				.map(e -> e.getKey())
				.filter(s -> s.toString().contains(":p") || s.toString().contains("_p"))
				.map(NamespacedKey::toString)
				.sorted()
				.toList(),
			server.getSuggestions(player, "test p")
		);

		// /test x
		// No enchantments should be suggested
		assertEquals(List.of(), server.getSuggestions(player, "test x"));
	}

}
