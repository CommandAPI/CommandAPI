package dev.jorel.commandapi.test.arguments;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.SoundArgument;
import dev.jorel.commandapi.test.MockPlatform;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * Tests for the {@link SoundArgument}
 */
class ArgumentSoundTests extends TestBase {

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
	void executionTestWithSoundArgument() {
		Mut<Sound> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new SoundArgument("sound"))
			.executesPlayer((player, args) -> {
				results.set((Sound) args.get(0));
			})
			.register();

		Player player = addPlayer();

		// /test entity.enderman.death
		assertStoresResult(player, "test entity.enderman.death", results, Sound.ENTITY_ENDERMAN_DEATH);

		// /test minecraft:entity.enderman.death
		assertStoresResult(player, "test minecraft:entity.enderman.death", results, Sound.ENTITY_ENDERMAN_DEATH);

		// /test unknownsound
		assertStoresResult(player, "test unknownsound", results, null);

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithSoundArgumentAllSounds() {
		Mut<Sound> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new SoundArgument("sound"))
			.executesPlayer((player, args) -> {
				results.set((Sound) args.get(0));
			})
			.register();

		Player player = addPlayer();

		for (Sound sound : MockPlatform.getInstance().getSounds()) {
			assertStoresResult(player, "test " + sound.getKey(), results, sound);
		}

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithSoundArgumentNamespaced() {
		Mut<NamespacedKey> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new SoundArgument.NamespacedKey("sound"))
			.executesPlayer((player, args) -> {
				results.set((NamespacedKey) args.get(0));
			})
			.register();

		Player player = addPlayer();

		// /test entity.enderman.death
		assertStoresResult(player, "test entity.enderman.death", results, NamespacedKey.minecraft("entity.enderman.death"));

		// /test minecraft:entity.enderman.death
		assertStoresResult(player, "test minecraft:entity.enderman.death", results, NamespacedKey.minecraft("entity.enderman.death"));

		// /test unknownsound
		assertStoresResult(player, "test unknownsound", results, NamespacedKey.minecraft("unknownsound"));

		// /test mynamespace:unknownsound
		assertStoresResult(player, "test mynamespace:unknownsound", results, new NamespacedKey("mynamespace", "unknownsound"));

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithSoundArgument() {
		new CommandAPICommand("test")
			.withArguments(new SoundArgument("Sound"))
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		// /test
		assertCommandSuggests(player, "test ",
			Arrays.stream(MockPlatform.getInstance().getSounds())
				.map(s -> s.getKey().toString())
				.sorted()
				.toList()
		);

		// /test minecraft:s
		assertCommandSuggests(player, "test minecraft:s",
			Arrays.stream(MockPlatform.getInstance().getSounds())
				.map(s -> s.getKey().toString())
				.filter(s -> s.startsWith("minecraft:s"))
				.sorted()
				.toList()
		);
	}
}
