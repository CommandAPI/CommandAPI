package dev.jorel.commandapi.test.dsltests

import be.seeseemelk.mockbukkit.entity.PlayerMock
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.exceptions.OptionalArgumentException
import dev.jorel.commandapi.kotlindsl.*
import dev.jorel.commandapi.test.Mut
import dev.jorel.commandapi.test.TestBase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OptionalArgumentTests: TestBase() {

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
	fun executionTestWithCommandAPICommandAndOptionalArgumentMethod() {
		val results: Mut<String> = Mut.of()

		commandAPICommand("test") {
			optionalArgument(StringArgument("value"))
			playerExecutor { player, args ->
				results.set(args.getOptional("value").orElse("DefaultValue") as String)
			}
		}

		val player: PlayerMock = server.addPlayer()

		// /test
		server.dispatchCommand(player, "test")
		assertEquals("DefaultValue", results.get())

		// /test hello
		server.dispatchCommand(player, "test hello")
		assertEquals("hello", results.get())

		assertNoMoreResults(results)
	}

	@Test
	fun exceptionTestWithCommandAPICommandAndOptionalArgumentMethod() {
		// This throws an OptionalArgumentException because
		// a required argument is declared after an optional argument
		assertThrows<OptionalArgumentException> {
			commandAPICommand("test") {
				optionalArgument(StringArgument("value"))
				literalArgument("test")
				playerExecutor { player, args ->

				}
			}
		}
	}

	@Test
	fun executionTestWithOptionalValue() {
		val results: Mut<String> = Mut.of()

		commandAPICommand("test") {
			stringArgument("value", optional = true)
			playerExecutor { player, args ->
				results.set(args.getOptional("value").orElse("DefaultValue") as String)
			}
		}

		val player: PlayerMock = server.addPlayer()

		// /test
		server.dispatchCommand(player, "test")
		assertEquals("DefaultValue", results.get())

		// /test hello
		server.dispatchCommand(player, "test hello")
		assertEquals("hello", results.get())

		assertNoMoreResults(results)
	}

}