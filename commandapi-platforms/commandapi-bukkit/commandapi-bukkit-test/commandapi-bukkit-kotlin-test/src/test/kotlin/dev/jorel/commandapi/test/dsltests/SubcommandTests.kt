package dev.jorel.commandapi.test.dsltests

import org.mockbukkit.mockbukkit.entity.PlayerMock
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.jorel.commandapi.test.Mut
import dev.jorel.commandapi.test.TestBase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SubcommandTests : TestBase() {

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
	fun executionTestWithSubcommandsOne() {
		val results: Mut<String> = Mut.of()

		commandAPICommand("test") {
			subcommand("one") {
				playerExecutor { player, args ->
					results.set("/test one")
				}
			}
			subcommand("two") {
				playerExecutor { player, args ->
					results.set("/test two")
				}
			}
		}

		val player: PlayerMock = server.addPlayer()

		// /test one
		server.dispatchCommand(player, "test one")
		assertEquals("/test one", results.get())

		// /test two
		server.dispatchCommand(player, "test two")
		assertEquals("/test two", results.get())

		// /test
		assertCommandFailsWith(player, "test", "Unknown or incomplete command. See below for error at position 4: test<--[HERE]")

		assertNoMoreResults(results)
	}

	@Test
	fun executionTestWithSubcommandsTwo() {
		val results: Mut<String> = Mut.of()

		val one = subcommand("one") {
			playerExecutor { player, args ->
				results.set("/test one")
			}
		}

		val two = subcommand("two") {
			playerExecutor { player, args ->
				results.set("/test two")
			}
		}

		commandAPICommand("test") {
			subcommand(one)
			subcommand(two)
		}

		val player: PlayerMock = server.addPlayer()

		// /test one
		server.dispatchCommand(player, "test one")
		assertEquals("/test one", results.get())

		// /test two
		server.dispatchCommand(player, "test two")
		assertEquals("/test two", results.get())

		// /test
		assertCommandFailsWith(player, "test", "Unknown or incomplete command. See below for error at position 4: test<--[HERE]")

		assertNoMoreResults(results)
	}

}