package dev.jorel.commandapi;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.jorel.commandapi.arguments.ArgumentSubType;
import dev.jorel.commandapi.arguments.SuggestionProviders;
import dev.jorel.commandapi.commandsenders.AbstractCommandSender;
import dev.jorel.commandapi.commandsenders.BukkitCommandSender;
import dev.jorel.commandapi.wrappers.DoubleRange;
import dev.jorel.commandapi.wrappers.FunctionWrapper;
import dev.jorel.commandapi.wrappers.IntegerRange;
import dev.jorel.commandapi.wrappers.Location2D;
import dev.jorel.commandapi.wrappers.MathOperation;
import dev.jorel.commandapi.wrappers.ParticleData;
import dev.jorel.commandapi.wrappers.Rotation;
import dev.jorel.commandapi.wrappers.ScoreboardSlot;
import dev.jorel.commandapi.wrappers.SimpleFunctionWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.chat.BaseComponent;
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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

public interface MockNMS<CommandSource> {

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
	 * @param subType one of {@link dev.jorel.commandapi.arguments.ArgumentSubType#ENTITYSELECTOR_MANY_ENTITIES},
	 *                {@link dev.jorel.commandapi.arguments.ArgumentSubType#ENTITYSELECTOR_MANY_PLAYERS},
	 *                {@link dev.jorel.commandapi.arguments.ArgumentSubType#ENTITYSELECTOR_ONE_ENTITY} or
	 *                {@link dev.jorel.commandapi.arguments.ArgumentSubType#ENTITYSELECTOR_ONE_PLAYER}
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

	Advancement getAdvancement(CommandContext<CommandSource> cmdCtx, String key) throws CommandSyntaxException;

	Component getAdventureChat(CommandContext<MockCommandSource> cmdCtx, String key) throws CommandSyntaxException;

	NamedTextColor getAdventureChatColor(CommandContext<MockCommandSource> cmdCtx, String key);

	Component getAdventureChatComponent(CommandContext<MockCommandSource> cmdCtx, String key);

	float getAngle(CommandContext<CommandSource> cmdCtx, String key);

	EnumSet<Axis> getAxis(CommandContext<CommandSource> cmdCtx, String key);

	Object getBiome(CommandContext<CommandSource> cmdCtx, String key, ArgumentSubType subType) throws CommandSyntaxException;

	Predicate<Block> getBlockPredicate(CommandContext<CommandSource> cmdCtx, String key)
		throws CommandSyntaxException;

	BlockData getBlockState(CommandContext<CommandSource> cmdCtx, String key);

	BaseComponent[] getChat(CommandContext<MockCommandSource> cmdCtx, String key) throws CommandSyntaxException;

	ChatColor getChatColor(CommandContext<MockCommandSource> cmdCtx, String key);

	BaseComponent[] getChatComponent(CommandContext<MockCommandSource> cmdCtx, String key);

	World getDimension(CommandContext<CommandSource> cmdCtx, String key) throws CommandSyntaxException;

	Enchantment getEnchantment(CommandContext<CommandSource> cmdCtx, String key) throws CommandSyntaxException; // Throws exception in 1.19.3

	Object getEntitySelector(CommandContext<CommandSource> cmdCtx, String key, ArgumentSubType subType, boolean allowEmpty)
		throws CommandSyntaxException;

	EntityType getEntityType(CommandContext<CommandSource> cmdCtx, String key) throws CommandSyntaxException;

	DoubleRange getFloatRange(CommandContext<CommandSource> cmdCtx, String key);

	FunctionWrapper[] getFunction(CommandContext<CommandSource> cmdCtx, String key)
		throws CommandSyntaxException;

	SimpleFunctionWrapper getFunction(NamespacedKey key);

	Set<NamespacedKey> getFunctions();

	IntegerRange getIntRange(CommandContext<CommandSource> cmdCtx, String key);

	ItemStack getItemStack(CommandContext<CommandSource> cmdCtx, String key) throws CommandSyntaxException;

	Predicate<ItemStack> getItemStackPredicate(CommandContext<CommandSource> cmdCtx, String key)
		throws CommandSyntaxException;

	Location2D getLocation2DBlock(CommandContext<CommandSource> cmdCtx, String key)
		throws CommandSyntaxException;

	Location2D getLocation2DPrecise(CommandContext<CommandSource> cmdCtx, String key)
		throws CommandSyntaxException;

	Location getLocationBlock(CommandContext<CommandSource> cmdCtx, String str) throws CommandSyntaxException;

	Location getLocationPrecise(CommandContext<CommandSource> cmdCtx, String str)
		throws CommandSyntaxException;

	LootTable getLootTable(CommandContext<CommandSource> cmdCtx, String key);

	MathOperation getMathOperation(CommandContext<CommandSource> cmdCtx, String key)
		throws CommandSyntaxException;

	NamespacedKey getMinecraftKey(CommandContext<CommandSource> cmdCtx, String key);

	<NBTContainer> Object getNBTCompound(CommandContext<CommandSource> cmdCtx, String key,
														 Function<Object, NBTContainer> nbtContainerConstructor);

	Objective getObjective(CommandContext<CommandSource> cmdCtx, String key)
		throws IllegalArgumentException, CommandSyntaxException;

	String getObjectiveCriteria(CommandContext<CommandSource> cmdCtx, String key);

	ParticleData<?> getParticle(CommandContext<CommandSource> cmdCtx, String key);

	Player getPlayer(CommandContext<CommandSource> cmdCtx, String key) throws CommandSyntaxException;

	OfflinePlayer getOfflinePlayer(CommandContext<CommandSource> cmdCtx, String key)
		throws CommandSyntaxException;

	Object getPotionEffect(CommandContext<CommandSource> cmdCtx, String key, ArgumentSubType subType)
		throws CommandSyntaxException;

	Recipe getRecipe(CommandContext<CommandSource> cmdCtx, String key) throws CommandSyntaxException;

	Rotation getRotation(CommandContext<CommandSource> cmdCtx, String key);

	ScoreboardSlot getScoreboardSlot(CommandContext<CommandSource> cmdCtx, String key);

	Collection<String> getScoreHolderMultiple(CommandContext<CommandSource> cmdCtx, String key)
		throws CommandSyntaxException;

	String getScoreHolderSingle(CommandContext<CommandSource> cmdCtx, String key)
		throws CommandSyntaxException;

	Team getTeam(CommandContext<CommandSource> cmdCtx, String key) throws CommandSyntaxException;

	int getTime(CommandContext<CommandSource> cmdCtx, String key);

	UUID getUUID(CommandContext<CommandSource> cmdCtx, String key);

	World getWorldForCSS(CommandSource clw);

	/**
	 * Returns the Server's internal (OBC) CommandMap
	 *
	 * @return A SimpleCommandMap from the OBC server
	 */
	SimpleCommandMap getSimpleCommandMap();

	Object getSound(CommandContext<CommandSource> cmdCtx, String key, ArgumentSubType subType);

	/**
	 * Retrieve a specific NMS implemented SuggestionProvider
	 *
	 * @param provider The SuggestionProvider type to retrieve
	 * @return A SuggestionProvider that matches the SuggestionProviders input
	 */
	SuggestionProvider<CommandSource> getSuggestionProvider(SuggestionProviders provider);

	SimpleFunctionWrapper[] getTag(NamespacedKey key);

	Set<NamespacedKey> getTags();

	/**
	 * Reloads the datapacks by using the updated the commandDispatcher tree
	 */
	void reloadDataPacks();

	HelpTopic generateHelpTopic(String commandName, String shortDescription, String fullDescription, String permission);

	Map<String, HelpTopic> getHelpMap();

	Message generateMessageFromJson(String json);

	BukkitCommandSender<? extends CommandSender> getSenderForCommand(CommandContext<CommandSource> cmdCtx, boolean forceNative);

	BukkitCommandSender<? extends CommandSender> getCommandSenderFromCommandSource(CommandSource css);

	CommandSource getBrigadierSourceFromCommandSender(AbstractCommandSender<? extends CommandSender> sender);

	void createDispatcherFile(File file, CommandDispatcher<CommandSource> brigadierDispatcher) throws IOException;

	<T> T getMinecraftServer();

}
