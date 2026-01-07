package dev.jorel.commandapi.test.arguments;

import be.seeseemelk.mockbukkit.WorldMock;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.RotationArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import dev.jorel.commandapi.test.mockbukkit.CommandAPIPlayerMock;
import dev.jorel.commandapi.wrappers.Rotation;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

		CommandAPIPlayerMock player = addPlayer();

		{
			// /test 90 180
			getServer().dispatchCommand(player, "test 90 180"); // yaw = 90, pitch = 180
			Rotation rotation = results.get();
			assertEquals(90.0f, rotation.getYaw());
			assertEquals(180.0f, rotation.getPitch());
		}

		{
			// /test 360 360
			getServer().dispatchCommand(player, "test 360 360");
			Rotation rotation = results.get();
			assertEquals(360.0f, rotation.getYaw());
			assertEquals(360.0f, rotation.getPitch());
		}

		{
			// /test ~ ~
			getServer().dispatchCommand(player, "test ~ ~");
			Rotation rotation = results.get();
			assertEquals(0.0f, rotation.getYaw());
			assertEquals(0.0f, rotation.getPitch());
		}

		{
			player.setLocation(new Location(new WorldMock(), 2, 2, 2, 75, 135));

			// /test ~ ~
			getServer().dispatchCommand(player, "test ~ ~");
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

		Player player = addPlayer();

		// /test
		assertNoSuggestions(player, "test ");
	}
}
