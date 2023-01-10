package dev.jorel.commandapi.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemFactory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.help.HelpTopic;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.mockito.Mockito;

import com.google.common.io.Files;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.enchantments.EnchantmentsMock;
import be.seeseemelk.mockbukkit.potion.MockPotionEffectType;
import dev.jorel.commandapi.commandsenders.AbstractCommandSender;
import dev.jorel.commandapi.commandsenders.BukkitCommandSender;
import dev.jorel.commandapi.nms.NMS;
import dev.jorel.commandapi.wrappers.ParticleData;
import net.minecraft.SharedConstants;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.Advancements;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.arguments.ArgumentAnchor;
import net.minecraft.commands.arguments.ArgumentAngle;
import net.minecraft.commands.arguments.ArgumentChat;
import net.minecraft.commands.arguments.ArgumentChatComponent;
import net.minecraft.commands.arguments.ArgumentChatFormat;
import net.minecraft.commands.arguments.ArgumentCriterionValue;
import net.minecraft.commands.arguments.ArgumentDimension;
import net.minecraft.commands.arguments.ArgumentEnchantment;
import net.minecraft.commands.arguments.ArgumentEntity;
import net.minecraft.commands.arguments.ArgumentEntitySummon;
import net.minecraft.commands.arguments.ArgumentInventorySlot;
import net.minecraft.commands.arguments.ArgumentMathOperation;
import net.minecraft.commands.arguments.ArgumentMinecraftKeyRegistered;
import net.minecraft.commands.arguments.ArgumentMobEffect;
import net.minecraft.commands.arguments.ArgumentNBTBase;
import net.minecraft.commands.arguments.ArgumentNBTKey;
import net.minecraft.commands.arguments.ArgumentNBTTag;
import net.minecraft.commands.arguments.ArgumentParticle;
import net.minecraft.commands.arguments.ArgumentProfile;
import net.minecraft.commands.arguments.ArgumentScoreboardCriteria;
import net.minecraft.commands.arguments.ArgumentScoreboardObjective;
import net.minecraft.commands.arguments.ArgumentScoreboardSlot;
import net.minecraft.commands.arguments.ArgumentScoreboardTeam;
import net.minecraft.commands.arguments.ArgumentScoreholder;
import net.minecraft.commands.arguments.ArgumentTime;
import net.minecraft.commands.arguments.ArgumentUUID;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.ResourceOrTagLocationArgument;
import net.minecraft.commands.arguments.TemplateMirrorArgument;
import net.minecraft.commands.arguments.TemplateRotationArgument;
import net.minecraft.commands.arguments.blocks.ArgumentBlockPredicate;
import net.minecraft.commands.arguments.blocks.ArgumentTile;
import net.minecraft.commands.arguments.coordinates.ArgumentPosition;
import net.minecraft.commands.arguments.coordinates.ArgumentRotation;
import net.minecraft.commands.arguments.coordinates.ArgumentRotationAxis;
import net.minecraft.commands.arguments.coordinates.ArgumentVec2;
import net.minecraft.commands.arguments.coordinates.ArgumentVec2I;
import net.minecraft.commands.arguments.coordinates.ArgumentVec3;
import net.minecraft.commands.arguments.item.ArgumentItemPredicate;
import net.minecraft.commands.arguments.item.ArgumentItemStack;
import net.minecraft.commands.arguments.item.ArgumentTag;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.commands.synchronization.brigadier.ArgumentSerializerString;
import net.minecraft.commands.synchronization.brigadier.DoubleArgumentInfo;
import net.minecraft.commands.synchronization.brigadier.FloatArgumentInfo;
import net.minecraft.commands.synchronization.brigadier.IntegerArgumentInfo;
import net.minecraft.commands.synchronization.brigadier.LongArgumentInfo;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.IRegistry;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.AdvancementDataWorld;
import net.minecraft.server.DispenserRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ScoreboardServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.UserCache;
import net.minecraft.world.phys.Vec2F;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.scores.ScoreboardTeam;

public class MockNMS extends ArgumentNMS {

	public MockNMS(NMS<?> baseNMS) {
		super(baseNMS);
		try {
			initializeArgumentsInArgumentTypeInfos();

			// Initialize WorldVersion (game version)
			SharedConstants.a();

			// MockBukkit is very helpful and registers all of the potion
			// effects and enchantments for us. We need to not do this (because
			// we call DispenserRegistry.a() below which does the same thing)
			unregisterAllEnchantments();
			unregisterAllPotionEffects();

			// Invoke Minecraft's registry (I think that's what this does anyway)
			DispenserRegistry.a();
			
			// Sometimes, and I have no idea why, DispenserRegistry.a() only works
			// on the very first test in the test suite. After that, everything else
			// doesn't work. At this point, we'll use the ServerMock#createPotionEffectTypes
			// method (which unfortunately is private and pure, so instead of using reflection
			// we'll just implement it right here instead)
			@SuppressWarnings("unchecked")
			Map<NamespacedKey, PotionEffectType> byKey = (Map<NamespacedKey, PotionEffectType>) getField(PotionEffectType.class, "byKey", null);
			if(byKey.isEmpty()) {
				createPotionEffectTypes();
			}
			EnchantmentsMock.registerDefaultEnchantments();
//			System.out.println(byKey);
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
	}
	
	private static void registerPotionEffectType(int id, @NotNull String name, boolean instant, int rgb) {
		NamespacedKey key = NamespacedKey.minecraft(name.toLowerCase(Locale.ROOT));
		PotionEffectType type = new MockPotionEffectType(key, id, name, instant, Color.fromRGB(rgb));
		PotionEffectType.registerPotionEffectType(type);
	}
	
	// ItemStackArgument requirements
	public static ItemFactory getItemFactory() {
		return CraftItemFactory.instance();
	}

	/**
	 * @return A list of all item names, sorted in alphabetical order. Each item
	 * is prefixed with {@code minecraft:}
	 */
	public static List<String> getAllItemNames() {
		// Registry.ITEM
		return StreamSupport.stream(IRegistry.Y.spliterator(), false)
			.map(Object::toString)
			.map(s -> "minecraft:" + s)
			.sorted()
			.toList();
	}
	
	/**
	 * This registers Minecraft's default {@link PotionEffectType PotionEffectTypes}. It also prevents any new effects to
	 * be created afterwards.
	 */
	public static void createPotionEffectTypes() {
		for (PotionEffectType type : PotionEffectType.values()) {
			// We probably already registered all Potion Effects
			// otherwise this would be null
			if (type != null) {
				// This is not perfect, but it works.
				return;
			}
		}

		registerPotionEffectType(1, "SPEED", false, 8171462);
		registerPotionEffectType(2, "SLOWNESS", false, 5926017);
		registerPotionEffectType(3, "HASTE", false, 14270531);
		registerPotionEffectType(4, "MINING_FATIGUE", false, 4866583);
		registerPotionEffectType(5, "STRENGTH", false, 9643043);
		registerPotionEffectType(6, "INSTANT_HEALTH", true, 16262179);
		registerPotionEffectType(7, "INSTANT_DAMAGE", true, 4393481);
		registerPotionEffectType(8, "JUMP_BOOST", false, 2293580);
		registerPotionEffectType(9, "NAUSEA", false, 5578058);
		registerPotionEffectType(10, "REGENERATION", false, 13458603);
		registerPotionEffectType(11, "RESISTANCE", false, 10044730);
		registerPotionEffectType(12, "FIRE_RESISTANCE", false, 14981690);
		registerPotionEffectType(13, "WATER_BREATHING", false, 3035801);
		registerPotionEffectType(14, "INVISIBILITY", false, 8356754);
		registerPotionEffectType(15, "BLINDNESS", false, 2039587);
		registerPotionEffectType(16, "NIGHT_VISION", false, 2039713);
		registerPotionEffectType(17, "HUNGER", false, 5797459);
		registerPotionEffectType(18, "WEAKNESS", false, 4738376);
		registerPotionEffectType(19, "POISON", false, 5149489);
		registerPotionEffectType(20, "WITHER", false, 3484199);
		registerPotionEffectType(21, "HEALTH_BOOST", false, 16284963);
		registerPotionEffectType(22, "ABSORPTION", false, 2445989);
		registerPotionEffectType(23, "SATURATION", true, 16262179);
		registerPotionEffectType(24, "GLOWING", false, 9740385);
		registerPotionEffectType(25, "LEVITATION", false, 13565951);
		registerPotionEffectType(26, "LUCK", false, 3381504);
		registerPotionEffectType(27, "UNLUCK", false, 12624973);
		registerPotionEffectType(28, "SLOW_FALLING", false, 16773073);
		registerPotionEffectType(29, "CONDUIT_POWER", false, 1950417);
		registerPotionEffectType(30, "DOLPHINS_GRACE", false, 8954814);
		registerPotionEffectType(31, "BAD_OMEN", false, 745784);
		registerPotionEffectType(32, "HERO_OF_THE_VILLAGE", false, 4521796);
		registerPotionEffectType(33, "DARKNESS", false, 2696993);
		PotionEffectType.stopAcceptingRegistrations();
	}

	@SuppressWarnings("unchecked")
	public static void unregisterAllPotionEffects() {
		PotionEffectType[] byId = (PotionEffectType[]) getField(PotionEffectType.class, "byId", null);
		for (int i = 0; i < 34; i++) {
			byId[i] = null;
		}

		Map<String, PotionEffectType> byName = (Map<String, PotionEffectType>) getField(PotionEffectType.class, "byName", null);
		byName.clear();

		Map<NamespacedKey, PotionEffectType> byKey = (Map<NamespacedKey, PotionEffectType>) getField(PotionEffectType.class, "byKey", null);
		byKey.clear();

		try {
			Field field = PotionEffectType.class.getDeclaredField("acceptingNew");
			field.setAccessible(true);
			field.set(null, true);
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private void unregisterAllEnchantments() {

		Map<String, Enchantment> byName = (Map<String, Enchantment>) getField(Enchantment.class, "byName", null);
		byName.clear();

		Map<NamespacedKey, Enchantment> byKey = (Map<NamespacedKey, Enchantment>) getField(Enchantment.class, "byKey", null);
		byKey.clear();

		try {
			Field field = Enchantment.class.getDeclaredField("acceptingNew");
			field.setAccessible(true);
			field.set(null, true);
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String[] compatibleVersions() {
		return new String[] { "1.19.2" };
	}

	CommandDispatcher<CommandListenerWrapper> dispatcher;

	@Override
	public CommandDispatcher<CommandListenerWrapper> getBrigadierDispatcher() {
		if (this.dispatcher == null) {
			this.dispatcher = new CommandDispatcher<>();
		}
		return this.dispatcher;
	}

	@Override
	public SimpleCommandMap getSimpleCommandMap() {
		return ((ServerMock) Bukkit.getServer()).getCommandMap();
	}

	List<EntityPlayer> players = new ArrayList<>();
	PlayerList playerListMock;
	
	@SuppressWarnings({ "deprecation", "unchecked" })
	@Override
	public CommandListenerWrapper getBrigadierSourceFromCommandSender(AbstractCommandSender<? extends CommandSender> senderWrapper) {
		CommandSender sender = senderWrapper.getSource();
		CommandListenerWrapper clw = Mockito.mock(CommandListenerWrapper.class);
		Mockito.when(clw.getBukkitSender()).thenReturn(sender);

		if (sender instanceof Player player) {
			// Location argument
			Location loc = player.getLocation();
			Mockito.when(clw.e()).thenReturn(new Vec3D(loc.getX(), loc.getY(), loc.getZ()));
			
			WorldServer worldServerMock = Mockito.mock(WorldServer.class);
			Mockito.when(clw.f()).thenReturn(worldServerMock);
			Mockito.when(clw.f().E(any(BlockPosition.class))).thenReturn(true);
			Mockito.when(clw.f().j(any(BlockPosition.class))).thenReturn(true);

			// Advancement argument
			MinecraftServer minecraftServerMock = Mockito.mock(MinecraftServer.class);
			Mockito.when(minecraftServerMock.az()).thenReturn(mockAdvancementDataWorld());
			Mockito.when(clw.m()).thenReturn(minecraftServerMock); // CommandSourceStack#getServer

			// Entity selector argument
			for(Player onlinePlayer : Bukkit.getOnlinePlayers()) {
				EntityPlayer entityPlayerMock = Mockito.mock(EntityPlayer.class);
				CraftPlayer craftPlayerMock = Mockito.mock(CraftPlayer.class);
				Mockito.when(craftPlayerMock.getName()).thenReturn(onlinePlayer.getName());
				Mockito.when(craftPlayerMock.getUniqueId()).thenReturn(onlinePlayer.getUniqueId());
				Mockito.when(entityPlayerMock.getBukkitEntity()).thenReturn(craftPlayerMock);
				players.add(entityPlayerMock);
			}
			
			if(playerListMock == null) {
				playerListMock = Mockito.mock(PlayerList.class);
				Mockito.when(playerListMock.a(anyString())).thenAnswer(invocation -> {
					String playerName = invocation.getArgument(0);
					for(EntityPlayer onlinePlayer : players) {
						if(onlinePlayer.getBukkitEntity().getName().equals(playerName)) {
							return onlinePlayer;
						}
					}
					return null;
				});
			}
			
			Mockito.when(minecraftServerMock.ac()).thenReturn(playerListMock);
			Mockito.when(minecraftServerMock.ac().t()).thenReturn(players);
			
			// Player argument
			UserCache userCacheMock = Mockito.mock(UserCache.class);
			Mockito.when(userCacheMock.a(anyString())).thenAnswer(invocation -> {
				String playerName = invocation.getArgument(0);
				for(EntityPlayer onlinePlayer : players) {
					if(onlinePlayer.getBukkitEntity().getName().equals(playerName)) {
						return Optional.of(new GameProfile(onlinePlayer.getBukkitEntity().getUniqueId(), playerName));
					}
				}
				return Optional.empty();
			});
			Mockito.when(minecraftServerMock.ap()).thenReturn(userCacheMock);
			
			// World (Dimension) argument
			Mockito.when(minecraftServerMock.a(any(ResourceKey.class))).thenAnswer(invocation -> {
				// Get the ResourceKey<World> and extract the world name from it
				ResourceKey<net.minecraft.world.level.World> resourceKey = invocation.getArgument(0);
				String worldName = resourceKey.a().a();
				
				// Get the world via Bukkit (returns a WorldMock) and create a
				// CraftWorld clone of it for WorldServer.getWorld()
				World world = Bukkit.getServer().getWorld(worldName);
				if(world == null) {
					return null;
				} else {
					CraftWorld craftWorldMock = Mockito.mock(CraftWorld.class);
					Mockito.when(craftWorldMock.getName()).thenReturn(world.getName());
					Mockito.when(craftWorldMock.getUID()).thenReturn(world.getUID());
					
					// Create our return WorldServer object
					WorldServer bukkitWorldServerMock = Mockito.mock(WorldServer.class);
					Mockito.when(bukkitWorldServerMock.getWorld()).thenReturn(craftWorldMock);
					return bukkitWorldServerMock;
				}
			});
			
			Mockito.when(clw.u()).thenAnswer(invocation -> {
				Set<ResourceKey<net.minecraft.world.level.World>> set = new HashSet<>();
				// We only need to implement resourceKey.a()
				
				for(World world : Bukkit.getWorlds()) {
					ResourceKey<net.minecraft.world.level.World> key = Mockito.mock(ResourceKey.class);
					Mockito.when(key.a()).thenReturn(new MinecraftKey(world.getName()));
					set.add(key);
				}
				
				return set;
			});
			
			// Rotation argument
			Mockito.when(clw.l()).thenReturn(new Vec2F(loc.getYaw(), loc.getPitch()));
			
			// Team argument
			ScoreboardServer scoreboardServerMock = Mockito.mock(ScoreboardServer.class);
			Mockito.when(scoreboardServerMock.f(anyString())).thenAnswer(invocation -> { // Scoreboard#getPlayerTeam
				String teamName = invocation.getArgument(0);
				Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(teamName);
				if (team == null) {
					return null;
				} else {
					return new ScoreboardTeam(scoreboardServerMock, teamName);
				}
			});
			Mockito.when(minecraftServerMock.aF()).thenReturn(scoreboardServerMock); // MinecraftServer#getScoreboard
			
			Mockito.when(clw.r()).thenAnswer(invocation -> { // CommandSourceStack#getAllTeams
				return Bukkit.getScoreboardManager().getMainScoreboard().getTeams().stream().map(Team::getName).toList();
			});
		}
		return clw;
	}

	public AdvancementDataWorld mockAdvancementDataWorld() {
		AdvancementDataWorld advancementDataWorld = new AdvancementDataWorld(null);
		Advancements advancements = (Advancements) getField(AdvancementDataWorld.class, "c", advancementDataWorld);

		advancements.b.put(new MinecraftKey("my:advancement"), new Advancement(new MinecraftKey("my:advancement"), null, null, null, new HashMap<>(), null));
		advancements.b.put(new MinecraftKey("my:advancement2"), new Advancement(new MinecraftKey("my:advancement2"), null, null, null, new HashMap<>(), null));
		return advancementDataWorld;
	}

	public static Object getField(Class<?> className, String fieldName, Object instance) {
		try {
			Field field = className.getDeclaredField(fieldName);
			field.setAccessible(true);
			return field.get(instance);
		} catch (ReflectiveOperationException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T getFieldAs(Class<?> className, String fieldName, Object instance, Class<T> asType) {
		try {
			Field field = className.getDeclaredField(fieldName);
			field.setAccessible(true);
			return (T) field.get(instance);
		} catch (ReflectiveOperationException e) {
			return null;
		}
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
	public BukkitCommandSender<? extends CommandSender> getSenderForCommand(CommandContext<CommandListenerWrapper> cmdCtx, boolean forceNative) {
		return getCommandSenderFromCommandSource(cmdCtx.getSource());
	}

	@Override
	public void createDispatcherFile(File file, CommandDispatcher<CommandListenerWrapper> dispatcher)
			throws IOException {
		Files
			.asCharSink(file, StandardCharsets.UTF_8)
			.write(new GsonBuilder()
				.setPrettyPrinting()
				.create()
				.toJson(DispatcherUtil.toJSON(dispatcher, dispatcher.getRoot())));
	}

	@SuppressWarnings("rawtypes")
	private void initializeArgumentsInArgumentTypeInfos() throws ReflectiveOperationException {
		Field f = ArgumentTypeInfos.class.getDeclaredField("a");
		f.setAccessible(true);
		@SuppressWarnings("unchecked")
		Map<Class<?>, ArgumentTypeInfo<?, ?>> map = (Map<Class<?>, ArgumentTypeInfo<?, ?>>) f.get(null);
		map.put(BoolArgumentType.class, SingletonArgumentInfo.a(BoolArgumentType::bool));
		map.put(FloatArgumentType.class, new FloatArgumentInfo());
		map.put(DoubleArgumentType.class, new DoubleArgumentInfo());
		map.put(IntegerArgumentType.class, new IntegerArgumentInfo());
		map.put(LongArgumentType.class, new LongArgumentInfo());
		map.put(StringArgumentType.class, new ArgumentSerializerString());
		map.put(ArgumentEntity.class, new ArgumentEntity.Info());
		map.put(ArgumentProfile.class, SingletonArgumentInfo.a(ArgumentProfile::a));
		map.put(ArgumentPosition.class, SingletonArgumentInfo.a(ArgumentPosition::a));
		map.put(ArgumentVec2I.class, SingletonArgumentInfo.a(ArgumentVec2I::a));
		map.put(ArgumentVec3.class, SingletonArgumentInfo.a((Supplier<ArgumentType<?>>) ArgumentVec3::a));
		map.put(ArgumentVec2.class, SingletonArgumentInfo.a((Supplier<ArgumentType<?>>) ArgumentVec2::a));
		map.put(ArgumentTile.class, SingletonArgumentInfo.a((Function<CommandBuildContext, ArgumentType<?>>) ArgumentTile::a));
		map.put(ArgumentBlockPredicate.class, SingletonArgumentInfo.a((Function<CommandBuildContext, ArgumentType<?>>) ArgumentBlockPredicate::a));
		map.put(ArgumentItemStack.class, SingletonArgumentInfo.a((Function<CommandBuildContext, ArgumentType<?>>) ArgumentItemStack::a));
		map.put(ArgumentItemPredicate.class, SingletonArgumentInfo.a((Function<CommandBuildContext, ArgumentType<?>>) ArgumentItemPredicate::a));
		map.put(ArgumentChatFormat.class, SingletonArgumentInfo.a(ArgumentChatFormat::a));
		map.put(ArgumentChatComponent.class, SingletonArgumentInfo.a(ArgumentChatComponent::a));
		map.put(ArgumentChat.class, SingletonArgumentInfo.a(ArgumentChat::a));
		map.put(ArgumentNBTTag.class, SingletonArgumentInfo.a(ArgumentNBTTag::a));
		map.put(ArgumentNBTBase.class, SingletonArgumentInfo.a(ArgumentNBTBase::a));
		map.put(ArgumentNBTKey.class, SingletonArgumentInfo.a((Supplier<ArgumentType<?>>) ArgumentNBTKey::a));
		map.put(ArgumentScoreboardObjective.class, SingletonArgumentInfo.a(ArgumentScoreboardObjective::a));
		map.put(ArgumentScoreboardCriteria.class, SingletonArgumentInfo.a(ArgumentScoreboardCriteria::a));
		map.put(ArgumentMathOperation.class, SingletonArgumentInfo.a((Supplier<ArgumentType<?>>) ArgumentMathOperation::a));
		map.put(ArgumentParticle.class, SingletonArgumentInfo.a(ArgumentParticle::a));
		map.put(ArgumentAngle.class, SingletonArgumentInfo.a(ArgumentAngle::a));
		map.put(ArgumentRotation.class, SingletonArgumentInfo.a(ArgumentRotation::a));
		map.put(ArgumentScoreboardSlot.class, SingletonArgumentInfo.a(ArgumentScoreboardSlot::a));
		map.put(ArgumentScoreholder.class, new ArgumentScoreholder.a());
		map.put(ArgumentRotationAxis.class, SingletonArgumentInfo.a(ArgumentRotationAxis::a));
		map.put(ArgumentScoreboardTeam.class, SingletonArgumentInfo.a(ArgumentScoreboardTeam::a));
		map.put(ArgumentInventorySlot.class, SingletonArgumentInfo.a(ArgumentInventorySlot::a));
		map.put(ArgumentMinecraftKeyRegistered.class, SingletonArgumentInfo.a(ArgumentMinecraftKeyRegistered::a));
		map.put(ArgumentMobEffect.class, SingletonArgumentInfo.a(ArgumentMobEffect::a));
		map.put(ArgumentTag.class, SingletonArgumentInfo.a(ArgumentTag::a));
		map.put(ArgumentAnchor.class, SingletonArgumentInfo.a(ArgumentAnchor::a));
		map.put(ArgumentCriterionValue.b.class, SingletonArgumentInfo.a(ArgumentCriterionValue::a));
		map.put(ArgumentCriterionValue.a.class, SingletonArgumentInfo.a(ArgumentCriterionValue::b));
		map.put(ArgumentEnchantment.class, SingletonArgumentInfo.a(ArgumentEnchantment::a));
		map.put(ArgumentEntitySummon.class, SingletonArgumentInfo.a((Supplier<ArgumentType<?>>) ArgumentEntitySummon::a));
		map.put(ArgumentDimension.class, SingletonArgumentInfo.a(ArgumentDimension::a));
		map.put(ArgumentTime.class, SingletonArgumentInfo.a(ArgumentTime::a));
		map.put(ResourceOrTagLocationArgument.class, new ResourceOrTagLocationArgument.a());
		map.put(ResourceKeyArgument.class, new ResourceKeyArgument.a());
		map.put(TemplateMirrorArgument.class, SingletonArgumentInfo.a(TemplateMirrorArgument::a));
		map.put(TemplateRotationArgument.class, SingletonArgumentInfo.a(TemplateRotationArgument::a));
		map.put(ArgumentUUID.class, SingletonArgumentInfo.a(ArgumentUUID::a));
	}

	@Override
	public World getWorldForCSS(CommandListenerWrapper clw) {
		return new WorldMock();
	}
	
	@Override
	public void resendPackets(Player player) {
		// There's nothing to do here, we can't "send packets to players"
		return;
	}

	@Override
	public void addToHelpMap(Map<String, HelpTopic> helpTopicsToAdd) {
		new RuntimeException("unimplemented").printStackTrace();
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String convert(ItemStack is) {
		new RuntimeException("unimplemented").printStackTrace();
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String convert(ParticleData<?> particle) {
		new RuntimeException("unimplemented").printStackTrace();
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String convert(PotionEffectType potion) {
		new RuntimeException("unimplemented").printStackTrace();
		throw new RuntimeException("unimplemented");
	}

	@Override
	public String convert(Sound sound) {
		new RuntimeException("unimplemented").printStackTrace();
		throw new RuntimeException("unimplemented");
	}

	@Override
	public HelpTopic generateHelpTopic(String commandName, String shortDescription, String fullDescription, String permission) {
		new RuntimeException("unimplemented").printStackTrace();
		throw new RuntimeException("unimplemented");
	}

	@Override
	public boolean isVanillaCommandWrapper(Command command) {
		new RuntimeException("unimplemented").printStackTrace();
		throw new RuntimeException("unimplemented");
	}

	@Override
	public void reloadDataPacks() {
		new RuntimeException("unimplemented").printStackTrace();
		throw new RuntimeException("unimplemented");
	}
	
	/**
	 * An implementation of {@link ArgumentUtils} which produces JSON from a command
	 * dispatcher and its root node. We have to avoid accessing IRegistry because it
	 * isn't mock-able and cannot be instantiated through normal means
	 */
	private static class DispatcherUtil {

		static Map<Class<?>, String> argumentParsers = new HashMap<>();

		static {
			argumentParsers.put(BoolArgumentType.class, "brigadier:bool");
			argumentParsers.put(FloatArgumentType.class, "brigadier:float");
			argumentParsers.put(DoubleArgumentType.class, "brigadier:double");
			argumentParsers.put(IntegerArgumentType.class, "brigadier:integer");
			argumentParsers.put(LongArgumentType.class, "brigadier:long");
			argumentParsers.put(StringArgumentType.class, "brigadier:string");
			argumentParsers.put(ArgumentEntity.class, "entity");
			argumentParsers.put(ArgumentProfile.class, "game_profile");
			argumentParsers.put(ArgumentPosition.class, "block_pos");
			argumentParsers.put(ArgumentVec2I.class, "column_pos");
			argumentParsers.put(ArgumentVec3.class, "vec3");
			argumentParsers.put(ArgumentVec2.class, "vec2");
			argumentParsers.put(ArgumentTile.class, "block_state");
			argumentParsers.put(ArgumentBlockPredicate.class, "block_predicate");
			argumentParsers.put(ArgumentItemStack.class, "item_stack");
			argumentParsers.put(ArgumentItemPredicate.class, "item_predicate");
			argumentParsers.put(ArgumentChatFormat.class, "color");
			argumentParsers.put(ArgumentChatComponent.class, "component");
			argumentParsers.put(ArgumentChat.class, "message");
			argumentParsers.put(ArgumentNBTTag.class, "nbt_compound_tag");
			argumentParsers.put(ArgumentNBTBase.class, "nbt_tag");
			argumentParsers.put(ArgumentNBTKey.class, "nbt_path");
			argumentParsers.put(ArgumentScoreboardObjective.class, "objective");
			argumentParsers.put(ArgumentScoreboardCriteria.class, "objective_criteria");
			argumentParsers.put(ArgumentMathOperation.class, "operation");
			argumentParsers.put(ArgumentParticle.class, "particle");
			argumentParsers.put(ArgumentAngle.class, "angle");
			argumentParsers.put(ArgumentRotation.class, "rotation");
			argumentParsers.put(ArgumentScoreboardSlot.class, "scoreboard_slot");
			argumentParsers.put(ArgumentScoreholder.class, "score_holder");
			argumentParsers.put(ArgumentRotationAxis.class, "swizzle");
			argumentParsers.put(ArgumentScoreboardTeam.class, "team");
			argumentParsers.put(ArgumentInventorySlot.class, "item_slot");
			argumentParsers.put(ArgumentMinecraftKeyRegistered.class, "resource_location");
			argumentParsers.put(ArgumentMobEffect.class, "mob_effect");
			argumentParsers.put(ArgumentTag.class, "function");
			argumentParsers.put(ArgumentAnchor.class, "entity_anchor");
			argumentParsers.put(ArgumentCriterionValue.b.class, "int_range");
			argumentParsers.put(ArgumentCriterionValue.a.class, "float_range");
			argumentParsers.put(ArgumentEnchantment.class, "item_enchantment");
			argumentParsers.put(ArgumentEntitySummon.class, "entity_summon");
			argumentParsers.put(ArgumentDimension.class, "dimension");
			argumentParsers.put(ArgumentTime.class, "time");
			argumentParsers.put(ResourceOrTagLocationArgument.class, "resource_or_tag");
			argumentParsers.put(ResourceKeyArgument.class, "resource");
			argumentParsers.put(TemplateMirrorArgument.class, "template_mirror");
			argumentParsers.put(TemplateRotationArgument.class, "template_rotation");
			argumentParsers.put(ArgumentUUID.class, "uuid");
		}

		public static <S> JsonObject toJSON(CommandDispatcher<S> dispatcher, CommandNode<S> node) {
			JsonObject jsonObject = new JsonObject();

			// Unpack nodes
			if (node instanceof RootCommandNode) {
				jsonObject.addProperty("type", "root");
			} else if (node instanceof LiteralCommandNode) {
				jsonObject.addProperty("type", "literal");
			} else if (node instanceof ArgumentCommandNode) {
				ArgumentCommandNode<?, ?> argumentCommandNode = (ArgumentCommandNode<?, ?>) node;
				argToJSON(jsonObject, argumentCommandNode.getType());
			} else {
				jsonObject.addProperty("type", "unknown");
			}

			// Write children
			JsonObject children = new JsonObject();
			for (CommandNode<S> child : node.getChildren()) {
				children.add(child.getName(), (JsonElement) toJSON(dispatcher, child));
			}
			if (children.size() > 0) {
				jsonObject.add("children", (JsonElement) children);
			}

			// Write whether the command is executable
			if (node.getCommand() != null) {
				jsonObject.addProperty("executable", Boolean.valueOf(true));
			}
			if (node.getRedirect() != null) {
				Collection<String> redirectPaths = dispatcher.getPath(node.getRedirect());
				if (!redirectPaths.isEmpty()) {
					JsonArray redirects = new JsonArray();
					for (String redirectPath : redirectPaths) {
						redirects.add(redirectPath);
					}
					jsonObject.add("redirect", (JsonElement) redirects);
				}
			}
			return jsonObject;
		}

		@SuppressWarnings("unchecked")
		private static <T extends ArgumentType<?>> void argToJSON(JsonObject jsonObject, T argument) {
			ArgumentTypeInfo.a<T> argumentInfo = ArgumentTypeInfos.b(argument);
			jsonObject.addProperty("type", "argument");
			jsonObject.addProperty("parser", argumentParsers.get(argument.getClass()));
			
			// Properties
			JsonObject properties = new JsonObject();
			@SuppressWarnings("rawtypes")
			ArgumentTypeInfo argumentTypeInfo = argumentInfo.a();
			argumentTypeInfo.a(argumentInfo, properties);
			if (properties.size() > 0) {
				jsonObject.add("properties", (JsonElement) properties);
			}
		}

	}


}
