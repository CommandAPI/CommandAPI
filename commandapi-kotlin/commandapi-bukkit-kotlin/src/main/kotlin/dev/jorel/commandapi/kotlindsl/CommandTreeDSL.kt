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

inline fun CommandTree.optionalArgument(base: Argument<*>, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(base.setOptional(true).setOptional(optional).apply(block))

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
inline fun CommandTree.integerArgument(nodeName: String, min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(IntegerArgument(nodeName, min, max).setOptional(optional).apply(block))
inline fun CommandTree.integerRangeArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(IntegerRangeArgument(nodeName).setOptional(optional).apply(block))

// Float arguments
inline fun CommandTree.floatArgument(nodeName: String, min: Float = -Float.MAX_VALUE, max: Float = Float.MAX_VALUE, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(FloatArgument(nodeName, min, max).setOptional(optional).apply(block))
inline fun CommandTree.floatRangeArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(FloatRangeArgument(nodeName).setOptional(optional).apply(block))

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

// Positional arguments
inline fun CommandTree.locationArgument(nodeName: String, locationType: LocationType = LocationType.PRECISE_POSITION, centerPosition: Boolean = true, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(LocationArgument(nodeName, locationType, centerPosition).setOptional(optional).apply(block))
inline fun CommandTree.location2DArgument(nodeName: String, locationType: LocationType = LocationType.PRECISE_POSITION, centerPosition: Boolean = true, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(Location2DArgument(nodeName, locationType, centerPosition).setOptional(optional).apply(block))
inline fun CommandTree.rotationArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(RotationArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandTree.axisArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(AxisArgument(nodeName).setOptional(optional).apply(block))

// Chat arguments
inline fun CommandTree.chatColorArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(ChatColorArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandTree.chatComponentArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(ChatComponentArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandTree.chatArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(ChatArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandTree.adventureChatColorArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(AdventureChatColorArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandTree.adventureChatComponentArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(AdventureChatComponentArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandTree.adventureChatArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(AdventureChatArgument(nodeName).setOptional(optional).apply(block))

// Entity & Player arguments
inline fun CommandTree.entitySelectorArgumentOneEntity(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(EntitySelectorArgument.OneEntity(nodeName).setOptional(optional).apply(block))
inline fun CommandTree.entitySelectorArgumentManyEntities(nodeName: String, allowEmpty: Boolean = true, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(EntitySelectorArgument.ManyEntities(nodeName, allowEmpty).setOptional(optional).apply(block))
inline fun CommandTree.entitySelectorArgumentOnePlayer(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(EntitySelectorArgument.OnePlayer(nodeName).setOptional(optional).apply(block))
inline fun CommandTree.entitySelectorArgumentManyPlayers(nodeName: String, allowEmpty: Boolean = true, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(EntitySelectorArgument.ManyPlayers(nodeName, allowEmpty).setOptional(optional).apply(block))
inline fun CommandTree.playerArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(PlayerArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandTree.offlinePlayerArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(OfflinePlayerArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandTree.asyncOfflinePlayerArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(AsyncOfflinePlayerArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandTree.entityTypeArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(EntityTypeArgument(nodeName).setOptional(optional).apply(block))

// Scoreboard arguments
inline fun CommandTree.scoreHolderArgumentSingle(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(ScoreHolderArgument.Single(nodeName).setOptional(optional).apply(block))
inline fun CommandTree.scoreHolderArgumentMultiple(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(ScoreHolderArgument.Multiple(nodeName).setOptional(optional).apply(block))
inline fun CommandTree.scoreboardSlotArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(ScoreboardSlotArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandTree.objectiveArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(ObjectiveArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandTree.objectiveCriteriaArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(ObjectiveCriteriaArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandTree.teamArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(TeamArgument(nodeName).setOptional(optional).apply(block))

// Miscellaneous arguments
inline fun CommandTree.angleArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(AngleArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandTree.advancementArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(AdvancementArgument(nodeName).setOptional(optional).apply(block))

inline fun CommandTree.biomeArgument(nodeName: String, useNamespacedKey: Boolean = false, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree =
	if (useNamespacedKey) then(BiomeArgument.NamespacedKey(nodeName).setOptional(optional).apply(block)) else then(BiomeArgument(nodeName).setOptional(optional).apply(block))

inline fun CommandTree.blockStateArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(BlockStateArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandTree.commandArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(CommandArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandTree.enchantmentArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(EnchantmentArgument(nodeName).setOptional(optional).apply(block))

inline fun CommandTree.itemStackArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(ItemStackArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandTree.lootTableArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(LootTableArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandTree.mathOperationArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(MathOperationArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandTree.namespacedKeyArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(NamespacedKeyArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandTree.particleArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(ParticleArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandTree.potionEffectArgument(nodeName: String, useNamespacedKey: Boolean = false, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree =
	if (useNamespacedKey) then(PotionEffectArgument.NamespacedKey(nodeName).setOptional(optional).apply(block)) else then(PotionEffectArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandTree.recipeArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(RecipeArgument(nodeName).setOptional(optional).apply(block))

inline fun CommandTree.soundArgument(nodeName: String, useNamespacedKey: Boolean = false, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree =
	if (useNamespacedKey) then(SoundArgument.NamespacedKey(nodeName).setOptional(optional).apply(block)) else then(SoundArgument(nodeName).setOptional(optional).apply(block))

inline fun CommandTree.timeArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(TimeArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandTree.uuidArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(UUIDArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandTree.worldArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(WorldArgument(nodeName).setOptional(optional).apply(block))

// Predicate arguments
inline fun CommandTree.blockPredicateArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(BlockPredicateArgument(nodeName).setOptional(optional).apply(block))
inline fun CommandTree.itemStackPredicateArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(ItemStackPredicateArgument(nodeName).setOptional(optional).apply(block))

// NBT arguments
inline fun <NBTContainer> CommandTree.nbtCompoundArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(NBTCompoundArgument<NBTContainer>(nodeName).setOptional(optional).apply(block))

// Literal arguments
inline fun CommandTree.literalArgument(literal: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree= then(LiteralArgument.of(literal, literal).setOptional(optional).apply(block))
inline fun CommandTree.literalArgument(nodeName: String, literal: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree= then(LiteralArgument.of(nodeName, literal).setOptional(optional).apply(block))

@Deprecated("This version has been deprecated since version 9.0.2", ReplaceWith("multiLiteralArgument(nodeName, listOf(literals))", "dev.jorel.commandapi.kotlindsl.*"), DeprecationLevel.WARNING)
inline fun CommandTree.multiLiteralArgument(vararg literals: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree= then(MultiLiteralArgument(literals).setOptional(optional).apply(block))
@Deprecated("This method has been deprecated since version 9.1.0", ReplaceWith("multiLiteralArgument(nodeName, literals)", "dev.jorel.commandapi.kotlindsl.*"), DeprecationLevel.WARNING)
inline fun CommandTree.multiLiteralArgument(nodeName: String, literals: List<String>, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree= then(MultiLiteralArgument(nodeName, literals).setOptional(optional).apply(block))

inline fun CommandTree.multiLiteralArgument(nodeName: String, vararg literals: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree= then(MultiLiteralArgument(nodeName, *literals).setOptional(optional).apply(block))

// Function arguments
inline fun CommandTree.functionArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): CommandTree = then(FunctionArgument(nodeName).setOptional(optional).apply(block))

// ArgumentTree start
inline fun Argument<*>.argument(base: Argument<*>, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(base.setOptional(optional).apply(block))

inline fun Argument<*>.optionalArgument(base: Argument<*>, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(base.setOptional(true).setOptional(optional).apply(block))

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
inline fun Argument<*>.integerArgument(nodeName: String, min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(IntegerArgument(nodeName, min, max).setOptional(optional).apply(block))
inline fun Argument<*>.integerRangeArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(IntegerRangeArgument(nodeName).setOptional(optional).apply(block))

// Float arguments
inline fun Argument<*>.floatArgument(nodeName: String, min: Float = -Float.MAX_VALUE, max: Float = Float.MAX_VALUE, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(FloatArgument(nodeName, min, max).setOptional(optional).apply(block))
inline fun Argument<*>.floatRangeArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(FloatRangeArgument(nodeName).setOptional(optional).apply(block))

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

// Positional arguments
inline fun Argument<*>.locationArgument(nodeName: String, locationType: LocationType = LocationType.PRECISE_POSITION, centerPosition: Boolean = true, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(LocationArgument(nodeName, locationType, centerPosition).setOptional(optional).apply(block))
inline fun Argument<*>.location2DArgument(nodeName: String, locationType: LocationType = LocationType.PRECISE_POSITION, centerPosition: Boolean = true, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(Location2DArgument(nodeName, locationType, centerPosition).setOptional(optional).apply(block))
inline fun Argument<*>.rotationArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(RotationArgument(nodeName).setOptional(optional).apply(block))
inline fun Argument<*>.axisArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(AxisArgument(nodeName).setOptional(optional).apply(block))

// Chat arguments
inline fun Argument<*>.chatColorArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(ChatColorArgument(nodeName).setOptional(optional).apply(block))
inline fun Argument<*>.chatComponentArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(ChatComponentArgument(nodeName).setOptional(optional).apply(block))
inline fun Argument<*>.chatArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(ChatArgument(nodeName).setOptional(optional).apply(block))
inline fun Argument<*>.adventureChatColorArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(AdventureChatColorArgument(nodeName).setOptional(optional).apply(block))
inline fun Argument<*>.adventureChatComponentArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(AdventureChatComponentArgument(nodeName).setOptional(optional).apply(block))
inline fun Argument<*>.adventureChatArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(AdventureChatArgument(nodeName).setOptional(optional).apply(block))

// Entity & Player arguments
inline fun Argument<*>.entitySelectorArgumentOneEntity(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(EntitySelectorArgument.OneEntity(nodeName).setOptional(optional).apply(block))
inline fun Argument<*>.entitySelectorArgumentManyEntities(nodeName: String, allowEmpty: Boolean = true, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(EntitySelectorArgument.ManyEntities(nodeName, allowEmpty).setOptional(optional).apply(block))
inline fun Argument<*>.entitySelectorArgumentOnePlayer(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(EntitySelectorArgument.OnePlayer(nodeName).setOptional(optional).apply(block))
inline fun Argument<*>.entitySelectorArgumentManyPlayers(nodeName: String, allowEmpty: Boolean = true, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(EntitySelectorArgument.ManyPlayers(nodeName, allowEmpty).setOptional(optional).apply(block))
inline fun Argument<*>.playerArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(PlayerArgument(nodeName).setOptional(optional).apply(block))
inline fun Argument<*>.offlinePlayerArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(OfflinePlayerArgument(nodeName).setOptional(optional).apply(block))
inline fun Argument<*>.asyncOfflinePlayerArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(AsyncOfflinePlayerArgument(nodeName).setOptional(optional).apply(block))
inline fun Argument<*>.entityTypeArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(EntityTypeArgument(nodeName).setOptional(optional).apply(block))

// Scoreboard arguments
inline fun Argument<*>.scoreHolderArgumentSingle(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(ScoreHolderArgument.Single(nodeName).setOptional(optional).apply(block))
inline fun Argument<*>.scoreHolderArgumentMultiple(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(ScoreHolderArgument.Multiple(nodeName).setOptional(optional).apply(block))
inline fun Argument<*>.scoreboardSlotArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(ScoreboardSlotArgument(nodeName).setOptional(optional).apply(block))
inline fun Argument<*>.objectiveArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(ObjectiveArgument(nodeName).setOptional(optional).apply(block))
inline fun Argument<*>.objectiveCriteriaArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(ObjectiveCriteriaArgument(nodeName).setOptional(optional).apply(block))
inline fun Argument<*>.teamArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(TeamArgument(nodeName).setOptional(optional).apply(block))

// Miscellaneous arguments
inline fun Argument<*>.angleArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(AngleArgument(nodeName).setOptional(optional).apply(block))
inline fun Argument<*>.advancementArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(AdvancementArgument(nodeName).setOptional(optional).apply(block))

inline fun Argument<*>.biomeArgument(nodeName: String, useNamespacedKey: Boolean = false, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> =
	if (useNamespacedKey) then(BiomeArgument.NamespacedKey(nodeName).setOptional(optional).apply(block)) else then(BiomeArgument(nodeName).setOptional(optional).apply(block))

inline fun Argument<*>.blockStateArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(BlockStateArgument(nodeName).setOptional(optional).apply(block))
inline fun Argument<*>.commandArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(CommandArgument(nodeName).setOptional(optional).apply(block))
inline fun Argument<*>.enchantmentArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(EnchantmentArgument(nodeName).setOptional(optional).apply(block))

inline fun Argument<*>.itemStackArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(ItemStackArgument(nodeName).setOptional(optional).apply(block))
inline fun Argument<*>.lootTableArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(LootTableArgument(nodeName).setOptional(optional).apply(block))
inline fun Argument<*>.mathOperationArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(MathOperationArgument(nodeName).setOptional(optional).apply(block))
inline fun Argument<*>.namespacedKeyArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(NamespacedKeyArgument(nodeName).setOptional(optional).apply(block))
inline fun Argument<*>.particleArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(ParticleArgument(nodeName).setOptional(optional).apply(block))
inline fun Argument<*>.potionEffectArgument(nodeName: String, useNamespacedKey: Boolean = false, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> =
	if (useNamespacedKey) then(PotionEffectArgument.NamespacedKey(nodeName).setOptional(optional).apply(block)) else then(PotionEffectArgument(nodeName).setOptional(optional).apply(block))
inline fun Argument<*>.recipeArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(RecipeArgument(nodeName).setOptional(optional).apply(block))

inline fun Argument<*>.soundArgument(nodeName: String, useNamespacedKey: Boolean = false, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> =
	if (useNamespacedKey) then(SoundArgument.NamespacedKey(nodeName).setOptional(optional).apply(block)) else then(SoundArgument(nodeName).setOptional(optional).apply(block))

inline fun Argument<*>.timeArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(TimeArgument(nodeName).setOptional(optional).apply(block))
inline fun Argument<*>.uuidArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(UUIDArgument(nodeName).setOptional(optional).apply(block))
inline fun Argument<*>.worldArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(WorldArgument(nodeName).setOptional(optional).apply(block))

// Predicate arguments
inline fun Argument<*>.blockPredicateArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(BlockPredicateArgument(nodeName).setOptional(optional).apply(block))
inline fun Argument<*>.itemStackPredicateArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(ItemStackPredicateArgument(nodeName).setOptional(optional).apply(block))

// NBT arguments
inline fun <NBTContainer> Argument<*>.nbtCompoundArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(NBTCompoundArgument<NBTContainer>(nodeName).setOptional(optional).apply(block))

// Literal arguments
inline fun Argument<*>.literalArgument(literal: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(LiteralArgument.of(literal, literal).setOptional(optional).apply(block))
inline fun Argument<*>.literalArgument(nodeName: String, literal: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(LiteralArgument.of(nodeName, literal).setOptional(optional).apply(block))

@Deprecated("This version has been deprecated since version 9.0.2", ReplaceWith("multiLiteralArgument(nodeName, listOf(literals))", "dev.jorel.commandapi.kotlindsl.*"), DeprecationLevel.WARNING)
inline fun Argument<*>.multiLiteralArgument(vararg literals: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(MultiLiteralArgument(literals).setOptional(optional).apply(block))
@Deprecated("This method has been deprecated since version 9.1.0", ReplaceWith("multiLiteralArgument(nodeName, literals)", "dev.jorel.commandapi.kotlindsl.*"), DeprecationLevel.WARNING)
inline fun Argument<*>.multiLiteralArgument(nodeName: String, literals: List<String>, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(MultiLiteralArgument(nodeName, literals).setOptional(optional).apply(block))

inline fun Argument<*>.multiLiteralArgument(nodeName: String, vararg literals: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(MultiLiteralArgument(nodeName, *literals).setOptional(optional).apply(block))

// Function arguments
inline fun Argument<*>.functionArgument(nodeName: String, optional: Boolean = false, block: Argument<*>.() -> Unit = {}): Argument<*> = then(FunctionArgument(nodeName).setOptional(optional).apply(block))

@Deprecated("This method has been deprecated since version 9.1.0 as it is not needed anymore. See the documentation for more information", ReplaceWith(""), DeprecationLevel.WARNING)
inline fun CommandTree.requirement(base: Argument<*>, predicate: Predicate<CommandSender>, block: Argument<*>.() -> Unit = {}): CommandTree = then(base.withRequirement(predicate).apply(block))
@Deprecated("This method has been deprecated since version 9.1.0 as it is not needed anymore. See the documentation for more information", ReplaceWith(""), DeprecationLevel.WARNING)
inline fun Argument<*>.requirement(base: Argument<*>, predicate: Predicate<CommandSender>, block: Argument<*>.() -> Unit = {}): Argument<*> = then(base.withRequirement(predicate).apply(block))
