package dev.jorel.commandapi.test.arguments;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.CustomArgument.CustomArgumentException;
import dev.jorel.commandapi.arguments.CustomArgument.MessageBuilder;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;

/**
 * Tests for the {@link CustomArgument}
 */
class ArgumentCustomTests extends TestBase {

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

	// From the documentation
	public Argument<World> customWorldArgument(String nodeName) {
		return new CustomArgument<World, String>(new StringArgument(nodeName), info -> {
			World world = Bukkit.getWorld(info.input());
			if (world == null) {
				throw CustomArgumentException.fromMessageBuilder(new MessageBuilder("Unknown world: ").appendArgInput());
			} else {
				return world;
			}
		}).replaceSuggestions(ArgumentSuggestions.strings(info -> Bukkit.getWorlds().stream().map(World::getName).toArray(String[]::new)));
	}

	/*********
	 * Tests *
	 *********/

	@Test
	void executionTestWithCustomWorldArgument() {
		Mut<World> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(customWorldArgument("world"))
			.executesPlayer((player, args) -> {
				results.set(args.getUnchecked(0));
			})
			.register();

		PlayerMock player = server.addPlayer();

		WorldMock world1 = server.addSimpleWorld("world1");
		server.addSimpleWorld("world2");
		server.addSimpleWorld("world3");

		// /test world
		assertCommandFailsWith(player, "test unknownworld", "Unknown world: unknownworld");

		// /test world1
		server.dispatchCommand(player, "test world1");
		assertEquals(world1, results.get());

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithCustomWorldArgument() {
		new CommandAPICommand("test")
			.withArguments(customWorldArgument("world"))
			.executesPlayer(P_EXEC)
			.register();

		PlayerMock player = server.addPlayer();

		server.addSimpleWorld("world1");
		server.addSimpleWorld("world2");
		server.addSimpleWorld("world3");

		// /test
		// We expect to see "world" because that's the default world
		assertEquals(List.of("world", "world1", "world2", "world3"), server.getSuggestions(player, "test "));
	}

}
