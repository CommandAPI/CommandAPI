package dev.jorel.commandapi.test

import be.seeseemelk.mockbukkit.MockBukkit
import com.mojang.brigadier.exceptions.CommandSyntaxException
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.executors.NormalExecutor
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandMap
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.junit.jupiter.api.Assertions
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.*
import java.util.stream.Collectors

abstract class TestBase {

	lateinit var server: CommandAPIServerMock
	lateinit var plugin: Main

	open fun setUp() {
		server = MockBukkit.mock(CommandAPIServerMock())
		plugin = MockBukkit.load(Main::class.java)
	}

	open fun tearDown() {
		Bukkit.getScheduler().cancelTasks(plugin)
		if (this::plugin.isInitialized) {
			plugin.onDisable()
		}
		MockBukkit.unmock()
		MockPlatform.unload()
	}

	fun <T : Any> assertStoresResult(sender: CommandSender, command: String, queue: Mut<T>, expected: T) {
		Assertions.assertDoesNotThrow {
			Assertions.assertTrue(
				server.dispatchThrowableCommand(sender, command), "Expected command dispatch to return true, but it gave false"
			)
		}
		Assertions.assertEquals(expected, queue.get())
	}

	fun assertCommandFailsWith(sender: CommandSender, command: String, message: String) {
		val exception = Assertions.assertThrows(CommandSyntaxException::class.java) { server.dispatchThrowableCommand(sender, command) }
		Assertions.assertEquals(message, exception.message)
	}

	fun assertNotCommandFailsWith(sender: CommandSender, command: String, message: String) {
		val exception = Assertions.assertThrows(CommandSyntaxException::class.java) { server.dispatchThrowableCommand(sender, command) }
		Assertions.assertNotEquals(message, exception.message)
	}

	fun assertNoMoreResults(mut: Mut<*>) {
		Assertions.assertThrows(NoSuchElementException::class.java, { mut.get() }, "Expected there to be no results left, but at least one was found")
	}

	val dispatcherString: String
		get() = try {
			Files.readString(File(plugin!!.dataFolder, "command_registration.json").toPath())
		} catch (e: IOException) {
			e.printStackTrace(System.out)
			""
		}

	fun registerDummyCommands(commandMap: CommandMap, vararg commandName: String) {
		commandMap.registerAll("minecraft", Arrays.stream(commandName).map { name: String? ->
			object : Command(name!!) {
				override fun execute(commandSender: CommandSender, s: String, strings: Array<String>): Boolean {
					return true
				}
			}
		}.collect(Collectors.toList<Command>()))
	}

	fun <T> compareLists(list1: Collection<T>?, list2: Collection<T>?) {
		val s1: MutableSet<T> = LinkedHashSet(list1)
		val s2: Set<T> = LinkedHashSet(list2)
		val s1_2: Set<T> = LinkedHashSet(list1)
		val s2_2: MutableSet<T> = LinkedHashSet(list2)
		s1.removeAll(s2)
		s2_2.removeAll(s1_2)
		println("List 1 has the following extra items: $s1")
		println("List 2 has the following extra items: $s2_2")
	}

	companion object {
		val P_EXEC = NormalExecutor<Player, Any> { _: Player, _: CommandArguments -> }
	}
}