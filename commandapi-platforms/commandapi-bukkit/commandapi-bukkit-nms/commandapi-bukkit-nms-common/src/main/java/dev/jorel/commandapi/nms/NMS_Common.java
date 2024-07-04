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

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPIHandler;
import dev.jorel.commandapi.CommandRegistrationStrategy;
import dev.jorel.commandapi.arguments.ArgumentSubType;
import dev.jorel.commandapi.arguments.SuggestionProviders;
import dev.jorel.commandapi.commandsenders.AbstractCommandSender;
import dev.jorel.commandapi.commandsenders.BukkitCommandSender;
import dev.jorel.commandapi.preprocessor.Differs;
import dev.jorel.commandapi.preprocessor.Overridden;
import dev.jorel.commandapi.preprocessor.Unimplemented;
import dev.jorel.commandapi.wrappers.Rotation;
import dev.jorel.commandapi.wrappers.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.*;
import net.minecraft.commands.arguments.coordinates.*;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import org.bukkit.*;
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
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static dev.jorel.commandapi.preprocessor.Unimplemented.REASON.*;

/**
 * Common NMS code To ensure that this code actually works across all versions
 * of Minecraft that this is supposed to support (1.17+), you should be
 * compiling this code against all of the declared Maven profiles specified in
 * this submodule's pom.xml file, by running the following commands:
 * <ul>
 * <li><code>mvn clean package -pl :commandapi-bukkit-nms-common -P Platform.Bukkit,Spigot_1_19_3_R2</code></li>
 * <li><code>mvn clean package -pl :commandapi-bukkit-nms-common -P Platform.Bukkit,Spigot_1_19_R1</code></li>
 * <li><code>mvn clean package -pl :commandapi-bukkit-nms-common -P Platform.Bukkit,Spigot_1_18_2_R2</code></li>
 * <li><code>mvn clean package -pl :commandapi-bukkit-nms-common -P Platform.Bukkit,Spigot_1_18_R1</code></li>
 * <li><code>mvn clean package -pl :commandapi-bukkit-nms-common -P Platform.Bukkit,Spigot_1_17_R1</code></li>
 * </ul>
 * Any of these that do not work should be removed or implemented otherwise
 * (introducing another NMS_Common module perhaps?
 */
public abstract class NMS_Common extends CommandAPIBukkit<CommandSourceStack> {
	private static NamespacedKey fromResourceLocation(ResourceLocation key) {
		return NamespacedKey.fromString(key.getNamespace() + ":" + key.getPath());
	}

	@Override
	public ArgumentType<?> _ArgumentAdvancement() {
		return ResourceLocationArgument.id();
	}

	@Override
	public final ArgumentType<?> _ArgumentAngle() {
		return AngleArgument.angle();
	}

	@Override
	public final ArgumentType<?> _ArgumentAxis() {
		return SwizzleArgument.swizzle();
	}

	@Override
	@Unimplemented(because = VERSION_SPECIFIC_IMPLEMENTATION, introducedIn = "1.19")
	public abstract ArgumentType<?> _ArgumentBlockPredicate();

	@Override
	@Unimplemented(because = VERSION_SPECIFIC_IMPLEMENTATION, introducedIn = "1.19")
	public abstract ArgumentType<?> _ArgumentBlockState();

	@Override
	public final ArgumentType<?> _ArgumentChat() {
		return MessageArgument.message();
	}

	@Override
	@Overridden(in = "1.20.5", because = "This now takes in a CommandBuildContext")
	public ArgumentType<?> _ArgumentChatComponent() {
		return ComponentArgument.textComponent();
	}

	@Override
	public final ArgumentType<?> _ArgumentChatFormat() {
		return ColorArgument.color();
	}

	@Override
	public ArgumentType<?> _ArgumentDimension() {
		return DimensionArgument.dimension();
	}

	@Override
	@Unimplemented(because = VERSION_SPECIFIC_IMPLEMENTATION, introducedIn = "1.19.3")
	public abstract ArgumentType<?> _ArgumentEnchantment();

	@Override
	// I mean... really? Why?
	@Unimplemented(because = NAME_CHANGED, info = "a (1.17)                -> entity   (1.18) -> a (1.19)")
	@Unimplemented(because = NAME_CHANGED, info = "multipleEntities (1.17) -> entities (1.18) -> b (1.19)")
	@Unimplemented(because = NAME_CHANGED, info = "c (1.17)                -> player   (1.18) -> c (1.19)")
	@Unimplemented(because = NAME_CHANGED, info = "d (1.17)                -> players  (1.18) -> d (1.19)")
	public abstract ArgumentType<?> _ArgumentEntity(ArgumentSubType subType);

	@Override
	@Unimplemented(because = VERSION_SPECIFIC_IMPLEMENTATION, introducedIn = "1.19.3")
	public abstract ArgumentType<?> _ArgumentEntitySummon();

	@Override
	public final ArgumentType<?> _ArgumentFloatRange() {
		return RangeArgument.floatRange();
	}

	@Override
	public final ArgumentType<?> _ArgumentIntRange() {
		return RangeArgument.intRange();
	}

	@Override
	@Unimplemented(because = VERSION_SPECIFIC_IMPLEMENTATION, introducedIn = "1.19")
	public abstract ArgumentType<?> _ArgumentItemPredicate();

	@Override
	@Unimplemented(because = VERSION_SPECIFIC_IMPLEMENTATION, introducedIn = "1.19")
	public abstract ArgumentType<?> _ArgumentItemStack();

	@Override
	public final ArgumentType<?> _ArgumentMathOperation() {
		return OperationArgument.operation();
	}

	@Override
	public final ArgumentType<?> _ArgumentMinecraftKeyRegistered() {
		return ResourceLocationArgument.id();
	}

	@Override
	@Unimplemented(because = VERSION_SPECIFIC_IMPLEMENTATION, introducedIn = "1.19.3")
	public abstract ArgumentType<?> _ArgumentMobEffect();

	@Override
	public final ArgumentType<?> _ArgumentNBTCompound() {
		return CompoundTagArgument.compoundTag();
	}

	@Override
	@Unimplemented(because = VERSION_SPECIFIC_IMPLEMENTATION, introducedIn = "1.19.3")
	public abstract ArgumentType<?> _ArgumentParticle();

	@Override
	public ArgumentType<?> _ArgumentRecipe() {
		return ResourceLocationArgument.id();
	}

	@Override
	public final ArgumentType<?> _ArgumentPosition() {
		return BlockPosArgument.blockPos();
	}

	@Override
	public final ArgumentType<?> _ArgumentPosition2D() {
		return ColumnPosArgument.columnPos();
	}

	@Override
	public final ArgumentType<?> _ArgumentProfile() {
		return GameProfileArgument.gameProfile();
	}

	@Override
	public final ArgumentType<?> _ArgumentRotation() {
		return RotationArgument.rotation();
	}

	@Override
	public final ArgumentType<?> _ArgumentScoreboardCriteria() {
		return ObjectiveCriteriaArgument.criteria();
	}

	@Override
	public final ArgumentType<?> _ArgumentScoreboardObjective() {
		return ObjectiveArgument.objective();
	}

	@Override
	public final ArgumentType<?> _ArgumentScoreboardSlot() {
		return ScoreboardSlotArgument.displaySlot();
	}

	@Override
	public final ArgumentType<?> _ArgumentScoreboardTeam() {
		return TeamArgument.team();
	}

	@Override
	public final ArgumentType<?> _ArgumentScoreholder(ArgumentSubType subType) {
		return switch(subType) {
			case SCOREHOLDER_SINGLE -> ScoreHolderArgument.scoreHolder();
			case SCOREHOLDER_MULTIPLE -> ScoreHolderArgument.scoreHolders();
			default -> throw new IllegalArgumentException("Unexpected value: " + subType);
		};
	}

	@Override
	@Unimplemented(because = VERSION_SPECIFIC_IMPLEMENTATION, introducedIn = "1.18.2")
	public abstract ArgumentType<?> _ArgumentSyntheticBiome();

	@Override
	public final ArgumentType<?> _ArgumentTag() {
		return FunctionArgument.functions();
	}

	@Override
	public final ArgumentType<?> _ArgumentTime() {
		return TimeArgument.time();
	}

	@Override
	public final ArgumentType<?> _ArgumentUUID() {
		return UuidArgument.uuid();
	}

	@Override
	public final ArgumentType<?> _ArgumentVec2(boolean centerPosition) {
		return Vec2Argument.vec2(centerPosition);
	}

	@Override
	public final ArgumentType<?> _ArgumentVec3(boolean centerPosition) {
		return Vec3Argument.vec3(centerPosition);
	}

	@Override
	@Unimplemented(because = REQUIRES_CRAFTBUKKIT, classNamed = "SimpleHelpMap")
	public abstract Map<String, HelpTopic> getHelpMap();

	@Override
	@Unimplemented(because = VERSION_SPECIFIC_IMPLEMENTATION)
	public abstract String[] compatibleVersions();

	@Override
	@Unimplemented(because = REQUIRES_CRAFTBUKKIT, classNamed = "CraftItemStack")
	public abstract String convert(ItemStack is);

	@Override
	@Unimplemented(because = VERSION_SPECIFIC_IMPLEMENTATION)
	public abstract String convert(ParticleData<?> particle);

	@Override
	public final String convert(PotionEffectType potion) {
		return potion.getName().toLowerCase(Locale.ENGLISH);
	}

	@Override
	public final String convert(Sound sound) {
		return sound.getKey().toString();
	}

	@Override
	@Unimplemented(because = VERSION_SPECIFIC_IMPLEMENTATION, introducedIn = "1.19")
	public abstract void createDispatcherFile(File file, CommandDispatcher<CommandSourceStack> dispatcher) throws IOException;

	@Override
	@Unimplemented(because = REQUIRES_CRAFTBUKKIT, classNamed = "CustomHelpTopic")
	public abstract HelpTopic generateHelpTopic(String commandName, String shortDescription, String fullDescription,
		String permission);

	@Override
	@Unimplemented(because = VERSION_SPECIFIC_IMPLEMENTATION, introducedIn = "1.20.2")
	public abstract org.bukkit.advancement.Advancement getAdvancement(CommandContext<CommandSourceStack> cmdCtx, String key)
		throws CommandSyntaxException;

	@Override
	@Overridden(in = "1.20.5", because = "Serializer.toJson now needs a Provider")
	public Component getAdventureChat(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return GsonComponentSerializer.gson().deserialize(Serializer.toJson(MessageArgument.getMessage(cmdCtx, key)));
	}

	@Override
	@Unimplemented(because = VERSION_SPECIFIC_IMPLEMENTATION, from = "ofExact", to = "namedColor", in = "NamedTextColor", introducedIn = "Adventure 4.10.0", info = "1.18 uses Adventure 4.9.3. 1.18.2 uses Adventure 4.11.0")
	public abstract NamedTextColor getAdventureChatColor(CommandContext<CommandSourceStack> cmdCtx, String key);

	@Override
	public final float getAngle(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return AngleArgument.getAngle(cmdCtx, key);
	}

	@Override
	public final EnumSet<Axis> getAxis(CommandContext<CommandSourceStack> cmdCtx, String key) {
		EnumSet<Axis> set = EnumSet.noneOf(Axis.class);
		EnumSet<net.minecraft.core.Direction.Axis> parsedEnumSet = SwizzleArgument.getSwizzle(cmdCtx, key);
		for (net.minecraft.core.Direction.Axis element : parsedEnumSet) {
			set.add(switch (element) {
				case X -> Axis.X;
				case Y -> Axis.Y;
				case Z -> Axis.Z;
			});
		}
		return set;
	}

	@Override
	@Unimplemented(because = NAME_CHANGED, from = "getWorld()", to = "f()", in = "1.19")
	public abstract Predicate<Block> getBlockPredicate(CommandContext<CommandSourceStack> cmdCtx, String key)
		throws CommandSyntaxException;

	@Override
	@Unimplemented(because = REQUIRES_CRAFTBUKKIT, classNamed = "CraftBlockData")
	public abstract BlockData getBlockState(CommandContext<CommandSourceStack> cmdCtx, String key);

	@Override
	@Overridden(in = "1.20.5", because = "Serializer.toJson now needs a Provider")
	public BaseComponent[] getChat(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return ComponentSerializer.parse(Serializer.toJson(MessageArgument.getMessage(cmdCtx, key)));
	}

	@Override
	public final ChatColor getChatColor(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return ChatColor.getByChar(ColorArgument.getColor(cmdCtx, key).getChar());
	}

	@Override
	public BaseComponent[] getChatComponent(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return ComponentSerializer.parse(Serializer.toJson(ComponentArgument.getComponent(cmdCtx, key)));
	}

	@Override
	public abstract CommandSourceStack getBrigadierSourceFromCommandSender(AbstractCommandSender<? extends CommandSender> sender);

	@Override
	public final BukkitCommandSender<? extends CommandSender> getCommandSenderFromCommandSource(CommandSourceStack css) {
		try {
			return wrapCommandSender(css.getBukkitSender());
		} catch (UnsupportedOperationException e) {
			return null;
		}
	}

	@Override
	@Unimplemented(because = REQUIRES_CRAFTBUKKIT, classNamed = "CraftWorld", info = "CraftWorld is implicitly referenced by ServerLevel#getWorld, due to package renaming, it can't resolve at runtime")
	public abstract World getDimension(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException;

	@Override
	@Unimplemented(because = VERSION_SPECIFIC_IMPLEMENTATION, introducedIn = "1.19.3")
	public abstract Enchantment getEnchantment(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException;

	@Override
	@Unimplemented(because = NAME_CHANGED, from = "getKey()", to = "a()", in = "1.19")
	public abstract EntityType getEntityType(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException;

	@Override
	@Unimplemented(because = VERSION_SPECIFIC_IMPLEMENTATION, introducedIn = "1.20.2")
	public abstract FloatRange getFloatRange(CommandContext<CommandSourceStack> cmdCtx, String key);

	@Override
	@Unimplemented(because = REQUIRES_CRAFTBUKKIT, classNamed = "CraftEntity")
	public abstract FunctionWrapper[] getFunction(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException;

	@Override
	@Unimplemented(because = VERSION_SPECIFIC_IMPLEMENTATION, in = "1.17, 1.18 and 1.18.2")
	public abstract SimpleFunctionWrapper getFunction(NamespacedKey key);

	@Override
	@Unimplemented(because = VERSION_SPECIFIC_IMPLEMENTATION, in = "1.17, 1.18 and 1.18.2")
	public abstract Set<NamespacedKey> getFunctions();

	@Override
	@Unimplemented(because = VERSION_SPECIFIC_IMPLEMENTATION, introducedIn = "1.20.2")
	public abstract IntegerRange getIntRange(CommandContext<CommandSourceStack> cmdCtx, String key);

	@Override
	@Unimplemented(because = REQUIRES_CRAFTBUKKIT, classNamed = "CraftItemStack")
	public abstract ItemStack getItemStack(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException;

	@Override
	@Unimplemented(because = REQUIRES_CRAFTBUKKIT, classNamed = "CraftItemStack")
	public abstract Predicate<ItemStack> getItemStackPredicate(CommandContext<CommandSourceStack> cmdCtx, String key)
		throws CommandSyntaxException;

	@Override
	@Unimplemented(because = { NAME_CHANGED, REQUIRES_CSS }, from = "a, b", to = "c(), d()")
	public abstract Location2D getLocation2DBlock(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException;

	@Override
	public Location2D getLocation2DPrecise(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		Vec2 vecPos = Vec2Argument.getVec2(cmdCtx, key);
		return new Location2D(getWorldForCSS(cmdCtx.getSource()), vecPos.x, vecPos.y);
	}

	@Override
	@Unimplemented(because = { NAME_CHANGED, REQUIRES_CSS }, from = "getX(), getY(), getZ()", to = "u(), v(), w()")
	public abstract Location getLocationBlock(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException;

	@Override
	@Unimplemented(because = { NAME_CHANGED, REQUIRES_CSS }, from = "getX(), getY(), getZ()", to = "a(), b(), c()")
	public abstract Location getLocationPrecise(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException;

	@Override
	@Unimplemented(because = REQUIRES_CRAFTBUKKIT, classNamed = "CraftLootTable")
	public abstract LootTable getLootTable(CommandContext<CommandSourceStack> cmdCtx, String key);

	@Override
	public final MathOperation getMathOperation(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		// We run this to ensure the argument exists/parses properly
		OperationArgument.getOperation(cmdCtx, key);
		return MathOperation.fromString(CommandAPIHandler.getRawArgumentInput(cmdCtx, key));
	}

	@Override
	@Overridden(in = "1.17 common; 1.18", because = "1.17 uses ArgumentMinecraftKeyRegistered.f instead of ArgumentMinecraftKeyRegistered.e")
	@Overridden(in = "1.20.5, 1.20.6", because = "ArgumentMinecraftKeyRegistered.e -> ArgumentMinecraftKeyRegistered.c")
	public NamespacedKey getMinecraftKey(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return fromResourceLocation(ResourceLocationArgument.getId(cmdCtx, key));
	}

	@Override
	public final <NBTContainer> Object getNBTCompound(CommandContext<CommandSourceStack> cmdCtx, String key,
		Function<Object, NBTContainer> nbtContainerConstructor) {
		return nbtContainerConstructor.apply(CompoundTagArgument.getCompoundTag(cmdCtx, key));
	}

	@Override
	@Overridden(in = "1.17 common", because = "The Objective.getName() method mangles itself sometimes and I don't know why. Seems to be looking for Objective.b() or something")
	public Objective getObjective(CommandContext<CommandSourceStack> cmdCtx, String key)
		throws CommandSyntaxException {
		String objectiveName = ObjectiveArgument.getObjective(cmdCtx, key).getName();
		return Bukkit.getScoreboardManager().getMainScoreboard().getObjective(objectiveName);
	}

	@Override
	public final String getObjectiveCriteria(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return ObjectiveCriteriaArgument.getCriteria(cmdCtx, key).getName();
	}

	@Override
	public final OfflinePlayer getOfflinePlayer(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return Bukkit.getOfflinePlayer(GameProfileArgument.getGameProfiles(cmdCtx, key).iterator().next().getId());
	}

	@Override
	@Unimplemented(because = VERSION_SPECIFIC_IMPLEMENTATION, introducedIn = "1.18, 1.19")
	public abstract ParticleData<?> getParticle(CommandContext<CommandSourceStack> cmdCtx, String key);

	@Override
	public final Player getPlayer(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		Player target = Bukkit.getPlayer(GameProfileArgument.getGameProfiles(cmdCtx, key).iterator().next().getId());
		if (target == null) {
			throw GameProfileArgument.ERROR_UNKNOWN_PLAYER.create();
		} else {
			return target;
		}
	}

	@Override
	@Unimplemented(because = VERSION_SPECIFIC_IMPLEMENTATION, introducedIn = "1.18")
	public abstract Object getPotionEffect(CommandContext<CommandSourceStack> cmdCtx, String key, ArgumentSubType subType)
		throws CommandSyntaxException;

	@Override
	@Unimplemented(because = VERSION_SPECIFIC_IMPLEMENTATION, info = """
		1.17 has what appears to be a different obfuscation for recipe.getId().
		I can't be bothered to figure out what it is, but all I know is it doesn't work,
		and we need to move it outta here!
		""")
	public abstract Recipe getRecipe(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException;

	@Override
	public final Rotation getRotation(CommandContext<CommandSourceStack> cmdCtx, String key) {
		Vec2 rotation = RotationArgument.getRotation(cmdCtx, key).getRotation(cmdCtx.getSource());
		return new Rotation(rotation.y, rotation.x);
	}

	@Override
	@Unimplemented(because = VERSION_SPECIFIC_IMPLEMENTATION, introducedIn = "1.20.2")
	public abstract ScoreboardSlot getScoreboardSlot(CommandContext<CommandSourceStack> cmdCtx, String key);

	@Override
	// TODO: Overridden in 1.20.3 because this now returns a Collection<ScoreHolder>
	public Collection<String> getScoreHolderMultiple(CommandContext<CommandSourceStack> cmdCtx, String key)
		throws CommandSyntaxException {
		return ScoreHolderArgument.getNames(cmdCtx, key);
	}

	@Override
	// TODO: Overridden in 1.20.3 because this now returns a ScoreHolder
	public String getScoreHolderSingle(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return ScoreHolderArgument.getName(cmdCtx, key);
	}

	@Override
	// Whyyyyyyyyyyyyyyyyyyy
	@Unimplemented(because = NAME_CHANGED, info = "CommandListenerWrapper changes:")
	@Unimplemented(because = NAME_CHANGED, info = "getPosition (1.17)  -> getPosition (1.18) -> e (1.19)")
	@Unimplemented(because = NAME_CHANGED, info = "i (1.17)            -> getRotation (1.18) -> l (1.19)")
	@Unimplemented(because = NAME_CHANGED, info = "getEntity (1.17)    -> getEntity (1.18)   -> g (1.19)")
	@Unimplemented(because = NAME_CHANGED, info = "getWorld (1.17)     -> getLevel (1.18)    -> f (1.19)")
	public abstract BukkitCommandSender<? extends CommandSender> getSenderForCommand(CommandContext<CommandSourceStack> cmdCtx, boolean isNative);

	@Override
	@Unimplemented(because = REQUIRES_CRAFTBUKKIT, classNamed = "ProxiedNativeCommandSender")
	public abstract NativeProxyCommandSender createNativeProxyCommandSender(CommandSender caller, CommandSender callee, Location location, World world);

	@Override
	@Unimplemented(because = REQUIRES_CRAFTBUKKIT, classNamed = "CraftServer")
	public abstract SimpleCommandMap getSimpleCommandMap();

	@Override
	@Unimplemented(because = REQUIRES_CRAFTBUKKIT, classNamed = "CraftSound")
	public abstract Object getSound(CommandContext<CommandSourceStack> cmdCtx, String key, ArgumentSubType subType);

	@Override
	@Unimplemented(because = VERSION_SPECIFIC_IMPLEMENTATION, info = """
		The various methods that this uses is obfuscated to different method
		names for different versions. For example, getMinecraftServer().getLootTables.getIds()
		is mapped to a different method in 1.18 compared to 1.19.2. This also has various other
		implications across all sorts of versions, so it's much more reliable to just implement
		them in every version.
		""")
	public abstract SuggestionProvider<CommandSourceStack> getSuggestionProvider(SuggestionProviders provider);

	@Override
	@Unimplemented(because = VERSION_SPECIFIC_IMPLEMENTATION, from = "1.18.2", to = "1.19")
	@Differs(from = "1.18.2", by = "getTag() now returns a Collection<> instead of a Tag<>, so don't have to call .getValues()")
	public abstract SimpleFunctionWrapper[] getTag(NamespacedKey key);

	@Override
	@Unimplemented(because = NAME_CHANGED, info = "See https://github.com/JorelAli/CommandAPI/issues/524")
	public abstract Set<NamespacedKey> getTags();

	@Override
	public Team getTeam(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		String teamName = TeamArgument.getTeam(cmdCtx, key).getName();
		return Bukkit.getScoreboardManager().getMainScoreboard().getTeam(teamName);
	}

	@Override
	public final int getTime(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return cmdCtx.getArgument(key, Integer.class);
	}

	@Override
	public final UUID getUUID(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return UuidArgument.getUuid(cmdCtx, key);
	}

	@Override
	@Unimplemented(because = VERSION_SPECIFIC_IMPLEMENTATION)
	public abstract void reloadDataPacks();

	@Override
	@Unimplemented(because = NAME_CHANGED, info = "MinecraftServer#getCommands() obfuscated differently across multiple versions")
	@Unimplemented(because = REQUIRES_CRAFTBUKKIT, classNamed = "VanillaCommandWrapper")
	@Unimplemented(because = REQUIRES_CRAFTBUKKIT, classNamed = "BukkitCommandWrapper")
	public abstract CommandRegistrationStrategy<CommandSourceStack> createCommandRegistrationStrategy();
}
