package dev.jorel.commandapi.test

import be.seeseemelk.mockbukkit.command.ConsoleCommandSenderMock
import be.seeseemelk.mockbukkit.entity.SimpleEntityMock
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.*
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender
import org.bukkit.command.BlockCommandSender
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.command.RemoteConsoleCommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito

/**
 * Tests for the methods provided by {@link BukkitExecutable}. We're also specifically
 * testing this in Kotlin because type inference can be temperamental for the lambdas
 * passed into the `executes` methods.
 */
open class CommandExecutionTests : TestBase() {

	/*********
	 * Setup *
	 *********/

	@BeforeEach
	override fun setUp() {
		super.setUp()
	}

	@AfterEach
	override fun tearDown() {
		super.tearDown()
	}

	// Using a linked map here to keep the values in insertion order, rather
	//  than in whatever order the hash code feels like, which makes debugging easier
	private val executorTypeToMockSender: Map<ExecutorType, () -> CommandSender> = linkedMapOf(
		ExecutorType.PLAYER to { server.addCraftPlayer() },
		ExecutorType.ENTITY to { SimpleEntityMock(server) },
		ExecutorType.CONSOLE to { ConsoleCommandSenderMock() },
		// Apparently `Mockito.mock(BlockCommandSender::class) as CommandSender` is not correct here ¯\_(ツ)_/¯
		//  It reeeaaaly wants to just infer the type
		ExecutorType.BLOCK to { val block : BlockCommandSender = Mockito.mock(); block},
		// This is a little odd, but `ProxiedCommandSender`s will always be CommandAPI `NativeProxyCommandSender`s.
		//  It is possible that a different plugin could implement the `ProxiedCommandSender`, which would cause
		//  a class cast exception if that sender tried to run an `executesProxy` executor. However, the Spigot/Paper
		//  server itself will never use its own `org.bukkit.craftbukkit.command.ProxiedNativeCommandSender` class.
		//  So, if you can make a class cast exception happen on a server, change this mock to `ProxiedCommandSender`
		//  and fix `executesProxy`, but otherwise we can provide the more specific `NativeProxyCommandSender` class.
//		ExecutorType.PROXY to { val proxy : ProxiedCommandSender = Mockito.mock(); proxy },
		ExecutorType.PROXY to { val proxy : NativeProxyCommandSender = Mockito.mock(); proxy },
		ExecutorType.REMOTE to { val remoteConsole : RemoteConsoleCommandSender = Mockito.mock(); remoteConsole }
	)

	private fun testAllCommands(
		shouldSucceed: (String) -> (CommandSender) -> Unit,
		shouldFail: (String) -> (CommandSender) -> Unit,
		commandNames: List<String>,
		vararg types: ExecutorType
	) {
		for (name in commandNames) {
			try {
				assertOnlyDefinedExecutorTypesSucceed(shouldSucceed(name), shouldFail(name), *types)
			} catch (e: Throwable) {
				fail("Unexpected error while running command with name \"$name\"", e)
			}
		}
	}

	private fun assertOnlyDefinedExecutorTypesSucceed(
		shouldSucceed: (CommandSender) -> Unit, shouldFail: (CommandSender) -> Unit, vararg types: ExecutorType
	) {
		for (executorType in executorTypeToMockSender.entries) {
			try {
				(if (executorType.key in types) shouldSucceed else shouldFail).invoke(executorType.value())
			} catch (e: Throwable) {
				fail("Unexpected error while running command with executor type \"" + executorType.key + "\"", e)
			}
		}
	}

	/*********
	 * Tests *
	 *********/

	// Single sender type allowed
	@Test
	fun testPlayerExecution() {
		val results: Mut<Player> = Mut.of()

		CommandAPICommand("normalinfo")
			.executesPlayer(NormalExecutorInfo<Player, Any> { info ->
				results.set(info.sender())
			})
			.register()

		CommandAPICommand("normal")
			.executesPlayer(NormalExecutor<Player, Any> { player, _ ->
				results.set(player)
			})
			.register()

		CommandAPICommand("resultinginfo")
			.executesPlayer(ResultingExecutorInfo<Player, Any> { info ->
				results.set(info.sender())
				2
			})
			.register()

		CommandAPICommand("resulting")
			.executesPlayer(ResultingExecutor<Player, Any> { player, _ ->
				results.set(player)
				2
			})
			.register()

		testAllCommands(
			{ name -> { sender -> assertStoresResult(sender, name, results, sender as Player) } },
			{ name -> { sender -> assertCommandFailsWith(sender, name,
						"This command has no implementations for " + sender.javaClass.getSimpleName().lowercase()) } },
			listOf("normal", "normalinfo", "resultinginfo", "resulting"),
			ExecutorType.PLAYER
		)

		assertNoMoreResults(results)
	}

	@Test
	fun testEntityExecution() {
		val results: Mut<Entity> = Mut.of()

		CommandAPICommand("normalinfo")
			.executesEntity(NormalExecutorInfo<Entity, Any> { info ->
				results.set(info.sender())
			})
			.register()

		CommandAPICommand("normal")
			.executesEntity(NormalExecutor<Entity, Any> { entity, _ ->
				results.set(entity)
			})
			.register()

		CommandAPICommand("resultinginfo")
			.executesEntity(ResultingExecutorInfo<Entity, Any> { info ->
				results.set(info.sender())
				2
			})
			.register()

		CommandAPICommand("resulting")
			.executesEntity(ResultingExecutor<Entity, Any> { entity, _ ->
				results.set(entity)
				2
			})
			.register()

		testAllCommands(
			{ name -> { sender -> assertStoresResult(sender, name, results, sender as Entity) } },
			{ name -> { sender -> assertCommandFailsWith(sender, name,
						"This command has no implementations for " + sender.javaClass.getSimpleName().lowercase()) } },
			listOf("normal", "normalinfo", "resultinginfo", "resulting"),
			// Players count as Entities
			ExecutorType.ENTITY, ExecutorType.PLAYER
		)

		assertNoMoreResults(results)
	}

	@Test
	fun testConsoleExecution() {
		val results: Mut<ConsoleCommandSender> = Mut.of()

		CommandAPICommand("normalinfo")
			.executesConsole(NormalExecutorInfo<ConsoleCommandSender, Any> { info ->
				results.set(info.sender())
			})
			.register()

		CommandAPICommand("normal")
			.executesConsole(NormalExecutor<ConsoleCommandSender, Any> { console, _ ->
				results.set(console)
			})
			.register()

		CommandAPICommand("resultinginfo")
			.executesConsole(ResultingExecutorInfo<ConsoleCommandSender, Any> { info ->
				results.set(info.sender())
				2
			})
			.register()

		CommandAPICommand("resulting")
			.executesConsole(ResultingExecutor<ConsoleCommandSender, Any> { console, _ ->
				results.set(console)
				2
			})
			.register()

		testAllCommands(
			{ name -> { sender -> assertStoresResult(sender, name, results, sender as ConsoleCommandSender) } },
			{ name -> { sender -> assertCommandFailsWith(sender, name,
						"This command has no implementations for " + sender.javaClass.getSimpleName().lowercase()) } },
			listOf("normal", "normalinfo", "resultinginfo", "resulting"),
			ExecutorType.CONSOLE
		)

		assertNoMoreResults(results)
	}

	@Test
	fun testBlockExecution() {
		val results: Mut<BlockCommandSender> = Mut.of()

		CommandAPICommand("normalinfo")
			.executesCommandBlock(NormalExecutorInfo<BlockCommandSender, Any> { info ->
				results.set(info.sender())
			})
			.register()

		CommandAPICommand("normal")
			.executesCommandBlock(NormalExecutor<BlockCommandSender, Any> { block, _ ->
				results.set(block)
			})
			.register()

		CommandAPICommand("resultinginfo")
			.executesCommandBlock(ResultingExecutorInfo<BlockCommandSender, Any> { info ->
				results.set(info.sender())
				2
			})
			.register()

		CommandAPICommand("resulting")
			.executesCommandBlock(ResultingExecutor<BlockCommandSender, Any> { block, _ ->
				results.set(block)
				2
			})
			.register()

		testAllCommands(
			{ name -> { sender -> assertStoresResult(sender, name, results, sender as BlockCommandSender) } },
			{ name -> { sender -> assertCommandFailsWith(sender, name,
						"This command has no implementations for " + sender.javaClass.getSimpleName().lowercase()) } },
			listOf("normal", "normalinfo", "resultinginfo", "resulting"),
			ExecutorType.BLOCK
		)
	}

	@Test
	fun testProxyExecution() {
		val results: Mut<NativeProxyCommandSender> = Mut.of()

		CommandAPICommand("normalinfo")
			.executesProxy(NormalExecutorInfo<NativeProxyCommandSender, Any> { info ->
				results.set(info.sender())
			})
			.register()

		CommandAPICommand("normal")
			.executesProxy(NormalExecutor<NativeProxyCommandSender, Any> { proxy, _ ->
				results.set(proxy)
			})
			.register()

		CommandAPICommand("resultinginfo")
			.executesProxy(ResultingExecutorInfo<NativeProxyCommandSender, Any> { info ->
				results.set(info.sender())
				2
			})
			.register()

		CommandAPICommand("resulting")
			.executesProxy(ResultingExecutor<NativeProxyCommandSender, Any> { proxy, _ ->
				results.set(proxy)
				2
			})
			.register()

		testAllCommands(
			{ name -> { sender -> assertStoresResult(sender, name, results, sender as NativeProxyCommandSender) } },
			{ name -> { sender -> assertCommandFailsWith(sender, name,
						"This command has no implementations for " + sender.javaClass.getSimpleName().lowercase()) } },
			listOf("normal", "normalinfo", "resultinginfo", "resulting"),
			ExecutorType.PROXY
		)

		assertNoMoreResults(results)
	}

	@Test
	fun testRemoteConsoleExecution() {
		val results: Mut<RemoteConsoleCommandSender> = Mut.of()

		CommandAPICommand("normalinfo")
			.executesRemoteConsole(NormalExecutorInfo<RemoteConsoleCommandSender, Any> { info ->
				results.set(info.sender())
			})
			.register()

		CommandAPICommand("normal")
			.executesRemoteConsole(NormalExecutor<RemoteConsoleCommandSender, Any> { console, _ ->
				results.set(console)
			})
			.register()

		CommandAPICommand("resultinginfo")
			.executesRemoteConsole(ResultingExecutorInfo<RemoteConsoleCommandSender, Any> { info ->
				results.set(info.sender())
				2
			})
			.register()

		CommandAPICommand("resulting")
			.executesRemoteConsole(ResultingExecutor<RemoteConsoleCommandSender, Any> { console, _ ->
				results.set(console)
				2
			})
			.register()

		testAllCommands(
			{ name -> { sender -> assertStoresResult(sender, name, results, sender as RemoteConsoleCommandSender) } },
			{ name -> { sender -> assertCommandFailsWith(sender, name,
						"This command has no implementations for " + sender.javaClass.getSimpleName().lowercase()) } },
			listOf("normal", "normalinfo", "resultinginfo", "resulting"),
			ExecutorType.REMOTE
		)

		assertNoMoreResults(results)
	}

	// Special cases
	@Test
	fun testNativeExecution() {
		val results: Mut<NativeProxyCommandSender> = Mut.of()

		CommandAPICommand("normalinfo")
			.executesNative(NormalExecutorInfo<NativeProxyCommandSender, Any> { info ->
				results.set(info.sender())
			})
			.register()

		CommandAPICommand("normal")
			.executesNative(NormalExecutor<NativeProxyCommandSender, Any> { proxy, _ ->
				results.set(proxy)
			})
			.register()

		CommandAPICommand("resultinginfo")
			.executesNative(ResultingExecutorInfo<NativeProxyCommandSender, Any> { info ->
				results.set(info.sender())
				2
			})
			.register()

		CommandAPICommand("resulting")
			.executesNative(ResultingExecutor<NativeProxyCommandSender, Any> { proxy, _ ->
				results.set(proxy)
				2
			})
			.register()

		// NATIVE allows all command senders, wrapping them as a NativeProxyCommandSender
		testAllCommands(
			{ name -> { sender ->
					server.dispatchCommand(sender, name)
					val nativeSender = results.get()
					assertEquals(sender, nativeSender.caller)
			} },
			{ { sender -> fail("All command senders should be accepted to a NATIVE sender. " + sender.javaClass.getSimpleName().lowercase() + " was not.") } },
			listOf("normal", "normalinfo", "resultinginfo", "resulting"),
			ExecutorType.PLAYER, ExecutorType.ENTITY, ExecutorType.CONSOLE, ExecutorType.BLOCK, ExecutorType.PROXY, ExecutorType.REMOTE
		)

		assertNoMoreResults(results)
	}

	@ParameterizedTest
	@ValueSource(ints = [0, 1, 2])
	fun testAllExecution(mode: Int) {
		// Test all (?) 4 ways of creating an effective ALL executor
		//  Actually, I don't think you can pass a null array into a Java varargs from Kotlin,
		//  e.g. there is no equivalent to `ExecutorType[] types = null; .executes(info -> {}, types)`,
		//  so I guess we can't test that here ¯\_(ツ)_/¯
		val types: Array<ExecutorType> = when (mode) {
			0 -> arrayOf()
			1 -> arrayOf(ExecutorType.ALL)
			2 -> arrayOf(ExecutorType.PLAYER, ExecutorType.ENTITY, ExecutorType.CONSOLE, ExecutorType.BLOCK, ExecutorType.PROXY, ExecutorType.REMOTE)
			else -> throw AssertionError("Unexpected switch value $mode")
		}

		val results: Mut<CommandSender> = Mut.of()

		CommandAPICommand("normalinfo")
			.executes(NormalExecutorInfo<CommandSender, Any> { info ->
				results.set(info.sender())
			}, *types)
			.register()

		CommandAPICommand("normal")
			.executes(NormalExecutor<CommandSender, Any> { sender, _ ->
				results.set(sender)
			}, *types)
			.register()

		CommandAPICommand("resultinginfo")
			.executes(ResultingExecutorInfo<CommandSender, Any> { info ->
				results.set(info.sender())
				2
			}, *types)
			.register()

		CommandAPICommand("resulting")
			.executes(ResultingExecutor<CommandSender, Any> { sender, _ ->
				results.set(sender)
				2
			}, *types)
			.register()

		// ALL allows all command senders
		testAllCommands(
			{ name -> { sender -> assertStoresResult(sender, name, results, sender) } },
			{ { sender -> fail( "All command senders should be accepted to an ALL sender. " + sender.javaClass.getSimpleName().lowercase() + " was not.") } },
			listOf("normal", "normalinfo", "resultinginfo", "resulting"),
			ExecutorType.PLAYER, ExecutorType.ENTITY, ExecutorType.CONSOLE, ExecutorType.BLOCK, ExecutorType.PROXY, ExecutorType.REMOTE
		)

		assertNoMoreResults(results)
	}

	@Test
	fun testExecutesWithTwoTypes() {
		val results: Mut<CommandSender> = Mut.of()

		CommandAPICommand("normalinfo")
			.executes(NormalExecutorInfo<CommandSender, Any> { info ->
				results.set(info.sender())
			}, ExecutorType.PLAYER, ExecutorType.CONSOLE)
			.register()

		CommandAPICommand("normal")
			.executes(NormalExecutor<CommandSender, Any> { sender, _ ->
				results.set(sender)
			}, ExecutorType.PLAYER, ExecutorType.CONSOLE)
			.register()

		CommandAPICommand("resultinginfo")
			.executes(ResultingExecutorInfo<CommandSender, Any> { info ->
				results.set(info.sender())
				2
			}, ExecutorType.PLAYER, ExecutorType.CONSOLE)
			.register()

		CommandAPICommand("resulting")
			.executes( ResultingExecutor<CommandSender, Any> { sender, _ ->
				results.set(sender)
				2
			}, ExecutorType.PLAYER, ExecutorType.CONSOLE)
			.register()

		// Both player and console are accepted
		testAllCommands(
			{ name -> { sender -> assertStoresResult(sender, name, results, sender) } },
			{ name -> { sender -> assertCommandFailsWith(sender, name,
						"This command has no implementations for " + sender.javaClass.getSimpleName().lowercase()) } },
			listOf("normal", "normalinfo", "resultinginfo", "resulting"),
			ExecutorType.PLAYER, ExecutorType.CONSOLE
		)
	}

	@Test
	fun testMultipleExecutors() {
		val results: Mut<CommandSender> = Mut.of()

		CommandAPICommand("normalinfo")
			.executesConsole(NormalExecutorInfo<ConsoleCommandSender, Any> { info ->
				results.set(info.sender())
			})
			.executesPlayer(NormalExecutorInfo<Player, Any> { info ->
				results.set(info.sender())
			})
			.register()

		CommandAPICommand("normal")
			.executesConsole(NormalExecutor<ConsoleCommandSender, Any> { console, _ ->
				results.set(console)
			})
			.executesPlayer(NormalExecutor<Player, Any> { player, _ ->
				results.set(player)
			})
			.register()

		CommandAPICommand("resultinginfo")
			.executesConsole(ResultingExecutorInfo<ConsoleCommandSender, Any> { info ->
				results.set(info.sender())
				2
			})
			.executesPlayer(ResultingExecutorInfo<Player, Any> { info ->
				results.set(info.sender())
				2
			})
			.register()

		CommandAPICommand("resulting")
			.executesConsole(ResultingExecutor<ConsoleCommandSender, Any> { console, _ ->
				results.set(console)
				2
			})
			.executesPlayer(ResultingExecutor<Player, Any> { player, _ ->
				results.set(player)
				2
			})
			.register()

		// Both player and console are accepted
		testAllCommands(
			{ name -> { sender -> assertStoresResult(sender, name, results, sender) } },
			{ name -> { sender -> assertCommandFailsWith(sender, name,
						"This command has no implementations for " + sender.javaClass.getSimpleName().lowercase()) } },
			listOf("normal", "normalinfo", "resultinginfo", "resulting"),
			ExecutorType.PLAYER, ExecutorType.CONSOLE
		)
	}
}
