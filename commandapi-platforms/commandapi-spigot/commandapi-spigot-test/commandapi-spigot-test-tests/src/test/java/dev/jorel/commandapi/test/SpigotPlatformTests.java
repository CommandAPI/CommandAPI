package dev.jorel.commandapi.test;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandAPISpigot;
import dev.jorel.commandapi.Platform;
import dev.jorel.commandapi.SpigotCommandRegistration;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Smoke tests for the Spigot test framework: the CommandAPI loads the Spigot platform, and commands can be registered,
 * executed and suggested
 */
class SpigotPlatformTests extends TestBase {

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
	void loadsSpigotPlatform() {
		CommandAPIBukkit<?> platform = CommandAPIBukkit.get();
		assertInstanceOf(CommandAPISpigot.class, platform);
		assertEquals(Platform.SPIGOT, platform.activePlatform());
		assertInstanceOf(SpigotCommandRegistration.class, platform.getCommandRegistrationStrategy());

		// The platform's NMS is our mocked NMS
		assertInstanceOf(MockNMS.class, platform.getNMS());
		assertSame(MockPlatform.getInstance(), platform.getNMS());
	}

	@Test
	void executionTestWithArguments() {
		Mut<String> name = Mut.of();
		Mut<Integer> amount = Mut.of();

		new CommandAPICommand("test")
			.withArguments(new StringArgument("name"), new IntegerArgument("amount", 0, 10))
			.executesPlayer((player, args) -> {
				name.set(args.getUnchecked("name"));
				amount.set(args.getUnchecked("amount"));
			})
			.register();

		PlayerMock player = server.addPlayer();

		server.dispatchCommand(player, "test hello 5");
		assertEquals("hello", name.get());
		assertEquals(5, amount.get());

		assertCommandFailsWith(player, "test hello 11", "Integer must not be more than 10: found 11 at position 11: ...est hello <--[HERE]");

		assertNoMoreResults(name);
		assertNoMoreResults(amount);
	}

	@Test
	void executionTestWithNamespace() {
		Mut<PlayerMock> results = Mut.of();

		new CommandAPICommand("test")
			.executesPlayer((player, args) -> {
				results.set((PlayerMock) player);
			})
			.register("custom");

		PlayerMock player = server.addPlayer();

		server.dispatchCommand(player, "test");
		assertSame(player, results.get());

		server.dispatchCommand(player, "custom:test");
		assertSame(player, results.get());

		assertNoMoreResults(results);
	}

	@Test
	void suggestionTestWithMultiLiteralArgument() {
		new CommandAPICommand("test")
			.withArguments(new MultiLiteralArgument("option", "alpha", "beta", "gamma"))
			.executesPlayer((player, args) -> {})
			.register();

		PlayerMock player = server.addPlayer();

		assertCommandSuggests(player, "test ", "alpha", "beta", "gamma");
		assertCommandSuggests(player, "test b", "beta");
	}

	@Test
	void enableServerStopsRegistration() {
		enableServer();
	}
}
