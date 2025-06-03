@file:Suppress("NOTHING_TO_INLINE")

package dev.jorel.commandapi.kotlindsl

import dev.jorel.commandapi.*
import dev.jorel.commandapi.arguments.*
import org.bukkit.plugin.java.JavaPlugin

inline fun commandAPICommand(name: String, command: CommandAPICommand.() -> Unit = {}) = CommandAPICommand(name).apply(command).register()
inline fun commandAPICommand(name: String, namespace: String, command: CommandAPICommand.() -> Unit = {}) = CommandAPICommand(name).apply(command).register(namespace)
inline fun commandAPICommand(name: String, namespace: JavaPlugin, command: CommandAPICommand.() -> Unit = {}) = CommandAPICommand(name).apply(command).register(namespace)

inline fun CommandAPICommand.argument(base: Argument<*>, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(base.apply(block))
inline fun CommandAPICommand.arguments(vararg arguments: Argument<*>): CommandAPICommand = withArguments(*arguments)

inline fun CommandAPICommand.optionalArgument(base: Argument<*>, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withOptionalArguments(base.apply(block))
inline fun CommandAPICommand.optionalArguments(vararg arguments: Argument<*>): CommandAPICommand = withOptionalArguments(*arguments)

inline fun subcommand(name: String, command: CommandAPICommand.() -> Unit = {}): CommandAPICommand = CommandAPICommand(name).apply(command)
inline fun CommandAPICommand.subcommand(command: CommandAPICommand): CommandAPICommand = withSubcommand(command)
inline fun CommandAPICommand.subcommand(name: String, command: CommandAPICommand.() -> Unit = {}): CommandAPICommand = withSubcommand(CommandAPICommand(name).apply(command))

// Integer arguments
inline fun CommandAPICommand.integerArgument(nodeName: String, min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(IntegerArgument(nodeName, min, max).setOptional(optional).apply(block))
inline fun CommandAPICommand.integerRangeArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(IntegerRangeArgument(nodeName).setOptional(optional).apply(block))

// Float arguments
inline fun CommandAPICommand.floatArgument(nodeName: String, min: Float = -Float.MAX_VALUE, max: Float = Float.MAX_VALUE, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(FloatArgument(nodeName, min, max).setOptional(optional).apply(block))

// Double arguments
inline fun CommandAPICommand.doubleArgument(nodeName: String, min: Double = -Double.MAX_VALUE, max: Double = Double.MAX_VALUE, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(DoubleArgument(nodeName, min, max).setOptional(optional).apply(block))
inline fun CommandAPICommand.doubleRangeArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(DoubleRangeArgument(nodeName).setOptional(optional).apply(block))

// Long arguments
inline fun CommandAPICommand.longArgument(nodeName: String, min: Long = Long.MIN_VALUE, max: Long = Long.MAX_VALUE, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(LongArgument(nodeName, min, max).setOptional(optional).apply(block))

// Boolean argument
inline fun CommandAPICommand.booleanArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(BooleanArgument(nodeName).setOptional(optional).apply(block))

// String arguments
inline fun CommandAPICommand.stringArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(StringArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandAPICommand.textArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(TextArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandAPICommand.greedyStringArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(GreedyStringArgument(nodeName).setOptional(optional).apply(block))

// Positional arguments
inline fun CommandAPICommand.locationArgument(nodeName: String, locationType: LocationType = LocationType.PRECISE_POSITION, centerPosition: Boolean = true, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(LocationArgument(nodeName, locationType, centerPosition).setOptional(optional).apply(block))
inline fun CommandAPICommand.location2DArgument(nodeName: String, locationType: LocationType = LocationType.PRECISE_POSITION, centerPosition: Boolean = true, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(Location2DArgument(nodeName, locationType, centerPosition).setOptional(optional).apply(block))
inline fun CommandAPICommand.rotationArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(RotationArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandAPICommand.axisArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(AxisArgument(nodeName).setOptional(optional).apply(block))

// Entity & Player arguments
inline fun CommandAPICommand.entitySelectorArgumentOneEntity(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(EntitySelectorArgument.OneEntity(nodeName).setOptional(optional).apply(block))
inline fun CommandAPICommand.entitySelectorArgumentManyEntities(nodeName: String, allowEmpty: Boolean = true, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(EntitySelectorArgument.ManyEntities(nodeName, allowEmpty).setOptional(optional).apply(block))
inline fun CommandAPICommand.entitySelectorArgumentOnePlayer(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(EntitySelectorArgument.OnePlayer(nodeName).setOptional(optional).apply(block))
inline fun CommandAPICommand.entitySelectorArgumentManyPlayers(nodeName: String, allowEmpty: Boolean = true, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(EntitySelectorArgument.ManyPlayers(nodeName, allowEmpty).setOptional(optional).apply(block))
inline fun CommandAPICommand.entityTypeArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(EntityTypeArgument(nodeName).setOptional(optional).apply(block))

// Scoreboard arguments
inline fun CommandAPICommand.scoreHolderArgumentSingle(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(ScoreHolderArgument.Single(nodeName).setOptional(optional).apply(block))
inline fun CommandAPICommand.scoreHolderArgumentMultiple(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(ScoreHolderArgument.Multiple(nodeName).setOptional(optional).apply(block))
inline fun CommandAPICommand.scoreboardSlotArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(ScoreboardSlotArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandAPICommand.objectiveArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(ObjectiveArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandAPICommand.objectiveCriteriaArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(ObjectiveCriteriaArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandAPICommand.teamArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(TeamArgument(nodeName).setOptional(optional).apply(block))

// Miscellaneous arguments
inline fun CommandAPICommand.angleArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(AngleArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandAPICommand.advancementArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(AdvancementArgument(nodeName).setOptional(optional).apply(block))

inline fun CommandAPICommand.biomeArgument(nodeName: String, useNamespacedKey: Boolean = false, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand =
	if (useNamespacedKey) withArguments(BiomeArgument.NamespacedKey(nodeName).setOptional(optional).apply(block)) else withArguments(BiomeArgument(nodeName).setOptional(optional).apply(block))

inline fun CommandAPICommand.blockStateArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(BlockStateArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandAPICommand.commandArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(CommandArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandAPICommand.enchantmentArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(EnchantmentArgument(nodeName).setOptional(optional).apply(block))

inline fun CommandAPICommand.itemStackArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(ItemStackArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandAPICommand.lootTableArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(LootTableArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandAPICommand.mathOperationArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(MathOperationArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandAPICommand.namespacedKeyArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(NamespacedKeyArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandAPICommand.particleArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(ParticleArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandAPICommand.potionEffectArgument(nodeName: String, useNamespacedKey: Boolean = false, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand =
	if (useNamespacedKey) withArguments(PotionEffectArgument.NamespacedKey(nodeName).setOptional(optional).apply(block)) else withArguments(PotionEffectArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandAPICommand.recipeArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(RecipeArgument(nodeName).setOptional(optional).apply(block))

inline fun CommandAPICommand.soundArgument(nodeName: String, useNamespacedKey: Boolean = false, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand =
	if (useNamespacedKey) withArguments(SoundArgument.NamespacedKey(nodeName).setOptional(optional).apply(block)) else withArguments(SoundArgument(nodeName).setOptional(optional).apply(block))

inline fun CommandAPICommand.timeArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(TimeArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandAPICommand.uuidArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(UUIDArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandAPICommand.worldArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(WorldArgument(nodeName).setOptional(optional).apply(block))

// Predicate arguments
inline fun CommandAPICommand.blockPredicateArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(BlockPredicateArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandAPICommand.itemStackPredicateArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(ItemStackPredicateArgument(nodeName).setOptional(optional).apply(block))

// NBT arguments
inline fun <NBTContainer> CommandAPICommand.nbtCompoundArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(NBTCompoundArgument<NBTContainer>(nodeName).setOptional(optional).apply(block))

// Literal arguments
inline fun CommandAPICommand.literalArgument(literal: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(LiteralArgument.of(literal, literal).setOptional(optional).apply(block))
inline fun CommandAPICommand.literalArgument(nodeName: String, literal: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(LiteralArgument.of(nodeName, literal).setOptional(optional).apply(block))

inline fun CommandAPICommand.multiLiteralArgument(nodeName: String, vararg literals: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(MultiLiteralArgument(nodeName, *literals).setOptional(optional).apply(block))

// Function arguments
inline fun CommandAPICommand.functionArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandAPICommand = withArguments(FunctionArgument(nodeName).setOptional(optional).apply(block))
