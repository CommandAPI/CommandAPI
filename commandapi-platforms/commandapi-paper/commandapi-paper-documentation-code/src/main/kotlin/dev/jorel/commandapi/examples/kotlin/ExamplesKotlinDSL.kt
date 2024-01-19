package dev.jorel.commandapi.examples.kotlin

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.chatArgument
import dev.jorel.commandapi.kotlindsl.chatColorArgument
import dev.jorel.commandapi.kotlindsl.chatComponentArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.stringArgument
import dev.jorel.commandapi.kotlindsl.textArgument
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.entity.Player

class ExamplesKotlinDSL {

fun argument_chatAdventure() {
/* ANCHOR: argumentChatAdventure1 */
commandAPICommand("namecolor") {
    chatColorArgument("chatcolor")
    playerExecutor { player, args ->
        val color = args["chatcolor"] as NamedTextColor
        player.displayName(Component.text().color(color).append(Component.text(player.name)).build())
    }
}
/* ANCHOR_END: argumentChatAdventure1 */

/* ANCHOR: argumentChatAdventure2 */
commandAPICommand("showbook") {
    playerArgument("target")
    textArgument("title")
    stringArgument("author")
    chatComponentArgument("contents")
    anyExecutor { _, args ->
        val target = args["target"] as Player
        val title = args["title"] as String
        val author = args["author"] as String
        val content = args["contents"] as Component

        // Create a book and show it to the user (Requires Paper)
        val mybook = Book.book(Component.text(title), Component.text(author), content)
        target.openBook(mybook)
    }
}
/* ANCHOR_END: argumentChatAdventure2 */

/* ANCHOR: argumentChatAdventure3 */
commandAPICommand("pbroadcast") {
    chatArgument("message")
    anyExecutor { _, args ->
        val message = args["message"] as Component

        // Broadcast the message to everyone with broadcast permissions.
        Bukkit.getServer().broadcast(message, Server.BROADCAST_CHANNEL_USERS)
        Bukkit.getServer().broadcast(message)
    }
}
/* ANCHOR_END: argumentChatAdventure3 */
}

}