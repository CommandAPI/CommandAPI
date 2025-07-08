package dev.jorel.commandapi.nms;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
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
import dev.jorel.commandapi.wrappers.DoubleRange;
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
import io.papermc.paper.command.brigadier.argument.range.DoubleRangeProvider;
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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class APITypeProvider extends BundledNMS<CommandSourceStack> {

	private final PaperNMS<CommandSourceStack> paperNMS;

	public APITypeProvider(PaperNMS<?> paperNMS) {
		this.paperNMS = (PaperNMS<CommandSourceStack>) paperNMS;
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
			() -> paperNMS.bukkitNMS()._ArgumentAdvancement()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentAngle() {
		return getArgumentType(
			() -> paperNMS.bukkitNMS()._ArgumentAngle()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentAxis() {
		return getArgumentType(
			() -> paperNMS.bukkitNMS()._ArgumentAxis()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentBlockPredicate() {
		return getArgumentType(
			() -> paperNMS.bukkitNMS()._ArgumentBlockPredicate()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentBlockState() {
		return ArgumentTypes.blockState();
	}

	@Override
	public ArgumentType<?> _ArgumentChat() {
		return ArgumentTypes.signedMessage();
	}

	@Override
	public ArgumentType<?> _ArgumentChatComponent() {
		return ArgumentTypes.component();
	}

	@Override
	public ArgumentType<?> _ArgumentChatFormat() {
		return ArgumentTypes.namedColor();
	}

	@Override
	public ArgumentType<?> _ArgumentDimension() {
		return ArgumentTypes.world();
	}

	@Override
	public ArgumentType<?> _ArgumentEnchantment() {
		return ArgumentTypes.resource(RegistryKey.ENCHANTMENT);
	}

	@Override
	public ArgumentType<?> _ArgumentEntity(ArgumentSubType subType) {
		return switch (subType) {
			case ENTITYSELECTOR_ONE_ENTITY -> ArgumentTypes.entity();
			case ENTITYSELECTOR_MANY_ENTITIES -> ArgumentTypes.entities();
			case ENTITYSELECTOR_ONE_PLAYER -> ArgumentTypes.player();
			case ENTITYSELECTOR_MANY_PLAYERS -> ArgumentTypes.players();
			default -> throw new IllegalArgumentException("Unexpected value: " + subType);
		};
	}

	@Override
	public ArgumentType<?> _ArgumentEntitySummon() {
		return ArgumentTypes.resource(RegistryKey.ENTITY_TYPE);
	}

	@Override
	public ArgumentType<?> _ArgumentFloatRange() {
		return ArgumentTypes.doubleRange();
	}

	@Override
	public ArgumentType<?> _ArgumentIntRange() {
		return ArgumentTypes.integerRange();
	}

	@Override
	public ArgumentType<?> _ArgumentItemPredicate() {
		return ArgumentTypes.itemPredicate();
	}

	@Override
	public ArgumentType<?> _ArgumentItemStack() {
		return ArgumentTypes.itemStack();
	}

	@Override
	public ArgumentType<?> _ArgumentMathOperation() {
		return getArgumentType(
			() -> paperNMS.bukkitNMS()._ArgumentMathOperation()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentMinecraftKeyRegistered() {
		return ArgumentTypes.namespacedKey();
	}

	@Override
	public ArgumentType<?> _ArgumentMobEffect() {
		return ArgumentTypes.resource(RegistryKey.MOB_EFFECT);
	}

	@Override
	public ArgumentType<?> _ArgumentNBTCompound() {
		return getArgumentType(
			() -> paperNMS.bukkitNMS()._ArgumentNBTCompound()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentParticle() {
		return getArgumentType(
			() -> paperNMS.bukkitNMS()._ArgumentParticle()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentPosition() {
		return ArgumentTypes.blockPosition();
	}

	@Override
	public ArgumentType<?> _ArgumentPosition2D() {
		return getArgumentType(
			() -> paperNMS.bukkitNMS()._ArgumentPosition2D()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentProfile() {
		return ArgumentTypes.playerProfiles();
	}

	@Override
	public ArgumentType<?> _ArgumentRecipe() {
		return getArgumentType(
			() -> paperNMS.bukkitNMS()._ArgumentRecipe()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentRotation() {
		return getArgumentType(
			() -> ArgumentTypes.rotation(), // Since 1.21.4
			() -> paperNMS.bukkitNMS()._ArgumentRotation()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentScoreboardCriteria() {
		return ArgumentTypes.objectiveCriteria();
	}

	@Override
	public ArgumentType<?> _ArgumentScoreboardObjective() {
		return getArgumentType(
			() -> paperNMS.bukkitNMS()._ArgumentScoreboardObjective()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentScoreboardSlot() {
		return ArgumentTypes.scoreboardDisplaySlot();
	}

	@Override
	public ArgumentType<?> _ArgumentScoreboardTeam() {
		return getArgumentType(
			() -> paperNMS.bukkitNMS()._ArgumentScoreboardTeam()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentScoreholder(ArgumentSubType subType) {
		return getArgumentType(
			() -> paperNMS.bukkitNMS()._ArgumentScoreholder(subType)
		);
	}

	@Override
	public ArgumentType<?> _ArgumentTag() {
		return getArgumentType(
			() -> paperNMS.bukkitNMS()._ArgumentTag()
		);
	}

	@Override
	public ArgumentType<?> _ArgumentTime() {
		return ArgumentTypes.time();
	}

	@Override
	public ArgumentType<?> _ArgumentUUID() {
		return ArgumentTypes.uuid();
	}

	@Override
	public ArgumentType<?> _ArgumentVec2(boolean centerPosition) {
		return getArgumentType(
			() -> paperNMS.bukkitNMS()._ArgumentVec2(centerPosition)
		);
	}

	@Override
	public ArgumentType<?> _ArgumentVec3(boolean centerPosition) {
		return getArgumentType(
			() -> ArgumentTypes.finePosition(centerPosition), // Since 1.21
			() -> paperNMS.bukkitNMS()._ArgumentVec3(centerPosition)
		);
	}

	@Override
	public ArgumentType<?> _ArgumentSyntheticBiome() {
		return ArgumentTypes.resource(RegistryKey.BIOME);
	}

	@Override
	public String[] compatibleVersions() {
		return paperNMS.bukkitNMS().compatibleVersions();
	}

	@Override
	public String convert(ItemStack is) {
		return paperNMS.bukkitNMS().convert(is);
	}

	@Override
	public String convert(ParticleData<?> particle) {
		return paperNMS.bukkitNMS().convert(particle);
	}

	@Override
	public String convert(PotionEffectType potion) {
		return paperNMS.bukkitNMS().convert(potion);
	}

	@Override
	public String convert(Sound sound) {
		return paperNMS.bukkitNMS().convert(sound);
	}

	@Override
	public Advancement getAdvancement(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> paperNMS.bukkitNMS().getAdvancement(ctx, name)
		);
	}

	@Override
	public float getAngle(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return parse(cmdCtx, key,
			(ctx, name) -> paperNMS.bukkitNMS().getAngle(ctx, name)
		);
	}

	@Override
	public EnumSet<Axis> getAxis(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return parse(cmdCtx, key,
			(ctx, name) -> paperNMS.bukkitNMS().getAxis(ctx, name)
		);
	}

	@Override
	public RegistryParser<Biome> getBiome(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return new RegistryParser<>(
			() -> cmdCtx.getArgument(key, Biome.class),
			() -> cmdCtx.getArgument(key, Biome.class).getKey()
		);
	}

	@Override
	public Predicate<Block> getBlockPredicate(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> paperNMS.bukkitNMS().getBlockPredicate(ctx, name)
		);
	}

	@Override
	public BlockState getBlockState(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return cmdCtx.getArgument(key, BlockState.class);
	}

	@Override
	public World getDimension(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return cmdCtx.getArgument(key, World.class);
	}

	@Override
	public Enchantment getEnchantment(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return cmdCtx.getArgument(key, Enchantment.class);
	}

	@Override
	public EntitySelectorParser getEntitySelector(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return new EntitySelectorParser(
			() -> cmdCtx.getArgument(key, PlayerSelectorArgumentResolver.class).resolve(cmdCtx.getSource()).getFirst(),
			() -> cmdCtx.getArgument(key, EntitySelectorArgumentResolver.class).resolve(cmdCtx.getSource()).getFirst(),
			(allowEmpty) -> cmdCtx.getArgument(key, PlayerSelectorArgumentResolver.class).resolve(cmdCtx.getSource()),
			(allowEmpty) -> cmdCtx.getArgument(key, EntitySelectorArgumentResolver.class).resolve(cmdCtx.getSource())
		);
	}

	@Override
	public EntityType getEntityType(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return cmdCtx.getArgument(key, EntityType.class);
	}

	@Override
	public DoubleRange getDoubleRange(CommandContext<CommandSourceStack> cmdCtx, String key) {
		DoubleRangeProvider rangeProvider = cmdCtx.getArgument(key, DoubleRangeProvider.class);
		final double low = rangeProvider.range().hasLowerBound() ? rangeProvider.range().lowerEndpoint() : -Double.MAX_VALUE;
		final double high = rangeProvider.range().hasUpperBound() ? rangeProvider.range().upperEndpoint() : Double.MAX_VALUE;
		return new DoubleRange(low, high);
	}

	@Override
	public FunctionWrapper[] getFunction(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> paperNMS.bukkitNMS().getFunction(ctx, name)
		);
	}

	@Override
	public SimpleFunctionWrapper getFunction(NamespacedKey key) {
		return paperNMS.bukkitNMS().getFunction(key);
	}

	@Override
	public Set<NamespacedKey> getFunctions() {
		return paperNMS.bukkitNMS().getFunctions();
	}

	@Override
	public IntegerRange getIntRange(CommandContext<CommandSourceStack> cmdCtx, String key) {
		IntegerRangeProvider rangeProvider = cmdCtx.getArgument(key, IntegerRangeProvider.class);
		final int low = rangeProvider.range().hasLowerBound() ? rangeProvider.range().lowerEndpoint() : Integer.MIN_VALUE;
		final int high = rangeProvider.range().hasUpperBound() ? rangeProvider.range().upperEndpoint() : Integer.MAX_VALUE;
		return new IntegerRange(low, high);
	}

	@Override
	public ItemStack getItemStack(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return cmdCtx.getArgument(key, ItemStack.class);
	}

	@Override
	public Predicate<ItemStack> getItemStackPredicate(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return cmdCtx.getArgument(key, ItemStackPredicate.class);
	}

	@Override
	public Location2D getLocation2DBlock(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> paperNMS.bukkitNMS().getLocation2DBlock(ctx, name)
		);
	}

	@Override
	public Location2D getLocation2DPrecise(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> paperNMS.bukkitNMS().getLocation2DPrecise(ctx, name)
		);
	}

	@Override
	public Location getLocationBlock(CommandContext<CommandSourceStack> cmdCtx, String str) throws CommandSyntaxException {
		BlockPosition blockPosition = cmdCtx.getArgument(str, BlockPositionResolver.class).resolve(cmdCtx.getSource());
		return new Location(getWorldForCSS(cmdCtx.getSource()), blockPosition.blockX(), blockPosition.blockY(), blockPosition.blockZ());
	}

	@Override
	public Location getLocationPrecise(CommandContext<CommandSourceStack> cmdCtx, String str) throws CommandSyntaxException {
		return parseT(cmdCtx, str,
			(ctx, name) -> {
				FinePosition finePosition = ctx.getArgument(name, FinePositionResolver.class).resolve(ctx.getSource());
				return new Location(getWorldForCSS(ctx.getSource()), finePosition.x(), finePosition.y(), finePosition.z());
			},
			(ctx, name) -> paperNMS.bukkitNMS().getLocationPrecise(ctx, name)
		);
	}

	@Override
	public LootTable getLootTable(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return parse(cmdCtx, key,
			(ctx, name) -> Bukkit.getLootTable(ctx.getArgument(name, NamespacedKey.class)),
			(ctx, name) -> paperNMS.bukkitNMS().getLootTable(ctx, name)
		);
	}

	@Override
	public MathOperation getMathOperation(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> paperNMS.bukkitNMS().getMathOperation(ctx, name)
		);
	}

	@Override
	public NamespacedKey getMinecraftKey(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return cmdCtx.getArgument(key, NamespacedKey.class);
	}

	@Override
	public <NBTContainer> Object getNBTCompound(CommandContext<CommandSourceStack> cmdCtx, String key, Function<Object, NBTContainer> nbtContainerConstructor) {
		return parse(cmdCtx, key,
			(ctx, name) -> paperNMS.bukkitNMS().getNBTCompound(ctx, name, nbtContainerConstructor)
		);
	}

	@Override
	public Objective getObjective(CommandContext<CommandSourceStack> cmdCtx, String key) throws IllegalArgumentException, CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> paperNMS.bukkitNMS().getObjective(ctx, name)
		);
	}

	@Override
	public String getObjectiveCriteria(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return cmdCtx.getArgument(key, Criteria.class).getName();
	}

	@Override
	public ParticleData<?> getParticle(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return parse(cmdCtx, key,
			(ctx, name) -> paperNMS.bukkitNMS().getParticle(ctx, name)
		);
	}

	@Override
	public OfflinePlayer getOfflinePlayer(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> Bukkit.getOfflinePlayer(getIdFromProfile(ctx, name)),
			(ctx, name) -> paperNMS.bukkitNMS().getOfflinePlayer(ctx, name)
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
		return new RegistryParser<>(
			() -> cmdCtx.getArgument(key, PotionEffectType.class),
			() -> cmdCtx.getArgument(key, PotionEffectType.class).getKey()
		);
	}

	@Override
	public Recipe getRecipe(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> paperNMS.bukkitNMS().getRecipe(ctx, name)
		);
	}

	@Override
	public Rotation getRotation(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> {
				io.papermc.paper.math.Rotation rotation = ctx.getArgument(name, RotationResolver.class).resolve((CommandSourceStack) ctx.getSource());
				return new Rotation(rotation.yaw(), rotation.pitch());
			},
			(ctx, name) -> paperNMS.bukkitNMS().getRotation(ctx, name)
		);
	}

	@Override
	public ScoreboardSlot getScoreboardSlot(CommandContext<CommandSourceStack> cmdCtx, String key) {
		DisplaySlot displaySlot = cmdCtx.getArgument(key, DisplaySlot.class);
		return ScoreboardSlot.of(displaySlot);
	}

	@Override
	public Collection<String> getScoreHolderMultiple(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> paperNMS.bukkitNMS().getScoreHolderMultiple(ctx, name)
		);
	}

	@Override
	public String getScoreHolderSingle(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> paperNMS.bukkitNMS().getScoreHolderSingle(ctx, name)
		);
	}

	@Override
	public Team getTeam(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return parseT(cmdCtx, key,
			(ctx, name) -> paperNMS.bukkitNMS().getTeam(ctx, name)
		);
	}

	@Override
	public int getTime(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return cmdCtx.getArgument(key, Integer.class);
	}

	@Override
	public UUID getUUID(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return cmdCtx.getArgument(key, UUID.class);
	}

	@Override
	public World getWorldForCSS(CommandSourceStack clw) {
		return paperNMS.bukkitNMS().getWorldForCSS(clw);
	}

	@Override
	public SimpleCommandMap getSimpleCommandMap() {
		return paperNMS.bukkitNMS().getSimpleCommandMap();
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
			(ctx, name) -> paperNMS.bukkitNMS().getSound(ctx, name)
		);
	}

	@Override
	public SuggestionProvider<CommandSourceStack> getSuggestionProvider(SuggestionProviders provider) {
		return paperNMS.bukkitNMS().getSuggestionProvider(provider);
	}

	@Override
	public SimpleFunctionWrapper[] getTag(NamespacedKey key) {
		return paperNMS.bukkitNMS().getTag(key);
	}

	@Override
	public Set<NamespacedKey> getTags() {
		return paperNMS.bukkitNMS().getTags();
	}

	@Override
	public void reloadDataPacks() {
		paperNMS.bukkitNMS().reloadDataPacks();
	}

	@Override
	public HelpTopic generateHelpTopic(String commandName, String shortDescription, String fullDescription, String permission) {
		return paperNMS.bukkitNMS().generateHelpTopic(commandName, shortDescription, fullDescription, permission);
	}

	@Override
	public Map<String, HelpTopic> getHelpMap() {
		return paperNMS.bukkitNMS().getHelpMap();
	}

	@Override
	public Message generateMessageFromJson(String json) {
		return paperNMS.bukkitNMS().generateMessageFromJson(json);
	}

	@Override
	public BukkitCommandSender<? extends CommandSender> getSenderForCommand(CommandContext<CommandSourceStack> cmdCtx, boolean forceNative) {
		return paperNMS.bukkitNMS().getSenderForCommand(cmdCtx, forceNative);
	}

	@Override
	public <Source> BukkitCommandSender<? extends CommandSender> getCommandSenderFromCommandSource(Source css) {
		return paperNMS.bukkitNMS().getCommandSenderFromCommandSource(css);
	}

	@Override
	public CommandSourceStack getBrigadierSourceFromCommandSender(AbstractCommandSender<? extends CommandSender> sender) {
		return paperNMS.bukkitNMS().getBrigadierSourceFromCommandSender(sender);
	}

	@Override
	public void createDispatcherFile(File file, CommandDispatcher<CommandSourceStack> brigadierDispatcher) throws IOException {
		paperNMS.bukkitNMS().createDispatcherFile(file, brigadierDispatcher);
	}

	@Override
	public <T> T getMinecraftServer() {
		return paperNMS.bukkitNMS().getMinecraftServer();
	}

	@Override
	public NativeProxyCommandSender createNativeProxyCommandSender(CommandSender caller, CommandSender callee, Location location, World world) {
		return paperNMS.bukkitNMS().createNativeProxyCommandSender(caller, callee, location, world);
	}

	@Override
	public SignedMessage getChat(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return cmdCtx.getArgument(key, SignedMessageResolver.class).resolveSignedMessage(key, cmdCtx).join();
	}

	@Override
	public NamedTextColor getChatColor(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return cmdCtx.getArgument(key, NamedTextColor.class);
	}

	@Override
	public Component getChatComponent(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return cmdCtx.getArgument(key, Component.class);
	}

	@Override
	public List<PlayerProfile> getProfile(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return new ArrayList<>(cmdCtx.getArgument(key, PlayerProfileListResolver.class).resolve(cmdCtx.getSource()));
	}

	@Override
	public NMS<CommandSourceStack> bukkitNMS() {
		return ((PaperNMS<CommandSourceStack>) paperNMS).bukkitNMS();
	}

	@Override
	public CommandRegistrationStrategy<CommandSourceStack> createCommandRegistrationStrategy() {
		return ((PaperNMS<CommandSourceStack>) paperNMS).createCommandRegistrationStrategy();
	}
}
