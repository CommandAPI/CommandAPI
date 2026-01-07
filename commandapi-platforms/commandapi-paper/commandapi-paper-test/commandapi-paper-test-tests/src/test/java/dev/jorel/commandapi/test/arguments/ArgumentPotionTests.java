package dev.jorel.commandapi.test.arguments;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PotionEffectArgument;
import dev.jorel.commandapi.test.MockPlatform;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

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

	private String getBukkitPotionEffectTypeName(PotionEffectType effectType) {
		return effectType.getKey().toString();
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

		Player player = addPlayer();

		// /test speed
		assertStoresResult(player, "test speed", results, PotionEffectType.SPEED);

		// /test minecraft:speed
		assertStoresResult(player, "test minecraft:speed", results, PotionEffectType.SPEED);

		// /test bukkit:speed
		// Unknown effect, bukkit:speed is not a valid potion effect
		assertCommandFailsWith(player, "test bukkit:speed", "Can't find element 'bukkit:speed' of type 'minecraft:mob_effect'");

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

		Player player = addPlayer();

		for (PotionEffectType potionEffect : MockPlatform.getInstance().getPotionEffects()) {
			assertStoresResult(player, "test " + getBukkitPotionEffectTypeName(potionEffect), results, potionEffect);
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

		Player player = addPlayer();

		assertStoresResult(player, "test speed", results, NamespacedKey.minecraft("speed"));

		assertStoresResult(player, "test minecraft:speed", results, NamespacedKey.minecraft("speed"));

		assertStoresResult(player, "test unknowneffect", results, NamespacedKey.minecraft("unknowneffect"));

		assertStoresResult(player, "test custom:unknowneffect", results, NamespacedKey.fromString("custom:unknowneffect"));

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

		Player player = addPlayer();

		// /test minecraft:
		assertCommandSuggests(player, "test minecraft:",
			Arrays.stream(MockPlatform.getInstance().getPotionEffects())
				.map(this::getBukkitPotionEffectTypeName)
				.sorted()
				.toList()
		);

		// /test minecraft:s
		assertCommandSuggests(player, "test minecraft:s",
			Arrays.stream(MockPlatform.getInstance().getPotionEffects())
				.map(this::getBukkitPotionEffectTypeName)
				.filter(s -> s.startsWith("minecraft:s"))
				.sorted()
				.toList()
		);

		// /test s
		assertCommandSuggests(player, "test s",
			Arrays.stream(MockPlatform.getInstance().getPotionEffects())
				.map(this::getBukkitPotionEffectTypeName)
				.filter(s -> s.startsWith("minecraft:s"))
				.sorted()
				.toList()
		);
	}
}
