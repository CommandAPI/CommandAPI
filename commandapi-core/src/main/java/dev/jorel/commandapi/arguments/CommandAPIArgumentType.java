/*******************************************************************************
 * Copyright 2018, 2021 Jorel Ali (Skepter) - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package dev.jorel.commandapi.arguments;

import java.util.Arrays;

/**
 * An enum that represents the different arguments declared in the Command API
 */
public enum CommandAPIArgumentType {

	/**
	 * The AdvancementArgument
	 */
	ADVANCEMENT("api:advancement"),

	/**
	 * The ChatArgument (with Paper Adventure backend)
	 */
	ADVENTURE_CHAT,

	/**
	 * The ChatColorArgument (with Paper Adventure backend)
	 */
	ADVENTURE_CHATCOLOR,

	/**
	 * The ChatComponentArgument (with Paper Adventure backend)
	 */
	ADVENTURE_CHAT_COMPONENT,

	/**
	 * The AngleArgument
	 */
	ANGLE("minecraft:angle"),

	/**
	 * The AsyncOfflinePlayerArgument
	 */
	ASYNC_OFFLINE_PLAYER("api:async_offline_player"),


	/**
	 * The AxisArgument
	 */
	AXIS("minecraft:swizzle"),

	/**
	 * The BiomeArgument
	 */
	BIOME("api:biome"),

	/**
	 * The BlockPredicateArgument
	 */
	BLOCK_PREDICATE("minecraft:block_predicate"),

	/**
	 * The BlockStateArgument
	 */
	BLOCKSTATE("minecraft:block_state"),

	/**
	 * The ChatArgument (with BaseComponent backend)
	 */
	CHAT("minecraft:message"),

	/**
	 * The ChatComponentArgument (with BaseComponent backend)
	 */
	CHAT_COMPONENT("minecraft:component"),

	/**
	 * The ChatColorArgument
	 */
	CHATCOLOR("minecraft:color"),

	/**
	 * The CommandArgument
	 */
	COMMAND("api:command"),

	/**
	 * The CustomArgument
	 */
	CUSTOM,
	
	/**
	 * The DimensionArgument
	 */
	DIMENSION("minecraft:dimension"),

	/**
	 * The DynamicMultiLiteralArgument
	 */
	DYNAMIC_MULTI_LITERAL,

	/**
	 * The EnchantmentArgument
	 * 
	 */
	ENCHANTMENT("minecraft:item_enchantment"),

	/**
	 * The EntitySelectorArgument
	 */
	ENTITY_SELECTOR("minecraft:entity"),

	/**
	 * The EntityTypeArgument
	 */
	ENTITY_TYPE("minecraft:entity_summon"),

	/**
	 * The EnvironmentArgument
	 */
	ENVIRONMENT("api:environment"),

	/**
	 * The FlagsArgument
	 */
	FLAGS_ARGUMENT,

	/**
	 * The FloatRangeArgument
	 */
	FLOAT_RANGE("minecraft:float_range"),

	/**
	 * The FunctionArgument
	 */
	FUNCTION("minecraft:function"),

	/**
	 * The IntegerRangeArgument
	 */
	INT_RANGE("minecraft:int_range"),

	/**
	 * The ItemStackArgument
	 */
	ITEMSTACK("minecraft:item_stack"),

	/**
	 * The ItemStackPredicateArgument
	 */
	ITEMSTACK_PREDICATE("minecraft:item_predicate"),

	/**
	 * The ListArgument
	 */
	LIST("api:list"),

	/**
	 * The ListTextArgument
	 */
	LIST_TEXT("api:list_text"),

	/**
	 * The LiteralArgument
	 */
	LITERAL,
	
	/**
	 * The LocationArgument
	 */
	LOCATION(new String[] { "minecraft:vec3", "minecraft:block_pos" }),

	/**
	 * The Location2dArgument
	 */
	LOCATION_2D(new String[] { "minecraft:vec2", "minecraft:column_pos" }),

	/**
	 * The LootTableArgument
	 */
	LOOT_TABLE("api:loot_table"),

	/**
	 * The MapArgument
	 */
	MAP("api:map"),

	/**
	 * The MathOperationArgument
	 */
	MATH_OPERATION("minecraft:operation"),

	/**
	 * The MultiLiteralArgument
	 */
	MULTI_LITERAL,

	/**
	 * The NamespacedKeyArgument
	 */
	NAMESPACED_KEY("minecraft:resource_location"),

	/**
	 * The NBTCompoundArgument
	 */
	NBT_COMPOUND("minecraft:nbt_compound_tag"),

	/**
	 * The ObjectiveArgument
	 */
	OBJECTIVE("minecraft:objective"),

	/**
	 * The ObjectiveCriteriaArgument
	 */
	OBJECTIVE_CRITERIA("minecraft:objective_criteria"),

	/**
	 * The OfflinePlayerArgument
	 */
	OFFLINE_PLAYER("api:offline_player"),

	/**
	 * The ParticleArgument
	 */
	PARTICLE("minecraft:particle"),

	/**
	 * The PlayerArgument
	 */
	PLAYER("minecraft:game_profile"),

	/**
	 * The PotionEffectArgument
	 */
	POTION_EFFECT("minecraft:mob_effect"),

	/**
	 * Primitive argument BooleanArgument
	 */
	PRIMITIVE_BOOLEAN("brigadier:bool"),

	/**
	 * Primitive argument DoubleArgument
	 */
	PRIMITIVE_DOUBLE("brigadier:double"),

	/**
	 * Primitive argument F;patArgument
	 */
	PRIMITIVE_FLOAT("brigadier:float"),

	/**
	 * Primitive argument GreedyStringArgument
	 */
	PRIMITIVE_GREEDY_STRING("api:greedy_string"),

	/**
	 * Primitive argument IntegerArgument
	 */
	PRIMITIVE_INTEGER("brigadier:integer"),

	/**
	 * Primitive argument LongArgument
	 */
	PRIMITIVE_LONG("brigadier:long"),

	/**
	 * Primitive argument StringArgument
	 */
	PRIMITIVE_STRING("brigadier:string"),

	/**
	 * Primitive argument TextArgument
	 */
	PRIMITIVE_TEXT("api:text"),

	/**
	 * The RecipeArgument
	 */
	RECIPE("api:recipe"),

	/**
	 * The RotationArgument
	 */
	ROTATION("minecraft:rotation"),

	/**
	 * The ScoreHolderArgument
	 */
	SCORE_HOLDER("minecraft:score_holder"),

	/**
	 * The ScoreboardSlotArgument
	 */
	SCOREBOARD_SLOT("minecraft:scoreboard_slot"),

	/**
	 * The SoundArgument
	 */
	SOUND("api:sound"),

	/**
	 * The TeamArgument
	 */
	TEAM("minecraft:team"),

	/**
	 * The TimeArgument
	 */
	TIME("minecraft:time"),

	/**
	 * The UUIDArgument
	 */
	UUID("minecraft:uuid");

	/**
	 * Converts an internal name (e.g. minecraft:time) to a CommandAPIArgumentType
	 * 
	 * @param internal the internal name for this argument
	 * @return the corresponding CommandAPIArgumentType for the given input
	 */
	public static CommandAPIArgumentType fromInternal(String internal) {
		for (CommandAPIArgumentType type : CommandAPIArgumentType.values()) {
			if (Arrays.asList(type.internals).contains(internal)) {
				return type;
			}
		}
		return null;
	}

	private String[] internals;

	CommandAPIArgumentType() {
		internals = new String[0];
	}

	CommandAPIArgumentType(String internal) {
		this.internals = new String[] { internal };
	}

	CommandAPIArgumentType(String[] internals) {
		this.internals = internals;
	}

}
