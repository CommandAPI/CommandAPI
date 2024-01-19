package dev.jorel.commandapi.kotlindsl

import dev.jorel.commandapi.*
import dev.jorel.commandapi.arguments.*

inline fun CommandTree.chatColorArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(ChatColorArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandTree.chatComponentArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(ChatComponentArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandTree.chatArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(ChatArgument(nodeName).setOptional(optional).apply(block))

inline fun Argument<*>.chatColorArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(ChatColorArgument(nodeName).setOptional(optional).apply(block))
inline fun Argument<*>.chatComponentArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(ChatComponentArgument(nodeName).setOptional(optional).apply(block))
inline fun Argument<*>.chatArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(ChatArgument(nodeName).setOptional(optional).apply(block))
