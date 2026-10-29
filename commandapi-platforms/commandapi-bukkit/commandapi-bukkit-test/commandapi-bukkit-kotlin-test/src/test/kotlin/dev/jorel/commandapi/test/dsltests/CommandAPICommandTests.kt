package dev.jorel.commandapi.test.dsltests

import org.mockbukkit.mockbukkit.entity.PlayerMock
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.stringArgument
import dev.jorel.commandapi.test.Mut
import dev.jorel.commandapi.test.TestBase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CommandAPICommandTests : TestBase() {

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
	fun executionTestWithSimpleCommandAPICommandAndNoArguments() {
		val results: Mut<String> = Mut.of()

		commandAPICommand("test") {
			playerExecutor { player, args ->
				results.set("Test successful!")
			}
		}

		val player: PlayerMock = server.addPlayer()

		// /test
		server.dispatchCommand(player, "test")
		assertEquals("Test successful!", results.get())

		assertNoMoreResults(results)
	}

	@Test
	fun executionTestWithSimpleCommandAPICommandAndArguments() {
		val results: Mut<String> = Mut.of()

		commandAPICommand("test") {
			stringArgument("value")
			playerExecutor { player, args ->
				val string: String = args["value"] as String
				results.set(string)
			}
		}

		val player: PlayerMock = server.addPlayer()

		// /test
		assertCommandFailsWith(player, "test", "Unknown or incomplete command. See below for error at position 4: test<--[HERE]")

		// /test hello
		server.dispatchCommand(player, "test hello")
		assertEquals("hello", results.get())

		assertNoMoreResults(results)
	}

}