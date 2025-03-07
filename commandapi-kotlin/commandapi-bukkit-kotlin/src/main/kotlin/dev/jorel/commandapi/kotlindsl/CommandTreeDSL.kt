package dev.jorel.commandapi.kotlindsl

import dev.jorel.commandapi.*
import dev.jorel.commandapi.arguments.*
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import java.util.function.Predicate

const val WILL_NOT_REGISTER = "will-not-reg"

inline fun commandTree(name: String, tree: CommandTree.() -> Unit = {}) = CommandTree(name).apply(tree).register()
inline fun commandTree(name: String, namespace: String, tree: CommandTree.() -> Unit = {}) = CommandTree(name).apply(tree).register(namespace)
inline fun commandTree(name: String, namespace: JavaPlugin, tree: CommandTree.() -> Unit = {}) = CommandTree(name).apply(tree).register(namespace)
@Deprecated("This method has been deprecated since version 9.1.0 as it is not needed anymore. See the documentation for more information", ReplaceWith(""), DeprecationLevel.WARNING)
inline fun commandTree(name: String, predicate: Predicate<CommandSender>, tree: CommandTree.() -> Unit = {}) = CommandTree(name).withRequirement(predicate).apply(tree).register()

// CommandTree start
inline fun CommandTree.argument(base: Argument<*>, block: Argument<*>.() -> Unit = {}): CommandTree = then(base.apply(block))


inline fun CommandTree.nestedArguments(vararg arguments: Argument<*>,block: Argument<*>.() -> Unit = {}): CommandTree = thenNested(*arguments.also { it.last().apply(block) })
inline fun CommandTree.nested(block: CommandTree.() -> Unit): CommandTree {
	val arguments = mutableListOf<AbstractArgumentTree<*, Argument<*>?, CommandSender?>?>()
	object : CommandTree(WILL_NOT_REGISTER) {
		override fun then(tree: AbstractArgumentTree<*, Argument<*>?, CommandSender?>?): CommandTree? {
			arguments.add(tree)
			return this
		}
	}.block()
	return thenNested(arguments)
}

// Integer arguments
inline fun CommandTree.integerArgument(nodeName: String, min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE, block: Argument<*>.() -> Unit = {}): CommandTree = then(IntegerArgument(nodeName, min, max).apply(block))
inline fun CommandTree.integerRangeArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(IntegerRangeArgument(nodeName).apply(block))

// Float arguments
inline fun CommandTree.floatArgument(nodeName: String, min: Float = -Float.MAX_VALUE, max: Float = Float.MAX_VALUE, block: Argument<*>.() -> Unit = {}): CommandTree = then(FloatArgument(nodeName, min, max).apply(block))
inline fun CommandTree.floatRangeArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(FloatRangeArgument(nodeName).apply(block))

// Double arguments
inline fun CommandTree.doubleArgument(nodeName: String, min: Double = -Double.MAX_VALUE, max: Double = Double.MAX_VALUE, block: Argument<*>.() -> Unit = {}): CommandTree = then(DoubleArgument(nodeName, min, max).apply(block))

// Long arguments
inline fun CommandTree.longArgument(nodeName: String, min: Long = Long.MIN_VALUE, max: Long = Long.MAX_VALUE, block: Argument<*>.() -> Unit = {}): CommandTree = then(LongArgument(nodeName, min, max).apply(block))

// Boolean argument
inline fun CommandTree.booleanArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(BooleanArgument(nodeName).apply(block))

// String arguments
inline fun CommandTree.stringArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(StringArgument(nodeName).apply(block))
inline fun CommandTree.textArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(TextArgument(nodeName).apply(block))
inline fun CommandTree.greedyStringArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(GreedyStringArgument(nodeName).apply(block))

// Positional arguments
inline fun CommandTree.locationArgument(nodeName: String, locationType: LocationType = LocationType.PRECISE_POSITION, centerPosition: Boolean = true, block: Argument<*>.() -> Unit = {}): CommandTree = then(LocationArgument(nodeName, locationType, centerPosition).apply(block))
inline fun CommandTree.location2DArgument(nodeName: String, locationType: LocationType = LocationType.PRECISE_POSITION, centerPosition: Boolean = true, block: Argument<*>.() -> Unit = {}): CommandTree = then(Location2DArgument(nodeName, locationType, centerPosition).apply(block))
inline fun CommandTree.rotationArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(RotationArgument(nodeName).apply(block))
inline fun CommandTree.axisArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(AxisArgument(nodeName).apply(block))

// Chat arguments
inline fun CommandTree.chatColorArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(ChatColorArgument(nodeName).apply(block))
inline fun CommandTree.chatComponentArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(ChatComponentArgument(nodeName).apply(block))
inline fun CommandTree.chatArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(ChatArgument(nodeName).apply(block))
inline fun CommandTree.adventureChatColorArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(AdventureChatColorArgument(nodeName).apply(block))
inline fun CommandTree.adventureChatComponentArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(AdventureChatComponentArgument(nodeName).apply(block))
inline fun CommandTree.adventureChatArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(AdventureChatArgument(nodeName).apply(block))

// Entity & Player arguments
inline fun CommandTree.entitySelectorArgumentOneEntity(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(EntitySelectorArgument.OneEntity(nodeName).apply(block))
inline fun CommandTree.entitySelectorArgumentManyEntities(nodeName: String, allowEmpty: Boolean = true, block: Argument<*>.() -> Unit = {}): CommandTree = then(EntitySelectorArgument.ManyEntities(nodeName, allowEmpty).apply(block))
inline fun CommandTree.entitySelectorArgumentOnePlayer(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(EntitySelectorArgument.OnePlayer(nodeName).apply(block))
inline fun CommandTree.entitySelectorArgumentManyPlayers(nodeName: String, allowEmpty: Boolean = true, block: Argument<*>.() -> Unit = {}): CommandTree = then(EntitySelectorArgument.ManyPlayers(nodeName, allowEmpty).apply(block))
inline fun CommandTree.playerArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(PlayerArgument(nodeName).apply(block))
inline fun CommandTree.offlinePlayerArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(OfflinePlayerArgument(nodeName).apply(block))
inline fun CommandTree.asyncOfflinePlayerArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(AsyncOfflinePlayerArgument(nodeName).apply(block))
inline fun CommandTree.entityTypeArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(EntityTypeArgument(nodeName).apply(block))

// Scoreboard arguments
inline fun CommandTree.scoreHolderArgumentSingle(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(ScoreHolderArgument.Single(nodeName).apply(block))
inline fun CommandTree.scoreHolderArgumentMultiple(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(ScoreHolderArgument.Multiple(nodeName).apply(block))
inline fun CommandTree.scoreboardSlotArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(ScoreboardSlotArgument(nodeName).apply(block))
inline fun CommandTree.objectiveArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(ObjectiveArgument(nodeName).apply(block))
inline fun CommandTree.objectiveCriteriaArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(ObjectiveCriteriaArgument(nodeName).apply(block))
inline fun CommandTree.teamArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(TeamArgument(nodeName).apply(block))

// Miscellaneous arguments
inline fun CommandTree.angleArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(AngleArgument(nodeName).apply(block))
inline fun CommandTree.advancementArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(AdvancementArgument(nodeName).apply(block))

inline fun CommandTree.biomeArgument(nodeName: String, useNamespacedKey: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree =
	if (useNamespacedKey) then(BiomeArgument.NamespacedKey(nodeName).apply(block)) else then(BiomeArgument(nodeName).apply(block))

inline fun CommandTree.blockStateArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(BlockStateArgument(nodeName).apply(block))
inline fun CommandTree.commandArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(CommandArgument(nodeName).apply(block))
inline fun CommandTree.enchantmentArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(EnchantmentArgument(nodeName).apply(block))

inline fun CommandTree.itemStackArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(ItemStackArgument(nodeName).apply(block))
inline fun CommandTree.lootTableArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(LootTableArgument(nodeName).apply(block))
inline fun CommandTree.mathOperationArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(MathOperationArgument(nodeName).apply(block))
inline fun CommandTree.namespacedKeyArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(NamespacedKeyArgument(nodeName).apply(block))
inline fun CommandTree.particleArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(ParticleArgument(nodeName).apply(block))
inline fun CommandTree.potionEffectArgument(nodeName: String, useNamespacedKey: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree =
	if (useNamespacedKey) then(PotionEffectArgument.NamespacedKey(nodeName).apply(block)) else then(PotionEffectArgument(nodeName).apply(block))
inline fun CommandTree.recipeArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(RecipeArgument(nodeName).apply(block))

inline fun CommandTree.soundArgument(nodeName: String, useNamespacedKey: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree =
	if (useNamespacedKey) then(SoundArgument.NamespacedKey(nodeName).apply(block)) else then(SoundArgument(nodeName).apply(block))

inline fun CommandTree.timeArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(TimeArgument(nodeName).apply(block))
inline fun CommandTree.uuidArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(UUIDArgument(nodeName).apply(block))
inline fun CommandTree.worldArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(WorldArgument(nodeName).apply(block))

// Predicate arguments
inline fun CommandTree.blockPredicateArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(BlockPredicateArgument(nodeName).apply(block))
inline fun CommandTree.itemStackPredicateArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(ItemStackPredicateArgument(nodeName).apply(block))

// NBT arguments
inline fun <NBTContainer> CommandTree.nbtCompoundArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(NBTCompoundArgument<NBTContainer>(nodeName).apply(block))

// Literal arguments
inline fun CommandTree.literalArgument(literal: String, block: Argument<*>.() -> Unit = {}): CommandTree= then(LiteralArgument.of(literal, literal).apply(block))
inline fun CommandTree.literalArgument(nodeName: String, literal: String, block: Argument<*>.() -> Unit = {}): CommandTree= then(LiteralArgument.of(nodeName, literal).apply(block))

@Deprecated("This method has been deprecated since version 9.1.0", ReplaceWith("multiLiteralArgument(nodeName, literals)", "dev.jorel.commandapi.kotlindsl.*"), DeprecationLevel.WARNING)
inline fun CommandTree.multiLiteralArgument(nodeName: String, literals: List<String>, block: Argument<*>.() -> Unit = {}): CommandTree= then(MultiLiteralArgument(nodeName, literals).apply(block))

inline fun CommandTree.multiLiteralArgument(nodeName: String, vararg literals: String, block: Argument<*>.() -> Unit = {}): CommandTree= then(MultiLiteralArgument(nodeName, *literals).apply(block))

// Function arguments
inline fun CommandTree.functionArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): CommandTree = then(FunctionArgument(nodeName).apply(block))

// ArgumentTree start
inline fun Argument<*>.argument(base: Argument<*>, block: Argument<*>.() -> Unit = {}): Argument<*> = then(base.apply(block))

inline fun Argument<*>.nestedArguments(vararg arguments: Argument<*>, block: Argument<*>.() -> Unit = {}): Argument<*> = thenNested(*arguments.also { it.last().apply(block) })
inline fun Argument<*>.nested(block: Argument<*>.() -> Unit): Argument<*> {
	val arguments = mutableListOf<AbstractArgumentTree<*, Argument<*>?, CommandSender?>?>()
	object : LiteralArgument(WILL_NOT_REGISTER) {
		override fun then(tree: AbstractArgumentTree<*, Argument<*>?, CommandSender?>?): Argument<String?>? {
			arguments.add(tree)
			return this
		}
	}.block()
	return thenNested(arguments)
}

// Integer arguments
inline fun Argument<*>.integerArgument(nodeName: String, min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE, block: Argument<*>.() -> Unit = {}): Argument<*> = then(IntegerArgument(nodeName, min, max).apply(block))
inline fun Argument<*>.integerRangeArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(IntegerRangeArgument(nodeName).apply(block))

// Float arguments
inline fun Argument<*>.floatArgument(nodeName: String, min: Float = -Float.MAX_VALUE, max: Float = Float.MAX_VALUE, block: Argument<*>.() -> Unit = {}): Argument<*> = then(FloatArgument(nodeName, min, max).apply(block))
inline fun Argument<*>.floatRangeArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(FloatRangeArgument(nodeName).apply(block))

// Double arguments
inline fun Argument<*>.doubleArgument(nodeName: String, min: Double = -Double.MAX_VALUE, max: Double = Double.MAX_VALUE, block: Argument<*>.() -> Unit = {}): Argument<*> = then(DoubleArgument(nodeName, min, max).apply(block))

// Long arguments
inline fun Argument<*>.longArgument(nodeName: String, min: Long = Long.MIN_VALUE, max: Long = Long.MAX_VALUE, block: Argument<*>.() -> Unit = {}): Argument<*> = then(LongArgument(nodeName, min, max).apply(block))

// Boolean argument
inline fun Argument<*>.booleanArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(BooleanArgument(nodeName).apply(block))

// String arguments
inline fun Argument<*>.stringArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(StringArgument(nodeName).apply(block))
inline fun Argument<*>.textArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(TextArgument(nodeName).apply(block))
inline fun Argument<*>.greedyStringArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(GreedyStringArgument(nodeName).apply(block))

// Positional arguments
inline fun Argument<*>.locationArgument(nodeName: String, locationType: LocationType = LocationType.PRECISE_POSITION, centerPosition: Boolean = true, block: Argument<*>.() -> Unit = {}): Argument<*> = then(LocationArgument(nodeName, locationType, centerPosition).apply(block))
inline fun Argument<*>.location2DArgument(nodeName: String, locationType: LocationType = LocationType.PRECISE_POSITION, centerPosition: Boolean = true, block: Argument<*>.() -> Unit = {}): Argument<*> = then(Location2DArgument(nodeName, locationType, centerPosition).apply(block))
inline fun Argument<*>.rotationArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(RotationArgument(nodeName).apply(block))
inline fun Argument<*>.axisArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(AxisArgument(nodeName).apply(block))

// Chat arguments
inline fun Argument<*>.chatColorArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(ChatColorArgument(nodeName).apply(block))
inline fun Argument<*>.chatComponentArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(ChatComponentArgument(nodeName).apply(block))
inline fun Argument<*>.chatArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(ChatArgument(nodeName).apply(block))
inline fun Argument<*>.adventureChatColorArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(AdventureChatColorArgument(nodeName).apply(block))
inline fun Argument<*>.adventureChatComponentArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(AdventureChatComponentArgument(nodeName).apply(block))
inline fun Argument<*>.adventureChatArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(AdventureChatArgument(nodeName).apply(block))

// Entity & Player arguments
inline fun Argument<*>.entitySelectorArgumentOneEntity(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(EntitySelectorArgument.OneEntity(nodeName).apply(block))
inline fun Argument<*>.entitySelectorArgumentManyEntities(nodeName: String, allowEmpty: Boolean = true, block: Argument<*>.() -> Unit = {}): Argument<*> = then(EntitySelectorArgument.ManyEntities(nodeName, allowEmpty).apply(block))
inline fun Argument<*>.entitySelectorArgumentOnePlayer(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(EntitySelectorArgument.OnePlayer(nodeName).apply(block))
inline fun Argument<*>.entitySelectorArgumentManyPlayers(nodeName: String, allowEmpty: Boolean = true, block: Argument<*>.() -> Unit = {}): Argument<*> = then(EntitySelectorArgument.ManyPlayers(nodeName, allowEmpty).apply(block))
inline fun Argument<*>.playerArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(PlayerArgument(nodeName).apply(block))
inline fun Argument<*>.offlinePlayerArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(OfflinePlayerArgument(nodeName).apply(block))
inline fun Argument<*>.asyncOfflinePlayerArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(AsyncOfflinePlayerArgument(nodeName).apply(block))
inline fun Argument<*>.entityTypeArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(EntityTypeArgument(nodeName).apply(block))

// Scoreboard arguments
inline fun Argument<*>.scoreHolderArgumentSingle(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(ScoreHolderArgument.Single(nodeName).apply(block))
inline fun Argument<*>.scoreHolderArgumentMultiple(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(ScoreHolderArgument.Multiple(nodeName).apply(block))
inline fun Argument<*>.scoreboardSlotArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(ScoreboardSlotArgument(nodeName).apply(block))
inline fun Argument<*>.objectiveArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(ObjectiveArgument(nodeName).apply(block))
inline fun Argument<*>.objectiveCriteriaArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(ObjectiveCriteriaArgument(nodeName).apply(block))
inline fun Argument<*>.teamArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(TeamArgument(nodeName).apply(block))

// Miscellaneous arguments
inline fun Argument<*>.angleArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(AngleArgument(nodeName).apply(block))
inline fun Argument<*>.advancementArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(AdvancementArgument(nodeName).apply(block))

inline fun Argument<*>.biomeArgument(nodeName: String, useNamespacedKey: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> =
	if (useNamespacedKey) then(BiomeArgument.NamespacedKey(nodeName).apply(block)) else then(BiomeArgument(nodeName).apply(block))

inline fun Argument<*>.blockStateArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(BlockStateArgument(nodeName).apply(block))
inline fun Argument<*>.commandArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(CommandArgument(nodeName).apply(block))
inline fun Argument<*>.enchantmentArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(EnchantmentArgument(nodeName).apply(block))

inline fun Argument<*>.itemStackArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(ItemStackArgument(nodeName).apply(block))
inline fun Argument<*>.lootTableArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(LootTableArgument(nodeName).apply(block))
inline fun Argument<*>.mathOperationArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(MathOperationArgument(nodeName).apply(block))
inline fun Argument<*>.namespacedKeyArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(NamespacedKeyArgument(nodeName).apply(block))
inline fun Argument<*>.particleArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(ParticleArgument(nodeName).apply(block))
inline fun Argument<*>.potionEffectArgument(nodeName: String, useNamespacedKey: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> =
	if (useNamespacedKey) then(PotionEffectArgument.NamespacedKey(nodeName).apply(block)) else then(PotionEffectArgument(nodeName).apply(block))
inline fun Argument<*>.recipeArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(RecipeArgument(nodeName).apply(block))

inline fun Argument<*>.soundArgument(nodeName: String, useNamespacedKey: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> =
	if (useNamespacedKey) then(SoundArgument.NamespacedKey(nodeName).apply(block)) else then(SoundArgument(nodeName).apply(block))

inline fun Argument<*>.timeArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(TimeArgument(nodeName).apply(block))
inline fun Argument<*>.uuidArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(UUIDArgument(nodeName).apply(block))
inline fun Argument<*>.worldArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(WorldArgument(nodeName).apply(block))

// Predicate arguments
inline fun Argument<*>.blockPredicateArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(BlockPredicateArgument(nodeName).apply(block))
inline fun Argument<*>.itemStackPredicateArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(ItemStackPredicateArgument(nodeName).apply(block))

// NBT arguments
inline fun <NBTContainer> Argument<*>.nbtCompoundArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(NBTCompoundArgument<NBTContainer>(nodeName).apply(block))

// Literal arguments
inline fun Argument<*>.literalArgument(literal: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(LiteralArgument.of(literal, literal).apply(block))
inline fun Argument<*>.literalArgument(nodeName: String, literal: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(LiteralArgument.of(nodeName, literal).apply(block))

@Deprecated("This method has been deprecated since version 9.1.0", ReplaceWith("multiLiteralArgument(nodeName, literals)", "dev.jorel.commandapi.kotlindsl.*"), DeprecationLevel.WARNING)
inline fun Argument<*>.multiLiteralArgument(nodeName: String, literals: List<String>, block: Argument<*>.() -> Unit = {}): Argument<*> = then(MultiLiteralArgument(nodeName, literals).apply(block))

inline fun Argument<*>.multiLiteralArgument(nodeName: String, vararg literals: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(MultiLiteralArgument(nodeName, *literals).apply(block))

// Function arguments
inline fun Argument<*>.functionArgument(nodeName: String, block: Argument<*>.() -> Unit = {}): Argument<*> = then(FunctionArgument(nodeName).apply(block))

@Deprecated("This method has been deprecated since version 9.1.0 as it is not needed anymore. See the documentation for more information", ReplaceWith(""), DeprecationLevel.WARNING)
inline fun CommandTree.requirement(base: Argument<*>, predicate: Predicate<CommandSender>, block: Argument<*>.() -> Unit = {}): CommandTree = then(base.withRequirement(predicate).apply(block))
@Deprecated("This method has been deprecated since version 9.1.0 as it is not needed anymore. See the documentation for more information", ReplaceWith(""), DeprecationLevel.WARNING)
inline fun Argument<*>.requirement(base: Argument<*>, predicate: Predicate<CommandSender>, block: Argument<*>.() -> Unit = {}): Argument<*> = then(base.withRequirement(predicate).apply(block))
