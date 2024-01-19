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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

import com.mojang.brigadier.CommandDispatcher;
import dev.jorel.commandapi.CommandRegistrationStrategy;
import dev.jorel.commandapi.commandsenders.AbstractCommandSender;
import dev.jorel.commandapi.commandsenders.BukkitCommandSender;
import dev.jorel.commandapi.preprocessor.Unimplemented;
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

import static dev.jorel.commandapi.preprocessor.Unimplemented.REASON.REQUIRES_CRAFTBUKKIT;
import static dev.jorel.commandapi.preprocessor.Unimplemented.REASON.REQUIRES_CSS;
import static dev.jorel.commandapi.preprocessor.Unimplemented.REASON.REQUIRES_MINECRAFT_SERVER;
import static dev.jorel.commandapi.preprocessor.Unimplemented.REASON.VERSION_SPECIFIC_IMPLEMENTATION;

public abstract class NMS<CommandListenerWrapper> {

	/**
	 * @return Advancement argument (api:advancement)
	 */
	public abstract ArgumentType<?> _ArgumentAdvancement();

	/**
	 * @return Angle argument (minecraft:angle)
	 */
	public abstract ArgumentType<?> _ArgumentAngle();

	/**
	 * @return Axis argument (minecraft:swizzle)
	 */
	public abstract ArgumentType<?> _ArgumentAxis();

	/**
	 * @return Block predicate argument (minecraft:block_predicate)
	 */
	public abstract ArgumentType<?> _ArgumentBlockPredicate();

	/**
	 * @return Block state argument (minecraft:block_state)
	 */
	public abstract ArgumentType<?> _ArgumentBlockState();

	/**
	 * @return Chat argument (minecraft:message)
	 */
	public abstract ArgumentType<?> _ArgumentChat();

	/**
	 * @return Chat component argument (minecraft:component)
	 */
	public abstract ArgumentType<?> _ArgumentChatComponent();

	/**
	 * @return Chat color argument (minecraft:color)
	 */
	public abstract ArgumentType<?> _ArgumentChatFormat();

	/**
	 * @return Dimension argument (minecraft:dimension)
	 */
	public abstract ArgumentType<?> _ArgumentDimension();

	/**
	 * @return Enchantment argument (minecraft:item_enchantment)
	 */
	public abstract ArgumentType<?> _ArgumentEnchantment();

	/**
	 * @param subType one of {@link ArgumentSubType#ENTITYSELECTOR_MANY_ENTITIES},
	 *                {@link ArgumentSubType#ENTITYSELECTOR_MANY_PLAYERS},
	 *                {@link ArgumentSubType#ENTITYSELECTOR_ONE_ENTITY} or
	 *                {@link ArgumentSubType#ENTITYSELECTOR_ONE_PLAYER}
	 * @return Entity selector argument (minecraft:entity)
	 */
	public abstract ArgumentType<?> _ArgumentEntity(ArgumentSubType subType);

	/**
	 * @return Entity type argument (minecraft:entity_summon)
	 */
	public abstract ArgumentType<?> _ArgumentEntitySummon();

	/**
	 * @return Float range argument (minecraft:float_range)
	 */
	public abstract ArgumentType<?> _ArgumentFloatRange();

	/**
	 * @return Int range argument (minecraft:int_range)
	 */
	public abstract ArgumentType<?> _ArgumentIntRange();

	/**
	 * @return Item predicate argument (minecraft:item_predicate)
	 */
	public abstract ArgumentType<?> _ArgumentItemPredicate();

	/**
	 * @return Itemstack argument (minecraft:item_stack)
	 */
	public abstract ArgumentType<?> _ArgumentItemStack();

	/**
	 * @return Math operation argument (minecraft:operation)
	 */
	public abstract ArgumentType<?> _ArgumentMathOperation();

	/**
	 * @return Minecraft key argument (minecraft:resource_location)
	 */
	public abstract ArgumentType<?> _ArgumentMinecraftKeyRegistered();

	/**
	 * @return Potion effect argument (minecraft:mob_effect)
	 */
	public abstract ArgumentType<?> _ArgumentMobEffect();

	/**
	 * @return NBT compound tag argument (minecraft:nbt_compound_tag)
	 */
	public abstract ArgumentType<?> _ArgumentNBTCompound();

	/**
	 * @return Particle argument (minecraft:particle)
	 */
	public abstract ArgumentType<?> _ArgumentParticle();

	/**
	 * @return Position argument (minecraft:block_pos)
	 */
	public abstract ArgumentType<?> _ArgumentPosition();

	/**
	 * @return 2D position (column) argument (minecraft:column_pos)
	 */
	public abstract ArgumentType<?> _ArgumentPosition2D();

	/**
	 * @return Player argument (minecraft:game_profile)
	 */
	public abstract ArgumentType<?> _ArgumentProfile();

	/**
	 * @return Recipe argument (api:recipe)
	 */
	public abstract ArgumentType<?> _ArgumentRecipe();

	/**
	 * @return Rotation argument (minecraft:rotation)
	 */
	public abstract ArgumentType<?> _ArgumentRotation();

	/**
	 * @return Scoreboard objective criteria argument (minecraft:objective_criteria)
	 */
	public abstract ArgumentType<?> _ArgumentScoreboardCriteria();

	/**
	 * @return Scoreboard objective argument (minecraft:objective)
	 */
	public abstract ArgumentType<?> _ArgumentScoreboardObjective();

	/**
	 * @return Scoreboard slot argument (minecraft:scoreboard_slot)
	 */
	public abstract ArgumentType<?> _ArgumentScoreboardSlot();

	/**
	 * @return Scoreboard team argument (minecraft:team)
	 */
	public abstract ArgumentType<?> _ArgumentScoreboardTeam();

	/**
	 * @param subType one of {@link ArgumentSubType#SCOREHOLDER_MULTIPLE} or
	 *                {@link ArgumentSubType#SCOREHOLDER_SINGLE}
	 * @return Scoreholder argument (minecraft:score_holder)
	 */
	public abstract ArgumentType<?> _ArgumentScoreholder(ArgumentSubType subType);

	/**
	 * @return Function argument (minecraft:function)
	 */
	public abstract ArgumentType<?> _ArgumentTag();

	/**
	 * @return Time argument (minecraft:time)
	 */
	public abstract ArgumentType<?> _ArgumentTime();

	/**
	 * @return UUID argument (minecraft:uuid)
	 */
	public abstract ArgumentType<?> _ArgumentUUID();

	/**
	 * @return Location 2D argument (precise position) (minecraft:vec2)
	 */
	public abstract ArgumentType<?> _ArgumentVec2(boolean centerPosition);

	/**
	 * @return Location argument (precise position) (minecraft:vec3)
	 */
	public abstract ArgumentType<?> _ArgumentVec3(boolean centerPosition);

	/*
	 * Synthetic arguments - arguments that don't actually exist, but have
	 * version-specific implementations, so we can switch their implementation as
	 * needed. For example, the BiomeArgument is both a
	 * _ArgumentMinecraftKeyRegistered and a _ArgumentResourceOrTag, but we'll refer
	 * to it as an _ArgumentSyntheticBiome
	 */

	public abstract ArgumentType<?> _ArgumentSyntheticBiome();

	/**
	 * A String array of Minecraft versions that this NMS implementation is
	 * compatible with. For example, ["1.14", "1.14.1", "1.14.2", "1.14.3"]. This
	 * can be found by opening a Minecraft jar file, viewing the version.json file
	 * and reading the object "name".
	 * 
	 * @return A String array of compatible Minecraft versions
	 */
	public abstract String[] compatibleVersions();

	public abstract String convert(ItemStack is);

	public abstract String convert(ParticleData<?> particle);

	public abstract String convert(PotionEffectType potion);

	public abstract String convert(Sound sound);

	public abstract Advancement getAdvancement(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException;

	public abstract float getAngle(CommandContext<CommandListenerWrapper> cmdCtx, String key);

	public abstract EnumSet<Axis> getAxis(CommandContext<CommandListenerWrapper> cmdCtx, String key);

	public abstract Object getBiome(CommandContext<CommandListenerWrapper> cmdCtx, String key, ArgumentSubType subType) throws CommandSyntaxException;

	public abstract Predicate<Block> getBlockPredicate(CommandContext<CommandListenerWrapper> cmdCtx, String key)
		throws CommandSyntaxException;

	public abstract BlockData getBlockState(CommandContext<CommandListenerWrapper> cmdCtx, String key);

	public abstract World getDimension(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException;

	public abstract Enchantment getEnchantment(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException; // Throws exception in 1.19.3

	public abstract Object getEntitySelector(CommandContext<CommandListenerWrapper> cmdCtx, String key, ArgumentSubType subType, boolean allowEmpty)
		throws CommandSyntaxException;

	public abstract EntityType getEntityType(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException;

	public abstract FloatRange getFloatRange(CommandContext<CommandListenerWrapper> cmdCtx, String key);

	public abstract FunctionWrapper[] getFunction(CommandContext<CommandListenerWrapper> cmdCtx, String key)
		throws CommandSyntaxException;

	public abstract SimpleFunctionWrapper getFunction(NamespacedKey key);

	public abstract Set<NamespacedKey> getFunctions();

	public abstract IntegerRange getIntRange(CommandContext<CommandListenerWrapper> cmdCtx, String key);

	public abstract ItemStack getItemStack(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException;

	public abstract Predicate<ItemStack> getItemStackPredicate(CommandContext<CommandListenerWrapper> cmdCtx, String key)
		throws CommandSyntaxException;

	public abstract Location2D getLocation2DBlock(CommandContext<CommandListenerWrapper> cmdCtx, String key)
		throws CommandSyntaxException;

	public abstract Location2D getLocation2DPrecise(CommandContext<CommandListenerWrapper> cmdCtx, String key)
		throws CommandSyntaxException;

	public abstract Location getLocationBlock(CommandContext<CommandListenerWrapper> cmdCtx, String str) throws CommandSyntaxException;

	public abstract Location getLocationPrecise(CommandContext<CommandListenerWrapper> cmdCtx, String str)
		throws CommandSyntaxException;

	public abstract LootTable getLootTable(CommandContext<CommandListenerWrapper> cmdCtx, String key);

	public abstract MathOperation getMathOperation(CommandContext<CommandListenerWrapper> cmdCtx, String key)
		throws CommandSyntaxException;

	public abstract NamespacedKey getMinecraftKey(CommandContext<CommandListenerWrapper> cmdCtx, String key);

	public abstract <NBTContainer> Object getNBTCompound(CommandContext<CommandListenerWrapper> cmdCtx, String key,
		Function<Object, NBTContainer> nbtContainerConstructor);

	public abstract Objective getObjective(CommandContext<CommandListenerWrapper> cmdCtx, String key)
		throws IllegalArgumentException, CommandSyntaxException;

	public abstract String getObjectiveCriteria(CommandContext<CommandListenerWrapper> cmdCtx, String key);

	public abstract ParticleData<?> getParticle(CommandContext<CommandListenerWrapper> cmdCtx, String key);

	public abstract Player getPlayer(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException;

	public abstract OfflinePlayer getOfflinePlayer(CommandContext<CommandListenerWrapper> cmdCtx, String key)
		throws CommandSyntaxException;

	public abstract Object getPotionEffect(CommandContext<CommandListenerWrapper> cmdCtx, String key, ArgumentSubType subType)
		throws CommandSyntaxException;

	public abstract Recipe getRecipe(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException;

	public abstract Rotation getRotation(CommandContext<CommandListenerWrapper> cmdCtx, String key);

	public abstract ScoreboardSlot getScoreboardSlot(CommandContext<CommandListenerWrapper> cmdCtx, String key);

	public abstract Collection<String> getScoreHolderMultiple(CommandContext<CommandListenerWrapper> cmdCtx, String key)
		throws CommandSyntaxException;

	public abstract String getScoreHolderSingle(CommandContext<CommandListenerWrapper> cmdCtx, String key)
		throws CommandSyntaxException;

	public abstract Team getTeam(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException;

	public abstract int getTime(CommandContext<CommandListenerWrapper> cmdCtx, String key);

	public abstract UUID getUUID(CommandContext<CommandListenerWrapper> cmdCtx, String key);

	public abstract World getWorldForCSS(CommandListenerWrapper clw);

	/**
	 * Returns the Server's internal (OBC) CommandMap
	 * 
	 * @return A SimpleCommandMap from the OBC server
	 */
	public abstract SimpleCommandMap getSimpleCommandMap();

	public abstract Object getSound(CommandContext<CommandListenerWrapper> cmdCtx, String key, ArgumentSubType subType);

	/**
	 * Retrieve a specific NMS implemented SuggestionProvider
	 * 
	 * @param provider The SuggestionProvider type to retrieve
	 * @return A SuggestionProvider that matches the SuggestionProviders input
	 */
	public abstract SuggestionProvider<CommandListenerWrapper> getSuggestionProvider(SuggestionProviders provider);

	public abstract SimpleFunctionWrapper[] getTag(NamespacedKey key);

	public abstract Set<NamespacedKey> getTags();

	/**
	 * Reloads the datapacks by using the updated the commandDispatcher tree
	 */
	public abstract void reloadDataPacks();

	public abstract HelpTopic generateHelpTopic(String commandName, String shortDescription, String fullDescription, String permission);

	public abstract Map<String, HelpTopic> getHelpMap();

	public abstract Message generateMessageFromJson(String json);

	@Unimplemented(because = REQUIRES_CSS)
	public abstract BukkitCommandSender<? extends CommandSender> getSenderForCommand(CommandContext<CommandListenerWrapper> cmdCtx, boolean forceNative);

	@Unimplemented(because = REQUIRES_CSS)
	public abstract BukkitCommandSender<? extends CommandSender> getCommandSenderFromCommandSource(CommandListenerWrapper css);

	@Unimplemented(because = REQUIRES_CRAFTBUKKIT)
	public abstract CommandListenerWrapper getBrigadierSourceFromCommandSender(AbstractCommandSender<? extends CommandSender> sender);

	@Unimplemented(because = {REQUIRES_MINECRAFT_SERVER, VERSION_SPECIFIC_IMPLEMENTATION})
	public abstract void createDispatcherFile(File file, CommandDispatcher<CommandListenerWrapper> brigadierDispatcher) throws IOException;

	@Unimplemented(because = REQUIRES_MINECRAFT_SERVER) // What are the odds?
	public abstract <T> T getMinecraftServer();

	public abstract NativeProxyCommandSender createNativeProxyCommandSender(CommandSender caller, CommandSender callee, Location location, World world);

}
