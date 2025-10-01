package dev.jorel.commandapi.kotlindsl

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.AsyncPlayerProfileArgument
import dev.jorel.commandapi.arguments.ChatArgument
import dev.jorel.commandapi.arguments.ChatColorArgument
import dev.jorel.commandapi.arguments.ChatComponentArgument
import dev.jorel.commandapi.arguments.PlayerProfileArgument
import dev.jorel.commandapi.arguments.RecipeArgument

inline fun CommandAPICommand.chatColorArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(ChatColorArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandAPICommand.chatComponentArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(ChatComponentArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandAPICommand.chatArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(ChatArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandAPICommand.playerProfileArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(PlayerProfileArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandAPICommand.asyncPlayerProfileArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(AsyncPlayerProfileArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandAPICommand.recipeArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(RecipeArgument(nodeName).setOptional(optional).apply(block))
