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
package dev.jorel.commandapi.nms;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

import dev.jorel.commandapi.CommandRegistrationStrategy;
import org.bukkit.Axis;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.help.HelpTopic;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.loot.LootTable;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import dev.jorel.commandapi.arguments.ArgumentSubType;
import dev.jorel.commandapi.arguments.SuggestionProviders;
import dev.jorel.commandapi.wrappers.FloatRange;
import dev.jorel.commandapi.wrappers.FunctionWrapper;
import dev.jorel.commandapi.wrappers.IntegerRange;
import dev.jorel.commandapi.wrappers.Location2D;
import dev.jorel.commandapi.wrappers.MathOperation;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import dev.jorel.commandapi.wrappers.ParticleData;
import dev.jorel.commandapi.wrappers.Rotation;
import dev.jorel.commandapi.wrappers.ScoreboardSlot;
import dev.jorel.commandapi.wrappers.SimpleFunctionWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.chat.BaseComponent;

public interface NMS<CommandListenerWrapper> {

	/**
	 * @return Advancement argument (api:advancement)
	 */
	ArgumentType<?> _ArgumentAdvancement();

	/**
	 * @return Angle argument (minecraft:angle)
	 */
	ArgumentType<?> _ArgumentAngle();

	/**
	 * @return Axis argument (minecraft:swizzle)
	 */
	ArgumentType<?> _ArgumentAxis();

	/**
	 * @return Block predicate argument (minecraft:block_predicate)
	 */
	ArgumentType<?> _ArgumentBlockPredicate();

	/**
	 * @return Block state argument (minecraft:block_state)
	 */
	ArgumentType<?> _ArgumentBlockState();

	/**
	 * @return Chat argument (minecraft:message)
	 */
	ArgumentType<?> _ArgumentChat();

	/**
	 * @return Chat component argument (minecraft:component)
	 */
	ArgumentType<?> _ArgumentChatComponent();

	/**
	 * @return Chat color argument (minecraft:color)
	 */
	ArgumentType<?> _ArgumentChatFormat();

	/**
	 * @return Dimension argument (minecraft:dimension)
	 */
	ArgumentType<?> _ArgumentDimension();

	/**
	 * @return Enchantment argument (minecraft:item_enchantment)
	 */
	ArgumentType<?> _ArgumentEnchantment();

	/**
	 * @param subType one of {@link ArgumentSubType#ENTITYSELECTOR_MANY_ENTITIES},
	 *                {@link ArgumentSubType#ENTITYSELECTOR_MANY_PLAYERS},
	 *                {@link ArgumentSubType#ENTITYSELECTOR_ONE_ENTITY} or
	 *                {@link ArgumentSubType#ENTITYSELECTOR_ONE_PLAYER}
	 * @return Entity selector argument (minecraft:entity)
	 */
	ArgumentType<?> _ArgumentEntity(ArgumentSubType subType);

	/**
	 * @return Entity type argument (minecraft:entity_summon)
	 */
	ArgumentType<?> _ArgumentEntitySummon();

	/**
	 * @return Float range argument (minecraft:float_range)
	 */
	ArgumentType<?> _ArgumentFloatRange();

	/**
	 * @return Int range argument (minecraft:int_range)
	 */
	ArgumentType<?> _ArgumentIntRange();

	/**
	 * @return Item predicate argument (minecraft:item_predicate)
	 */
	ArgumentType<?> _ArgumentItemPredicate();

	/**
	 * @return Itemstack argument (minecraft:item_stack)
	 */
	ArgumentType<?> _ArgumentItemStack();

	/**
	 * @return Math operation argument (minecraft:operation)
	 */
	ArgumentType<?> _ArgumentMathOperation();

	/**
	 * @return Minecraft key argument (minecraft:resource_location)
	 */
	ArgumentType<?> _ArgumentMinecraftKeyRegistered();

	/**
	 * @return Potion effect argument (minecraft:mob_effect)
	 */
	ArgumentType<?> _ArgumentMobEffect();

	/**
	 * @return NBT compound tag argument (minecraft:nbt_compound_tag)
	 */
	ArgumentType<?> _ArgumentNBTCompound();

	/**
	 * @return Particle argument (minecraft:particle)
	 */
	ArgumentType<?> _ArgumentParticle();

	/**
	 * @return Position argument (minecraft:block_pos)
	 */
	ArgumentType<?> _ArgumentPosition();

	/**
	 * @return 2D position (column) argument (minecraft:column_pos)
	 */
	ArgumentType<?> _ArgumentPosition2D();

	/**
	 * @return Player argument (minecraft:game_profile)
	 */
	ArgumentType<?> _ArgumentProfile();

	/**
	 * @return Recipe argument (api:recipe)
	 */
	ArgumentType<?> _ArgumentRecipe();

	/**
	 * @return Rotation argument (minecraft:rotation)
	 */
	ArgumentType<?> _ArgumentRotation();

	/**
	 * @return Scoreboard objective criteria argument (minecraft:objective_criteria)
	 */
	ArgumentType<?> _ArgumentScoreboardCriteria();

	/**
	 * @return Scoreboard objective argument (minecraft:objective)
	 */
	ArgumentType<?> _ArgumentScoreboardObjective();

	/**
	 * @return Scoreboard slot argument (minecraft:scoreboard_slot)
	 */
	ArgumentType<?> _ArgumentScoreboardSlot();

	/**
	 * @return Scoreboard team argument (minecraft:team)
	 */
	ArgumentType<?> _ArgumentScoreboardTeam();

	/**
	 * @param subType one of {@link ArgumentSubType#SCOREHOLDER_MULTIPLE} or
	 *                {@link ArgumentSubType#SCOREHOLDER_SINGLE}
	 * @return Scoreholder argument (minecraft:score_holder)
	 */
	ArgumentType<?> _ArgumentScoreholder(ArgumentSubType subType);

	/**
	 * @return Function argument (minecraft:function)
	 */
	ArgumentType<?> _ArgumentTag();

	/**
	 * @return Time argument (minecraft:time)
	 */
	ArgumentType<?> _ArgumentTime();

	/**
	 * @return UUID argument (minecraft:uuid)
	 */
	ArgumentType<?> _ArgumentUUID();

	/**
	 * @return Location 2D argument (precise position) (minecraft:vec2)
	 */
	ArgumentType<?> _ArgumentVec2(boolean centerPosition);

	/**
	 * @return Location argument (precise position) (minecraft:vec3)
	 */
	ArgumentType<?> _ArgumentVec3(boolean centerPosition);

	/*
	 * Synthetic arguments - arguments that don't actually exist, but have
	 * version-specific implementations, so we can switch their implementation as
	 * needed. For example, the BiomeArgument is both a
	 * _ArgumentMinecraftKeyRegistered and a _ArgumentResourceOrTag, but we'll refer
	 * to it as an _ArgumentSyntheticBiome
	 */

	ArgumentType<?> _ArgumentSyntheticBiome();

	/**
	 * A String array of Minecraft versions that this NMS implementation is
	 * compatible with. For example, ["1.14", "1.14.1", "1.14.2", "1.14.3"]. This
	 * can be found by opening a Minecraft jar file, viewing the version.json file
	 * and reading the object "name".
	 * 
	 * @return A String array of compatible Minecraft versions
	 */
	String[] compatibleVersions();

	String convert(ItemStack is);

	String convert(ParticleData<?> particle);

	String convert(PotionEffectType potion);

	String convert(Sound sound);

	Advancement getAdvancement(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException;

	Component getAdventureChat(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException;

	NamedTextColor getAdventureChatColor(CommandContext<CommandListenerWrapper> cmdCtx, String key);

	Component getAdventureChatComponent(CommandContext<CommandListenerWrapper> cmdCtx, String key);

	float getAngle(CommandContext<CommandListenerWrapper> cmdCtx, String key);

	EnumSet<Axis> getAxis(CommandContext<CommandListenerWrapper> cmdCtx, String key);

	Object getBiome(CommandContext<CommandListenerWrapper> cmdCtx, String key, ArgumentSubType subType) throws CommandSyntaxException;

	Predicate<Block> getBlockPredicate(CommandContext<CommandListenerWrapper> cmdCtx, String key)
		throws CommandSyntaxException;

	BlockData getBlockState(CommandContext<CommandListenerWrapper> cmdCtx, String key);

	BaseComponent[] getChat(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException;

	ChatColor getChatColor(CommandContext<CommandListenerWrapper> cmdCtx, String key);

	BaseComponent[] getChatComponent(CommandContext<CommandListenerWrapper> cmdCtx, String key);

	World getDimension(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException;

	Enchantment getEnchantment(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException; // Throws exception in 1.19.3

	Object getEntitySelector(CommandContext<CommandListenerWrapper> cmdCtx, String key, ArgumentSubType subType, boolean allowEmpty)
		throws CommandSyntaxException;

	EntityType getEntityType(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException;

	FloatRange getFloatRange(CommandContext<CommandListenerWrapper> cmdCtx, String key);

	FunctionWrapper[] getFunction(CommandContext<CommandListenerWrapper> cmdCtx, String key)
		throws CommandSyntaxException;

	SimpleFunctionWrapper getFunction(NamespacedKey key);

	Set<NamespacedKey> getFunctions();

	IntegerRange getIntRange(CommandContext<CommandListenerWrapper> cmdCtx, String key);

	ItemStack getItemStack(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException;

	Predicate<ItemStack> getItemStackPredicate(CommandContext<CommandListenerWrapper> cmdCtx, String key)
		throws CommandSyntaxException;

	Location2D getLocation2DBlock(CommandContext<CommandListenerWrapper> cmdCtx, String key)
		throws CommandSyntaxException;

	Location2D getLocation2DPrecise(CommandContext<CommandListenerWrapper> cmdCtx, String key)
		throws CommandSyntaxException;

	Location getLocationBlock(CommandContext<CommandListenerWrapper> cmdCtx, String str) throws CommandSyntaxException;

	Location getLocationPrecise(CommandContext<CommandListenerWrapper> cmdCtx, String str)
		throws CommandSyntaxException;

	LootTable getLootTable(CommandContext<CommandListenerWrapper> cmdCtx, String key);

	MathOperation getMathOperation(CommandContext<CommandListenerWrapper> cmdCtx, String key)
		throws CommandSyntaxException;

	NamespacedKey getMinecraftKey(CommandContext<CommandListenerWrapper> cmdCtx, String key);

	<NBTContainer> Object getNBTCompound(CommandContext<CommandListenerWrapper> cmdCtx, String key,
		Function<Object, NBTContainer> nbtContainerConstructor);

	Objective getObjective(CommandContext<CommandListenerWrapper> cmdCtx, String key)
		throws IllegalArgumentException, CommandSyntaxException;

	String getObjectiveCriteria(CommandContext<CommandListenerWrapper> cmdCtx, String key);

	ParticleData<?> getParticle(CommandContext<CommandListenerWrapper> cmdCtx, String key);

	Player getPlayer(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException;

	OfflinePlayer getOfflinePlayer(CommandContext<CommandListenerWrapper> cmdCtx, String key)
		throws CommandSyntaxException;

	Object getPotionEffect(CommandContext<CommandListenerWrapper> cmdCtx, String key, ArgumentSubType subType)
		throws CommandSyntaxException;

	Recipe getRecipe(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException;

	Rotation getRotation(CommandContext<CommandListenerWrapper> cmdCtx, String key);

	ScoreboardSlot getScoreboardSlot(CommandContext<CommandListenerWrapper> cmdCtx, String key);

	Collection<String> getScoreHolderMultiple(CommandContext<CommandListenerWrapper> cmdCtx, String key)
		throws CommandSyntaxException;

	String getScoreHolderSingle(CommandContext<CommandListenerWrapper> cmdCtx, String key)
		throws CommandSyntaxException;

	Team getTeam(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException;

	int getTime(CommandContext<CommandListenerWrapper> cmdCtx, String key);

	UUID getUUID(CommandContext<CommandListenerWrapper> cmdCtx, String key);

	World getWorldForCSS(CommandListenerWrapper clw);

	/**
	 * Returns the Server's internal (OBC) CommandMap
	 * 
	 * @return A SimpleCommandMap from the OBC server
	 */
	SimpleCommandMap getSimpleCommandMap();

	Object getSound(CommandContext<CommandListenerWrapper> cmdCtx, String key, ArgumentSubType subType);

    NativeProxyCommandSender getNativeProxyCommandSender(CommandSender sender, CommandListenerWrapper css);

	/**
	 * Retrieve a specific NMS implemented SuggestionProvider
	 * 
	 * @param provider The SuggestionProvider type to retrieve
	 * @return A SuggestionProvider that matches the SuggestionProviders input
	 */
	SuggestionProvider<CommandListenerWrapper> getSuggestionProvider(SuggestionProviders provider);

	SimpleFunctionWrapper[] getTag(NamespacedKey key);

	Set<NamespacedKey> getTags();

	/**
	 * Reloads the datapacks by using the updated the commandDispatcher tree
	 */
	void reloadDataPacks();

	Map<String, HelpTopic> getHelpMap();

	Message generateMessageFromJson(String json);

	CommandRegistrationStrategy<CommandListenerWrapper> createCommandRegistrationStrategy();
}
