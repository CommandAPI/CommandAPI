package dev.jorel.commandapi.test.arguments;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import dev.jorel.commandapi.test.Mut;
import dev.jorel.commandapi.test.TestBase;
import dev.jorel.commandapi.test.mockbukkit.CommandAPIPlayerMock;
import org.bukkit.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@link LocationArgument}
 */
class ArgumentLocationTests extends TestBase {

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

	private void assertLocationEquals(Location loc1, Location loc2) {
		assertEquals(loc1.getWorld(), loc2.getWorld());
		assertEquals(loc1.getX(), loc2.getX(), 0.0001);
		assertEquals(loc1.getY(), loc2.getY(), 0.0001);
		assertEquals(loc1.getZ(), loc2.getZ(), 0.0001);

		// The LocationArgument doesn't fill pitch and yaw, nobody cares about them
//		assertEquals(loc1.getYaw(), loc2.getYaw(), 0.0001);
//		assertEquals(loc1.getPitch(), loc2.getPitch(), 0.0001);
	}

	/*********
	 * Tests *
	 *********/

	@Test
	void executionTestWithLocationArgumentPrecisePosition() {
		Mut<Location> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new LocationArgument("location", LocationType.PRECISE_POSITION))
			.executesPlayer((player, args) -> {
				results.set((Location) args.get(0));
			})
			.register();

		CommandAPIPlayerMock player = addPlayer();

		// /test 1 10 15
		getServer().dispatchCommand(player, "test 1 10 15");
		assertLocationEquals(new Location(null, 1.5, 10.0, 15.5), results.get());

		// /test 1 10 15
		getServer().dispatchCommand(player, "test 1.0 10.0 15.0");
		assertLocationEquals(new Location(null, 1.0, 10.0, 15.0), results.get());

		// /test 1.2 10.2 15.2
		getServer().dispatchCommand(player, "test 1.2 10.2 15.2");
		assertLocationEquals(new Location(null, 1.2, 10.2, 15.2), results.get());

		// test ~ ~5 ~
		// Where the player's position is (2, 2, 2)
		player.setLocation(new Location(player.getWorld(), 2, 2.5, 2));
		getServer().dispatchCommand(player, "test ~ ~5 ~");
		assertLocationEquals(new Location(null, 2.0, 7.5, 2.0), results.get());

		// Test ^ ^ ^ (local) coordinates
		{
			/**
			 * For the horizontal rotation ( yaw ), -180.0 for due north, -90.0 for due
			 * east, 0.0 for due south, 90.0 for due west, to 179.9 for just west of due
			 * north, before wrapping back around to -180.0. For the vertical rotation (
			 * pitch ), -90.0 for straight up to 90.0 for straight down.
			 *
			 * X gives your distance east of the origin, and Z gives the distance south.
			 */

			// test ^ ^ ^5
			// Where the player's position is (2, 2, 2), facing south (positive Z)
			player.setLocation(new Location(player.getWorld(), 2, 2, 2, 0.0f, 0.0f));
			getServer().dispatchCommand(player, "test ^ ^ ^5");
			assertLocationEquals(new Location(null, 2.0, 2.0, 7.0), results.get());

			// As defined in https://minecraft.wiki/w/Coordinates#Local_coordinates:
			// For example, /tp ^ ^ ^5 teleports the player 5 blocks forward. If they turn
			// around and repeat the command, they are teleported back to where they
			// started.

			player.setLocation(new Location(player.getWorld(), 2, 2, 2, -180.0f, 0.0f));
			getServer().dispatchCommand(player, "test ^ ^ ^5");
			assertLocationEquals(new Location(null, 2.0, 2.0, -3.0), results.get());
		}


//		player.setLocation(new Location(player.getWorld(), 2, 2, 2, 0.0f, 0.0f));
//		server.dispatchCommand(player, "test ^ ^ ^5");
//		assertLocationEquals(new Location(null, 2.0, 7.0, 2.0), results.get());

//		server.dispatchCommand(player, "loc3b 1 10 15");
//		server.dispatchCommand(player, "loc2 1 15");
//		server.dispatchCommand(player, "loc2b 1 15");
//		assertEquals("1.5, 10.0, 15.5", player.nextMessage());
//		assertEquals("1.0, 10.0, 15.0", player.nextMessage());
//		assertEquals("1.5, 15.5", player.nextMessage());
//		assertEquals("1.0, 15.0", player.nextMessage());
//		
//		player.setLocation(new Location(new WorldMock(), 2, 2, 2));
//		server.dispatchCommand(player, "loc3 ~ ~5 ~");
//		server.dispatchCommand(player, "loc3b ~ ~5 ~");
//		server.dispatchCommand(player, "loc2 ~ ~5");
//		server.dispatchCommand(player, "loc2b ~ ~5");
//		assertEquals("2.0, 7.0, 2.0", player.nextMessage());
//		assertEquals("2.0, 7.0, 2.0", player.nextMessage());
//		assertEquals("2.0, 7.0", player.nextMessage());
//		assertEquals("2.0, 7.0", player.nextMessage());
	}

	@Test
	void executionTestWithLocationArgumentPrecisePositionNoCentering() {
		// With no centering (LocationArgument(nodeName, LocationType.PRECISE_POSITION, false)),
		// we don't add the +0.5 to each thing if the location is integral
		Mut<Location> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new LocationArgument("location", LocationType.PRECISE_POSITION, false))
			.executesPlayer((player, args) -> {
				results.set((Location) args.get(0));
			})
			.register();

		CommandAPIPlayerMock player = addPlayer();

		// /test 1 10 15
		getServer().dispatchCommand(player, "test 1 10 15");
		assertLocationEquals(new Location(null, 1, 10, 15), results.get());

		// /test 1 10 15
		getServer().dispatchCommand(player, "test 1.0 10.0 15.0");
		assertLocationEquals(new Location(null, 1, 10, 15), results.get());

		// /test 1.2 10.2 15.2
		getServer().dispatchCommand(player, "test 1.2 10.2 15.2");
		assertLocationEquals(new Location(null, 1.2, 10.2, 15.2), results.get());

		// test ~ ~5 ~
		// Where the player's position is (2, 2, 2)
		player.setLocation(new Location(player.getWorld(), 2, 2.5, 2));
		getServer().dispatchCommand(player, "test ~ ~5 ~");
		assertLocationEquals(new Location(null, 2.0, 7.5, 2.0), results.get());
	}

	@Test
	void executionTestWithLocationArgumentBlockPosition() {
		Mut<Location> results = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new LocationArgument("location", LocationType.BLOCK_POSITION))
			.executesPlayer((player, args) -> {
				results.set((Location) args.get(0));
			})
			.register();

		CommandAPIPlayerMock player = addPlayer();

		// /test 1 10 15
		getServer().dispatchCommand(player, "test 1 10 15");
		assertLocationEquals(new Location(null, 1, 10, 15), results.get());

		// test ~ ~5 ~
		// Where the player's position is (2, 2, 2)
		player.setLocation(new Location(player.getWorld(), 2, 2.5, 2));
		getServer().dispatchCommand(player, "test ~ ~5 ~");
		assertLocationEquals(new Location(null, 2, 7, 2), results.get());

		// Test ^ ^ ^ (local) coordinates
		{
			/**
			 * For the horizontal rotation ( yaw ), -180.0 for due north, -90.0 for due
			 * east, 0.0 for due south, 90.0 for due west, to 179.9 for just west of due
			 * north, before wrapping back around to -180.0. For the vertical rotation (
			 * pitch ), -90.0 for straight up to 90.0 for straight down.
			 *
			 * X gives your distance east of the origin, and Z gives the distance south.
			 */

			// test ^ ^ ^5
			// Where the player's position is (2, 2, 2), facing south (positive Z)
			player.setLocation(new Location(player.getWorld(), 2, 2, 2, 0.0f, 0.0f));
			getServer().dispatchCommand(player, "test ^ ^ ^5");
			assertLocationEquals(new Location(null, 2, 2, 7), results.get());

			// As defined in https://minecraft.wiki/w/Coordinates#Local_coordinates:
			// For example, /tp ^ ^ ^5 teleports the player 5 blocks forward. If they turn
			// around and repeat the command, they are teleported back to where they
			// started.

			player.setLocation(new Location(player.getWorld(), 2, 2, 2, -180.0f, 0.0f));
			getServer().dispatchCommand(player, "test ^ ^ ^5");
			assertLocationEquals(new Location(null, 2, 2, -3), results.get());
		}
	}

	/********************
	 * Suggestion tests *
	 ********************/

//	@Test
//	public void suggestionTestWithLocationArgument() {
//		new CommandAPICommand("test")
//			.withArguments(new LocationArgument("team"))
//			.executesPlayer((player, args) -> {
//			})
//			.register();
//
//		Player player = addPlayer();
//
//		Bukkit.getScoreboardManager().getMainScoreboard().registerNewLocation("myteam");
//		Bukkit.getScoreboardManager().getMainScoreboard().registerNewLocation("my_other_team");
//
//		// /test
//		// Should suggest a list of all teams
//		assertCommandSuggests(player, "test ",
//			"my_other_team", "myteam");
//	}
}
