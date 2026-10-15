package dev.jorel.commandapi.test.arguments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandAPIVersionHandler;
import dev.jorel.commandapi.arguments.BiomeArgument;
import dev.jorel.commandapi.test.MockPlatform;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;

/**
 * Tests for the {@link BiomeArgument}
 */
class ArgumentBiomeTests extends TestBase {

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
	void executionTestWithBiomeArgument() {
		Mut<Biome> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new BiomeArgument("biome"))
			.executesPlayer((player, args) -> {
				results.set((Biome) args.get(0));
			})
			.register();

		PlayerMock player = server.addPlayer();

		// /test plains
		server.dispatchCommand(player, "test plains");
		assertEquals(Biome.PLAINS, results.get());
		
		// /test minecraft:plains
		server.dispatchCommand(player, "test minecraft:plains");
		assertEquals(Biome.PLAINS, results.get());
		
		// /test unknownbiome
		if (CommandAPIVersionHandler.IS_PAPER) {
			// Paper's biome argument only accepts biomes that exist
			assertCommandFailsWith(player, "test unknownbiome", "Can't find element 'minecraft:unknownbiome' of type 'minecraft:worldgen/biome' at position 17: ...knownbiome<--[HERE]");
		} else {
			server.dispatchCommand(player, "test unknownbiome");
			assertNull(results.get());
		}

		assertNoMoreResults(results);
	}
	
	@Test
	void executionTestWithBiomeArgumentAllBiomes() {
		Mut<Biome> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new BiomeArgument("biome"))
			.executesPlayer((player, args) -> {
				results.set((Biome) args.get(0));
			})
			.register();

		PlayerMock player = server.addPlayer();
		
		for (Biome biome : MockPlatform.getInstance().getBiomes()) {
			if (biome != Biome.CUSTOM) {
				server.dispatchCommand(player, "test " + biome.getKey());
				assertEquals(biome, results.get());
			}
		}

		assertNoMoreResults(results);
	}
	
	@Test
	void executionTestWithBiomeArgumentNamespaced() {
		Mut<NamespacedKey> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new BiomeArgument.NamespacedKey("biome"))
			.executesPlayer((player, args) -> {
				results.set((NamespacedKey) args.get(0));
			})
			.register();

		PlayerMock player = server.addPlayer();

		// /test plains
		server.dispatchCommand(player, "test plains");
		assertEquals(NamespacedKey.minecraft("plains"), results.get());
		
		// /test minecraft:plains
		server.dispatchCommand(player, "test minecraft:plains");
		assertEquals(NamespacedKey.minecraft("plains"), results.get());
		
		if (CommandAPIVersionHandler.IS_PAPER) {
			// Paper's biome argument only accepts biomes that exist
			assertCommandFailsWith(player, "test unknownbiome", "Can't find element 'minecraft:unknownbiome' of type 'minecraft:worldgen/biome' at position 17: ...knownbiome<--[HERE]");
			assertCommandFailsWith(player, "test mynamespace:unknownbiome", "Can't find element 'mynamespace:unknownbiome' of type 'minecraft:worldgen/biome' at position 29: ...knownbiome<--[HERE]");
		} else {
			// /test unknownbiome
			server.dispatchCommand(player, "test unknownbiome");
			assertEquals(NamespacedKey.minecraft("unknownbiome"), results.get());

			// /test mynamespace:unknownbiome
			server.dispatchCommand(player, "test mynamespace:unknownbiome");
			assertEquals(new NamespacedKey("mynamespace", "unknownbiome"), results.get());
		}

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	// TODO: For reasons I don't know yet, server.getSuggestions for the BiomeArgument
	// throws a NullPointerException when getting suggestions from Mojang Brigadier.
	// Not sure why, so just gonna skip this for now...
//	@Test
//	void suggestionTestWithBiomeArgument() {
//		new CommandAPICommand("test")
//			.withArguments(new BiomeArgument("biome"))
//			.executesPlayer((player, args) -> {
//			})
//			.register();
//
//		PlayerMock player = server.addPlayer();
//
//		// /test
//		assertEquals(
//			Arrays.stream(MockPlatform.getInstance().getBiomes())
//				.map(s -> s.getKey().toString())
//				.sorted()
//				.toList(), 
//			server.getSuggestions(player, "test "));
//
//		// /test minecraft:s
//		assertEquals(
//			Arrays.stream(MockPlatform.getInstance().getBiomes())
//				.map(s -> s.getKey().toString())
//				.filter(s -> s.startsWith("minecraft:s"))
//				.sorted()
//				.toList(),
//			server.getSuggestions(player, "test minecraft:s"));
//		
//	}

}
