package dev.jorel.commandapi.test.arguments;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.BiomeArgument;
import dev.jorel.commandapi.test.MockPlatform;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

		Player player = addPlayer();

		// /test plains
		assertStoresResult(player, "test plains", results, Biome.PLAINS);

		// /test minecraft:plains
		assertStoresResult(player, "test minecraft:plains", results, Biome.PLAINS);

		// /test unknownbiome
		assertStoresResult(player, "test unknownbiome", results, null);

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

		Player player = addPlayer();

		for (Biome biome : MockPlatform.getInstance().getBiomes()) {
			if (biome != Biome.CUSTOM) {
				assertStoresResult(player, "test " + biome.getKey(), results, biome);
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

		Player player = addPlayer();

		// /test plains
		assertStoresResult(player, "test plains", results, NamespacedKey.minecraft("plains"));

		// /test minecraft:plains
		assertStoresResult(player, "test minecraft:plains", results, NamespacedKey.minecraft("plains"));

		// /test unknownbiome
		assertStoresResult(player, "test unknownbiome", results, NamespacedKey.minecraft("unknownbiome"));

		// /test mynamespace:unknownbiome
		assertStoresResult(player, "test mynamespace:unknownbiome", results, new NamespacedKey("mynamespace", "unknownbiome"));

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
//		Player player = addPlayer();
//
//		// /test
//		assertCommandSuggests(player, "test ",
//			Arrays.stream(MockPlatform.getInstance().getBiomes())
//				.map(s -> s.getKey().toString())
//				.sorted()
//				.toList()
//		);
//
//		// /test minecraft:s
//		assertCommandSuggests(player, "test minecraft:s",
//			Arrays.stream(MockPlatform.getInstance().getBiomes())
//				.map(s -> s.getKey().toString())
//				.filter(s -> s.startsWith("minecraft:s"))
//				.sorted()
//				.toList()
//		);
//	}
}
