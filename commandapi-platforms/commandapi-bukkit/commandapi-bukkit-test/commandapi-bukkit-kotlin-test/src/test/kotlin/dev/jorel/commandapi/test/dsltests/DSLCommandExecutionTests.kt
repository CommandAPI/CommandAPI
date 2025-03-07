package dev.jorel.commandapi.test.dsltests

import be.seeseemelk.mockbukkit.command.ConsoleCommandSenderMock
import be.seeseemelk.mockbukkit.entity.SimpleEntityMock
import dev.jorel.commandapi.executors.*
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.*
import dev.jorel.commandapi.test.Mut
import dev.jorel.commandapi.test.TestBase
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
import org.mockito.Mockito

/**
 * Tests for the methods provided by {@link BukkitExecutable}. We're also specifically
 * testing this in Kotlin because type inference can be temperamental for the lambdas
 * passed into the `executes` methods.
 */
class DSLCommandExecutionTests : TestBase() {

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
				org.junit.jupiter.api.fail("Unexpected error while running command with name \"$name\"", e)
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
				org.junit.jupiter.api.fail(
					"Unexpected error while running command with executor type \"" + executorType.key + "\"",
					e
				)
			}
		}
	}

	/*********
	 * Tests *
	 *********/

	// Single sender type allowed
	@Test
	fun testDSLPlayerExecution() {
		val results: Mut<Player> = Mut.of()

		commandAPICommand("normalinfo") {
			playerExecutionInfo { info ->
				results.set(info.sender())
			}
		}

		commandAPICommand("normal") {
			playerExecutor { player, _ ->
				results.set(player)
			}
		}

		commandAPICommand("resultinginfo") {
			playerResultingExecutionInfo { info ->
				results.set(info.sender())
				2
			}
		}

		commandAPICommand("resulting") {
			playerResultingExecutor { player, _ ->
				results.set(player)
				2
			}
		}

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
	fun testDSLEntityExecution() {
		val results: Mut<Entity> = Mut.of()

		commandAPICommand("normalinfo") {
			entityExecutionInfo { info ->
				results.set(info.sender())
			}
		}

		commandAPICommand("normal") {
			entityExecutor { entity, _ ->
				results.set(entity)
			}
		}

		commandAPICommand("resultinginfo") {
			entityResultingExecutionInfo { info ->
				results.set(info.sender())
				2
			}
		}

		commandAPICommand("resulting") {
			entityResultingExecutor { entity, _ ->
				results.set(entity)
				2
			}
		}

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
	fun testDSLConsoleExecution() {
		val results: Mut<ConsoleCommandSender> = Mut.of()

		commandAPICommand("normalinfo") {
			consoleExecutionInfo { info ->
				results.set(info.sender())
			}
		}

		commandAPICommand("normal") {
			consoleExecutor { console, _ ->
				results.set(console)
			}
		}

		commandAPICommand("resultinginfo") {
			consoleResultingExecutionInfo { info ->
				results.set(info.sender())
				2
			}
		}

		commandAPICommand("resulting") {
			consoleResultingExecutor { console, _ ->
				results.set(console)
				2
			}
		}

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
	fun testDSLBlockExecution() {
		val results: Mut<BlockCommandSender> = Mut.of()

		commandAPICommand("normalinfo") {
			commandBlockExecutionInfo { info ->
				results.set(info.sender())
			}
		}

		commandAPICommand("normal") {
			commandBlockExecutor { block, _ ->
				results.set(block)
			}
		}

		commandAPICommand("resultinginfo") {
			commandBlockResultingExecutionInfo { info ->
				results.set(info.sender())
				2
			}
		}

		commandAPICommand("resulting") {
			commandBlockResultingExecutor { block, _ ->
				results.set(block)
				2
			}
		}

		testAllCommands(
			{ name -> { sender -> assertStoresResult(sender, name, results, sender as BlockCommandSender) } },
			{ name -> { sender -> assertCommandFailsWith(sender, name,
						"This command has no implementations for " + sender.javaClass.getSimpleName().lowercase()) } },
			listOf("normal", "normalinfo", "resultinginfo", "resulting"),
			ExecutorType.BLOCK
		)
	}

	@Test
	fun testDSLProxyExecution() {
		val results: Mut<NativeProxyCommandSender> = Mut.of()

		commandAPICommand("normalinfo") {
			proxyExecutionInfo { info ->
				results.set(info.sender())
			}
		}

		commandAPICommand("normal") {
			proxyExecutor { proxy, _ ->
				results.set(proxy)
			}
		}

		commandAPICommand("resultinginfo") {
			proxyResultingExecutionInfo { info ->
				results.set(info.sender())
				2
			}
		}

		commandAPICommand("resulting") {
			proxyResultingExecutor { proxy, _ ->
				results.set(proxy)
				2
			}
		}

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
	fun testDSLRemoteConsoleExecution() {
		val results: Mut<RemoteConsoleCommandSender> = Mut.of()

		commandAPICommand("normalinfo") {
			remoteConsoleExecutionInfo { info ->
				results.set(info.sender())
			}
		}

		commandAPICommand("normal") {
			remoteConsoleExecutor { console, _ ->
				results.set(console)
			}
		}

		commandAPICommand("resultinginfo") {
			remoteConsoleResultingExecutionInfo { info ->
				results.set(info.sender())
				2
			}
		}

		commandAPICommand("resulting") {
			remoteConsoleResultingExecutor { console, _ ->
				results.set(console)
				2
			}
		}

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
	fun testDSLNativeExecution() {
		val results: Mut<NativeProxyCommandSender> = Mut.of()

		commandAPICommand("normalinfo") {
			nativeExecutionInfo { info ->
				results.set(info.sender())
			}
		}

		commandAPICommand("normal") {
			nativeExecutor { proxy, _ ->
				results.set(proxy)
			}
		}

		commandAPICommand("resultinginfo") {
			nativeResultingExecutionInfo { info ->
				results.set(info.sender())
				2
			}
		}

		commandAPICommand("resulting") {
			nativeResultingExecutor { proxy, _ ->
				results.set(proxy)
				2
			}
		}

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

	@Test
	fun testDSLAllExecution() {
		// Compared to other methods, there's only one way to have an ALL executor in the DSL
		val results: Mut<CommandSender> = Mut.of()

		commandAPICommand("normalinfo") {
			anyExecutionInfo { info ->
				results.set(info.sender())
			}
		}

		commandAPICommand("normal") {
			anyExecutor { sender, _ ->
				results.set(sender)
			}
		}

		commandAPICommand("resultinginfo") {
			anyResultingExecutionInfo { info ->
				results.set(info.sender())
				2
			}
		}

		commandAPICommand("resulting") {
			anyResultingExecutor { sender, _ ->
				results.set(sender)
				2
			}
		}

		// ALL allows all command senders
		testAllCommands(
			{ name -> { sender -> assertStoresResult(sender, name, results, sender) } },
			{ { sender -> fail( "All command senders should be accepted to an ALL sender. " + sender.javaClass.getSimpleName().lowercase() + " was not.") } },
			listOf("normal", "normalinfo", "resultinginfo", "resulting"),
			ExecutorType.PLAYER, ExecutorType.ENTITY, ExecutorType.CONSOLE, ExecutorType.BLOCK, ExecutorType.PROXY, ExecutorType.REMOTE
		)

		assertNoMoreResults(results)
	}

	// Hm, the method used in `testExecutesWithTwoTypes` doesn't seem possible with the DSL
	@Test
	fun testDSLMultipleExecutors() {
		val results: Mut<CommandSender> = Mut.of()

		commandAPICommand("normalinfo") {
			consoleExecutionInfo { info ->
				results.set(info.sender())
			}
			playerExecutionInfo { info ->
				results.set(info.sender())
			}
		}

		commandAPICommand("normal") {
			consoleExecutor { console, _ ->
				results.set(console)
			}
			playerExecutor { player, _ ->
				results.set(player)
			}
		}

		commandAPICommand("resultinginfo") {
			consoleResultingExecutionInfo { info ->
				results.set(info.sender())
				2
			}
			playerResultingExecutionInfo { info ->
				results.set(info.sender())
				2
			}
		}

		commandAPICommand("resulting") {
			consoleResultingExecutor { console, _ ->
				results.set(console)
				2
			}
			playerResultingExecutor { player, _ ->
				results.set(player)
				2
			}
		}

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
