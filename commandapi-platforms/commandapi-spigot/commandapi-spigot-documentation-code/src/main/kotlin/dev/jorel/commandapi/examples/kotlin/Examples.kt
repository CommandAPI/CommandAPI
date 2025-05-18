package dev.jorel.commandapi.examples.kotlin

import de.tr7zw.changeme.nbtapi.NBTContainer
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandAPISpigotConfig
import dev.jorel.commandapi.arguments.*
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.executors.CommandExecutor
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.plugin.java.JavaPlugin

class Examples {

fun argument_chatSpigot() {
/* ANCHOR: argumentChatSpigot1 */
CommandAPICommand("namecolor")
    .withArguments(ChatColorArgument("chatColor"))
    .executesPlayer(PlayerCommandExecutor { player: Player, args: CommandArguments ->
        val color = args["chatColor"] as ChatColor
        player.setDisplayName("$color${player.name}")
    })
    .register()
/* ANCHOR_END: argumentChatSpigot1 */

/* ANCHOR: argumentChatSpigot2 */
CommandAPICommand("makebook")
    .withArguments(PlayerArgument("player"))
    .withArguments(ChatComponentArgument("contents"))
    .executes(CommandExecutor { _, args ->
        val player = args["player"] as Player
        val arr = args["contents"] as Array<BaseComponent>

        // Create book
        val item = ItemStack(Material.WRITTEN_BOOK)
        val meta = item.itemMeta as BookMeta
        meta.title = "Custom Book"
        meta.author = player.name
        meta.spigot().setPages(arr)
        item.itemMeta = meta

        // Give player the book
        player.inventory.addItem(item)
    })
    .register()
/* ANCHOR_END: argumentChatSpigot2 */

/* ANCHOR: argumentChatSpigot3 */
CommandAPICommand("pbroadcast")
    .withArguments(ChatArgument("message"))
    .executes(CommandExecutor { _, args ->
        val message = args["message"] as Array<BaseComponent>

        // Broadcast the message to everyone on the server
        Bukkit.getServer().spigot().broadcast(*message)
    })
    .register()
/* ANCHOR_END: argumentChatSpigot3 */
}

class argument_nbt : JavaPlugin() {

/* ANCHOR: argumentNBT1 */
override fun onLoad() {
    CommandAPI.onLoad(CommandAPISpigotConfig(this)
        .initializeNBTAPI(NBTContainer::class.java, ::NBTContainer)
    )
}
/* ANCHOR_END: argumentNBT1 */
}

class setupShading {
val plugin: JavaPlugin = object : JavaPlugin() {}

fun setupShading1() {
/* ANCHOR: setupShading1 */
CommandAPI.onLoad(CommandAPISpigotConfig(plugin).silentLogs(true))
/* ANCHOR_END: setupShading1 */
}

/* ANCHOR: setupShading2 */
class MyPlugin : JavaPlugin() {

    override fun onLoad() {
        CommandAPI.onLoad(CommandAPISpigotConfig(this).verboseOutput(true)) // Load with verbose output

        CommandAPICommand("ping")
            .executes(CommandExecutor { sender: CommandSender, _ ->
                sender.sendMessage("pong!")
            })
            .register()
    }

    override fun onEnable() {
        CommandAPI.onEnable(this)

        // Register commands, listeners etc.
    }

    override fun onDisable() {
        CommandAPI.onDisable()
    }

}
/* ANCHOR_END: setupShading2 */
}

}