package dev.jorel.commandapi.test.arguments;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LootTableArgument;
import dev.jorel.commandapi.test.MockPlatform;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.loot.LootTable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@link LootTableArgument}
 */
class ArgumentLootTableTests extends TestBase {

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
	void executionTestWithLootTableArgument() {
		Mut<LootTable> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new LootTableArgument("loottable"))
			.executesPlayer((player, args) -> {
				results.set((LootTable) args.get(0));
			})
			.register();

		Player player = addPlayer();

		// /test chests/simple_dungeon
		getServer().dispatchCommand(player, "test chests/simple_dungeon");
		assertEquals(NamespacedKey.minecraft("chests/simple_dungeon"), results.get().getKey());

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithLootTableArgument() {
		new CommandAPICommand("test")
			.withArguments(new LootTableArgument("loottable"))
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		Stream<String> expected = Stream.concat(
			Arrays.stream(MockPlatform.getInstance().getLootTables())
				.map(lt -> lt.getKey().toString()),
			Stream.of("minecraft:entities/camel", "minecraft:entities/sniffer")
		);
		assertCommandSuggests(player, "test ", expected.sorted().toList());
	}
}
