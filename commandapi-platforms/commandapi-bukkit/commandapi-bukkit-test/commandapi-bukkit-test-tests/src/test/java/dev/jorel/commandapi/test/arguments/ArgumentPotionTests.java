package dev.jorel.commandapi.test.arguments;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PotionEffectArgument;
import dev.jorel.commandapi.test.MockPlatform;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;

/**
 * Tests for the {@link PotionEffectArgument}
 */
class ArgumentPotionTests extends TestBase {

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
	void executionTestWithPotionEffectArgumentWithNamespaces() {
		Mut<PotionEffectType> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new PotionEffectArgument("potion"))
			.executesPlayer((player, args) -> {
				results.set((PotionEffectType) args.get(0));
			})
			.register();

		PlayerMock player = server.addPlayer();

		// /test speed
		server.dispatchCommand(player, "test speed");
		assertEquals(PotionEffectType.SPEED, results.get());

		// /test minecraft:speed
		server.dispatchCommand(player, "test minecraft:speed");
		assertEquals(PotionEffectType.SPEED, results.get());

		// /test bukkit:speed
		// Unknown effect, bukkit:speed is not a valid potion effect
		assertCommandFailsWith(player, "test bukkit:speed", "Can't find element 'bukkit:speed' of type 'minecraft:mob_effect' at position 17: ...kkit:speed<--[HERE]");

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithPotionEffectArgumentAllPotionEffects() {
		Mut<PotionEffectType> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new PotionEffectArgument("potion"))
			.executesPlayer((player, args) -> {
				results.set((PotionEffectType) args.get("potion"));
			})
			.register();

		PlayerMock player = server.addPlayer();

		for (PotionEffectType potionEffect : MockPlatform.getInstance().getPotionEffects()) {
			server.dispatchCommand(player, "test " + MockPlatform.getInstance().getBukkitPotionEffectTypeName(potionEffect));
			assertEquals(potionEffect, results.get());
		}

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithPotionEffectArgumentNamespaced() {
		Mut<NamespacedKey> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new PotionEffectArgument.NamespacedKey("potion"))
			.executesPlayer((player, args) -> {
				results.set((NamespacedKey) args.get("potion"));
			})
			.register();

		PlayerMock player = server.addPlayer();

		server.dispatchCommand(player, "test speed");
		assertEquals(NamespacedKey.minecraft("speed"), results.get());

		server.dispatchCommand(player, "test minecraft:speed");
		assertEquals(NamespacedKey.minecraft("speed"), results.get());

		// Only existing potion effects can be given, like the PotionEffectType variant
		assertCommandFailsWith(player, "test unknowneffect", "Can't find element 'minecraft:unknowneffect' of type 'minecraft:mob_effect' at position 18: ...nowneffect<--[HERE]");
		assertCommandFailsWith(player, "test custom:unknowneffect", "Can't find element 'custom:unknowneffect' of type 'minecraft:mob_effect' at position 25: ...nowneffect<--[HERE]");

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithPotionEffectArgument() {
		new CommandAPICommand("test")
			.withArguments(new PotionEffectArgument("potion"))
			.executesPlayer(P_EXEC)
			.register();

		PlayerMock player = server.addPlayer();

		// /test minecraft:
		assertEquals(
			Arrays.stream(MockPlatform.getInstance().getPotionEffects())
				.map(MockPlatform.getInstance()::getBukkitPotionEffectTypeName)
				.sorted()
				.toList(),
			server.getSuggestions(player, "test minecraft:")
		);

		// /test minecraft:s
		assertEquals(
			Arrays.stream(MockPlatform.getInstance().getPotionEffects())
				.map(MockPlatform.getInstance()::getBukkitPotionEffectTypeName)
				.filter(s -> s.startsWith("minecraft:s"))
				.sorted()
				.toList(),
			server.getSuggestions(player, "test minecraft:s"));

		// /test s
		assertEquals(
			Arrays.stream(MockPlatform.getInstance().getPotionEffects())
				.map(MockPlatform.getInstance()::getBukkitPotionEffectTypeName)
				.filter(s -> s.startsWith("minecraft:s"))
				.sorted()
				.toList(),
			server.getSuggestions(player, "test s"));
	}

}
