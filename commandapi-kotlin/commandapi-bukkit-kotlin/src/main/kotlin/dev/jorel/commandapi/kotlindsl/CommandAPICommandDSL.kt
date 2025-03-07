@file:Suppress("NOTHING_TO_INLINE")

package dev.jorel.commandapi.kotlindsl

import dev.jorel.commandapi.*
import dev.jorel.commandapi.arguments.*
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import java.util.function.Predicate

inline fun commandAPICommand(name: String, command: CommandAPICommand.() -> Unit = {}) = CommandAPICommand(name).apply(command).register()
inline fun commandAPICommand(name: String, namespace: String, command: CommandAPICommand.() -> Unit = {}) = CommandAPICommand(name).apply(command).register(namespace)
inline fun commandAPICommand(name: String, namespace: JavaPlugin, command: CommandAPICommand.() -> Unit = {}) = CommandAPICommand(name).apply(command).register(namespace)
@Deprecated("This method has been deprecated since version 9.1.0 as it is not needed anymore. See the documentation for more information", ReplaceWith(""), DeprecationLevel.WARNING)
inline fun commandAPICommand(name: String, predicate: Predicate<CommandSender>, command: CommandAPICommand.() -> Unit = {}) = CommandAPICommand(name).withRequirement(predicate).apply(command).register()

inline fun CommandAPICommand.argument(base: Argument<*>, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(base.apply(block))
inline fun CommandAPICommand.arguments(vararg arguments: Argument<*>): CommandAPICommand = withArguments(*arguments)

inline fun CommandAPICommand.optionalArgument(base: Argument<*>, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withOptionalArguments(base.apply(block))
inline fun CommandAPICommand.optionalArguments(vararg arguments: Argument<*>): CommandAPICommand = withOptionalArguments(*arguments)

inline fun subcommand(name: String, command: CommandAPICommand.() -> Unit = {}): CommandAPICommand = CommandAPICommand(name).apply(command)
inline fun CommandAPICommand.subcommand(command: CommandAPICommand): CommandAPICommand = withSubcommand(command)
inline fun CommandAPICommand.subcommand(name: String, command: CommandAPICommand.() -> Unit = {}): CommandAPICommand = withSubcommand(CommandAPICommand(name).apply(command))

inline fun CommandAPICommand.addArgument(argument: Argument<*>, optional: Boolean, block: Argument<*>.() -> Unit = {}): CommandAPICommand = if (optional) optionalArgument(argument, block) else argument(argument, block)

// Integer arguments
inline fun CommandAPICommand.integerArgument(nodeName: String, min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(IntegerArgument(nodeName, min, max), optional, block)
inline fun CommandAPICommand.integerRangeArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(IntegerRangeArgument(nodeName), optional, block)

// Float arguments
inline fun CommandAPICommand.floatArgument(nodeName: String, min: Float = -Float.MAX_VALUE, max: Float = Float.MAX_VALUE, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(FloatArgument(nodeName, min, max), optional, block)
inline fun CommandAPICommand.floatRangeArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(FloatRangeArgument(nodeName), optional, block)

// Double arguments
inline fun CommandAPICommand.doubleArgument(nodeName: String, min: Double = -Double.MAX_VALUE, max: Double = Double.MAX_VALUE, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(DoubleArgument(nodeName, min, max), optional, block)

// Long arguments
inline fun CommandAPICommand.longArgument(nodeName: String, min: Long = Long.MIN_VALUE, max: Long = Long.MAX_VALUE, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(LongArgument(nodeName, min, max), optional, block)

// Boolean argument
inline fun CommandAPICommand.booleanArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(BooleanArgument(nodeName), optional, block)

// String arguments
inline fun CommandAPICommand.stringArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(StringArgument(nodeName), optional, block)
inline fun CommandAPICommand.textArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(TextArgument(nodeName), optional, block)
inline fun CommandAPICommand.greedyStringArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(GreedyStringArgument(nodeName), optional, block)

// Positional arguments
inline fun CommandAPICommand.locationArgument(nodeName: String, locationType: LocationType = LocationType.PRECISE_POSITION, centerPosition: Boolean = true, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(LocationArgument(nodeName, locationType, centerPosition), optional, block)
inline fun CommandAPICommand.location2DArgument(nodeName: String, locationType: LocationType = LocationType.PRECISE_POSITION, centerPosition: Boolean = true, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(Location2DArgument(nodeName, locationType, centerPosition), optional, block)
inline fun CommandAPICommand.rotationArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(RotationArgument(nodeName), optional, block)
inline fun CommandAPICommand.axisArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(AxisArgument(nodeName), optional, block)

// Chat arguments
inline fun CommandAPICommand.chatColorArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(ChatColorArgument(nodeName), optional, block)
inline fun CommandAPICommand.chatComponentArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(ChatComponentArgument(nodeName), optional, block)
inline fun CommandAPICommand.chatArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(ChatArgument(nodeName), optional, block)
inline fun CommandAPICommand.adventureChatColorArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(AdventureChatColorArgument(nodeName), optional, block)
inline fun CommandAPICommand.adventureChatComponentArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(AdventureChatComponentArgument(nodeName), optional, block)
inline fun CommandAPICommand.adventureChatArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(AdventureChatArgument(nodeName), optional, block)

// Entity & Player arguments
inline fun CommandAPICommand.entitySelectorArgumentOneEntity(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(EntitySelectorArgument.OneEntity(nodeName), optional, block)
inline fun CommandAPICommand.entitySelectorArgumentManyEntities(nodeName: String, allowEmpty: Boolean = true, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(EntitySelectorArgument.ManyEntities(nodeName, allowEmpty), optional, block)
inline fun CommandAPICommand.entitySelectorArgumentOnePlayer(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(EntitySelectorArgument.OnePlayer(nodeName), optional, block)
inline fun CommandAPICommand.entitySelectorArgumentManyPlayers(nodeName: String, allowEmpty: Boolean = true, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(EntitySelectorArgument.ManyPlayers(nodeName, allowEmpty), optional, block)
inline fun CommandAPICommand.playerArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(PlayerArgument(nodeName), optional, block)
inline fun CommandAPICommand.offlinePlayerArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(OfflinePlayerArgument(nodeName), optional, block)
inline fun CommandAPICommand.asyncOfflinePlayerArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(AsyncOfflinePlayerArgument(nodeName), optional, block)
inline fun CommandAPICommand.entityTypeArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(EntityTypeArgument(nodeName), optional, block)

// Scoreboard arguments
inline fun CommandAPICommand.scoreHolderArgumentSingle(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(ScoreHolderArgument.Single(nodeName), optional, block)
inline fun CommandAPICommand.scoreHolderArgumentMultiple(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(ScoreHolderArgument.Multiple(nodeName), optional, block)
inline fun CommandAPICommand.scoreboardSlotArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(ScoreboardSlotArgument(nodeName), optional, block)
inline fun CommandAPICommand.objectiveArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(ObjectiveArgument(nodeName), optional, block)
inline fun CommandAPICommand.objectiveCriteriaArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(ObjectiveCriteriaArgument(nodeName), optional, block)
inline fun CommandAPICommand.teamArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(TeamArgument(nodeName), optional, block)

// Miscellaneous arguments
inline fun CommandAPICommand.angleArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(AngleArgument(nodeName), optional, block)
inline fun CommandAPICommand.advancementArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(AdvancementArgument(nodeName), optional, block)

inline fun CommandAPICommand.biomeArgument(nodeName: String, useNamespacedKey: Boolean = false, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand =
	if (useNamespacedKey) addArgument(BiomeArgument.NamespacedKey(nodeName), optional, block) else addArgument(BiomeArgument(nodeName), optional, block)

inline fun CommandAPICommand.blockStateArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(BlockStateArgument(nodeName), optional, block)
inline fun CommandAPICommand.commandArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(CommandArgument(nodeName), optional, block)
inline fun CommandAPICommand.enchantmentArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(EnchantmentArgument(nodeName), optional, block)

inline fun CommandAPICommand.itemStackArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(ItemStackArgument(nodeName), optional, block)
inline fun CommandAPICommand.lootTableArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(LootTableArgument(nodeName), optional, block)
inline fun CommandAPICommand.mathOperationArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(MathOperationArgument(nodeName), optional, block)
inline fun CommandAPICommand.namespacedKeyArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(NamespacedKeyArgument(nodeName), optional, block)
inline fun CommandAPICommand.particleArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(ParticleArgument(nodeName), optional, block)
inline fun CommandAPICommand.potionEffectArgument(nodeName: String, useNamespacedKey: Boolean = false, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand =
	if (useNamespacedKey) addArgument(PotionEffectArgument.NamespacedKey(nodeName), optional, block) else addArgument(PotionEffectArgument(nodeName), optional, block)
inline fun CommandAPICommand.recipeArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(RecipeArgument(nodeName), optional, block)

inline fun CommandAPICommand.soundArgument(nodeName: String, useNamespacedKey: Boolean = false, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand =
	if (useNamespacedKey) addArgument(SoundArgument.NamespacedKey(nodeName), optional, block) else addArgument(SoundArgument(nodeName), optional, block)

inline fun CommandAPICommand.timeArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(TimeArgument(nodeName), optional, block)
inline fun CommandAPICommand.uuidArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(UUIDArgument(nodeName), optional, block)
inline fun CommandAPICommand.worldArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(WorldArgument(nodeName), optional, block)

// Predicate arguments
inline fun CommandAPICommand.blockPredicateArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(BlockPredicateArgument(nodeName), optional, block)
inline fun CommandAPICommand.itemStackPredicateArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(ItemStackPredicateArgument(nodeName), optional, block)

// NBT arguments
inline fun <NBTContainer> CommandAPICommand.nbtCompoundArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(NBTCompoundArgument<NBTContainer>(nodeName), optional, block)

// Literal arguments
inline fun CommandAPICommand.literalArgument(literal: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(LiteralArgument.of(literal, literal), optional, block)
inline fun CommandAPICommand.literalArgument(nodeName: String, literal: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(LiteralArgument.of(nodeName, literal), optional, block)

@Deprecated("This method has been deprecated since version 9.1.0", ReplaceWith("multiLiteralArgument(nodeName, literals)", "dev.jorel.commandapi.kotlindsl.*"), DeprecationLevel.WARNING)
inline fun CommandAPICommand.multiLiteralArgument(nodeName: String, literals: List<String>, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(MultiLiteralArgument(nodeName, literals), optional, block)

inline fun CommandAPICommand.multiLiteralArgument(nodeName: String, vararg literals: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(MultiLiteralArgument(nodeName, *literals), optional, block)

// Function arguments
inline fun CommandAPICommand.functionArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(FunctionArgument(nodeName), optional, block)

// Requirements
@Deprecated("This method has been deprecated since version 9.1.0 as it is not needed anymore. See the documentation for more information", ReplaceWith(""), DeprecationLevel.WARNING)
inline fun CommandAPICommand.requirement(base: Argument<*>, predicate: Predicate<CommandSender>, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = addArgument(base.withRequirement(predicate), optional, block)
