package dev.jorel.commandapi.test;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.jorel.commandapi.arguments.ArgumentSubType;
import dev.jorel.commandapi.arguments.SuggestionProviders;
import dev.jorel.commandapi.arguments.parser.EntitySelectorParser;
import dev.jorel.commandapi.arguments.parser.RegistryParser;
import dev.jorel.commandapi.nms.NMS;
import dev.jorel.commandapi.wrappers.DoubleRange;
import dev.jorel.commandapi.wrappers.FunctionWrapper;
import dev.jorel.commandapi.wrappers.IntegerRange;
import dev.jorel.commandapi.wrappers.Location2D;
import dev.jorel.commandapi.wrappers.MathOperation;
import dev.jorel.commandapi.wrappers.ParticleData;
import dev.jorel.commandapi.wrappers.Rotation;
import dev.jorel.commandapi.wrappers.ScoreboardSlot;
import dev.jorel.commandapi.wrappers.SimpleFunctionWrapper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.loot.LootTable;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Argument related method implementations. Since 26.3, the Bukkit NMS implementation is given a
 * {@link MockRegistries#commandBuildContext() real CommandBuildContext}, so (unlike the implementations for older
 * versions) the argument types don't need to be created with a mocked one here.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class ArgumentNMS extends MockPlatform<CommandSourceStack> {

	NMS<CommandSourceStack> baseNMS;

	protected ArgumentNMS(NMS<?> baseNMS) {
		this.baseNMS = (NMS<CommandSourceStack>) baseNMS;
	}

	@Override
	public ArgumentType<?> _ArgumentAdvancement() {
		return baseNMS._ArgumentAdvancement();
	}

	@Override
	public ArgumentType<?> _ArgumentAngle() {
		return baseNMS._ArgumentAngle();
	}

	@Override
	public ArgumentType<?> _ArgumentAxis() {
		return baseNMS._ArgumentAxis();
	}

	@Override
	public ArgumentType<?> _ArgumentBlockPredicate() {
		return baseNMS._ArgumentBlockPredicate();
	}

	@Override
	public ArgumentType<?> _ArgumentBlockState() {
		return baseNMS._ArgumentBlockState();
	}

	@Override
	public ArgumentType<?> _ArgumentChat() {
		return baseNMS._ArgumentChat();
	}

	@Override
	public ArgumentType<?> _ArgumentChatComponent() {
		return baseNMS._ArgumentChatComponent();
	}

	@Override
	public ArgumentType<?> _ArgumentChatFormat() {
		return baseNMS._ArgumentChatFormat();
	}

	@Override
	public ArgumentType<?> _ArgumentDimension() {
		return baseNMS._ArgumentDimension();
	}

	@Override
	public ArgumentType<?> _ArgumentEnchantment() {
		return baseNMS._ArgumentEnchantment();
	}

	@Override
	public ArgumentType<?> _ArgumentEntity(ArgumentSubType subType) {
		return baseNMS._ArgumentEntity(subType);
	}

	@Override
	public ArgumentType<?> _ArgumentEntitySummon() {
		return baseNMS._ArgumentEntitySummon();
	}

	@Override
	public ArgumentType<?> _ArgumentFloatRange() {
		return baseNMS._ArgumentFloatRange();
	}

	@Override
	public ArgumentType<?> _ArgumentIntRange() {
		return baseNMS._ArgumentIntRange();
	}

	@Override
	public ArgumentType<?> _ArgumentItemPredicate() {
		return baseNMS._ArgumentItemPredicate();
	}

	@Override
	public ArgumentType<?> _ArgumentItemStack() {
		return baseNMS._ArgumentItemStack();
	}

	@Override
	public ArgumentType<?> _ArgumentMathOperation() {
		return baseNMS._ArgumentMathOperation();
	}

	@Override
	public ArgumentType<?> _ArgumentMinecraftKeyRegistered() {
		return baseNMS._ArgumentMinecraftKeyRegistered();
	}

	@Override
	public ArgumentType<?> _ArgumentMobEffect() {
		return baseNMS._ArgumentMobEffect();
	}

	@Override
	public ArgumentType<?> _ArgumentNBTCompound() {
		return baseNMS._ArgumentNBTCompound();
	}

	@Override
	public ArgumentType<?> _ArgumentParticle() {
		return baseNMS._ArgumentParticle();
	}

	@Override
	public ArgumentType<?> _ArgumentPosition() {
		return baseNMS._ArgumentPosition();
	}

	@Override
	public ArgumentType<?> _ArgumentPosition2D() {
		return baseNMS._ArgumentPosition2D();
	}

	@Override
	public ArgumentType<?> _ArgumentProfile() {
		return baseNMS._ArgumentProfile();
	}

	@Override
	public ArgumentType<?> _ArgumentRecipe() {
		return baseNMS._ArgumentRecipe();
	}

	@Override
	public ArgumentType<?> _ArgumentRotation() {
		return baseNMS._ArgumentRotation();
	}

	@Override
	public ArgumentType<?> _ArgumentScoreboardCriteria() {
		return baseNMS._ArgumentScoreboardCriteria();
	}

	@Override
	public ArgumentType<?> _ArgumentScoreboardObjective() {
		return baseNMS._ArgumentScoreboardObjective();
	}

	@Override
	public ArgumentType<?> _ArgumentScoreboardSlot() {
		return baseNMS._ArgumentScoreboardSlot();
	}

	@Override
	public ArgumentType<?> _ArgumentScoreboardTeam() {
		return baseNMS._ArgumentScoreboardTeam();
	}

	@Override
	public ArgumentType<?> _ArgumentScoreholder(ArgumentSubType subType) {
		return baseNMS._ArgumentScoreholder(subType);
	}

	@Override
	public ArgumentType<?> _ArgumentSyntheticBiome() {
		return baseNMS._ArgumentSyntheticBiome();
	}

	@Override
	public ArgumentType<?> _ArgumentTag() {
		return baseNMS._ArgumentTag();
	}

	@Override
	public ArgumentType<?> _ArgumentTime() {
		return baseNMS._ArgumentTime();
	}

	@Override
	public ArgumentType<?> _ArgumentUUID() {
		return baseNMS._ArgumentUUID();
	}

	@Override
	public ArgumentType<?> _ArgumentVec2(boolean centerPosition) {
		return baseNMS._ArgumentVec2(centerPosition);
	}

	@Override
	public ArgumentType<?> _ArgumentVec3(boolean centerPosition) {
		return baseNMS._ArgumentVec3(centerPosition);
	}

	@Override
	public Message generateMessageFromJson(final String json) {
		return baseNMS.generateMessageFromJson(json);
	}

	@Override
	public org.bukkit.advancement.Advancement getAdvancement(CommandContext cmdCtx, String key) throws CommandSyntaxException {
		return baseNMS.getAdvancement(cmdCtx, key);
	}

	@Override
	public float getAngle(CommandContext cmdCtx, String key) throws CommandSyntaxException {
		return baseNMS.getAngle(cmdCtx, key);
	}

	@Override
	public EnumSet<Axis> getAxis(CommandContext cmdCtx, String key) {
		return baseNMS.getAxis(cmdCtx, key);
	}

	@Override
	public RegistryParser<Biome> getBiome(CommandContext cmdCtx, String key) throws CommandSyntaxException {
		return baseNMS.getBiome(cmdCtx, key);
	}

	@Override
	public Predicate<Block> getBlockPredicate(CommandContext cmdCtx, String key) throws CommandSyntaxException {
		return baseNMS.getBlockPredicate(cmdCtx, key);
	}

	@Override
	public BlockState getBlockState(CommandContext cmdCtx, String key) {
		return baseNMS.getBlockState(cmdCtx, key);
	}

	@Override
	public World getDimension(CommandContext cmdCtx, String key) throws CommandSyntaxException {
		return baseNMS.getDimension(cmdCtx, key);
	}

	@Override
	public Enchantment getEnchantment(CommandContext cmdCtx, String key) throws CommandSyntaxException {
		return baseNMS.getEnchantment(cmdCtx, key);
	}

	@Override
	public EntitySelectorParser getEntitySelector(CommandContext cmdCtx, String key) {
		return baseNMS.getEntitySelector(cmdCtx, key);
	}

	@Override
	public EntityType getEntityType(CommandContext cmdCtx, String key) throws CommandSyntaxException {
		return baseNMS.getEntityType(cmdCtx, key);
	}

	@Override
	public DoubleRange getDoubleRange(CommandContext cmdCtx, String key) {
		return baseNMS.getDoubleRange(cmdCtx, key);
	}

	@Override
	public FunctionWrapper[] getFunction(CommandContext cmdCtx, String key) throws CommandSyntaxException {
		return baseNMS.getFunction(cmdCtx, key);
	}

	@Override
	public SimpleFunctionWrapper getFunction(NamespacedKey key) {
		return baseNMS.getFunction(key);
	}

	@Override
	public Set<NamespacedKey> getFunctions() {
		return baseNMS.getFunctions();
	}

	@Override
	public IntegerRange getIntRange(CommandContext cmdCtx, String key) {
		return baseNMS.getIntRange(cmdCtx, key);
	}

	@Override
	public ItemStack getItemStack(CommandContext cmdCtx, String key) throws CommandSyntaxException {
		return baseNMS.getItemStack(cmdCtx, key);
	}

	@Override
	public Predicate<ItemStack> getItemStackPredicate(CommandContext cmdCtx, String key) throws CommandSyntaxException {
		return baseNMS.getItemStackPredicate(cmdCtx, key);
	}

	@Override
	public Location2D getLocation2DBlock(CommandContext cmdCtx, String key) throws CommandSyntaxException {
		return baseNMS.getLocation2DBlock(cmdCtx, key);
	}

	@Override
	public Location2D getLocation2DPrecise(CommandContext cmdCtx, String key) throws CommandSyntaxException {
		return baseNMS.getLocation2DPrecise(cmdCtx, key);
	}

	@Override
	public Location getLocationBlock(CommandContext cmdCtx, String str) throws CommandSyntaxException {
		return baseNMS.getLocationBlock(cmdCtx, str);
	}

	@Override
	public Location getLocationPrecise(CommandContext cmdCtx, String str) throws CommandSyntaxException {
		return baseNMS.getLocationPrecise(cmdCtx, str);
	}

	@Override
	public LootTable getLootTable(CommandContext cmdCtx, String key) {
		return baseNMS.getLootTable(cmdCtx, key);
	}

	@Override
	public MathOperation getMathOperation(CommandContext cmdCtx, String key) throws CommandSyntaxException {
		return baseNMS.getMathOperation(cmdCtx, key);
	}

	@Override
	public NamespacedKey getMinecraftKey(CommandContext cmdCtx, String key) {
		return baseNMS.getMinecraftKey(cmdCtx, key);
	}

	@Override
	public <NBTContainer> Object getNBTCompound(CommandContext<CommandSourceStack> cmdCtx, String key,
		Function<Object, NBTContainer> nbtContainerConstructor) {
		return baseNMS.getNBTCompound((CommandContext) cmdCtx, key, nbtContainerConstructor);
	}

	@Override
	public Objective getObjective(CommandContext cmdCtx, String key) throws IllegalArgumentException, CommandSyntaxException {
		return baseNMS.getObjective(cmdCtx, key);
	}

	@Override
	public String getObjectiveCriteria(CommandContext cmdCtx, String key) {
		return baseNMS.getObjectiveCriteria(cmdCtx, key);
	}

	@Override
	public ParticleData<?> getParticle(CommandContext cmdCtx, String key) {
		// The Bukkit NMS implementation only converts the particle type to Bukkit on a CraftServer, because
		//  CraftParticle needs the server's registries. Bukkit's Particle is an enum with the same keys as
		//  Minecraft's particle types, so we can look it up ourselves.
		final ParticleData<?> particleData = baseNMS.getParticle(cmdCtx, key);
		final Identifier particleKey = BuiltInRegistries.PARTICLE_TYPE.getKey(ParticleArgument.getParticle(cmdCtx, key).getType());

		Particle particle = null;
		if (particleKey != null) {
			NamespacedKey bukkitKey = new NamespacedKey(particleKey.getNamespace(), particleKey.getPath());
			for (Particle candidate : Particle.values()) {
				if (candidate.getKey().equals(bukkitKey)) {
					particle = candidate;
					break;
				}
			}
		}
		return new ParticleData(particle, particleData.data());
	}

	@Override
	public RegistryParser<PotionEffectType> getPotionEffect(CommandContext cmdCtx, String key) {
		return baseNMS.getPotionEffect(cmdCtx, key);
	}

	@Override
	public <T extends Recipe> T getRecipe(CommandContext<CommandSourceStack> cmdCtx, String key, BiFunction<NamespacedKey, Recipe, T> result) throws CommandSyntaxException {
		return baseNMS.getRecipe(cmdCtx, key, result);
	}

	@Override
	public Rotation getRotation(CommandContext cmdCtx, String key) throws CommandSyntaxException {
		return baseNMS.getRotation(cmdCtx, key);
	}

	@Override
	public ScoreboardSlot getScoreboardSlot(CommandContext cmdCtx, String key) {
		return baseNMS.getScoreboardSlot(cmdCtx, key);
	}

	@Override
	public Collection<String> getScoreHolderMultiple(CommandContext cmdCtx, String key) throws CommandSyntaxException {
		return baseNMS.getScoreHolderMultiple(cmdCtx, key);
	}

	@Override
	public String getScoreHolderSingle(CommandContext cmdCtx, String key) throws CommandSyntaxException {
		return baseNMS.getScoreHolderSingle(cmdCtx, key);
	}

	@Override
	public RegistryParser<Sound> getSound(CommandContext cmdCtx, String key) {
		return baseNMS.getSound(cmdCtx, key);
	}

	@Override
	public SuggestionProvider getSuggestionProvider(SuggestionProviders provider) {
		return baseNMS.getSuggestionProvider(provider);
	}

	@Override
	public SimpleFunctionWrapper[] getTag(NamespacedKey key) {
		return baseNMS.getTag(key);
	}

	@Override
	public Set<NamespacedKey> getTags() {
		return baseNMS.getTags();
	}

	@Override
	public Team getTeam(CommandContext cmdCtx, String key) throws CommandSyntaxException {
		return baseNMS.getTeam(cmdCtx, key);
	}

	@Override
	public int getTime(CommandContext cmdCtx, String key) {
		return baseNMS.getTime(cmdCtx, key);
	}

	@Override
	public UUID getUUID(CommandContext cmdCtx, String key) {
		return baseNMS.getUUID(cmdCtx, key);
	}

}
