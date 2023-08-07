package dev.jorel.commandapi.test;

import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProxiedNativeCommandSenderTests extends TestBase {

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
	void testSettingCaller() {
		// We need a CraftPlayer so VanillaCommandWrapper is happy
		Player caller = server.setupMockedCraftPlayer();

		NativeProxyCommandSender proxy = NativeProxyCommandSender.from(caller, null, null, null);

		// Caller should be as given
		assertEquals(caller, proxy.getCaller());
		// Callee defaults to caller since it wasn't given
		assertEquals(caller, proxy.getCallee());

		// Location defaults to caller's location
		assertEquals(caller.getLocation(), proxy.getLocation());
		// World defaults to caller's world
		assertEquals(caller.getWorld(), proxy.getWorld());
	}

	@Test
	void testSettingCallee() {
		Player caller = server.setupMockedCraftPlayer("caller");
		Player callee = server.setupMockedCraftPlayer("callee");

		NativeProxyCommandSender proxy = NativeProxyCommandSender.from(caller, callee, null, null);

		// Caller and callee should be as given
		assertEquals(caller, proxy.getCaller());
		assertEquals(callee, proxy.getCallee());

		// Location defaults to caller's location
		assertEquals(caller.getLocation(), proxy.getLocation());
		// World defaults to caller's world
		assertEquals(caller.getWorld(), proxy.getWorld());
	}

	@Test
	void testSettingLocation() {
		Player caller = server.setupMockedCraftPlayer("caller");

		// `setupMockedCraftPlayer` creates a properly mocked CraftWorld
		World world = server.setupMockedCraftPlayer().getWorld();
		Location location = new Location(world, 1, 2, 3, 4, 5);

		NativeProxyCommandSender proxy = NativeProxyCommandSender.from(caller, null, location, null);

		// Caller should be as given
		assertEquals(caller, proxy.getCaller());
		// Callee defaults to caller since it wasn't given
		assertEquals(caller, proxy.getCallee());

		// Location should be as given
		assertEquals(location, proxy.getLocation());
		// World defaults to location's world
		assertEquals(location.getWorld(), proxy.getWorld());
	}

	@Test
	void testSettingWorld() {
		Player caller = server.setupMockedCraftPlayer("caller");

		// `setupMockedCraftPlayer` creates a properly mocked CraftWorld
		World world = server.setupMockedCraftPlayer().getWorld();

		NativeProxyCommandSender proxy = NativeProxyCommandSender.from(caller, null, null, world);

		// Caller should be as given
		assertEquals(caller, proxy.getCaller());
		// Callee defaults to caller since it wasn't given
		assertEquals(caller, proxy.getCallee());

		// Location should be caller's location but with the given World
		Location expected = caller.getLocation().clone();
		expected.setWorld(world);
		assertEquals(expected, proxy.getLocation());
		// World should be as given
		assertEquals(world, proxy.getWorld());
	}
}
