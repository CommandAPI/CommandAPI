package dev.jorel.commandapi.test.dsltests;

import org.mockbukkit.mockbukkit.entity.PlayerMock
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.argument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.test.Mut
import dev.jorel.commandapi.test.TestBase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ArgumentMethodTests : TestBase() {

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

	/*********
	 * Tests *
	 *********/

	@Test
	fun executionTestWithCommandAPICommandAndArgumentMethod() {
		val results: Mut<String> = Mut.of()

		commandAPICommand("test") {
			argument(StringArgument("value"))
			playerExecutor { player, args ->
				results.set(args["value"] as String)
			}
		}

		val player: PlayerMock = server.addPlayer()

		// /test hello
		server.dispatchCommand(player, "test hello")
		assertEquals("hello", results.get())

		// /test world
		server.dispatchCommand(player, "test world")
		assertEquals("world", results.get())

		// /test
		assertCommandFailsWith(player, "test", "Unknown or incomplete command. See below for error at position 4: test<--[HERE]")

		assertNoMoreResults(results)
	}

	@Test
	fun executionTestWithCommandTreeAndArgumentMethod() {
		val results: Mut<String> = Mut.of()

		commandTree("test") {
			argument(StringArgument("value")) {
				playerExecutor { player, args ->
					results.set(args["value"] as String)
				}
			}
		}

		val player: PlayerMock = server.addPlayer()

		// /test hello
		server.dispatchCommand(player, "test hello")
		assertEquals("hello", results.get())

		// /test world
		server.dispatchCommand(player, "test world")
		assertEquals("world", results.get())

		// /test
		assertCommandFailsWith(player, "test", "Unknown or incomplete command. See below for error at position 4: test<--[HERE]")

		assertNoMoreResults(results)
	}

}
