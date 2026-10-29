package dev.jorel.commandapi.test.dsltests

import org.mockbukkit.mockbukkit.entity.PlayerMock
import dev.jorel.commandapi.kotlindsl.chatArgument
import dev.jorel.commandapi.kotlindsl.chatColorArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.test.Mut
import dev.jorel.commandapi.test.TestBase
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.text.format.NamedTextColor
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Tests for the arguments that commandapi-kotlin-paper adds to the Kotlin DSL
 */
class PaperDSLArgumentTests : TestBase() {

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
	fun executionTestWithCommandAPICommandAndChatColorArgument() {
		val results: Mut<NamedTextColor> = Mut.of()

		commandAPICommand("test") {
			chatColorArgument("color")
			playerExecutor { _, args ->
				results.set(args["color"] as NamedTextColor)
			}
		}

		val player: PlayerMock = server.addPlayer()

		// /test red
		server.dispatchCommand(player, "test red")
		assertEquals(NamedTextColor.RED, results.get())

		assertNoMoreResults(results)
	}

	@Test
	fun executionTestWithCommandTreeAndChatArgument() {
		val results: Mut<String> = Mut.of()

		commandTree("test") {
			chatArgument("message") {
				playerExecutor { _, args ->
					results.set((args["message"] as SignedMessage).message())
				}
			}
		}

		val player: PlayerMock = server.addPlayer("APlayer")

		// /test Hello, APlayer!
		server.dispatchCommand(player, "test Hello, APlayer!")
		assertEquals("Hello, APlayer!", results.get())

		assertNoMoreResults(results)
	}

	@Test
	fun executionTestWithNestedChatColorArgument() {
		val results: Mut<NamedTextColor> = Mut.of()

		commandTree("test") {
			literalArgument("color") {
				chatColorArgument("color") {
					playerExecutor { _, args ->
						results.set(args["color"] as NamedTextColor)
					}
				}
			}
		}

		val player: PlayerMock = server.addPlayer()

		// /test color blue
		server.dispatchCommand(player, "test color blue")
		assertEquals(NamedTextColor.BLUE, results.get())

		assertNoMoreResults(results)
	}

}
