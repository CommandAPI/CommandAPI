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
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.ChatColor
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Tests for the arguments that commandapi-kotlin-spigot adds to the Kotlin DSL
 */
class SpigotDSLArgumentTests : TestBase() {

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
		val results: Mut<ChatColor> = Mut.of()

		commandAPICommand("test") {
			chatColorArgument("color")
			playerExecutor { _, args ->
				results.set(args["color"] as ChatColor)
			}
		}

		val player: PlayerMock = server.addPlayer()

		// /test red
		server.dispatchCommand(player, "test red")
		assertEquals(ChatColor.RED, results.get())

		assertNoMoreResults(results)
	}

	@Test
	fun executionTestWithCommandTreeAndChatArgument() {
		val results: Mut<String> = Mut.of()

		commandTree("test") {
			chatArgument("message") {
				playerExecutor { _, args ->
					@Suppress("UNCHECKED_CAST")
					results.set(BaseComponent.toPlainText(*(args["message"] as Array<BaseComponent>)))
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
		val results: Mut<ChatColor> = Mut.of()

		commandTree("test") {
			literalArgument("color") {
				chatColorArgument("color") {
					playerExecutor { _, args ->
						results.set(args["color"] as ChatColor)
					}
				}
			}
		}

		val player: PlayerMock = server.addPlayer()

		// /test color blue
		server.dispatchCommand(player, "test color blue")
		assertEquals(ChatColor.BLUE, results.get())

		assertNoMoreResults(results)
	}

}
