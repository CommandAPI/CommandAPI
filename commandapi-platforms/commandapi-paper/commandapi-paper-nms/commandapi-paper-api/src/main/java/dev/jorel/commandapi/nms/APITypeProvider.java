package dev.jorel.commandapi.nms;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.jorel.commandapi.BukkitTooltip;
import dev.jorel.commandapi.CommandRegistrationStrategy;
import dev.jorel.commandapi.arguments.ArgumentSubType;
import dev.jorel.commandapi.arguments.SuggestionProviders;
import dev.jorel.commandapi.arguments.parser.EntitySelectorParser;
import dev.jorel.commandapi.arguments.parser.RegistryParser;
import dev.jorel.commandapi.arguments.parser.function.ThrowingBiFunction;
import dev.jorel.commandapi.arguments.parser.function.ThrowingSupplier;
import dev.jorel.commandapi.commandsenders.AbstractCommandSender;
import dev.jorel.commandapi.commandsenders.BukkitCommandSender;
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
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.SignedMessageResolver;
import io.papermc.paper.command.brigadier.argument.predicate.ItemStackPredicate;
import io.papermc.paper.command.brigadier.argument.range.IntegerRangeProvider;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.RotationResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.FinePosition;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.help.HelpTopic;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.loot.LootTable;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class APITypeProvider extends BundledNMS<CommandSourceStack> {

	private final PaperNMS<?> paperNMS;

	public APITypeProvider(PaperNMS<?> paperNMS) {
		this.paperNMS = paperNMS;
		bukkitNMS();
	}

	private ArgumentType<?> getArgumentType(ThrowingSupplier<ArgumentType<?>> paper, Supplier<ArgumentType<?>> nms) {
		try {
			return paper.get();
		} catch (Throwable t) {
			return nms.get();
		}
	}

	private ArgumentType<?> getArgumentType(Supplier<ArgumentType<?>> nms) {
		return nms.get();
	}

	private <T> T parseT(
		CommandContext<CommandSourceStack> cmdCtx,
		String key,
		ThrowingBiFunction<CommandContext<CommandSourceStack>, String, T, Exception> api,
		ThrowingBiFunction<CommandContext<CommandSourceStack>, String, T, CommandSyntaxException> nms) throws CommandSyntaxException {
		try {
			return api.apply(cmdCtx, key);
		} catch (Throwable t) {
			return nms.apply(cmdCtx, key);
		}
	}

	private <T> T parse(
		CommandContext<CommandSourceStack> cmdCtx,
		String key,
		BiFunction<CommandContext<CommandSourceStack>, String, T> api,
		BiFunction<CommandContext<CommandSourceStack>, String, T> nms) {
		try {
			return api.apply(cmdCtx, key);
		} catch (Throwable t) {
			return nms.apply(cmdCtx, key);
		}
	}

	private <T> T parseT(
		CommandContext<CommandSourceStack> cmdCtx,
		String key,
		ThrowingBiFunction<CommandContext<CommandSourceStack>, String, T, CommandSyntaxException> nms) throws CommandSyntaxException {
		return nms.apply(cmdCtx, key);
	}

	private <T> T parse(
		CommandContext<CommandSourceStack> cmdCtx,
		String key,
		BiFunction<CommandContext<CommandSourceStack>, String, T> nms) {
		return nms.apply(cmdCtx, key);
	}

	@Override
	public ArgumentType<?> _ArgumentAdvancement() {
		return getArgumentType(
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentAdvancement()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentAngle() {
		return getArgumentType(
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentAngle()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentAxis() {
		return getArgumentType(
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentAxis()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentBlockPredicate() {
		return getArgumentType(
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentBlockPredicate()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentBlockState() {
		return getArgumentType(
			() -> ArgumentTypes.blockState(),
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentBlockState()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentChat() {
		return getArgumentType(
			() -> ArgumentTypes.signedMessage(),
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentChat()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentChatComponent() {
		return getArgumentType(
			() -> ArgumentTypes.component(),
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentChatComponent()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentChatFormat() {
		return getArgumentType(
			() -> ArgumentTypes.namedColor(),
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentChatFormat()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentDimension() {
		return getArgumentType(
			() -> ArgumentTypes.world(),
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentDimension()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentEnchantment() {
		return getArgumentType(
			() -> ArgumentTypes.resource(RegistryKey.ENCHANTMENT),
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentEnchantment()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentEntity(ArgumentSubType subType) {
		return getArgumentType(
			() -> switch (subType) {
				case ENTITYSELECTOR_ONE_ENTITY -> ArgumentTypes.entity();
				case ENTITYSELECTOR_MANY_ENTITIES -> ArgumentTypes.entities();
				case ENTITYSELECTOR_ONE_PLAYER -> ArgumentTypes.player();
				case ENTITYSELECTOR_MANY_PLAYERS -> ArgumentTypes.players();
				default -> throw new Exception(); // Doesn't matter too much, it'll get to the right exception anyway
			},
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentEntity(subType)
		);
	}

	@Override
	public ArgumentType<?> _ArgumentEntitySummon() {
		return getArgumentType(
			() -> ArgumentTypes.resource(RegistryKey.ENTITY_TYPE),
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentEntitySummon()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentFloatRange() {
		return getArgumentType(
			() -> ArgumentTypes.doubleRange(),
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentFloatRange()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentIntRange() {
		return getArgumentType(
			() -> ArgumentTypes.integerRange(),
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentIntRange()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentItemPredicate() {
		return getArgumentType(
			() -> ArgumentTypes.itemPredicate(),
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentItemPredicate()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentItemStack() {
		return getArgumentType(
			() -> ArgumentTypes.itemStack(),
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentItemStack()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentMathOperation() {
		return getArgumentType(
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentMathOperation()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentMinecraftKeyRegistered() {
		return getArgumentType(
			() -> ArgumentTypes.namespacedKey(),
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentMinecraftKeyRegistered()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentMobEffect() {
		return getArgumentType(
			() -> ArgumentTypes.resource(RegistryKey.MOB_EFFECT),
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentMobEffect()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentNBTCompound() {
		return getArgumentType(
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentNBTCompound()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentParticle() {
		return getArgumentType(
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentParticle()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentPosition() {
		return getArgumentType(
			() -> ArgumentTypes.blockPosition(),
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentPosition()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentPosition2D() {
		return getArgumentType(
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentPosition2D()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentProfile() {
		return getArgumentType(
			() -> ArgumentTypes.playerProfiles(),
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentProfile()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentRecipe() {
		return getArgumentType(
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentRecipe()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentRotation() {
		return getArgumentType(
			() -> ArgumentTypes.rotation(),
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentRotation()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentScoreboardCriteria() {
		return getArgumentType(
			() -> ArgumentTypes.objectiveCriteria(),
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentScoreboardCriteria()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentScoreboardObjective() {
		return getArgumentType(
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentScoreboardObjective()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentScoreboardSlot() {
		return getArgumentType(
			() -> ArgumentTypes.scoreboardDisplaySlot(),
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentScoreboardSlot()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentScoreboardTeam() {
		return getArgumentType(
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentScoreboardTeam()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentScoreholder(ArgumentSubType subType) {
		return getArgumentType(
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentScoreholder(subType)
		);
	}

	@Override
	public ArgumentType<?> _ArgumentTag() {
		return getArgumentType(
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentTag()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentTime() {
		return getArgumentType(
			() -> ArgumentTypes.time(),
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentTime()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentUUID() {
		return getArgumentType(
			() -> ArgumentTypes.uuid(),
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentUUID()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentVec2(boolean centerPosition) {
		return getArgumentType(
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentVec2(centerPosition)
		);
	}

	@Override
	public ArgumentType<?> _ArgumentVec3(boolean centerPosition) {
		return getArgumentType(
			() -> ArgumentTypes.finePosition(centerPosition),
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentVec3(centerPosition)
		);
	}

	@Override
	public ArgumentType<?> _ArgumentSyntheticBiome() {
		return getArgumentType(
			() -> ArgumentTypes.resource(RegistryKey.BIOME),
			() -> paperNMS.<CommandSourceStack>bukkitNMS()._ArgumentSyntheticBiome()
		);
	}

	@Override
	public String[] compatibleVersions() {
		return paperNMS.<CommandSourceStack>bukkitNMS().compatibleVersions();
	}

	@Override
	public String convert(ItemStack is) {
		return paperNMS.<CommandSourceStack>bukkitNMS().convert(is);
	}

	@Override
	public String convert(ParticleData<?> particle) {
		return paperNMS.<CommandSourceStack>bukkitNMS().convert(particle);
	}

	@Override
	public String convert(PotionEffectType potion) {
		return paperNMS.<CommandSourceStack>bukkitNMS().convert(potion);
	}

	@Override
	public String convert(Sound sound) {
		return paperNMS.<CommandSourceStack>bukkitNMS().convert(sound);
	}

	@Override
	public Advancement getAdvancement(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getAdvancement(ctx, name)
		);
	}

	@Override
	public float getAngle(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return parse(cmdCtx, key,
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getAngle(ctx, name)
		);
	}

	@Override
	public EnumSet<Axis> getAxis(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return parse(cmdCtx, key,
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getAxis(ctx, name)
		);
	}

	@Override
	public RegistryParser<Biome> getBiome(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return parseT(
			cmdCtx, key,
			(ctx, name) -> new RegistryParser<>(
				() -> ctx.getArgument(name, Biome.class),
				() -> ctx.getArgument(name, Biome.class).getKey()
			),
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getBiome(ctx, name)
		);
	}

	@Override
	public Predicate<Block> getBlockPredicate(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getBlockPredicate(ctx, name)
		);
	}

	@Override
	public BlockState getBlockState(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return parse(cmdCtx, key,
			(ctx, name) -> ctx.getArgument(name, BlockState.class),
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getBlockState(ctx, name)
		);
	}

	@Override
	public World getDimension(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> ctx.getArgument(name, World.class),
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getDimension(ctx, name)
		);
	}

	@Override
	public Enchantment getEnchantment(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> ctx.getArgument(name, Enchantment.class),
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getEnchantment(ctx, name)
		);
	}

	@Override
	public EntitySelectorParser getEntitySelector(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return parse(cmdCtx, key,
			(ctx, name) -> new EntitySelectorParser(
				() -> ctx.getArgument(name, PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst(),
				() -> ctx.getArgument(name, EntitySelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst(),
				(allowEmpty) -> ctx.getArgument(name, PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()),
				(allowEmpty) -> ctx.getArgument(name, EntitySelectorArgumentResolver.class).resolve(ctx.getSource())
			),
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getEntitySelector(ctx, name)
		);
	}

	@Override
	public EntityType getEntityType(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> ctx.getArgument(name, EntityType.class),
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getEntityType(ctx, name)
		);
	}

	@Override
	public FloatRange getFloatRange(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return parse(cmdCtx, key,
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getFloatRange(ctx, name)
		);
	}

	@Override
	public FunctionWrapper[] getFunction(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getFunction(ctx, name)
		);
	}

	@Override
	public SimpleFunctionWrapper getFunction(NamespacedKey key) {
		return paperNMS.<CommandSourceStack>bukkitNMS().getFunction(key);
	}

	@Override
	public Set<NamespacedKey> getFunctions() {
		return paperNMS.<CommandSourceStack>bukkitNMS().getFunctions();
	}

	@Override
	public IntegerRange getIntRange(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return parse(cmdCtx, key,
			(ctx, name) -> {
				IntegerRangeProvider rangeProvider = ctx.getArgument(name, IntegerRangeProvider.class);
				final int low = rangeProvider.range().hasLowerBound() ? rangeProvider.range().lowerEndpoint() : Integer.MIN_VALUE;
				final int high = rangeProvider.range().hasUpperBound() ? rangeProvider.range().upperEndpoint() : Integer.MAX_VALUE;
				return new IntegerRange(low, high);
			},
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getIntRange(ctx, name)
		);
	}

	@Override
	public ItemStack getItemStack(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> ctx.getArgument(name, ItemStack.class),
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getItemStack(ctx, name)
		);
	}

	@Override
	public Predicate<ItemStack> getItemStackPredicate(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> ctx.getArgument(name, ItemStackPredicate.class),
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getItemStackPredicate(ctx, name)
		);
	}

	@Override
	public Location2D getLocation2DBlock(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getLocation2DBlock(ctx, name)
		);
	}

	@Override
	public Location2D getLocation2DPrecise(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getLocation2DPrecise(ctx, name)
		);
	}

	@Override
	public Location getLocationBlock(CommandContext<CommandSourceStack> cmdCtx, String str) throws CommandSyntaxException {
		return parseT(cmdCtx, str,
			(ctx, name) -> {
				BlockPosition blockPosition = ctx.getArgument(name, BlockPositionResolver.class).resolve(ctx.getSource());
				return new Location(getWorldForCSS(ctx.getSource()), blockPosition.blockX(), blockPosition.blockY(), blockPosition.blockZ());
			},
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getLocationBlock(ctx, name)
		);
	}

	@Override
	public Location getLocationPrecise(CommandContext<CommandSourceStack> cmdCtx, String str) throws CommandSyntaxException {
		return parseT(cmdCtx, str,
			(ctx, name) -> {
				FinePosition finePosition = ctx.getArgument(name, FinePositionResolver.class).resolve(ctx.getSource());
				return new Location(getWorldForCSS(ctx.getSource()), finePosition.x(), finePosition.y(), finePosition.z());
			},
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getLocationPrecise(ctx, name)
		);
	}

	@Override
	public LootTable getLootTable(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return parse(cmdCtx, key,
			(ctx, name) -> Bukkit.getLootTable(ctx.getArgument(name, NamespacedKey.class)),
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getLootTable(ctx, name)
		);
	}

	@Override
	public MathOperation getMathOperation(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getMathOperation(ctx, name)
		);
	}

	@Override
	public NamespacedKey getMinecraftKey(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return parse(cmdCtx, key,
			(ctx, name) -> ctx.getArgument(name, NamespacedKey.class),
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getMinecraftKey(ctx, name)
		);
	}

	@Override
	public <NBTContainer> Object getNBTCompound(CommandContext<CommandSourceStack> cmdCtx, String key, Function<Object, NBTContainer> nbtContainerConstructor) {
		return parse(cmdCtx, key,
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getNBTCompound(ctx, name, nbtContainerConstructor)
		);
	}

	@Override
	public Objective getObjective(CommandContext<CommandSourceStack> cmdCtx, String key) throws IllegalArgumentException, CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getObjective(ctx, name)
		);
	}

	@Override
	public String getObjectiveCriteria(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return parse(cmdCtx, key,
			(ctx, name) -> ctx.getArgument(name, Criteria.class).getName(),
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getObjectiveCriteria(ctx, name)
		);
	}

	@Override
	public ParticleData<?> getParticle(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return parse(cmdCtx, key,
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getParticle(ctx, name)
		);
	}

	@Override
	public OfflinePlayer getOfflinePlayer(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> Bukkit.getOfflinePlayer(getIdFromProfile(ctx, name)),
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getOfflinePlayer(ctx, name)
		);
	}

	private UUID getIdFromProfile(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
		Collection<PlayerProfile> playerProfiles = ctx.getArgument(name, PlayerProfileListResolver.class).resolve((CommandSourceStack) ctx.getSource());
		UUID id = playerProfiles.iterator().next().getId();
		if (id == null) {
			throw new SimpleCommandExceptionType(BukkitTooltip.messageFromAdventureComponent(Component.translatable("argument.player.unknown"))).create();
		}
		return id;
	}

	@Override
	public RegistryParser<PotionEffectType> getPotionEffect(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return parse(cmdCtx, key,
			(ctx, name) -> new RegistryParser<>(
				() -> ctx.getArgument(name, PotionEffectType.class),
				() -> ctx.getArgument(name, PotionEffectType.class).getKey()
			),
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getPotionEffect(ctx, name)
		);
	}

	@Override
	public Recipe getRecipe(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getRecipe(ctx, name)
		);
	}

	@Override
	public Rotation getRotation(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> {
				io.papermc.paper.math.Rotation rotation = ctx.getArgument(name, RotationResolver.class).resolve((CommandSourceStack) ctx.getSource());
				return new Rotation(rotation.yaw(), rotation.pitch());
			},
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getRotation(ctx, name)
		);
	}

	@Override
	public ScoreboardSlot getScoreboardSlot(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return parse(cmdCtx, key,
			(ctx, name) -> {
				DisplaySlot displaySlot = ctx.getArgument(name, DisplaySlot.class);
				return ScoreboardSlot.of(displaySlot);
			},
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getScoreboardSlot(ctx, name)
		);
	}

	@Override
	public Collection<String> getScoreHolderMultiple(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getScoreHolderMultiple(ctx, name)
		);
	}

	@Override
	public String getScoreHolderSingle(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getScoreHolderSingle(ctx, name)
		);
	}

	@Override
	public Team getTeam(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getTeam(ctx, name)
		);
	}

	@Override
	public int getTime(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return parse(cmdCtx, key,
			(ctx, name) -> ctx.getArgument(name, Integer.class),
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getTime(ctx, name)
		);
	}

	@Override
	public UUID getUUID(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return parse(cmdCtx, key,
			(ctx, name) -> ctx.getArgument(name, UUID.class),
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getUUID(ctx, name)
		);
	}

	@Override
	public World getWorldForCSS(CommandSourceStack clw) {
		return paperNMS.<CommandSourceStack>bukkitNMS().getWorldForCSS(clw);
	}

	@Override
	public SimpleCommandMap getSimpleCommandMap() {
		return paperNMS.<CommandSourceStack>bukkitNMS().getSimpleCommandMap();
	}

	@Override
	public RegistryParser<Sound> getSound(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return parse(cmdCtx, key,
			(ctx, name) -> {
				NamespacedKey namespace = ctx.getArgument(name, NamespacedKey.class);
				return new RegistryParser<>(
					() -> Registry.SOUND_EVENT.get(namespace),
					() -> namespace
				);
			},
			(ctx, name) -> paperNMS.<CommandSourceStack>bukkitNMS().getSound(ctx, name)
		);
	}

	@Override
	public SuggestionProvider<CommandSourceStack> getSuggestionProvider(SuggestionProviders provider) {
		return paperNMS.<CommandSourceStack>bukkitNMS().getSuggestionProvider(provider);
	}

	@Override
	public SimpleFunctionWrapper[] getTag(NamespacedKey key) {
		return paperNMS.<CommandSourceStack>bukkitNMS().getTag(key);
	}

	@Override
	public Set<NamespacedKey> getTags() {
		return paperNMS.<CommandSourceStack>bukkitNMS().getTags();
	}

	@Override
	public void reloadDataPacks() {
		paperNMS.<CommandSourceStack>bukkitNMS().reloadDataPacks();
	}

	@Override
	public HelpTopic generateHelpTopic(String commandName, String shortDescription, String fullDescription, String permission) {
		return paperNMS.<CommandSourceStack>bukkitNMS().generateHelpTopic(commandName, shortDescription, fullDescription, permission);
	}

	@Override
	public Map<String, HelpTopic> getHelpMap() {
		return paperNMS.<CommandSourceStack>bukkitNMS().getHelpMap();
	}

	@Override
	public Message generateMessageFromJson(String json) {
		return paperNMS.<CommandSourceStack>bukkitNMS().generateMessageFromJson(json);
	}

	@Override
	public BukkitCommandSender<? extends CommandSender> getSenderForCommand(CommandContext<CommandSourceStack> cmdCtx, boolean forceNative) {
		return paperNMS.<CommandSourceStack>bukkitNMS().getSenderForCommand(cmdCtx, forceNative);
	}

	@Override
	public <Source> BukkitCommandSender<? extends CommandSender> getCommandSenderFromCommandSource(Source css) {
		return paperNMS.<CommandSourceStack>bukkitNMS().getCommandSenderFromCommandSource(css);
	}

	@Override
	public CommandSourceStack getBrigadierSourceFromCommandSender(AbstractCommandSender<? extends CommandSender> sender) {
		return paperNMS.<CommandSourceStack>bukkitNMS().getBrigadierSourceFromCommandSender(sender);
	}

	@Override
	public void createDispatcherFile(File file, CommandDispatcher<CommandSourceStack> brigadierDispatcher) throws IOException {
		paperNMS.<CommandSourceStack>bukkitNMS().createDispatcherFile(file, brigadierDispatcher);
	}

	@Override
	public <T> T getMinecraftServer() {
		return paperNMS.<CommandSourceStack>bukkitNMS().getMinecraftServer();
	}

	@Override
	public NativeProxyCommandSender createNativeProxyCommandSender(CommandSender caller, CommandSender callee, Location location, World world) {
		return paperNMS.<CommandSourceStack>bukkitNMS().createNativeProxyCommandSender(caller, callee, location, world);
	}

	@Override
	public SignedMessage getChat(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> {
				CompletableFuture<SignedMessage> signedMessageResolver = ctx.getArgument(name, SignedMessageResolver.class).resolveSignedMessage(name, ctx);
				return signedMessageResolver.join();
			},
			((PaperNMS<CommandSourceStack>) paperNMS)::getChat
		);
	}

	@Override
	public NamedTextColor getChatColor(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return parse(cmdCtx, key,
			(ctx, name) -> ctx.getArgument(name, NamedTextColor.class),
			((PaperNMS<CommandSourceStack>) paperNMS)::getChatColor
		);
	}

	@Override
	public Component getChatComponent(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> ctx.getArgument(name, Component.class),
			((PaperNMS<CommandSourceStack>) paperNMS)::getChatComponent
		);
	}

	@Override
	public List<PlayerProfile> getProfile(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> {
				PlayerProfileListResolver profileListResolver = ctx.getArgument(name, PlayerProfileListResolver.class);
				return new ArrayList<>(profileListResolver.resolve(ctx.getSource()));
			},
			(ctx, name) -> ((PaperNMS<CommandSourceStack>) paperNMS).getProfile(ctx, name)
		);
	}

	@Override
	public <Source> NMS<Source> bukkitNMS() {
		return paperNMS.bukkitNMS();
	}

	@Override
	public CommandRegistrationStrategy<CommandSourceStack> createCommandRegistrationStrategy() {
		return ((PaperNMS<CommandSourceStack>) paperNMS).createCommandRegistrationStrategy();
	}

	@Override
	public boolean isDispatcherValid() {
		return paperNMS.isDispatcherValid();
	}

	@Override
	public <Source> LiteralCommandNode<Source> asPluginCommand(LiteralCommandNode<Source> commandNode, String description, List<String> aliases) {
		return paperNMS.asPluginCommand(commandNode, description, aliases);
	}

	@Override
	public <Source> CommandDispatcher<Source> getPaperCommandDispatcher() {
		return paperNMS.getPaperCommandDispatcher();
	}
}
