package dev.jorel.commandapi.kotlindsl

import com.velocitypowered.api.command.CommandSource
import dev.jorel.commandapi.*
import dev.jorel.commandapi.arguments.*
import java.util.function.Predicate

inline fun commandTree(name: String, tree: CommandTree.() -> Unit = {}) = CommandTree(name).apply(tree).register()
inline fun commandTree(name: String, namespace: String, tree: CommandTree.() -> Unit = {}) = CommandTree(name).apply(tree).register(namespace)

// CommandTree start
inline fun CommandTree.argument(base: Argument<*>, block: Argument<*>.() -> Unit = {}): CommandTree = then(base.apply(block))

inline fun CommandTree.optionalArgument(base: Argument<*>, block: Argument<*>.() -> Unit = {}): CommandTree = then(base.setOptional(true).apply(block))

// Integer arguments
inline fun CommandTree.integerArgument(nodeName: String, min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(IntegerArgument(nodeName, min, max).setOptional(optional).apply(block))

// Float arguments
inline fun CommandTree.floatArgument(nodeName: String, min: Float = -Float.MAX_VALUE, max: Float = Float.MAX_VALUE, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(FloatArgument(nodeName, min, max).setOptional(optional).apply(block))

// Double arguments
inline fun CommandTree.doubleArgument(nodeName: String, min: Double = -Double.MAX_VALUE, max: Double = Double.MAX_VALUE, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(DoubleArgument(nodeName, min, max).setOptional(optional).apply(block))

// Long arguments
inline fun CommandTree.longArgument(nodeName: String, min: Long = Long.MIN_VALUE, max: Long = Long.MAX_VALUE, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(LongArgument(nodeName, min, max).setOptional(optional).apply(block))

// Boolean argument
inline fun CommandTree.booleanArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(BooleanArgument(nodeName).setOptional(optional).apply(block))

// String arguments
inline fun CommandTree.stringArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(StringArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandTree.textArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(TextArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandTree.greedyStringArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(GreedyStringArgument(nodeName).setOptional(optional).apply(block))

// Literal arguments
inline fun CommandTree.literalArgument(literal: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree= then(LiteralArgument.of(literal, literal).setOptional(optional).apply(block))
inline fun CommandTree.literalArgument(nodeName: String, literal: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree= then(LiteralArgument.of(nodeName, literal).setOptional(optional).apply(block))

inline fun CommandTree.multiLiteralArgument(nodeName: String, vararg literals: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree= then(MultiLiteralArgument(nodeName, *literals).setOptional(optional).apply(block))


// ArgumentTree start
inline fun Argument<*>.argument(base: Argument<*>, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(base.setOptional(optional).apply(block))

inline fun Argument<*>.optionalArgument(base: Argument<*>, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(base.setOptional(true).setOptional(optional).apply(block))

// Integer arguments
inline fun Argument<*>.integerArgument(nodeName: String, min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(IntegerArgument(nodeName, min, max).setOptional(optional).apply(block))

// Float arguments
inline fun Argument<*>.floatArgument(nodeName: String, min: Float = -Float.MAX_VALUE, max: Float = Float.MAX_VALUE, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(FloatArgument(nodeName, min, max).setOptional(optional).apply(block))

// Double arguments
inline fun Argument<*>.doubleArgument(nodeName: String, min: Double = -Double.MAX_VALUE, max: Double = Double.MAX_VALUE, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(DoubleArgument(nodeName, min, max).setOptional(optional).apply(block))

// Long arguments
inline fun Argument<*>.longArgument(nodeName: String, min: Long = Long.MIN_VALUE, max: Long = Long.MAX_VALUE, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(LongArgument(nodeName, min, max).setOptional(optional).apply(block))

// Boolean argument
inline fun Argument<*>.booleanArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(BooleanArgument(nodeName).setOptional(optional).apply(block))

// String arguments
inline fun Argument<*>.stringArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(StringArgument(nodeName).setOptional(optional).apply(block))
inline fun Argument<*>.textArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(TextArgument(nodeName).setOptional(optional).apply(block))
inline fun Argument<*>.greedyStringArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(GreedyStringArgument(nodeName).setOptional(optional).apply(block))

// Literal arguments
inline fun Argument<*>.literalArgument(literal: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(LiteralArgument.of(literal, literal).setOptional(optional).apply(block))
inline fun Argument<*>.literalArgument(nodeName: String, literal: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(LiteralArgument.of(nodeName, literal).setOptional(optional).apply(block))

inline fun Argument<*>.multiLiteralArgument(nodeName: String, vararg literals: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(MultiLiteralArgument(nodeName, *literals).setOptional(optional).apply(block))
