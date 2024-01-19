package dev.jorel.commandapi.kotlindsl

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ChatArgument
import dev.jorel.commandapi.arguments.ChatColorArgument
import dev.jorel.commandapi.arguments.ChatComponentArgument

inline fun CommandAPICommand.chatColorArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(ChatColorArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandAPICommand.chatComponentArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(ChatComponentArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandAPICommand.chatArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(ChatArgument(nodeName).setOptional(optional).apply(block))
