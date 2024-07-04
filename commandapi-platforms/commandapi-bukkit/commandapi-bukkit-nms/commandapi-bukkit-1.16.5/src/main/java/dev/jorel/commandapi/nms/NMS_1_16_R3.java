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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_16_R3.CraftLootTable;
import org.bukkit.craftbukkit.v1_16_R3.CraftParticle;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftSound;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_16_R3.command.BukkitCommandWrapper;
import org.bukkit.craftbukkit.v1_16_R3.command.VanillaCommandWrapper;
import org.bukkit.craftbukkit.v1_16_R3.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.help.CustomHelpTopic;
import org.bukkit.craftbukkit.v1_16_R3.help.SimpleHelpMap;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_16_R3.potion.CraftPotionEffectType;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.help.HelpTopic;
import org.bukkit.inventory.ComplexRecipe;
import org.bukkit.inventory.Recipe;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;

import com.google.common.io.Files;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPIHandler;
import dev.jorel.commandapi.CommandRegistrationStrategy;
import dev.jorel.commandapi.SafeVarHandle;
import dev.jorel.commandapi.SpigotCommandRegistration;
import dev.jorel.commandapi.arguments.ArgumentSubType;
import dev.jorel.commandapi.arguments.SuggestionProviders;
import dev.jorel.commandapi.commandsenders.AbstractCommandSender;
import dev.jorel.commandapi.commandsenders.BukkitCommandSender;
import dev.jorel.commandapi.commandsenders.BukkitNativeProxyCommandSender;
import dev.jorel.commandapi.preprocessor.Differs;
import dev.jorel.commandapi.preprocessor.NMSMeta;
import dev.jorel.commandapi.preprocessor.RequireField;
import dev.jorel.commandapi.wrappers.ComplexRecipeImpl;
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
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_16_R3.Advancement;
import net.minecraft.server.v1_16_R3.ArgumentAngle;
import net.minecraft.server.v1_16_R3.ArgumentBlockPredicate;
import net.minecraft.server.v1_16_R3.ArgumentChat;
import net.minecraft.server.v1_16_R3.ArgumentChatComponent;
import net.minecraft.server.v1_16_R3.ArgumentChatFormat;
import net.minecraft.server.v1_16_R3.ArgumentCriterionValue;
import net.minecraft.server.v1_16_R3.ArgumentDimension;
import net.minecraft.server.v1_16_R3.ArgumentEnchantment;
import net.minecraft.server.v1_16_R3.ArgumentEntity;
import net.minecraft.server.v1_16_R3.ArgumentEntitySummon;
import net.minecraft.server.v1_16_R3.ArgumentItemPredicate;
import net.minecraft.server.v1_16_R3.ArgumentItemStack;
import net.minecraft.server.v1_16_R3.ArgumentMathOperation;
import net.minecraft.server.v1_16_R3.ArgumentMinecraftKeyRegistered;
import net.minecraft.server.v1_16_R3.ArgumentMobEffect;
import net.minecraft.server.v1_16_R3.ArgumentNBTTag;
import net.minecraft.server.v1_16_R3.ArgumentParticle;
import net.minecraft.server.v1_16_R3.ArgumentPosition;
import net.minecraft.server.v1_16_R3.ArgumentPredicateItemStack;
import net.minecraft.server.v1_16_R3.ArgumentProfile;
import net.minecraft.server.v1_16_R3.ArgumentRegistry;
import net.minecraft.server.v1_16_R3.ArgumentRotation;
import net.minecraft.server.v1_16_R3.ArgumentRotationAxis;
import net.minecraft.server.v1_16_R3.ArgumentScoreboardCriteria;
import net.minecraft.server.v1_16_R3.ArgumentScoreboardObjective;
import net.minecraft.server.v1_16_R3.ArgumentScoreboardSlot;
import net.minecraft.server.v1_16_R3.ArgumentScoreboardTeam;
import net.minecraft.server.v1_16_R3.ArgumentScoreholder;
import net.minecraft.server.v1_16_R3.ArgumentTag;
import net.minecraft.server.v1_16_R3.ArgumentTile;
import net.minecraft.server.v1_16_R3.ArgumentTime;
import net.minecraft.server.v1_16_R3.ArgumentUUID;
import net.minecraft.server.v1_16_R3.ArgumentVec2;
import net.minecraft.server.v1_16_R3.ArgumentVec2I;
import net.minecraft.server.v1_16_R3.ArgumentVec3;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.BlockPosition2D;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import net.minecraft.server.v1_16_R3.CompletionProviders;
import net.minecraft.server.v1_16_R3.CriterionConditionValue;
import net.minecraft.server.v1_16_R3.CustomFunction;
import net.minecraft.server.v1_16_R3.CustomFunctionData;
import net.minecraft.server.v1_16_R3.CustomFunctionManager;
import net.minecraft.server.v1_16_R3.DataPackResources;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.EntitySelector;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.EnumDirection.EnumAxis;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_16_R3.ICompletionProvider;
import net.minecraft.server.v1_16_R3.IRecipe;
import net.minecraft.server.v1_16_R3.IRegistry;
import net.minecraft.server.v1_16_R3.IReloadableResourceManager;
import net.minecraft.server.v1_16_R3.ItemStack;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.ParticleParam;
import net.minecraft.server.v1_16_R3.ParticleParamBlock;
import net.minecraft.server.v1_16_R3.ParticleParamItem;
import net.minecraft.server.v1_16_R3.ParticleParamRedstone;
import net.minecraft.server.v1_16_R3.ParticleType;
import net.minecraft.server.v1_16_R3.ShapeDetectorBlock;
import net.minecraft.server.v1_16_R3.SoundEffect;
import net.minecraft.server.v1_16_R3.SystemUtils;
import net.minecraft.server.v1_16_R3.Vec2F;
import net.minecraft.server.v1_16_R3.Vec3D;

/**
 * NMS implementation for Minecraft 1.16.5
 */
@NMSMeta(compatibleWith = "1.16.5")
@RequireField(in = DataPackResources.class, name = "i", ofType = CustomFunctionManager.class)
@RequireField(in = DataPackResources.class, name = "b", ofType = IReloadableResourceManager.class)
@RequireField(in = CustomFunctionManager.class, name = "h", ofType = CommandDispatcher.class)
@RequireField(in = EntitySelector.class, name = "checkPermissions", ofType = boolean.class)
@RequireField(in = SimpleHelpMap.class, name = "helpTopics", ofType = Map.class)
@RequireField(in = ParticleParamBlock.class, name = "c", ofType = IBlockData.class)
@RequireField(in = ParticleParamItem.class, name = "c", ofType = ItemStack.class)
@RequireField(in = ParticleParamRedstone.class, name = "g", ofType = float.class)
@RequireField(in = ArgumentPredicateItemStack.class, name = "c", ofType = NBTTagCompound.class)
public class NMS_1_16_R3 extends CommandAPIBukkit<CommandListenerWrapper> {
	
	private static final SafeVarHandle<SimpleHelpMap, Map<String, HelpTopic>> helpMapTopics;
	private static final Field entitySelectorCheckPermissions;
	private static final SafeVarHandle<ParticleParamBlock, IBlockData> particleParamBlockData;
	private static final SafeVarHandle<ParticleParamItem, ItemStack> particleParamItemStack;
	private static final SafeVarHandle<ParticleParamRedstone, Float> particleParamRedstoneSize;
	private static final SafeVarHandle<ArgumentPredicateItemStack, NBTTagCompound> itemStackPredicateArgument;
	private static final Field customFunctionManagerBrigadierDispatcher;
	private static final SafeVarHandle<DataPackResources, IReloadableResourceManager> dataPackResources;

	// Compute all var handles all in one go so we don't do this during main server
	// runtime
	static {
		// For some reason, MethodHandles fails for this field, but Field works okay
		entitySelectorCheckPermissions = CommandAPIHandler.getField(EntitySelector.class, "checkPermissions", "checkPermissions");
		helpMapTopics = SafeVarHandle.ofOrNull(SimpleHelpMap.class, "helpTopics", "helpTopics", Map.class);
		particleParamBlockData = SafeVarHandle.ofOrNull(ParticleParamBlock.class, "c", "c", IBlockData.class);
		particleParamItemStack = SafeVarHandle.ofOrNull(ParticleParamItem.class, "c", "c", ItemStack.class);
		particleParamRedstoneSize = SafeVarHandle.ofOrNull(ParticleParamRedstone.class, "g", "g", float.class);
		itemStackPredicateArgument = SafeVarHandle.ofOrNull(ArgumentPredicateItemStack.class, "c", "c", NBTTagCompound.class);
		// For some reason, MethodHandles fails for this field, but Field works okay
		customFunctionManagerBrigadierDispatcher = CommandAPIHandler.getField(CustomFunctionManager.class, "h", "h");
		dataPackResources = SafeVarHandle.ofOrNull(DataPackResources.class, "b", "b", IReloadableResourceManager.class);
	}

	@Differs(from = "1.16.4", by = "Use of non-deprecated NamespacedKey.fromString method")
	protected NamespacedKey fromMinecraftKey(MinecraftKey key) {
		return NamespacedKey.fromString(key.getNamespace() + ":" + key.getKey());
	}

	@Override
	public ArgumentType<?> _ArgumentAdvancement() {
		return _ArgumentMinecraftKeyRegistered();
	}

	@Override
	public ArgumentType<?> _ArgumentAngle() { return ArgumentAngle.a(); }

	@Override
	public ArgumentType<?> _ArgumentAxis() { return ArgumentRotationAxis.a(); }

	@Override
	public ArgumentType<?> _ArgumentBlockPredicate() { return ArgumentBlockPredicate.a(); }

	@Override
	public ArgumentType<?> _ArgumentBlockState() { return ArgumentTile.a(); }

	@Override
	public ArgumentType<?> _ArgumentChat() { return ArgumentChat.a(); }

	@Override
	public ArgumentType<?> _ArgumentChatComponent() { return ArgumentChatComponent.a(); }

	@Override
	public ArgumentType<?> _ArgumentChatFormat() { return ArgumentChatFormat.a(); }

	@Override
	public ArgumentType<?> _ArgumentDimension() { return ArgumentDimension.a(); }

	@Override
	public ArgumentType<?> _ArgumentEnchantment() { return ArgumentEnchantment.a(); }

	@Override
	public ArgumentType<?> _ArgumentEntity(ArgumentSubType subType) {
		return switch (subType) {
			case ENTITYSELECTOR_MANY_ENTITIES -> ArgumentEntity.multipleEntities();
			case ENTITYSELECTOR_MANY_PLAYERS -> ArgumentEntity.d();
			case ENTITYSELECTOR_ONE_ENTITY -> ArgumentEntity.a();
			case ENTITYSELECTOR_ONE_PLAYER -> ArgumentEntity.c();
			default -> throw new IllegalArgumentException("Unexpected value: " + subType);
		};
	}

	@Override
	public ArgumentType<?> _ArgumentEntitySummon() { return ArgumentEntitySummon.a(); }

	@Differs(from = "1.16.2", by = "new ArgumentCriterionValue.a() -> ArgumentCriterionValue.b()")
	@Override
	public ArgumentType<?> _ArgumentFloatRange() { return ArgumentCriterionValue.b(); }

	@Differs(from = "1.16.2", by = "new ArgumentCriterionValue.b() -> ArgumentCriterionValue.a()")
	@Override
	public ArgumentType<?> _ArgumentIntRange() { return ArgumentCriterionValue.a(); }

	@Override
	public ArgumentType<?> _ArgumentItemPredicate() { return ArgumentItemPredicate.a(); }

	@Override
	public ArgumentType<?> _ArgumentItemStack() { return ArgumentItemStack.a(); }

	@Override
	public ArgumentType<?> _ArgumentMathOperation() { return ArgumentMathOperation.a(); }

	@Override
	public ArgumentType<?> _ArgumentMinecraftKeyRegistered() { return ArgumentMinecraftKeyRegistered.a(); }

	@Override
	public ArgumentType<?> _ArgumentMobEffect() { return ArgumentMobEffect.a(); }

	@Override
	public ArgumentType<?> _ArgumentNBTCompound() { return ArgumentNBTTag.a(); }

	@Override
	public ArgumentType<?> _ArgumentParticle() { return ArgumentParticle.a(); }

	@Override
	public ArgumentType<?> _ArgumentPosition() { return ArgumentPosition.a(); }

	@Override
	public ArgumentType<?> _ArgumentPosition2D() { return ArgumentVec2I.a(); }

	@Override
	public ArgumentType<?> _ArgumentProfile() { return ArgumentProfile.a(); }

	@Override
	public ArgumentType<?> _ArgumentRecipe() {
		return _ArgumentMinecraftKeyRegistered();
	}

	@Override
	public ArgumentType<?> _ArgumentRotation() { return ArgumentRotation.a(); }

	@Override
	public ArgumentType<?> _ArgumentScoreboardCriteria() { return ArgumentScoreboardCriteria.a(); }

	@Override
	public ArgumentType<?> _ArgumentScoreboardObjective() { return ArgumentScoreboardObjective.a();
	}

	@Override
	public ArgumentType<?> _ArgumentScoreboardSlot() { return ArgumentScoreboardSlot.a(); }

	@Override
	public ArgumentType<?> _ArgumentScoreboardTeam() { return ArgumentScoreboardTeam.a(); }

	@Override
	public final ArgumentType<?> _ArgumentScoreholder(ArgumentSubType subType) {
		return switch(subType) {
			case SCOREHOLDER_SINGLE -> ArgumentScoreholder.a();
			case SCOREHOLDER_MULTIPLE -> ArgumentScoreholder.b();
			default -> throw new IllegalArgumentException("Unexpected value: " + subType);
		};
	}

	@Override
	public ArgumentType<?> _ArgumentSyntheticBiome() { return _ArgumentMinecraftKeyRegistered(); }

	@Override
	public ArgumentType<?> _ArgumentTag() { return ArgumentTag.a(); }

	@Override
	public ArgumentType<?> _ArgumentTime() { return ArgumentTime.a(); }

	@Override
	public ArgumentType<?> _ArgumentUUID() { return ArgumentUUID.a(); }

	@Override
	public ArgumentType<?> _ArgumentVec2(boolean centerPosition) { return new ArgumentVec2(centerPosition); }

	@Override
	public ArgumentType<?> _ArgumentVec3(boolean centerPosition) { return ArgumentVec3.a(centerPosition); }

	@Override
	public Map<String, HelpTopic> getHelpMap() {
		return helpMapTopics.get((SimpleHelpMap) Bukkit.getHelpMap());
	}

	@Override
	public String[] compatibleVersions() {
		return new String[] { "1.16.5" };
	}

	@Differs(from = "1.16.2", by = "Use of .asString() instead of .toString()")
	@Override
	public String convert(org.bukkit.inventory.ItemStack is) {
		return is.getType().getKey().toString() + CraftItemStack.asNMSCopy(is).getOrCreateTag().asString();
	}

	@Override
	public String convert(ParticleData<?> particle) { return CraftParticle.toNMS(particle.particle(), particle.data()).a(); }

	@Override
	public String convert(PotionEffectType potion) {
		return potion.getName().toLowerCase(Locale.ENGLISH);
	}

	@Differs(from = "1.16.2", by = "Use of sound.getKey().toString() instead of CraftSound.getSound()")
	@Override
	public String convert(Sound sound) {
		return sound.getKey().toString();
	}

	// Converts NMS function to SimpleFunctionWrapper
	private SimpleFunctionWrapper convertFunction(CustomFunction customFunction) {
		ToIntFunction<CommandListenerWrapper> appliedObj = clw -> this.<MinecraftServer>getMinecraftServer().getFunctionData().a(customFunction, clw);

		Object[] cArr = customFunction.b();
		String[] result = new String[cArr.length];
		for (int i = 0, size = cArr.length; i < size; i++) {
			result[i] = cArr[i].toString();
		}
		return new SimpleFunctionWrapper(fromMinecraftKey(customFunction.a()), appliedObj, result);
	}

	@Override
	public void createDispatcherFile(File file, CommandDispatcher<CommandListenerWrapper> dispatcher) throws IOException {
		Files.write(new GsonBuilder().setPrettyPrinting().create()
				.toJson(ArgumentRegistry.a(dispatcher, dispatcher.getRoot())), file, StandardCharsets.UTF_8);
	}

	@Override
	public HelpTopic generateHelpTopic(String commandName, String shortDescription, String fullDescription, String permission) {
		return new CustomHelpTopic(commandName, shortDescription, fullDescription, permission);
	}

	@Override
	public org.bukkit.advancement.Advancement getAdvancement(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException {
		return ArgumentMinecraftKeyRegistered.a(cmdCtx, key).bukkit;
	}

	/*
	 * ADVENTURE START
	 * These methods use the Adventure API, but the Adventure API isn't present
	 * in paper until Minecraft 1.16.5. We assume that the developer is shading
	 * Adventure manually (or otherwise), using https://docs.advntr.dev/platform/bukkit.html
	 */

	@Override
	public final Component getAdventureChat(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException {
		return GsonComponentSerializer.gson().deserialize(ChatSerializer.a(ArgumentChat.a(cmdCtx, key)));
	}

	@Override
	public final NamedTextColor getAdventureChatColor(CommandContext<CommandListenerWrapper> cmdCtx, String key) {
		final Integer color = ArgumentChatFormat.a(cmdCtx, key).e();
		return color == null ? NamedTextColor.WHITE : NamedTextColor.ofExact(color);
	}

	@Override
	public final Component getAdventureChatComponent(CommandContext<CommandListenerWrapper> cmdCtx, String key) {
		return GsonComponentSerializer.gson().deserialize(ChatSerializer.a(ArgumentChatComponent.a(cmdCtx, key)));
	}

	/*
	 * ADVENTURE END
	 */

	@Override
	public float getAngle(CommandContext<CommandListenerWrapper> cmdCtx, String key) { return ArgumentAngle.a(cmdCtx, key); }

	@Override
	public EnumSet<Axis> getAxis(CommandContext<CommandListenerWrapper> cmdCtx, String key) {
		EnumSet<Axis> set = EnumSet.noneOf(Axis.class);
		EnumSet<EnumAxis> parsedEnumSet = ArgumentRotationAxis.a(cmdCtx, key);
		for (EnumAxis element : parsedEnumSet) {
			set.add(switch (element) {
				case X -> Axis.X;
				case Y -> Axis.Y;
				case Z -> Axis.Z;
			});
		}
		return set;
	}

	@Override
	public Object getBiome(CommandContext<CommandListenerWrapper> cmdCtx, String key, ArgumentSubType subType) throws CommandSyntaxException {
		return switch(subType) {
			case BIOME_BIOME -> {
				Biome biome = null;
				try {
					biome = Biome.valueOf(cmdCtx.getArgument(key, MinecraftKey.class).getKey().toUpperCase());
				} catch(IllegalArgumentException biomeNotFound) {
					biome = null;
				}
				yield biome;
			}
			case BIOME_NAMESPACEDKEY -> (NamespacedKey) fromMinecraftKey(cmdCtx.getArgument(key, MinecraftKey.class));
			default -> null;
		};
	}

	@Override
	public Predicate<Block> getBlockPredicate(CommandContext<CommandListenerWrapper> cmdCtx, String key)  throws CommandSyntaxException {
		Predicate<ShapeDetectorBlock> predicate = ArgumentBlockPredicate.a(cmdCtx, key);
		return (Block block) -> predicate.test(new ShapeDetectorBlock(cmdCtx.getSource().getWorld(),
				new BlockPosition(block.getX(), block.getY(), block.getZ()), true));
	}

	@Override
	public BlockData getBlockState(CommandContext<CommandListenerWrapper> cmdCtx, String key) {
		return CraftBlockData.fromData(ArgumentTile.a(cmdCtx, key).a());
	}

	@Override
	public BaseComponent[] getChat(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException {
		return ComponentSerializer.parse(ChatSerializer.a(ArgumentChat.a(cmdCtx, key)));
	}

	@Override
	public ChatColor getChatColor(CommandContext<CommandListenerWrapper> cmdCtx, String key) {
		return CraftChatMessage.getColor(ArgumentChatFormat.a(cmdCtx, key));
	}

	@Override
	public BaseComponent[] getChatComponent(CommandContext<CommandListenerWrapper> cmdCtx, String key) {
		return ComponentSerializer.parse(ChatSerializer.a(ArgumentChatComponent.a(cmdCtx, key)));
	}

	@Override
	public CommandListenerWrapper getBrigadierSourceFromCommandSender(AbstractCommandSender<? extends CommandSender> senderWrapper) {
		return VanillaCommandWrapper.getListener(senderWrapper.getSource());
	}

	@Override
	public BukkitCommandSender<? extends CommandSender> getCommandSenderFromCommandSource(CommandListenerWrapper clw) {
		try {
			return wrapCommandSender(clw.getBukkitSender());
		} catch (UnsupportedOperationException e) {
			return null;
		}
	}

	@Override
	public World getDimension(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException {
		return ArgumentDimension.a(cmdCtx, key).getWorld();
	}

	@Override
	public Enchantment getEnchantment(CommandContext<CommandListenerWrapper> cmdCtx, String key) {
		return new CraftEnchantment(ArgumentEnchantment.a(cmdCtx, key));
	}

	@Override
	public Object getEntitySelector(CommandContext<CommandListenerWrapper> cmdCtx, String str, ArgumentSubType subType, boolean allowEmpty) throws CommandSyntaxException {
		EntitySelector argument = cmdCtx.getArgument(str, EntitySelector.class);
		try {
			entitySelectorCheckPermissions.set(argument, false);
		} catch (IllegalAccessException e) {
			// Shouldn't happen, CommandAPIHandler#getField makes it accessible
		}

		return switch (subType) {
			case ENTITYSELECTOR_MANY_ENTITIES:
				// ArgumentEntity.c -> EntitySelector.getEntities
				try {
					List<org.bukkit.entity.Entity> result = new ArrayList<>();
					for (Entity entity : argument.getEntities(cmdCtx.getSource())) {
						result.add(entity.getBukkitEntity());
					}
					if (result.isEmpty() && !allowEmpty) {
						throw ArgumentEntity.d.create();
					} else {
						yield result;
					}
				} catch (CommandSyntaxException e) {
					if (allowEmpty) {
						yield new ArrayList<org.bukkit.entity.Entity>();
					} else {
						throw e;
					}
				}
			case ENTITYSELECTOR_MANY_PLAYERS:
				// ArgumentEntity.d -> EntitySelector.d
				try {
					List<Player> result = new ArrayList<>();
					for (EntityPlayer player : argument.d(cmdCtx.getSource())) {
						result.add(player.getBukkitEntity());
					}
					if (result.isEmpty() && !allowEmpty) {
						throw ArgumentEntity.e.create();
					} else {
						yield result;
					}
				} catch (CommandSyntaxException e) {
					if (allowEmpty) {
						yield new ArrayList<Player>();
					} else {
						throw e;
					}
				}
			case ENTITYSELECTOR_ONE_ENTITY:
				// ArgumentEntity.a -> EntitySelector.a
				yield argument.a(cmdCtx.getSource()).getBukkitEntity();
			case ENTITYSELECTOR_ONE_PLAYER:
				// ArgumentEntity.e -> EntitySelector.c
				yield argument.c(cmdCtx.getSource()).getBukkitEntity();
			default:
				throw new IllegalArgumentException("Unexpected value: " + subType);
		};
	}

	@SuppressWarnings("deprecation")
	@Override
	public EntityType getEntityType(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException {
		return EntityType.fromName(EntityTypes.getName(IRegistry.ENTITY_TYPE.get(ArgumentEntitySummon.a(cmdCtx, key))).getKey());
	}

	@Override
	public FloatRange getFloatRange(CommandContext<CommandListenerWrapper> cmdCtx, String key) {
		CriterionConditionValue.FloatRange range = cmdCtx.getArgument(key, CriterionConditionValue.FloatRange.class);
		float low = range.a() == null ? -Float.MAX_VALUE : range.a();
		float high = range.b() == null ? Float.MAX_VALUE : range.b();
		return new FloatRange(low, high);
	}

	@Override
	public FunctionWrapper[] getFunction(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException {
		List<FunctionWrapper> result = new ArrayList<>();
		CommandListenerWrapper commandListenerWrapper = cmdCtx.getSource().a().b(2);

		for (CustomFunction customFunction : ArgumentTag.a(cmdCtx, key)) {
			result.add(FunctionWrapper.fromSimpleFunctionWrapper(convertFunction(customFunction),
					commandListenerWrapper, e -> cmdCtx.getSource().a(((CraftEntity) e).getHandle())));
		}
		return result.toArray(new FunctionWrapper[0]);
	}

	@Override
	public SimpleFunctionWrapper getFunction(NamespacedKey key) {
		return convertFunction(this.<MinecraftServer>getMinecraftServer().getFunctionData().a(new MinecraftKey(key.getNamespace(), key.getKey())).get());
	}

	@Override
	public Set<NamespacedKey> getFunctions() {
		Set<NamespacedKey> functions = new HashSet<>();
		for (MinecraftKey key : this.<MinecraftServer>getMinecraftServer().getFunctionData().f()) {
			functions.add(fromMinecraftKey(key));
		}
		return functions;
	}

	@Override
	public IntegerRange getIntRange(CommandContext<CommandListenerWrapper> cmdCtx, String key) {
		CriterionConditionValue.IntegerRange range = ArgumentCriterionValue.b.a(cmdCtx, key);
		int low = range.a() == null ? Integer.MIN_VALUE : range.a();
		int high = range.b() == null ? Integer.MAX_VALUE : range.b();
		return new IntegerRange(low, high);
	}

	@Override
	public org.bukkit.inventory.ItemStack getItemStack(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException {
		ArgumentPredicateItemStack input = ArgumentItemStack.a(cmdCtx, key);

		// Create the basic ItemStack with an amount of 1
		ItemStack itemWithMaybeTag = input.a(1, false);

		// Try and find the amount from the CompoundTag (if present)
		final NBTTagCompound tag = itemStackPredicateArgument.get(input);
		if(tag != null) {
			// The tag has some extra metadata we need! Get the Count (amount)
			// and create the ItemStack with the correct metadata
			int count = (int) tag.getByte("Count");
			itemWithMaybeTag = input.a(count == 0 ? 1 : count, false);
		}

		org.bukkit.inventory.ItemStack result = CraftItemStack.asBukkitCopy(itemWithMaybeTag);
		result.setItemMeta(CraftItemStack.getItemMeta(itemWithMaybeTag));
		return result;
	}

	@Override
	public Predicate<org.bukkit.inventory.ItemStack> getItemStackPredicate(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException {
		Predicate<ItemStack> predicate = ArgumentItemPredicate.a(cmdCtx, key);
		return item -> predicate.test(CraftItemStack.asNMSCopy(item));
	}

	@Override
	public Location2D getLocation2DBlock(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException {
		BlockPosition2D blockPos = ArgumentVec2I.a(cmdCtx, key);
		return new Location2D(getWorldForCSS(cmdCtx.getSource()), blockPos.a, blockPos.b);
	}

	@Override
	public Location2D getLocation2DPrecise(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException {
		Vec2F vecPos = ArgumentVec2.a(cmdCtx, key);
		return new Location2D(getWorldForCSS(cmdCtx.getSource()), vecPos.i, vecPos.j);
	}

	@Override
	public Location getLocationBlock(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException {
		BlockPosition blockPos = ArgumentPosition.b(cmdCtx, key);
		return new Location(getWorldForCSS(cmdCtx.getSource()), blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}

	@Override
	public Location getLocationPrecise(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException {
		Vec3D vecPos = ArgumentVec3.a(cmdCtx, key);
		return new Location(getWorldForCSS(cmdCtx.getSource()), vecPos.x, vecPos.y, vecPos.z);
	}

	@Override
	public org.bukkit.loot.LootTable getLootTable(CommandContext<CommandListenerWrapper> cmdCtx, String key) {
		MinecraftKey minecraftKey = ArgumentMinecraftKeyRegistered.e(cmdCtx, key);
		return new CraftLootTable(fromMinecraftKey(minecraftKey), this.<MinecraftServer>getMinecraftServer().getLootTableRegistry().getLootTable(minecraftKey));
	}

	@Override
	public MathOperation getMathOperation(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException {
		// We run this to ensure the argument exists/parses properly
		ArgumentMathOperation.a(cmdCtx, key);
		return MathOperation.fromString(CommandAPIHandler.getRawArgumentInput(cmdCtx, key));
	}

	@SuppressWarnings("deprecation")
	@Override
	public NamespacedKey getMinecraftKey(CommandContext<CommandListenerWrapper> cmdCtx, String key) {
		MinecraftKey resourceLocation = ArgumentMinecraftKeyRegistered.e(cmdCtx, key);
		return new NamespacedKey(resourceLocation.getNamespace(), resourceLocation.getKey());
	}

	@Override
	public <NBTContainer> Object getNBTCompound(CommandContext<CommandListenerWrapper> cmdCtx, String key, Function<Object, NBTContainer> nbtContainerConstructor) {
		return nbtContainerConstructor.apply(ArgumentNBTTag.a(cmdCtx, key));
	}

	@Override
	public Objective getObjective(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws IllegalArgumentException, CommandSyntaxException {
		String objectiveName = ArgumentScoreboardObjective.a(cmdCtx, key).getName();
		return Bukkit.getScoreboardManager().getMainScoreboard().getObjective(objectiveName);
	}

	@Override
	public String getObjectiveCriteria(CommandContext<CommandListenerWrapper> cmdCtx, String key) {
		return ArgumentScoreboardCriteria.a(cmdCtx, key).getName();
	}

	@Override
	public OfflinePlayer getOfflinePlayer(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException {
		OfflinePlayer target = Bukkit
				.getOfflinePlayer((ArgumentProfile.a(cmdCtx, key).iterator().next()).getId());
		if (target == null) {
			throw ArgumentProfile.a.create();
		} else {
			return target;
		}
	}

	@Differs(from = "1.16.2", by = "ParticleParamRedstone.f -> ParticleParamRedstone.g")
	@Override
	public ParticleData<?> getParticle(CommandContext<CommandListenerWrapper> cmdCtx, String key) {
		final ParticleParam particleOptions = ArgumentParticle.a(cmdCtx, key);
		final Particle particle = CraftParticle.toBukkit(particleOptions);

		if (particleOptions instanceof ParticleType) {
			return new ParticleData<Void>(particle, null);
		}
		else if (particleOptions instanceof ParticleParamBlock options) {
			return new ParticleData<BlockData>(particle, CraftBlockData.fromData(particleParamBlockData.get(options)));
		}
		else if (particleOptions instanceof ParticleParamRedstone options) {
			return getParticleDataAsDustOptions(particle, options);
		}
		else if (particleOptions instanceof ParticleParamItem options) {
			return new ParticleData<org.bukkit.inventory.ItemStack>(particle,
					CraftItemStack.asBukkitCopy(particleParamItemStack.get(options)));
		} else {
			CommandAPI.getLogger().warning("Invalid particle data type for " + particle.getDataType().toString());
			return new ParticleData<Void>(particle, null);
		}
	}

	private ParticleData<DustOptions> getParticleDataAsDustOptions(Particle particle, ParticleParamRedstone options) {
		String optionsStr = options.a(); // Of the format "particle_type float float float"
		String[] optionsArr = optionsStr.split(" ");
		final float red = Float.parseFloat(optionsArr[1]);
		final float green = Float.parseFloat(optionsArr[2]);
		final float blue = Float.parseFloat(optionsArr[3]);

		final Color color = Color.fromRGB((int) (red * 255.0F), (int) (green * 255.0F), (int) (blue * 255.0F));
		return new ParticleData<DustOptions>(particle,
			new DustOptions(color, particleParamRedstoneSize.get(options)));
	}

	@Override
	public Player getPlayer(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException {
		Player target = Bukkit.getPlayer((ArgumentProfile.a(cmdCtx, key).iterator().next()).getId());
		if (target == null) {
			throw ArgumentProfile.a.create();
		} else {
			return target;
		}
	}

	@Override
	public Object getPotionEffect(CommandContext<CommandListenerWrapper> cmdCtx, String key, ArgumentSubType subType) throws CommandSyntaxException {
		return switch (subType) {
			case POTION_EFFECT_POTION_EFFECT -> new CraftPotionEffectType(ArgumentMobEffect.a(cmdCtx, key));
			case POTION_EFFECT_NAMESPACEDKEY -> fromMinecraftKey(ArgumentMinecraftKeyRegistered.e(cmdCtx, key));
			default -> throw new IllegalArgumentException("Unexpected value: " + subType);
		};
	}

	@Override
	public ComplexRecipe getRecipe(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException {
		IRecipe<?> recipe = ArgumentMinecraftKeyRegistered.b(cmdCtx, key);
		return new ComplexRecipeImpl(fromMinecraftKey(recipe.getKey()), recipe.toBukkitRecipe());
	}

	@Override
	public Rotation getRotation(CommandContext<CommandListenerWrapper> cmdCtx, String key) {
		Vec2F vec = ArgumentRotation.a(cmdCtx, key).b(cmdCtx.getSource());
		return new Rotation(vec.j, vec.i);
	}

	@Override
	public ScoreboardSlot getScoreboardSlot(CommandContext<CommandListenerWrapper> cmdCtx, String key) {
		return ScoreboardSlot.ofMinecraft(ArgumentScoreboardSlot.a(cmdCtx, key));
	}

	@Override
	public Collection<String> getScoreHolderMultiple(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException {
		return ArgumentScoreholder.b(cmdCtx, key);
	}

	@Override
	public String getScoreHolderSingle(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException {
		return ArgumentScoreholder.a(cmdCtx, key);
	}

	@Override
	public BukkitCommandSender<? extends CommandSender> getSenderForCommand(CommandContext<CommandListenerWrapper> cmdCtx, boolean isNative) {
		CommandListenerWrapper clw = cmdCtx.getSource();

		CommandSender sender = clw.getBukkitSender();
		if (sender == null) {
			// Sender CANNOT be null. This can occur when using a remote console
			// sender. You can access it directly using this.<MinecraftServer>getMinecraftServer().remoteConsole
			// however this may also be null, so delegate to the next most-meaningful sender.
			sender = Bukkit.getConsoleSender();
		}

		Entity proxyEntity = clw.getEntity();
		CommandSender proxy = proxyEntity == null ? null : proxyEntity.getBukkitEntity();
		if (isNative || (proxy != null && !sender.equals(proxy))) {
			if (proxy == null) {
				proxy = sender;
			}
			return new BukkitNativeProxyCommandSender(new NativeProxyCommandSender_1_16_R3(clw, sender, proxy));
		} else {
			return wrapCommandSender(sender);
		}
	}

	@Override
	public NativeProxyCommandSender createNativeProxyCommandSender(CommandSender caller, CommandSender callee, Location location, World world) {
		if (callee == null) callee = caller;

		// Most parameters default to what is defined by the caller
		CommandListenerWrapper clw = getBrigadierSourceFromCommandSender(wrapCommandSender(caller));

		// Position and rotation may be overridden by the Location
		if (location != null) {
			clw = clw
				.a(new Vec3D(location.getX(), location.getY(), location.getZ()))
				.a(new Vec2F(location.getPitch(), location.getYaw()));
		}

		// WorldServer may be overridden by the World
		if (world == null && location != null) {
			world = location.getWorld();
		}
		if (world != null) {
			clw = clw.a(((CraftWorld) world).getHandle());
		}

		// The proxied sender can only be an Entity
		if (callee instanceof org.bukkit.entity.Entity e) {
			clw = clw.a(((CraftEntity) e).getHandle());
		}

		return new NativeProxyCommandSender_1_16_R3(clw, caller, callee);
	}

	@Override
	public SimpleCommandMap getSimpleCommandMap() {
		return ((CraftServer) Bukkit.getServer()).getCommandMap();
	}

	@Differs(from = "1.16.2", by = "Use of CraftSound.getBukkit()")
	@Override
	public final Object getSound(CommandContext<CommandListenerWrapper> cmdCtx, String key, ArgumentSubType subType) {
		final MinecraftKey soundResource = ArgumentMinecraftKeyRegistered.e(cmdCtx, key);
		return switch(subType) {
			case SOUND_SOUND -> {
				final SoundEffect soundEffect = IRegistry.SOUND_EVENT.get(soundResource);
				yield soundEffect == null ? null : CraftSound.getBukkit(soundEffect);
			}
			case SOUND_NAMESPACEDKEY -> fromMinecraftKey(soundResource);
			default -> throw new IllegalArgumentException("Unexpected value: " + subType);
		};
	}

	@Override
	public SuggestionProvider<CommandListenerWrapper> getSuggestionProvider(SuggestionProviders provider) {
		return switch (provider) {
			case FUNCTION -> (context, builder) -> {
				CustomFunctionData functionData = this.<MinecraftServer>getMinecraftServer().getFunctionData();
				ICompletionProvider.a(functionData.g(), builder, "#");
				return ICompletionProvider.a(functionData.f(), builder);
			};
			case RECIPES -> CompletionProviders.b;
			case SOUNDS -> CompletionProviders.c;
			case ADVANCEMENTS -> (cmdCtx, builder) -> ICompletionProvider.a(this.<MinecraftServer>getMinecraftServer().getAdvancementData().getAdvancements().stream().map(Advancement::getName), builder);
			case LOOT_TABLES -> (cmdCtx, builder) -> ICompletionProvider.a(this.<MinecraftServer>getMinecraftServer().getLootTableRegistry().a(), builder);
			case BIOMES -> CompletionProviders.d;
			case ENTITIES -> CompletionProviders.e;
			case POTION_EFFECTS -> (context, builder) -> ICompletionProvider.a(IRegistry.MOB_EFFECT.keySet(), builder);
			default -> (context, builder) -> Suggestions.empty();
		};
	}

	@Override
	public SimpleFunctionWrapper[] getTag(NamespacedKey key) {
		List<CustomFunction> customFunctions = new ArrayList<>(this.<MinecraftServer>getMinecraftServer().getFunctionData().b(new MinecraftKey(key.getNamespace(), key.getKey())).getTagged());
		SimpleFunctionWrapper[] result = new SimpleFunctionWrapper[customFunctions.size()];
		for (int i = 0, size = customFunctions.size(); i < size; i++) {
			result[i] = convertFunction(customFunctions.get(i));
		}
		return result;
	}

	@Override
	public Set<NamespacedKey> getTags() {
		Set<NamespacedKey> functions = new HashSet<>();
		for (MinecraftKey key : this.<MinecraftServer>getMinecraftServer().getFunctionData().g()) {
			functions.add(fromMinecraftKey(key));
		}
		return functions;
	}

	@Override
	public Team getTeam(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException {
		String teamName = ArgumentScoreboardTeam.a(cmdCtx, key).getName();
		return Bukkit.getScoreboardManager().getMainScoreboard().getTeam(teamName);
	}

	@Override
	public int getTime(CommandContext<CommandListenerWrapper> cmdCtx, String key) {
		return cmdCtx.getArgument(key, Integer.class);
	}

	@Override
	public UUID getUUID(CommandContext<CommandListenerWrapper> cmdCtx, String key) {
		return ArgumentUUID.a(cmdCtx, key);
	}

	@Override
	public World getWorldForCSS(CommandListenerWrapper clw) {
		return (clw.getWorld() == null) ? null : clw.getWorld().getWorld();
	}

	@Differs(from = "1.16.2", by = "CustomFunctionManager.g -> CustomFunctionManager.h")
	@Override
	public void reloadDataPacks() {
		CommandAPI.logNormal("Reloading datapacks...");

		// Get previously declared recipes to be re-registered later
		Iterator<Recipe> recipes = Bukkit.recipeIterator();

		// Update the commandDispatcher with the current server's commandDispatcher
		DataPackResources datapackResources = this.<MinecraftServer>getMinecraftServer().dataPackResources;
		datapackResources.commandDispatcher = this.<MinecraftServer>getMinecraftServer().getCommandDispatcher();

		// Update the CustomFunctionManager for the datapackResources which now has the new commandDispatcher
		try {
			customFunctionManagerBrigadierDispatcher.set(datapackResources.a(), getBrigadierDispatcher());
		} catch (IllegalAccessException ignored) {
			// Shouldn't happen, CommandAPIHandler#getField makes it accessible
		}

		// Construct the new CompletableFuture that now uses our updated
		// datapackResources
		CompletableFuture<?> unitCompletableFuture = dataPackResources
				.get(datapackResources).a(SystemUtils.f(), Runnable::run,
						this.<MinecraftServer>getMinecraftServer().getResourcePackRepository().f(), CompletableFuture.completedFuture(null));

		CompletableFuture<DataPackResources> completablefuture = unitCompletableFuture
				.whenComplete((Object u, Throwable t) -> {
					if (t != null) {
						datapackResources.close();
					}
				}).thenApply((Object u) -> datapackResources);

		// Run the completableFuture and bind tags
		try {
			completablefuture.get().i();

			// Register recipes again because reloading datapacks
			// removes all non-vanilla recipes
			registerBukkitRecipesSafely(recipes);

			CommandAPI.logNormal("Finished reloading datapacks");
		} catch (Exception e) {
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			e.printStackTrace(printWriter);

			CommandAPI.logError(
					"Failed to load datapacks, can't proceed with normal server load procedure. Try fixing your datapacks?\n"
							+ stringWriter.toString());
		}
	}

	@Override
	public Message generateMessageFromJson(String json) {
		return ChatSerializer.a(json);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getMinecraftServer() {
		if (Bukkit.getServer() instanceof CraftServer server) {
			return (T) server.getServer();
		} else {
			return null;
		}
	}

	@Override
	public CommandRegistrationStrategy<CommandListenerWrapper> createCommandRegistrationStrategy() {
		return new SpigotCommandRegistration<>(
			this.<MinecraftServer>getMinecraftServer().vanillaCommandDispatcher.a(),
			(SimpleCommandMap) getPaper().getCommandMap(),
			() -> this.<MinecraftServer>getMinecraftServer().getCommandDispatcher().a(),
			command -> command instanceof VanillaCommandWrapper,
			node -> new VanillaCommandWrapper(this.<MinecraftServer>getMinecraftServer().vanillaCommandDispatcher, node),
			node -> node.getCommand() instanceof BukkitCommandWrapper
		);
	}
}
