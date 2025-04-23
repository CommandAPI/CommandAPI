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

import com.google.common.io.Files;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPIHandler;
import dev.jorel.commandapi.arguments.ArgumentSubType;
import dev.jorel.commandapi.commandsenders.BukkitCommandSender;
import dev.jorel.commandapi.wrappers.Location2D;
import dev.jorel.commandapi.wrappers.MathOperation;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.AngleArgument;
import net.minecraft.commands.arguments.ColorArgument;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.commands.arguments.ObjectiveCriteriaArgument;
import net.minecraft.commands.arguments.OperationArgument;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.commands.arguments.RangeArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.ScoreboardSlotArgument;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.SwizzleArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Common NMS code To ensure that this code actually works across all versions
 * of Minecraft that this is supposed to support (1.20+), you should be
 * compiling this code against all of the declared Maven profiles specified in
 * this submodule's pom.xml file, by running the following command:
 * ./compileNMSCommon
 * <p>
 * In case that fails, the method that causes the failure should be implemented in all
 * NMS classes to maintain an error-free NMS_Common module that is applicable to all
 * versions.
 */
public abstract class NMS_Common extends CommandAPIBukkit<CommandSourceStack> {

	public final NamespacedKey fromResourceLocation(ResourceLocation key) {
		return NamespacedKey.fromString(key.getNamespace() + ":" + key.getPath());
	}

	protected abstract CommandBuildContext getCommandBuildContext();

	@Override
	public final ArgumentType<?> _ArgumentAngle() {
		return AngleArgument.angle();
	}

	@Override
	public final ArgumentType<?> _ArgumentAxis() {
		return SwizzleArgument.swizzle();
	}

	@Override
	public final ArgumentType<?> _ArgumentBlockPredicate() {
		return BlockPredicateArgument.blockPredicate(getCommandBuildContext());
	}

	@Override
	public final ArgumentType<?> _ArgumentBlockState() {
		return BlockStateArgument.block(getCommandBuildContext());
	}

	@Override
	public final ArgumentType<?> _ArgumentChat() {
		return MessageArgument.message();
	}

	@Override
	public final ArgumentType<?> _ArgumentChatFormat() {
		return ColorArgument.color();
	}

	@Override
	public final ArgumentType<?> _ArgumentDimension() {
		return DimensionArgument.dimension();
	}

	@Override
	public final ArgumentType<?> _ArgumentEntity(ArgumentSubType subType) {
		return switch (subType) {
			case ENTITYSELECTOR_MANY_ENTITIES -> EntityArgument.entities();
			case ENTITYSELECTOR_MANY_PLAYERS -> EntityArgument.players();
			case ENTITYSELECTOR_ONE_ENTITY -> EntityArgument.entity();
			case ENTITYSELECTOR_ONE_PLAYER -> EntityArgument.player();
			default -> throw new IllegalArgumentException("Unexpected value: " + subType);
		};
	}

	@Override
	public final ArgumentType<?> _ArgumentFloatRange() {
		return RangeArgument.floatRange();
	}

	@Override
	public final ArgumentType<?> _ArgumentIntRange() {
		return RangeArgument.intRange();
	}

	@Override
	public final ArgumentType<?> _ArgumentItemPredicate() {
		return ItemPredicateArgument.itemPredicate(getCommandBuildContext());
	}

	@Override
	public final ArgumentType<?> _ArgumentItemStack() {
		return ItemArgument.item(getCommandBuildContext());
	}

	@Override
	public final ArgumentType<?> _ArgumentMathOperation() {
		return OperationArgument.operation();
	}

	@Override
	public final ArgumentType<?> _ArgumentMinecraftKeyRegistered() {
		return ResourceLocationArgument.id();
	}

	@Override
	public final ArgumentType<?> _ArgumentNBTCompound() {
		return CompoundTagArgument.compoundTag();
	}

	@Override
	public final ArgumentType<?> _ArgumentParticle() {
		return ParticleArgument.particle(getCommandBuildContext());
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
	public final String convert(PotionEffectType potion) {
		return potion.getName().toLowerCase(Locale.ENGLISH);
	}

	@Override
	public final String convert(Sound sound) {
		return sound.getKey().toString();
	}

	@Override
	public final void createDispatcherFile(File file, CommandDispatcher<CommandSourceStack> dispatcher) throws IOException {
		Files.asCharSink(file, StandardCharsets.UTF_8).write(new GsonBuilder().setPrettyPrinting().create()
			.toJson(ArgumentUtils.serializeNodeToJson(dispatcher, dispatcher.getRoot())));
	}

	@Override
	public final float getAngle(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return AngleArgument.getAngle(cmdCtx, key);
	}

	@Override
	public final EnumSet<Axis> getAxis(CommandContext<CommandSourceStack> cmdCtx, String key) {
		EnumSet<Axis> set = EnumSet.noneOf(Axis.class);
		EnumSet<Direction.Axis> parsedEnumSet = SwizzleArgument.getSwizzle(cmdCtx, key);
		for (Direction.Axis element : parsedEnumSet) {
			set.add(switch (element) {
				case X -> Axis.X;
				case Y -> Axis.Y;
				case Z -> Axis.Z;
			});
		}
		return set;
	}

	@Override
	public final Predicate<Block> getBlockPredicate(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		Predicate<BlockInWorld> predicate = BlockPredicateArgument.getBlockPredicate(cmdCtx, key);
		return (Block block) -> predicate.test(new BlockInWorld(cmdCtx.getSource().getLevel(),
			new BlockPos(block.getX(), block.getY(), block.getZ()), true));
	}

	@Override
	public final ChatColor getChatColor(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return ChatColor.getByChar(ColorArgument.getColor(cmdCtx, key).getChar());
	}

	@Override
	public final BukkitCommandSender<? extends CommandSender> getCommandSenderFromCommandSource(CommandSourceStack css) {
		try {
			return wrapCommandSender(css.getBukkitSender());
		} catch (UnsupportedOperationException e) {
			return null;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public final EntityType getEntityType(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return EntityType.fromName(net.minecraft.world.entity.EntityType.getKey(ResourceArgument.getSummonableEntityType(cmdCtx, key).value()).getPath());
	}

	@Override
	public final Location2D getLocation2DBlock(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		ColumnPos blockPos = ColumnPosArgument.getColumnPos(cmdCtx, key);
		return new Location2D(getWorldForCSS(cmdCtx.getSource()), blockPos.x(), blockPos.z());
	}

	@Override
	public final Location getLocationBlock(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		BlockPos blockPos = BlockPosArgument.getSpawnablePos(cmdCtx, key);
		return new Location(getWorldForCSS(cmdCtx.getSource()), blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}

	@Override
	public final Location getLocationPrecise(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		Vec3 vecPos = Vec3Argument.getCoordinates(cmdCtx, key).getPosition(cmdCtx.getSource());
		return new Location(getWorldForCSS(cmdCtx.getSource()), vecPos.x(), vecPos.y(), vecPos.z());
	}

	@Override
	public final MathOperation getMathOperation(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		// We run this to ensure the argument exists/parses properly
		OperationArgument.getOperation(cmdCtx, key);
		return MathOperation.fromString(CommandAPIHandler.getRawArgumentInput(cmdCtx, key));
	}

	@Override
	public final <NBTContainer> Object getNBTCompound(CommandContext<CommandSourceStack> cmdCtx, String key,
		Function<Object, NBTContainer> nbtContainerConstructor) {
		return nbtContainerConstructor.apply(CompoundTagArgument.getCompoundTag(cmdCtx, key));
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
	public final Player getPlayer(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		Player target = Bukkit.getPlayer(GameProfileArgument.getGameProfiles(cmdCtx, key).iterator().next().getId());
		if (target == null) {
			throw GameProfileArgument.ERROR_UNKNOWN_PLAYER.create();
		} else {
			return target;
		}
	}

	@Override
	public final int getTime(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return cmdCtx.getArgument(key, Integer.class);
	}

	@Override
	public final UUID getUUID(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return UuidArgument.getUuid(cmdCtx, key);
	}
}
