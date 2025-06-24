package dev.jorel.commandapi.examples.kotlin

import de.tr7zw.changeme.nbtapi.NBTContainer
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.kotlindsl.*
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta

class ExamplesKotlinDSL {

fun argument_chatSpigot() {
/* ANCHOR: argumentChatSpigot1 */
commandAPICommand("namecolor") {
    chatColorArgument("chatcolor")
    playerExecutor { player: Player, args: CommandArguments ->
        val color = args["chatcolor"] as ChatColor
        player.setDisplayName("$color${player.name}")
    }
}
/* ANCHOR_END: argumentChatSpigot1 */

/* ANCHOR: argumentChatSpigot2 */
commandAPICommand("makebook") {
    playerArgument("player")
    chatComponentArgument("contents")
    anyExecutor { _, args ->
        val player = args["player"] as Player
        val array = args["contents"] as Array<BaseComponent>

        // Create book
        val item = ItemStack(Material.WRITTEN_BOOK)
        val meta = item.itemMeta as BookMeta
        meta.title = "Custom Book"
        meta.author = player.name
        meta.spigot().setPages(array)
        item.itemMeta = meta

        // Give player the book
        player.inventory.addItem(item)
    }
}
/* ANCHOR_END: argumentChatSpigot2 */

/* ANCHOR: argumentChatSpigot3 */
commandAPICommand("pbroadcast") {
    chatArgument("message")
    anyExecutor { _, args ->
        val message = args["message"] as Array<BaseComponent>

        // Broadcast the message to everyone on the server
        Bukkit.getServer().spigot().broadcast(*message)
    }
}
/* ANCHOR_END: argumentChatSpigot3 */
}

}