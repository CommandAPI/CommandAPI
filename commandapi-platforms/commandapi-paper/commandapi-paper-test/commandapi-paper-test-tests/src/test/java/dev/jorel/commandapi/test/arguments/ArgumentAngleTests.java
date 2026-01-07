package dev.jorel.commandapi.test.arguments;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.AngleArgument;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import dev.jorel.commandapi.test.mockbukkit.CommandAPIPlayerMock;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@link AngleArgument}
 */
class ArgumentAngleTests extends TestBase {

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

	private void setPlayerYaw(CommandAPIPlayerMock player, float yaw) {
		Location location = player.getLocation();
		location.setYaw(yaw);
		player.setLocation(location);
	}

	/*********
	 * Tests *
	 *********/

	@Test
	void executionTestWithAngleArgument() {
		Mut<Float> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new AngleArgument("angle"))
			.executesPlayer((player, args) -> {
				results.set(args.getUnchecked(0));
			})
			.register();

		Player player = addPlayer();

		// /test 10
		getServer().dispatchCommand(player, "test 10");
		assertEquals(10.0f, results.get(), 0.1);

		// /test -180
		getServer().dispatchCommand(player, "test -180");
		assertEquals(-180.0f, results.get(), 0.1);

		// /test 180
		getServer().dispatchCommand(player, "test 180");
		assertEquals(-180.0f, results.get(), 0.1);

		assertNoMoreResults(results);
	}

	@Test
	void executionTestWithAngleArgumentRelative() {
		Mut<Float> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new AngleArgument("angle"))
			.executesPlayer((player, args) -> {
				results.set(args.getUnchecked(0));
			})
			.register();

		CommandAPIPlayerMock player = addPlayer();

		/**
		 * Bukkit's yaw values differ from what the AngleArgument returns:
		 * A yaw of 0 or 360 represents the positive z direction.
		 * A yaw of 180 represents the negative z direction.
		 * A yaw of 90 represents the negative x direction.
		 * A yaw of 270 represents the positive x direction.
		 *
		 * negative z = north
		 * positive x = east
		 * positive z = south
		 * negative x = west
		 *
		 * | Bukkit     | AngleArgument |
		 * | value      | value         |
		 * |------------|---------------|
		 * |          0 |             0 |
		 * |         90 |            90 |
		 * |        180 |          -180 |
		 * |        270 |           -90 |
		 * |        360 |             0 |
		 */

		/**
		 * Developer's note: The NMS AngleArgument uses the rotation from the CSS
		 * using CommandSourceStack#getRotation().y.
		 *
		 * The minecraft:angle argument specifies that it must be a yaw angle,
		 * so by definition:
		 *
		 *   CommandSourceStack#getRotation().y = yaw
		 *
		 * And subsequently:
		 *
		 *   CommandSourceStack#getRotation().x = pitch
		 *
		 * This gets even more confusing when you take into account that the
		 * RotationArgument (minecraft:rotation) uses (yaw, pitch), but the
		 * resulting Vec2F returns (pitch, yaw)
		 */

		setPlayerYaw(player, 0);

		// /test ~
		getServer().dispatchCommand(player, "test ~");
		assertEquals(0.0f, results.get(), 0.1);

		setPlayerYaw(player, 90);

		// /test ~
		getServer().dispatchCommand(player, "test ~");
		assertEquals(90.0f, results.get(), 0.1);

		setPlayerYaw(player, 180);

		// /test ~
		getServer().dispatchCommand(player, "test ~");
		assertEquals(-180.0f, results.get(), 0.1);

		setPlayerYaw(player, 270);

		// /test ~
		getServer().dispatchCommand(player, "test ~");
		assertEquals(-90.0f, results.get(), 0.1);

		setPlayerYaw(player, 360);

		// /test ~
		getServer().dispatchCommand(player, "test ~");
		assertEquals(0.0f, results.get(), 0.1);

		assertNoMoreResults(results);
	}

	/********************
	 * Suggestion tests *
	 ********************/

	@Test
	void suggestionTestWithAngleArgument() {
		new CommandAPICommand("test")
			.withArguments(new AngleArgument("angle"))
			.executesPlayer(P_EXEC)
			.register();

		Player player = addPlayer();

		// /test
		// The angle argument doesn't have any suggestions
		assertNoSuggestions(player, "test ");
	}
}
