package dev.jorel.commandapi.test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import be.seeseemelk.mockbukkit.command.ConsoleCommandSenderMock;
import be.seeseemelk.mockbukkit.entity.SimpleEntityMock;
import dev.jorel.commandapi.BukkitExecutable;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.executors.ExecutorType;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the methods provided by {@link BukkitExecutable}.
 */
class CommandExecutionTests extends TestBase {

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

	private final Map<ExecutorType, Supplier<CommandSender>> executorTypeToMockSender = new LinkedHashMap<>();

	{
		// Not using Map.of here because I want the values to be in insertion order,
		//  rather than in whatever order the hash code feels like, which makes debugging easier
		executorTypeToMockSender.put(ExecutorType.PLAYER, () -> server.addCraftPlayer());
		executorTypeToMockSender.put(ExecutorType.ENTITY, () -> new SimpleEntityMock(server));
		executorTypeToMockSender.put(ExecutorType.CONSOLE, ConsoleCommandSenderMock::new);
		executorTypeToMockSender.put(ExecutorType.BLOCK, () -> Mockito.mock(BlockCommandSender.class));
		// This is a little odd, but `ProxiedCommandSender`s will always be CommandAPI `NativeProxyCommandSender`s.
		//  It is possible that a different plugin could implement the `ProxiedCommandSender`, which would cause
		//  a class cast exception if that sender tried to run an `executesProxy` executor. However, the Spigot/Paper
		//  server itself will never use its own `org.bukkit.craftbukkit.command.ProxiedNativeCommandSender` class.
		//  So, if you can make a class cast exception happen on a server, change this mock to `ProxiedCommandSender`
		//  and fix `executesProxy`, but otherwise we can provide the more specific `NativeProxyCommandSender` class.
//		executorTypeToMockSender.put(ExecutorType.PROXY, () -> Mockito.mock(ProxiedCommandSender.class));
		executorTypeToMockSender.put(ExecutorType.PROXY, () -> Mockito.mock(NativeProxyCommandSender.class));
		executorTypeToMockSender.put(ExecutorType.REMOTE, () -> Mockito.mock(RemoteConsoleCommandSender.class));
	}

	private void testAllCommands(
		Function<String, Consumer<CommandSender>> shouldSucceed,
		Function<String, Consumer<CommandSender>> shouldFail,
		List<String> commandNames,
		ExecutorType... types
	) {
		for (String name : commandNames) {
			try {
				assertOnlyDefinedExecutorTypesSucceed(shouldSucceed.apply(name), shouldFail.apply(name), types);
			} catch (Throwable e) {
				fail("Unexpected error while running command with name \"" + name + "\"", e);
			}
		}
	}

	private void assertOnlyDefinedExecutorTypesSucceed(
		Consumer<CommandSender> shouldSucceed, Consumer<CommandSender> shouldFail, ExecutorType... types
	) {
		List<ExecutorType> typesList = Arrays.asList(types);

		for (Map.Entry<ExecutorType, Supplier<CommandSender>> executorType : executorTypeToMockSender.entrySet()) {
			try {
				(typesList.contains(executorType.getKey()) ? shouldSucceed : shouldFail).accept(executorType.getValue().get());
			} catch (Throwable e) {
				fail("Unexpected error while running command with executor type \"" + executorType.getKey() + "\"", e);
			}
		}
	}

	/*********
	 * Tests *
	 *********/

	// Single sender type allowed
	@Test
	void testPlayerExecution() {
		final Mut<Player> results = Mut.of();

		new CommandAPICommand("normalinfo")
			.executesPlayer(info -> {
				results.set(info.sender());
			})
			.register();

		new CommandAPICommand("normal")
			.executesPlayer((player, args) -> {
				results.set(player);
			})
			.register();

		new CommandAPICommand("resultinginfo")
			.executesPlayer(info -> {
				results.set(info.sender());
				return 2;
			})
			.register();

		new CommandAPICommand("resulting")
			.executesPlayer((player, args) -> {
				results.set(player);
				return 2;
			})
			.register();

		testAllCommands(
			name -> sender -> assertStoresResult(sender, name, results, (Player) sender),
			name -> sender -> assertCommandFailsWith(sender, name,
				"This command has no implementations for " + sender.getClass().getSimpleName().toLowerCase()),
			List.of("normal", "normalinfo", "resultinginfo", "resulting"),
			ExecutorType.PLAYER
		);

		assertNoMoreResults(results);
	}

	@Test
	void testEntityExecution() {
		final Mut<Entity> results = Mut.of();

		new CommandAPICommand("normalinfo")
			.executesEntity(info -> {
				results.set(info.sender());
			})
			.register();

		new CommandAPICommand("normal")
			.executesEntity((entity, args) -> {
				results.set(entity);
			})
			.register();

		new CommandAPICommand("resultinginfo")
			.executesEntity(info -> {
				results.set(info.sender());
				return 2;
			})
			.register();

		new CommandAPICommand("resulting")
			.executesEntity((entity, args) -> {
				results.set(entity);
				return 2;
			})
			.register();

		testAllCommands(
			name -> sender -> assertStoresResult(sender, name, results, (Entity) sender),
			name -> sender -> assertCommandFailsWith(sender, name,
				"This command has no implementations for " + sender.getClass().getSimpleName().toLowerCase()),
			List.of("normal", "normalinfo", "resultinginfo", "resulting"),
			// Players count as Entities
			ExecutorType.ENTITY, ExecutorType.PLAYER
		);

		assertNoMoreResults(results);
	}

	@Test
	void testConsoleExecution() {
		final Mut<ConsoleCommandSender> results = Mut.of();

		new CommandAPICommand("normalinfo")
			.executesConsole(info -> {
				results.set(info.sender());
			})
			.register();

		new CommandAPICommand("normal")
			.executesConsole((console, args) -> {
				results.set(console);
			})
			.register();

		new CommandAPICommand("resultinginfo")
			.executesConsole(info -> {
				results.set(info.sender());
				return 2;
			})
			.register();

		new CommandAPICommand("resulting")
			.executesConsole((console, args) -> {
				results.set(console);
				return 2;
			})
			.register();

		testAllCommands(
			name -> sender -> assertStoresResult(sender, name, results, (ConsoleCommandSender) sender),
			name -> sender -> assertCommandFailsWith(sender, name,
				"This command has no implementations for " + sender.getClass().getSimpleName().toLowerCase()),
			List.of("normal", "normalinfo", "resultinginfo", "resulting"),
			ExecutorType.CONSOLE
		);

		assertNoMoreResults(results);
	}

	@Test
	void testBlockExecution() {
		final Mut<BlockCommandSender> results = Mut.of();

		new CommandAPICommand("normalinfo")
			.executesCommandBlock(info -> {
				results.set(info.sender());
			})
			.register();

		new CommandAPICommand("normal")
			.executesCommandBlock((block, args) -> {
				results.set(block);
			})
			.register();

		new CommandAPICommand("resultinginfo")
			.executesCommandBlock(info -> {
				results.set(info.sender());
				return 2;
			})
			.register();

		new CommandAPICommand("resulting")
			.executesCommandBlock((block, args) -> {
				results.set(block);
				return 2;
			})
			.register();

		testAllCommands(
			name -> sender -> assertStoresResult(sender, name, results, (BlockCommandSender) sender),
			name -> sender -> assertCommandFailsWith(sender, name,
				"This command has no implementations for " + sender.getClass().getSimpleName().toLowerCase()),
			List.of("normal", "normalinfo", "resultinginfo", "resulting"),
			ExecutorType.BLOCK
		);
	}

	@Test
	void testProxyExecution() {
		final Mut<NativeProxyCommandSender> results = Mut.of();

		new CommandAPICommand("normalinfo")
			.executesProxy(info -> {
				results.set(info.sender());
			})
			.register();

		new CommandAPICommand("normal")
			.executesProxy((proxy, args) -> {
				results.set(proxy);
			})
			.register();

		new CommandAPICommand("resultinginfo")
			.executesProxy(info -> {
				results.set(info.sender());
				return 2;
			})
			.register();

		new CommandAPICommand("resulting")
			.executesProxy((proxy, args) -> {
				results.set(proxy);
				return 2;
			})
			.register();

		testAllCommands(
			name -> sender -> assertStoresResult(sender, name, results, (NativeProxyCommandSender) sender),
			name -> sender -> assertCommandFailsWith(sender, name,
				"This command has no implementations for " + sender.getClass().getSimpleName().toLowerCase()),
			List.of("normal", "normalinfo", "resultinginfo", "resulting"),
			ExecutorType.PROXY
		);

		assertNoMoreResults(results);
	}

	@Test
	void testRemoteConsoleExecution() {
		final Mut<RemoteConsoleCommandSender> results = Mut.of();

		new CommandAPICommand("normalinfo")
			.executesRemoteConsole(info -> {
				results.set(info.sender());
			})
			.register();

		new CommandAPICommand("normal")
			.executesRemoteConsole((console, args) -> {
				results.set(console);
			})
			.register();

		new CommandAPICommand("resultinginfo")
			.executesRemoteConsole(info -> {
				results.set(info.sender());
				return 2;
			})
			.register();

		new CommandAPICommand("resulting")
			.executesRemoteConsole((console, args) -> {
				results.set(console);
				return 2;
			})
			.register();

		testAllCommands(
			name -> sender -> assertStoresResult(sender, name, results, (RemoteConsoleCommandSender) sender),
			name -> sender -> assertCommandFailsWith(sender, name,
				"This command has no implementations for " + sender.getClass().getSimpleName().toLowerCase()),
			List.of("normal", "normalinfo", "resultinginfo", "resulting"),
			ExecutorType.REMOTE
		);

		assertNoMoreResults(results);
	}

	// Special cases
	@Test
	void testNativeExecution() {
		final Mut<NativeProxyCommandSender> results = Mut.of();

		new CommandAPICommand("normalinfo")
			.executesNative(info -> {
				results.set(info.sender());
			})
			.register();

		new CommandAPICommand("normal")
			.executesNative((proxy, args) -> {
				results.set(proxy);
			})
			.register();

		new CommandAPICommand("resultinginfo")
			.executesNative(info -> {
				results.set(info.sender());
				return 2;
			})
			.register();

		new CommandAPICommand("resulting")
			.executesNative((proxy, args) -> {
				results.set(proxy);
				return 2;
			})
			.register();

		// NATIVE allows all command senders, wrapping them as a NativeProxyCommandSender
		testAllCommands(
			name -> sender -> {
				server.dispatchCommand(sender, name);
				NativeProxyCommandSender nativeSender = results.get();
				assertEquals(sender, nativeSender.getCaller());
			},
			name -> sender -> fail("All command senders should be accepted to a NATIVE sender. " + sender.getClass().getSimpleName().toLowerCase() + " was not."),
			List.of("normal", "normalinfo", "resultinginfo", "resulting"),
			ExecutorType.PLAYER, ExecutorType.ENTITY, ExecutorType.CONSOLE, ExecutorType.BLOCK, ExecutorType.PROXY, ExecutorType.REMOTE
		);

		assertNoMoreResults(results);
	}

	@ParameterizedTest
	@ValueSource(ints = {0, 1, 2, 3})
	void testAllExecution(int mode) {
		// Test all (?) 4 ways of creating an effective ALL executor
		ExecutorType[] types = switch (mode) {
			case 0 -> null;
			case 1 -> new ExecutorType[0];
			case 2 -> new ExecutorType[]{ExecutorType.ALL};
			case 3 ->
				new ExecutorType[]{ExecutorType.PLAYER, ExecutorType.ENTITY, ExecutorType.CONSOLE, ExecutorType.BLOCK, ExecutorType.PROXY, ExecutorType.REMOTE};
			default -> throw new AssertionError("Unexpected switch value " + mode);
		};

		final Mut<CommandSender> results = Mut.of();

		new CommandAPICommand("normalinfo")
			.executes(info -> {
				results.set(info.sender());
			}, types)
			.register();

		new CommandAPICommand("normal")
			.executes((sender, args) -> {
				results.set(sender);
			}, types)
			.register();

		new CommandAPICommand("resultinginfo")
			.executes(info -> {
				results.set(info.sender());
				return 2;
			}, types)
			.register();

		new CommandAPICommand("resulting")
			.executes((sender, args) -> {
				results.set(sender);
				return 2;
			}, types)
			.register();

		// ALL allows all command senders
		testAllCommands(
			name -> sender -> assertStoresResult(sender, "normalinfo", results, sender),
			name -> sender -> fail("All command senders should be accepted to an ALL sender. " + sender.getClass().getSimpleName().toLowerCase() + " was not."),
			List.of("normal", "normalinfo", "resultinginfo", "resulting"),
			ExecutorType.PLAYER, ExecutorType.ENTITY, ExecutorType.CONSOLE, ExecutorType.BLOCK, ExecutorType.PROXY, ExecutorType.REMOTE
		);

		assertNoMoreResults(results);
	}

	@Test
	void testExecutesWithTwoTypes() {
		final Mut<CommandSender> results = Mut.of();

		new CommandAPICommand("normalinfo")
			.executes(info -> {
				results.set(info.sender());
			}, ExecutorType.PLAYER, ExecutorType.CONSOLE)
			.register();

		new CommandAPICommand("normal")
			.executes((sender, args) -> {
				results.set(sender);
			}, ExecutorType.PLAYER, ExecutorType.CONSOLE)
			.register();

		new CommandAPICommand("resultinginfo")
			.executes(info -> {
				results.set(info.sender());
				return 2;
			}, ExecutorType.PLAYER, ExecutorType.CONSOLE)
			.register();

		new CommandAPICommand("resulting")
			.executes((sender, args) -> {
				results.set(sender);
				return 2;
			}, ExecutorType.PLAYER, ExecutorType.CONSOLE)
			.register();

		// Both player and console are accepted
		testAllCommands(
			name -> sender -> assertStoresResult(sender, name, results, sender),
			name -> sender -> assertCommandFailsWith(sender, name,
				"This command has no implementations for " + sender.getClass().getSimpleName().toLowerCase()),
			List.of("normal", "normalinfo", "resultinginfo", "resulting"),
			ExecutorType.PLAYER, ExecutorType.CONSOLE
		);
	}

	@Test
	void testMultipleExecutors() {
		final Mut<CommandSender> results = Mut.of();

		new CommandAPICommand("normalinfo")
			.executesConsole(info -> {
				results.set(info.sender());
			})
			.executesPlayer(info -> {
				results.set(info.sender());
			})
			.register();

		new CommandAPICommand("normal")
			.executesConsole((console, args) -> {
				results.set(console);
			})
			.executesPlayer((player, args) -> {
				results.set(player);
			})
			.register();

		new CommandAPICommand("resultinginfo")
			.executesConsole(info -> {
				results.set(info.sender());
				return 2;
			})
			.executesPlayer(info -> {
				results.set(info.sender());
				return 2;
			})
			.register();

		new CommandAPICommand("resulting")
			.executesConsole((console, args) -> {
				results.set(console);
				return 2;
			})
			.executesPlayer((player, args) -> {
				results.set(player);
				return 2;
			})
			.register();

		// Both player and console are accepted
		testAllCommands(
			name -> sender -> assertStoresResult(sender, name, results, sender),
			name -> sender -> assertCommandFailsWith(sender, name,
				"This command has no implementations for " + sender.getClass().getSimpleName().toLowerCase()),
			List.of("normal", "normalinfo", "resultinginfo", "resulting"),
			ExecutorType.PLAYER, ExecutorType.CONSOLE
		);
	}
}
