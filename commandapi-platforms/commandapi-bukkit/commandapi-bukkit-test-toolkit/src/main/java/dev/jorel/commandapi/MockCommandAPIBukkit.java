package dev.jorel.commandapi;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.commandsenders.AbstractCommandSender;
import dev.jorel.commandapi.commandsenders.BukkitCommandSender;
import dev.jorel.commandapi.spying.CommandAPIHandlerSpy;
import dev.jorel.commandapi.wrappers.Rotation;
import dev.jorel.commandapi.wrappers.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandMap;
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
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * An implementation of {@link CommandAPIBukkit} that is compatible with a MockBukkit testing environment.
 * Does not rely on any version-specific Minecraft code to (ideally) support testing in any version.
 */
public class MockCommandAPIBukkit extends CommandAPIBukkit<MockCommandSource> implements MockNMS<MockCommandSource> {
	// Static instance
	private static MockCommandAPIBukkit instance;

	/**
	 * @return The {@link MockCommandAPIBukkit} instance currently loaded. This is the same object as is returned by
	 * {@link CommandAPIBukkit#get()}, but explicitly using the {@link MockCommandAPIBukkit} class.
	 */
	public static MockCommandAPIBukkit getInstance() {
		return instance;
	}

	protected MockCommandAPIBukkit() {
		MockCommandAPIBukkit.instance = this;
	}

	// Reflection helpers

	/**
	 * Sets a field inside a target object to the given value. Ignores private access or final status.
	 *
	 * @param targetClass The {@link Class} that contains the field.
	 * @param fieldName   The name of the field to change.
	 * @param target      The object whose field will be changed. If the field is static, this is ignored and can be null.
	 * @param value       The new value for the field.
	 * @param <Target>    The type of the target object.
	 * @throws IllegalArgumentException If the field cannot be set.
	 */
	public static <Target> void setField(Class<? super Target> targetClass, String fieldName, Target target, Object value) {
		try {
			Field field = targetClass.getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, value);
		} catch (ReflectiveOperationException e) {
			throw new IllegalArgumentException("Reflection failed", e);
		}
	}

	// References to utility classes
	private CommandAPIHandlerSpy commandAPIHandlerSpy;
	private MockCommandRegistrationStrategy commandRegistrationStrategy;

	@Override
	public void onLoad(CommandAPIConfig<?> config) {
		// Intercept calls to CommandAPIHandler
		commandAPIHandlerSpy = new CommandAPIHandlerSpy(CommandAPIHandler.getInstance());
		CommandAPIHandler<Argument<?>, CommandSender, MockCommandSource> commandAPIHandler = commandAPIHandlerSpy.spyHandler();
		setField(CommandAPIHandler.class, "instance", null, commandAPIHandler);

		// Setup objects
		commandRegistrationStrategy = new MockCommandRegistrationStrategy(commandAPIHandler);

		// Continue load
		super.onLoad(config);
	}

	public static void onEnable() {
		CommandAPI.onEnable();
	}

	/**
	 * @return The {@link CommandAPIHandlerSpy} object intercepting calls to {@link CommandAPIHandler} methods.
	 */
	public CommandAPIHandlerSpy getCommandAPIHandlerSpy() {
		return commandAPIHandlerSpy;
	}

	@Override
	public CommandMap getCommandMap() {
		return Bukkit.getCommandMap();
	}

	@Override
	public Platform activePlatform() {
		return Platform.PAPER;
	}

	@Override
	public CommandRegistrationStrategy<MockCommandSource> createCommandRegistrationStrategy() {
		return commandRegistrationStrategy;
	}

	// CommandSender/MockCommandSource methods
	@Override
	public BukkitCommandSender<? extends CommandSender> getSenderForCommand(CommandContext<MockCommandSource> cmdCtx, boolean forceNative) {
		// TODO: Implement `forceNative` if necessary
		return getCommandSenderFromCommandSource(cmdCtx.getSource());
	}

	@Override
	public BukkitCommandSender<? extends CommandSender> getCommandSenderFromCommandSource(MockCommandSource cs) {
		return super.wrapCommandSender(cs.bukkitSender());
	}

	@Override
	public MockCommandSource getBrigadierSourceFromCommandSender(AbstractCommandSender<? extends CommandSender> sender) {
		return new MockCommandSource(sender.getSource());
	}

	@Override
	public NativeProxyCommandSender createNativeProxyCommandSender(CommandSender caller, CommandSender callee, Location location, World world) {
		return new MockNativeProxyCommandSender(caller, callee, location, world);
	}

	// Miscellaneous methods
	/**
	 * A global toggle for whether the default logger returned by {@link #getLogger()} should print messages to the
	 * console. This is {@code false} by default, so not messages will appear. If you don't provide your own logger
	 * using {@link CommandAPI#setLogger(CommandAPILogger)} and set this to {@code true} before calling
	 * {@link CommandAPI#onLoad(CommandAPIConfig)}, then the CommandAPI will write messages into the test log.
	 */
	public static boolean ENABLE_LOGGING = false;

	@Override
	public CommandAPILogger getLogger() {
		return ENABLE_LOGGING ?
			super.getLogger() :
			CommandAPILogger.bindToMethods(msg -> {}, msg -> {}, msg -> {}, (msg, ex) -> {});
	}

	@Override
	public Message generateMessageFromJson(String json) {
		Component component = GsonComponentSerializer.gson().deserialize(json);
		String text = PlainTextComponentSerializer.plainText().serialize(component);

		return () -> text;
	}

	// Arguments
	@Override
	public ArgumentType<?> _ArgumentEntity(ArgumentSubType subType) {
		return switch (subType) {
			case ENTITYSELECTOR_MANY_ENTITIES -> EntitySelectorArgumentType.entities();
			case ENTITYSELECTOR_MANY_PLAYERS -> EntitySelectorArgumentType.players();
			case ENTITYSELECTOR_ONE_ENTITY -> EntitySelectorArgumentType.entity();
			case ENTITYSELECTOR_ONE_PLAYER -> EntitySelectorArgumentType.player();
			default -> throw new IllegalArgumentException("Unexpected value: " + subType);
		};
	}

	@Override
	public Object getEntitySelector(CommandContext<MockCommandSource> cmdCtx, String key, ArgumentSubType subType, boolean allowEmpty) throws CommandSyntaxException {
		return switch (subType) {
			case ENTITYSELECTOR_MANY_ENTITIES -> EntitySelectorArgumentType.findManyEntities(cmdCtx, key, allowEmpty);
			case ENTITYSELECTOR_MANY_PLAYERS -> EntitySelectorArgumentType.findManyPlayers(cmdCtx, key, allowEmpty);
			case ENTITYSELECTOR_ONE_ENTITY -> EntitySelectorArgumentType.findSingleEntity(cmdCtx, key);
			case ENTITYSELECTOR_ONE_PLAYER -> EntitySelectorArgumentType.findSinglePlayer(cmdCtx, key);
			default -> throw new IllegalArgumentException("Unexpected value: " + subType);
		};
	}

	@Override
	public ArgumentType<?> _ArgumentIntRange() {
		return IntegerRangeArgumentType.INSTANCE;
	}

	@Override
	public IntegerRange getIntRange(CommandContext<MockCommandSource> cmdCtx, String key) {
		return IntegerRangeArgumentType.getRange(cmdCtx, key);
	}

	@Override
	public ArgumentType<?> _ArgumentProfile() {
		return ProfileArgumentType.INSTANCE;
	}

	@Override
	public Player getPlayer(CommandContext<MockCommandSource> cmdCtx, String key) throws CommandSyntaxException {
		Player target = Bukkit.getPlayer(ProfileArgumentType.getProfiles(cmdCtx, key).iterator().next());
		if (target == null) {
			throw ProfileArgumentType.ERROR_UNKNOWN_PLAYER.create();
		} else {
			return target;
		}
	}

	@Override
	public OfflinePlayer getOfflinePlayer(CommandContext<MockCommandSource> cmdCtx, String key) throws CommandSyntaxException {
		return Bukkit.getOfflinePlayer(ProfileArgumentType.getProfiles(cmdCtx, key).iterator().next());
	}

	///////////////////////////
	// UNIMPLEMENTED METHODS //
	///////////////////////////

	@Override
	public ArgumentType<?> _ArgumentAdvancement() {
		throw new UnimplementedMethodException();
	}

	@Override
	public ArgumentType<?> _ArgumentAngle() {
		throw new UnimplementedMethodException();
	}

	@Override
	public ArgumentType<?> _ArgumentAxis() {
		throw new UnimplementedMethodException();
	}

	@Override
	public ArgumentType<?> _ArgumentBlockPredicate() {
		throw new UnimplementedMethodException();
	}

	@Override
	public ArgumentType<?> _ArgumentBlockState() {
		throw new UnimplementedMethodException();
	}

	@Override
	public ArgumentType<?> _ArgumentChat() {
		throw new UnimplementedMethodException();
	}

	@Override
	public ArgumentType<?> _ArgumentChatComponent() {
		throw new UnimplementedMethodException();
	}

	@Override
	public ArgumentType<?> _ArgumentChatFormat() {
		throw new UnimplementedMethodException();
	}

	@Override
	public ArgumentType<?> _ArgumentDimension() {
		throw new UnimplementedMethodException();
	}

	@Override
	public ArgumentType<?> _ArgumentEnchantment() {
		throw new UnimplementedMethodException();
	}

	@Override
	public ArgumentType<?> _ArgumentEntitySummon() {
		throw new UnimplementedMethodException();
	}

	@Override
	public ArgumentType<?> _ArgumentFloatRange() {
		throw new UnimplementedMethodException();
	}

	@Override
	public ArgumentType<?> _ArgumentItemPredicate() {
		throw new UnimplementedMethodException();
	}

	@Override
	public ArgumentType<?> _ArgumentItemStack() {
		throw new UnimplementedMethodException();
	}

	@Override
	public ArgumentType<?> _ArgumentMathOperation() {
		throw new UnimplementedMethodException();
	}

	@Override
	public ArgumentType<?> _ArgumentMinecraftKeyRegistered() {
		throw new UnimplementedMethodException();
	}

	@Override
	public ArgumentType<?> _ArgumentMobEffect() {
		throw new UnimplementedMethodException();
	}

	@Override
	public ArgumentType<?> _ArgumentNBTCompound() {
		throw new UnimplementedMethodException();
	}

	@Override
	public ArgumentType<?> _ArgumentParticle() {
		throw new UnimplementedMethodException();
	}

	@Override
	public ArgumentType<?> _ArgumentPosition() {
		throw new UnimplementedMethodException();
	}

	@Override
	public ArgumentType<?> _ArgumentPosition2D() {
		throw new UnimplementedMethodException();
	}

	@Override
	public ArgumentType<?> _ArgumentRecipe() {
		throw new UnimplementedMethodException();
	}

	@Override
	public ArgumentType<?> _ArgumentRotation() {
		throw new UnimplementedMethodException();
	}

	@Override
	public ArgumentType<?> _ArgumentScoreboardCriteria() {
		throw new UnimplementedMethodException();
	}

	@Override
	public ArgumentType<?> _ArgumentScoreboardObjective() {
		throw new UnimplementedMethodException();
	}

	@Override
	public ArgumentType<?> _ArgumentScoreboardSlot() {
		throw new UnimplementedMethodException();
	}

	@Override
	public ArgumentType<?> _ArgumentScoreboardTeam() {
		throw new UnimplementedMethodException();
	}

	@Override
	public ArgumentType<?> _ArgumentScoreholder(ArgumentSubType subType) {
		throw new UnimplementedMethodException();
	}

	@Override
	public ArgumentType<?> _ArgumentTag() {
		throw new UnimplementedMethodException();
	}

	@Override
	public ArgumentType<?> _ArgumentTime() {
		throw new UnimplementedMethodException();
	}

	@Override
	public ArgumentType<?> _ArgumentUUID() {
		throw new UnimplementedMethodException();
	}

	@Override
	public ArgumentType<?> _ArgumentVec2(boolean centerPosition) {
		throw new UnimplementedMethodException();
	}

	@Override
	public ArgumentType<?> _ArgumentVec3(boolean centerPosition) {
		throw new UnimplementedMethodException();
	}

	@Override
	public ArgumentType<?> _ArgumentSyntheticBiome() {
		throw new UnimplementedMethodException();
	}

	@Override
	public String[] compatibleVersions() {
		throw new UnimplementedMethodException();
	}

	@Override
	public String convert(ItemStack is) {
		throw new UnimplementedMethodException();
	}

	@Override
	public String convert(ParticleData<?> particle) {
		throw new UnimplementedMethodException();
	}

	@Override
	public String convert(PotionEffectType potion) {
		throw new UnimplementedMethodException();
	}

	@Override
	public String convert(Sound sound) {
		throw new UnimplementedMethodException();
	}

	@Override
	public Advancement getAdvancement(CommandContext<MockCommandSource> cmdCtx, String key) throws CommandSyntaxException {
		throw new UnimplementedMethodException();
	}

	@Override
	public Component getAdventureChat(CommandContext<MockCommandSource> cmdCtx, String key) throws CommandSyntaxException {
		throw new UnimplementedMethodException();
	}

	@Override
	public NamedTextColor getAdventureChatColor(CommandContext<MockCommandSource> cmdCtx, String key) {
		throw new UnimplementedMethodException();
	}

	@Override
	public Component getAdventureChatComponent(CommandContext<MockCommandSource> cmdCtx, String key) {
		throw new UnimplementedMethodException();
	}

	@Override
	public float getAngle(CommandContext<MockCommandSource> cmdCtx, String key) {
		throw new UnimplementedMethodException();
	}

	@Override
	public EnumSet<Axis> getAxis(CommandContext<MockCommandSource> cmdCtx, String key) {
		throw new UnimplementedMethodException();
	}

	@Override
	public Object getBiome(CommandContext<MockCommandSource> cmdCtx, String key, ArgumentSubType subType) throws CommandSyntaxException {
		throw new UnimplementedMethodException();
	}

	@Override
	public Predicate<Block> getBlockPredicate(CommandContext<MockCommandSource> cmdCtx, String key) throws CommandSyntaxException {
		throw new UnimplementedMethodException();
	}

	@Override
	public BlockData getBlockState(CommandContext<MockCommandSource> cmdCtx, String key) {
		throw new UnimplementedMethodException();
	}

	@Override
	public BaseComponent[] getChat(CommandContext<MockCommandSource> cmdCtx, String key) throws CommandSyntaxException {
		throw new UnimplementedMethodException();
	}

	@Override
	public ChatColor getChatColor(CommandContext<MockCommandSource> cmdCtx, String key) {
		throw new UnimplementedMethodException();
	}

	@Override
	public BaseComponent[] getChatComponent(CommandContext<MockCommandSource> cmdCtx, String key) {
		throw new UnimplementedMethodException();
	}

	@Override
	public World getDimension(CommandContext<MockCommandSource> cmdCtx, String key) throws CommandSyntaxException {
		throw new UnimplementedMethodException();
	}

	@Override
	public Enchantment getEnchantment(CommandContext<MockCommandSource> cmdCtx, String key) throws CommandSyntaxException {
		throw new UnimplementedMethodException();
	}

	@Override
	public EntityType getEntityType(CommandContext<MockCommandSource> cmdCtx, String key) throws CommandSyntaxException {
		throw new UnimplementedMethodException();
	}

	@Override
	public DoubleRange getFloatRange(CommandContext<MockCommandSource> cmdCtx, String key) {
		throw new UnimplementedMethodException();
	}

	@Override
	public FunctionWrapper[] getFunction(CommandContext<MockCommandSource> cmdCtx, String key) throws CommandSyntaxException {
		throw new UnimplementedMethodException();
	}

	@Override
	public SimpleFunctionWrapper getFunction(NamespacedKey key) {
		throw new UnimplementedMethodException();
	}

	@Override
	public Set<NamespacedKey> getFunctions() {
		throw new UnimplementedMethodException();
	}

	@Override
	public ItemStack getItemStack(CommandContext<MockCommandSource> cmdCtx, String key) throws CommandSyntaxException {
		throw new UnimplementedMethodException();
	}

	@Override
	public Predicate<ItemStack> getItemStackPredicate(CommandContext<MockCommandSource> cmdCtx, String key) throws CommandSyntaxException {
		throw new UnimplementedMethodException();
	}

	@Override
	public Location2D getLocation2DBlock(CommandContext<MockCommandSource> cmdCtx, String key) throws CommandSyntaxException {
		throw new UnimplementedMethodException();
	}

	@Override
	public Location2D getLocation2DPrecise(CommandContext<MockCommandSource> cmdCtx, String key) throws CommandSyntaxException {
		throw new UnimplementedMethodException();
	}

	@Override
	public Location getLocationBlock(CommandContext<MockCommandSource> cmdCtx, String str) throws CommandSyntaxException {
		throw new UnimplementedMethodException();
	}

	@Override
	public Location getLocationPrecise(CommandContext<MockCommandSource> cmdCtx, String str) throws CommandSyntaxException {
		throw new UnimplementedMethodException();
	}

	@Override
	public LootTable getLootTable(CommandContext<MockCommandSource> cmdCtx, String key) {
		throw new UnimplementedMethodException();
	}

	@Override
	public MathOperation getMathOperation(CommandContext<MockCommandSource> cmdCtx, String key) throws CommandSyntaxException {
		throw new UnimplementedMethodException();
	}

	@Override
	public NamespacedKey getMinecraftKey(CommandContext<MockCommandSource> cmdCtx, String key) {
		throw new UnimplementedMethodException();
	}

	@Override
	public <NBTContainer> Object getNBTCompound(CommandContext<MockCommandSource> cmdCtx, String key, Function<Object, NBTContainer> nbtContainerConstructor) {
		throw new UnimplementedMethodException();
	}

	@Override
	public Objective getObjective(CommandContext<MockCommandSource> cmdCtx, String key) throws IllegalArgumentException, CommandSyntaxException {
		throw new UnimplementedMethodException();
	}

	@Override
	public String getObjectiveCriteria(CommandContext<MockCommandSource> cmdCtx, String key) {
		throw new UnimplementedMethodException();
	}

	@Override
	public ParticleData<?> getParticle(CommandContext<MockCommandSource> cmdCtx, String key) {
		throw new UnimplementedMethodException();
	}

	@Override
	public Object getPotionEffect(CommandContext<MockCommandSource> cmdCtx, String key, ArgumentSubType subType) throws CommandSyntaxException {
		throw new UnimplementedMethodException();
	}

	@Override
	public Recipe getRecipe(CommandContext<MockCommandSource> cmdCtx, String key) throws CommandSyntaxException {
		throw new UnimplementedMethodException();
	}

	@Override
	public Rotation getRotation(CommandContext<MockCommandSource> cmdCtx, String key) {
		throw new UnimplementedMethodException();
	}

	@Override
	public ScoreboardSlot getScoreboardSlot(CommandContext<MockCommandSource> cmdCtx, String key) {
		throw new UnimplementedMethodException();
	}

	@Override
	public Collection<String> getScoreHolderMultiple(CommandContext<MockCommandSource> cmdCtx, String key) throws CommandSyntaxException {
		throw new UnimplementedMethodException();
	}

	@Override
	public String getScoreHolderSingle(CommandContext<MockCommandSource> cmdCtx, String key) throws CommandSyntaxException {
		throw new UnimplementedMethodException();
	}

	@Override
	public Team getTeam(CommandContext<MockCommandSource> cmdCtx, String key) throws CommandSyntaxException {
		throw new UnimplementedMethodException();
	}

	@Override
	public int getTime(CommandContext<MockCommandSource> cmdCtx, String key) {
		throw new UnimplementedMethodException();
	}

	@Override
	public UUID getUUID(CommandContext<MockCommandSource> cmdCtx, String key) {
		throw new UnimplementedMethodException();
	}

	@Override
	public World getWorldForCSS(MockCommandSource clw) {
		throw new UnimplementedMethodException();
	}

	@Override
	public SimpleCommandMap getSimpleCommandMap() {
		throw new UnimplementedMethodException();
	}

	@Override
	public Object getSound(CommandContext<MockCommandSource> cmdCtx, String key, ArgumentSubType subType) {
		throw new UnimplementedMethodException();
	}

	@Override
	public SuggestionProvider<MockCommandSource> getSuggestionProvider(SuggestionProviders suggestionProvider) {
		throw new UnimplementedMethodException();
	}

	@Override
	public SimpleFunctionWrapper[] getTag(NamespacedKey key) {
		throw new UnimplementedMethodException();
	}

	@Override
	public Set<NamespacedKey> getTags() {
		throw new UnimplementedMethodException();
	}

	@Override
	public void createDispatcherFile(File file, CommandDispatcher<MockCommandSource> brigadierDispatcher) throws IOException {
		throw new UnimplementedMethodException();
	}

	@Override
	public <T> T getMinecraftServer() {
		throw new UnimplementedMethodException();
	}

	@Override
	public void reloadDataPacks() {
		throw new UnimplementedMethodException();
	}

	@Override
	public HelpTopic generateHelpTopic(String commandName, String shortDescription, String fullDescription, String permission) {
		throw new UnimplementedMethodException();
	}

	@Override
	public Map<String, HelpTopic> getHelpMap() {
		throw new UnimplementedMethodException();
	}
}
