package dev.jorel.commandapi.test.arguments;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.WorldArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link WorldArgument}
 */
class ArgumentWorldTests extends TestBase {

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
	void executionTestWithWorldArgument() {
		Mut<World> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new WorldArgument("world"))
			.executesPlayer((player, args) -> {
				results.set((World) args.get(0));
			})
			.register();

		Player player = addPlayer();

		World world = getServer().addSimpleWorld("my_world");
		World world_nether = getServer().addSimpleWorld("my_world_nether");
		World world_the_end = getServer().addSimpleWorld("my_world_the_end");

		// Dev note: Comparing worlds in the testing environment
		// Because WorldServer.getWorld() HAS to return a CraftWorld object (instead
		// of a World object), we can't pass the WorldMock instance through it. However
		// we can mock a CraftWorld using the same (randomly generated) UID as a
		// WorldMock instance, so when comparing worlds, please compare the UID
		// of both.
		//
		// When comparing UIDs, don't forget that by default we have a world called
		// "world", so when testing worlds, don't add another world called "world"
		// because that'll lead to unexpected side-effects!

		// /test my_world
		assertStoresResult(player, "test my_world", results, world);

		// /test my_world_nether
		assertStoresResult(player, "test my_world_nether", results, world_nether);

		// /test my_world_the_end
		assertStoresResult(player, "test my_world_the_end", results, world_the_end);

		// /test world_doesnt_exist
		assertCommandFailsWith(player, "test world_doesnt_exist", "Unknown dimension 'minecraft:world_doesnt_exist'");

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithWorldArgument() {
		new CommandAPICommand("test")
			.withArguments(new WorldArgument("world"))
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		getServer().addSimpleWorld("my_world");
		getServer().addSimpleWorld("my_world_nether");
		getServer().addSimpleWorld("my_world_the_end");

		// /test
		assertCommandSuggests(player, "test ",
			"minecraft:my_world", "minecraft:my_world_nether", "minecraft:my_world_the_end", "minecraft:world");

		// /test my_
		assertCommandSuggests(player, "test my_",
			"minecraft:my_world", "minecraft:my_world_nether", "minecraft:my_world_the_end");
	}
}
