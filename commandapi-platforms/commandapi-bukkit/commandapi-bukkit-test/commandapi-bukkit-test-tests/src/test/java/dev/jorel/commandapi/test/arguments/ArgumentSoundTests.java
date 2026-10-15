package dev.jorel.commandapi.test.arguments;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.SoundArgument;
import dev.jorel.commandapi.test.MockPlatform;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;

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

		PlayerMock player = server.addPlayer();

		// /test entity.enderman.death
		server.dispatchCommand(player, "test entity.enderman.death");
		assertEquals(Sound.ENTITY_ENDERMAN_DEATH, results.get());
		
		// /test minecraft:entity.enderman.death
		server.dispatchCommand(player, "test minecraft:entity.enderman.death");
		assertEquals(Sound.ENTITY_ENDERMAN_DEATH, results.get());
		
		// /test unknownsound
		server.dispatchCommand(player, "test unknownsound");
		assertEquals(null, results.get());

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

		PlayerMock player = server.addPlayer();
		
		for (Sound sound : MockPlatform.getInstance().getSounds()) {
			server.dispatchCommand(player, "test " + sound.getKey());
			assertEquals(sound, results.get());
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

		PlayerMock player = server.addPlayer();

		// /test entity.enderman.death
		server.dispatchCommand(player, "test entity.enderman.death");
		assertEquals(NamespacedKey.minecraft("entity.enderman.death"), results.get());
		
		// /test minecraft:entity.enderman.death
		server.dispatchCommand(player, "test minecraft:entity.enderman.death");
		assertEquals(NamespacedKey.minecraft("entity.enderman.death"), results.get());
		
		// /test unknownsound
		server.dispatchCommand(player, "test unknownsound");
		assertEquals(NamespacedKey.minecraft("unknownsound"), results.get());
		
		// /test mynamespace:unknownsound
		server.dispatchCommand(player, "test mynamespace:unknownsound");
		assertEquals(new NamespacedKey("mynamespace", "unknownsound"), results.get());

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

		PlayerMock player = server.addPlayer();

		// /test
		assertEquals(
			Arrays.stream(MockPlatform.getInstance().getSounds())
				.map(s -> s.getKey().toString())
				.sorted()
				.toList(), 
			server.getSuggestions(player, "test "));

		// /test minecraft:s
		assertEquals(
			Arrays.stream(MockPlatform.getInstance().getSounds())
				.map(s -> s.getKey().toString())
				.filter(s -> s.startsWith("minecraft:s"))
				.sorted()
				.toList(),
			server.getSuggestions(player, "test minecraft:s"));
		
	}

}
