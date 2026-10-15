package dev.jorel.commandapi.test.arguments;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.bukkit.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.world.WorldMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.RotationArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import dev.jorel.commandapi.wrappers.Rotation;

/**
 * Tests for the {@link RotationArgument}
 */
class ArgumentRotationTests extends TestBase {

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
	void executionTestWithRotationArgument() {
		Mut<Rotation> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new RotationArgument("rotation"))
			.executesPlayer((player, args) -> {
				results.set((Rotation) args.get(0));
			})
			.register();

		PlayerMock player = server.addPlayer();

		{
			// /test 90 180
			server.dispatchCommand(player, "test 90 180"); // yaw = 90, pitch = 180
			Rotation rotation = results.get();
			assertEquals(90.0f, rotation.getYaw());
			assertEquals(180.0f, rotation.getPitch());
		}

		{
			// /test 360 360
			server.dispatchCommand(player, "test 360 360");
			Rotation rotation = results.get();
			assertEquals(360.0f, rotation.getYaw());
			assertEquals(360.0f, rotation.getPitch());
		}

		{
			// /test ~ ~
			server.dispatchCommand(player, "test ~ ~");
			Rotation rotation = results.get();
			assertEquals(0.0f, rotation.getYaw());
			assertEquals(0.0f, rotation.getPitch());
		}

		{
			player.setLocation(new Location(new WorldMock(), 2, 2, 2, 75, 135));

			// /test ~ ~
			server.dispatchCommand(player, "test ~ ~");
			Rotation rotation = results.get();
			assertEquals(75.0f, rotation.getYaw());
			assertEquals(135.0f, rotation.getPitch());
		}

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithRotationArgument() {
		new CommandAPICommand("test")
			.withArguments(new RotationArgument("rotation"))
			.executesPlayer(P_EXEC)
			.register();

		PlayerMock player = server.addPlayer();

		// /test
		assertEquals(List.of(), server.getSuggestions(player, "test "));
	}

}
