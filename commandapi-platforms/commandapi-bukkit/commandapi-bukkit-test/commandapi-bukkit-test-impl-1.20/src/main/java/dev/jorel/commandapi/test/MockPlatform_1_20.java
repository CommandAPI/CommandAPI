package dev.jorel.commandapi.test;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.enchantments.EnchantmentMock;
import be.seeseemelk.mockbukkit.help.HelpMapMock;
import be.seeseemelk.mockbukkit.potion.MockPotionEffectType;
import com.google.common.collect.Streams;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import dev.jorel.commandapi.Brigadier;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.SafeVarHandle;
import dev.jorel.commandapi.commandsenders.AbstractCommandSender;
import dev.jorel.commandapi.commandsenders.BukkitPlayer;
import dev.jorel.commandapi.nms.NMS_1_20_R1;
import io.papermc.paper.advancement.AdvancementDisplay;
import net.minecraft.SharedConstants;
import net.minecraft.advancements.Advancement;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.profiling.metrics.profiling.InactiveMetricsRecorder;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_20_R1.CraftParticle;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemFactory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.help.HelpTopic;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.mockito.Mockito;

import java.security.CodeSource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.mockito.ArgumentMatchers.*;

public class MockPlatform_1_20 extends MockPlatform<CommandSourceStack> implements Enums_1_20 {
	private static final SafeVarHandle<HelpMapMock, Map<String, HelpTopic>> helpMapTopics =
		SafeVarHandle.ofOrNull(HelpMapMock.class, "topics", "topics", Map.class);

	static {
		CodeSource src = PotionEffectType.class.getProtectionDomain().getCodeSource();
		if (src != null) {
			System.err.println("Loading PotionEffectType sources from " + src.getLocation());
		}
	}

	static ServerAdvancementManager advancementDataWorld = new ServerAdvancementManager(null);

	private MinecraftServer minecraftServerMock = null;
	private final List<ServerPlayer> players = new ArrayList<>();
	private final PlayerList playerListMock;
	private final RecipeManager recipeManager;
	private final Map<ResourceLocation, CommandFunction> functions = new HashMap<>();
	private final Map<ResourceLocation, Collection<CommandFunction>> tags = new HashMap<>();

	public MockPlatform_1_20() {
//		initializeArgumentsInArgumentTypeInfos();

		// Initialize WorldVersion (game version)
		SharedConstants.tryDetectVersion();

		// MockBukkit is very helpful and registers all of the potion
		// effects and enchantments for us. We need to not do this (because
		// we call Bootstrap.bootStrap() below which does the same thing)
		unregisterAllEnchantments();
		unregisterAllPotionEffects();

		// Invoke Minecraft's registry
		Bootstrap.bootStrap();

		// Don't use EnchantmentMock.registerDefaultEnchantments because we want
		// to specify what enchantments to mock (i.e. only 1.18 ones, and not any
		// 1.19 ones!)
		registerDefaultPotionEffects();
		registerDefaultEnchantments();

		this.recipeManager = new RecipeManager();
		registerDefaultRecipes();

		// Setup playerListMock
		playerListMock = Mockito.mock(PlayerList.class);
		Mockito.when(playerListMock.getPlayerByName(anyString())).thenAnswer(invocation -> {
			String playerName = invocation.getArgument(0);
			for (ServerPlayer onlinePlayer : players) {
				if (onlinePlayer.getBukkitEntity().getName().equals(playerName)) {
					return onlinePlayer;
				}
			}
			return null;
		});
	}

	/*************************
	 * Registry manipulation *
	 *************************/

	private void unregisterAllPotionEffects() {
		PotionEffectType[] byId = getFieldAs(PotionEffectType.class, "byId", null, PotionEffectType[].class);
		for (int i = 0; i < byId.length; i++) {
			byId[i] = null;
		}

		getFieldAs(PotionEffectType.class, "byName", null, Map.class).clear();
		getFieldAs(PotionEffectType.class, "byKey", null, Map.class).clear();
		setField(PotionEffectType.class, "acceptingNew", null, true);
	}

	private void registerDefaultPotionEffects() {
		for (PotionEffectType type : PotionEffectType.values()) {
			if (type != null) {
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

	private void registerPotionEffectType(int id, @NotNull String name, boolean instant, int rgb) {
		final NamespacedKey key = NamespacedKey.minecraft(name.toLowerCase(Locale.ROOT));
		PotionEffectType.registerPotionEffectType(new MockPotionEffectType(key, id, name, instant, Color.fromRGB(rgb)));
	}

	private void unregisterAllEnchantments() {
		getFieldAs(Enchantment.class, "byName", null, Map.class).clear();
		getFieldAs(Enchantment.class, "byKey", null, Map.class).clear();
		setField(Enchantment.class, "acceptingNew", null, true);
	}

	private void registerDefaultEnchantments() {
		for (Enchantment enchantment : getEnchantments()) {
			if (Enchantment.getByKey(enchantment.getKey()) == null) {
				Enchantment.registerEnchantment(new EnchantmentMock(enchantment.getKey(), enchantment.getKey().getKey()));
			}
		}
	}

	private void registerDefaultRecipes() {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<Recipe<?>> recipes = (List) getRecipes(MinecraftServer.class)
			.stream()
			.map(p -> RecipeManager.fromJson(new ResourceLocation(p.first()), p.second()))
			.toList();
		recipeManager.replaceRecipes(recipes);
	}

	/**************************
	 * MockPlatform overrides *
	 **************************/

	@Override
	protected CommandAPIBukkit<CommandSourceStack> createCommandAPIBukkitSpy() {
		NMS_1_20_R1 nmsSpy = Mockito.spy(new NMS_1_20_R1());

		// Create CommandBuildContext - private static field
		//  Some ArgumentTypes need this when constructed, and the normal COMMAND_BUILD_CONTEXT
		//  is only defined for CraftServer instances, otherwise our field is null.
		setField(NMS_1_20_R1.class, "COMMAND_BUILD_CONTEXT", null, new MockCommandBuildContext());

		return nmsSpy;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getMinecraftServer() {
		if (minecraftServerMock != null) {
			return (T) minecraftServerMock;
		}
		minecraftServerMock = Mockito.mock(MinecraftServer.class);

		// LootTableArgument
		Mockito.when(minecraftServerMock.getLootData()).thenAnswer(invocation -> {
			//.getKeys(LootDataType.TABLE)
			LootDataManager lootDataManager = Mockito.mock(LootDataManager.class);

			Mockito.when(lootDataManager.getLootTable(any(ResourceLocation.class))).thenAnswer(i -> {
				if (BuiltInLootTables.all().contains(i.getArgument(0))) {
					return net.minecraft.world.level.storage.loot.LootTable.EMPTY;
				} else {
					return null;
				}
			});

			Mockito.when(lootDataManager.getKeys(any())).thenAnswer(i -> {
				return Streams
					.concat(
						Arrays.stream(getEntityTypes())
							.filter(e -> !e.equals(EntityType.UNKNOWN))
							// TODO? These entity types don't have corresponding
							// loot table entries! Did Spigot miss them out?
							.filter(e -> !e.equals(EntityType.ALLAY))
							.filter(e -> !e.equals(EntityType.FROG))
							.filter(e -> !e.equals(EntityType.TADPOLE))
							.filter(e -> !e.equals(EntityType.WARDEN))
							.filter(e -> e.isAlive())
							.map(EntityType::getKey)
							.map(k -> new ResourceLocation("minecraft", "entities/" + k.getKey())),
						BuiltInLootTables.all().stream())
					.collect(Collectors.toSet());
			});
			return lootDataManager;
		});

		// AdvancementArgument
		Mockito.when(minecraftServerMock.getAdvancements()).thenAnswer(i -> advancementDataWorld);

		// TeamArgument
		ServerScoreboard scoreboardServerMock = Mockito.mock(ServerScoreboard.class);
		Mockito.when(scoreboardServerMock.getPlayerTeam(anyString())).thenAnswer(invocation -> { // Scoreboard#getPlayerTeam
			String teamName = invocation.getArgument(0);
			Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(teamName);
			if (team == null) {
				return null;
			} else {
				return new PlayerTeam(scoreboardServerMock, teamName);
			}
		});
		Mockito.when(scoreboardServerMock.getObjective(anyString())).thenAnswer(invocation -> { // Scoreboard#getObjective
			String objectiveName = invocation.getArgument(0);
			org.bukkit.scoreboard.Objective bukkitObjective = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(objectiveName);
			if (bukkitObjective == null) {
				return null;
			} else {
				return new Objective(scoreboardServerMock, objectiveName, ObjectiveCriteria.byName(bukkitObjective.getCriteria()).get(), Component.literal(bukkitObjective.getDisplayName()), switch(bukkitObjective.getRenderType()) {
					case HEARTS:
						yield RenderType.HEARTS;
					case INTEGER:
						yield RenderType.INTEGER;
				});
			}
		});
		Mockito.when(minecraftServerMock.getScoreboard()).thenReturn(scoreboardServerMock); // MinecraftServer#getScoreboard

		// WorldArgument (Dimension)
		Mockito.when(minecraftServerMock.getLevel(any(ResourceKey.class))).thenAnswer(invocation -> {
			// Get the ResourceKey<World> and extract the world name from it
			ResourceKey<Level> resourceKey = invocation.getArgument(0);
			String worldName = resourceKey.location().getPath();

			// Get the world via Bukkit (returns a WorldMock) and create a
			// CraftWorld clone of it for WorldServer.getWorld()
			World world = Bukkit.getServer().getWorld(worldName);
			if (world == null) {
				return null;
			} else {
				CraftWorld craftWorldMock = Mockito.mock(CraftWorld.class);
				Mockito.when(craftWorldMock.getName()).thenReturn(world.getName());
				Mockito.when(craftWorldMock.getUID()).thenReturn(world.getUID());

				// Create our return WorldServer object
				ServerLevel bukkitWorldServerMock = Mockito.mock(ServerLevel.class);
				Mockito.when(bukkitWorldServerMock.getWorld()).thenReturn(craftWorldMock);
				return bukkitWorldServerMock;
			}
		});

		// Player lists
		Mockito.when(minecraftServerMock.getPlayerList()).thenAnswer(i -> playerListMock);
		Mockito.when(minecraftServerMock.getPlayerList().getPlayers()).thenAnswer(i -> players);

		// PlayerArgument
		GameProfileCache userCacheMock = Mockito.mock(GameProfileCache.class);
		Mockito.when(userCacheMock.get(anyString())).thenAnswer(invocation -> {
			String playerName = invocation.getArgument(0);
			for (ServerPlayer onlinePlayer : players) {
				if (onlinePlayer.getBukkitEntity().getName().equals(playerName)) {
					return Optional.of(new GameProfile(onlinePlayer.getBukkitEntity().getUniqueId(), playerName));
				}
			}
			return Optional.empty();
		});
		Mockito.when(minecraftServerMock.getProfileCache()).thenReturn(userCacheMock);

		// RecipeArgument
		Mockito.when(minecraftServerMock.getRecipeManager()).thenAnswer(i -> this.recipeManager);

		// FunctionArgument
		// We're using 2 as the function compilation level.
		Mockito.when(minecraftServerMock.getFunctionCompilationLevel()).thenReturn(2);
		Mockito.when(minecraftServerMock.getFunctions()).thenAnswer(i -> {
			ServerFunctionLibrary serverFunctionLibrary = Mockito.mock(ServerFunctionLibrary.class);

			// Functions
			Mockito.when(serverFunctionLibrary.getFunction(any())).thenAnswer(invocation -> Optional.ofNullable(functions.get(invocation.getArgument(0))));
			Mockito.when(serverFunctionLibrary.getFunctions()).thenAnswer(invocation -> functions);

			// Tags
			Mockito.when(serverFunctionLibrary.getTag(any())).thenAnswer(invocation -> tags.getOrDefault(invocation.getArgument(0), List.of()));
			Mockito.when(serverFunctionLibrary.getAvailableTags()).thenAnswer(invocation -> tags.keySet());

			return new ServerFunctionManager(minecraftServerMock, serverFunctionLibrary) {

				// Make sure we don't use ServerFunctionManager#getDispatcher!
				// That method accesses MinecraftServer.vanillaCommandDispatcher
				// directly (boo) and that causes all sorts of nonsense.
				@Override
				public CommandDispatcher<CommandSourceStack> getDispatcher() {
					return Brigadier.getCommandDispatcher();
				}
			};
		});

		Mockito.when(minecraftServerMock.getGameRules()).thenAnswer(i -> new GameRules());
		Mockito.when(minecraftServerMock.getProfiler()).thenAnswer(i -> InactiveMetricsRecorder.INSTANCE.getProfiler());

		// Brigadier and resources dispatcher, used in `NMS#createCommandRegistrationStrategy`
		Commands brigadierCommands = new Commands();
		MockPlatform.setField(brigadierCommands.getClass(), "g", "dispatcher",
			brigadierCommands, getMockBrigadierDispatcher());
		minecraftServerMock.vanillaCommandDispatcher = brigadierCommands;

		Commands resourcesCommands = new Commands();
		MockPlatform.setField(resourcesCommands.getClass(), "g", "dispatcher",
			resourcesCommands, getMockResourcesDispatcher());
		Mockito.when(minecraftServerMock.getCommands()).thenReturn(resourcesCommands);

		return (T) minecraftServerMock;
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	@Override
	public CommandSourceStack getBrigadierSourceFromCommandSender(AbstractCommandSender<? extends CommandSender> senderWrapper) {
		CommandSender sender = senderWrapper.getSource();
		CommandSourceStack css = Mockito.mock(CommandSourceStack.class);
		Mockito.when(css.getBukkitSender()).thenReturn(sender);

		if (sender instanceof Entity entity) {
			// LocationArgument
			Location loc = entity.getLocation();
			Mockito.when(css.getPosition()).thenReturn(new Vec3(loc.getX(), loc.getY(), loc.getZ()));

			// If entity gives us a ServerLevel, use it, otherwise mock it
			ServerLevel worldServerLevel;
			if(entity.getWorld() instanceof CraftWorld cw) worldServerLevel = cw.getHandle();
			else worldServerLevel = Mockito.mock(ServerLevel.class);

			Mockito.when(css.getLevel()).thenReturn(worldServerLevel);
			Mockito.when(css.getLevel().hasChunkAt(any(BlockPos.class))).thenReturn(true);
	//			Mockito.when(css.getLevel().getBlockState(any(BlockPos.class))).thenAnswer(i -> {
	//				BlockPos bp = i.getArgument(0);
	//				Block b = Bukkit.getWorlds().get(0).getBlockAt(bp.getX(), bp.getY(), bp.getZ());
	//				BlockState bs = Mockito.mock(BlockState.class);
	//				Mockito.when(bs.is(any(net.minecraft.world.level.block.Block.class))).thenAnswer(j -> {
	////					net.minecraft.world.level.block.Block nmsBlock = j.getArgument(0);
	////					nmsBlock.equals(bs.getBlock());
	//					return true;
	//				});
	//				return bs;
	//			});
			Mockito.when(css.getLevel().isInWorldBounds(any(BlockPos.class))).thenReturn(true);
			Mockito.when(css.getAnchor()).thenReturn(EntityAnchorArgument.Anchor.EYES);

			// Get mocked MinecraftServer
			Mockito.when(css.getServer()).thenAnswer(s -> getMinecraftServer());

			// EntitySelectorArgument
			for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
				ServerPlayer entityPlayerMock = Mockito.mock(ServerPlayer.class);
				CraftPlayer craftPlayerMock = Mockito.mock(CraftPlayer.class);

				// Extract these variables first in case the onlinePlayer is a Mockito object itself
				String name = onlinePlayer.getName();
				UUID uuid = onlinePlayer.getUniqueId();

				Mockito.when(craftPlayerMock.getName()).thenReturn(name);
				Mockito.when(craftPlayerMock.getUniqueId()).thenReturn(uuid);
				Mockito.when(entityPlayerMock.getBukkitEntity()).thenReturn(craftPlayerMock);
				Mockito.when(entityPlayerMock.getDisplayName()).thenReturn(net.minecraft.network.chat.Component.literal(name)); // ChatArgument, AdventureChatArgument
				Mockito.when(entityPlayerMock.getType()).thenReturn((net.minecraft.world.entity.EntityType) net.minecraft.world.entity.EntityType.PLAYER); // EntitySelectorArgument
				players.add(entityPlayerMock);
			}

			// CommandSourceStack#levels
			Mockito.when(css.levels()).thenAnswer(invocation -> {
				Set<ResourceKey<Level>> set = new HashSet<>();
				// We only need to implement resourceKey.a()

				for (World world : Bukkit.getWorlds()) {
					ResourceKey<Level> key = Mockito.mock(ResourceKey.class);
					Mockito.when(key.location()).thenReturn(new ResourceLocation(world.getName()));
					set.add(key);
				}

				return set;
			});

			// RotationArgument
			Mockito.when(css.getRotation()).thenReturn(new Vec2(loc.getPitch(), loc.getYaw()));

			// CommandSourceStack#getAllTeams
			Mockito.when(css.getAllTeams()).thenAnswer(invocation -> Bukkit.getScoreboardManager().getMainScoreboard().getTeams().stream().map(Team::getName).toList());

			// SoundArgument
			Mockito.when(css.getAvailableSounds()).thenAnswer(invocation -> BuiltInRegistries.SOUND_EVENT.keySet().stream());

			// RecipeArgument
			Mockito.when(css.getRecipeNames()).thenAnswer(invocation -> recipeManager.getRecipeIds());

			// ChatArgument, AdventureChatArgument
			Mockito.when(css.hasPermission(anyInt())).thenAnswer(invocation -> sender.isOp());
			Mockito.when(css.hasPermission(anyInt(), anyString())).thenAnswer(invocation -> sender.isOp());

			// Suggestions
			Mockito.when(css.enabledFeatures()).thenAnswer(invocation -> FeatureFlags.DEFAULT_FLAGS);

			// FunctionArgument
			// We don't really need to do anything funky here, we'll just return the same CSS
			Mockito.when(css.withSuppressedOutput()).thenReturn(css);
			Mockito.when(css.withMaximumPermission(anyInt())).thenReturn(css);
		} else {
			// `getPosition` and `getRotation` are always accessed when `NMS#getSenderForCommand` is called
			//  If sender is an entity then we can give a physical location, but here we'll just give some defaults
			Mockito.when(css.getPosition()).thenReturn(new Vec3(0, 0, 0));
			Mockito.when(css.getRotation()).thenReturn(new Vec2(0, 0));
		}

		// NativeProxyCommandSender construction (NMS#createNativeProxyCommandSender)
		//  Make sure these builder methods work as expected
		Mockito.when(css.withPosition(any())).thenAnswer(invocation -> {
			Mockito.when(css.getPosition()).thenReturn(invocation.getArgument(0));
			return css;
		});
		Mockito.when(css.withRotation(any())).thenAnswer(invocation -> {
			Mockito.when(css.getRotation()).thenReturn(invocation.getArgument(0));
			return css;
		});
		Mockito.when(css.withLevel(any())).thenAnswer(invocation -> {
			Mockito.when(css.getLevel()).thenReturn(invocation.getArgument(0));
			return css;
		});
		Mockito.when(css.withEntity(any())).thenAnswer(invocation -> {
			net.minecraft.world.entity.Entity entity = invocation.getArgument(0);
			String name = entity.getName().getString();
			net.minecraft.network.chat.Component displayName = entity.getDisplayName();

			Mockito.when(css.getEntity()).thenReturn(entity);
			Mockito.when(css.getTextName()).thenReturn(name);
			Mockito.when(css.getDisplayName()).thenReturn(displayName);
			return css;
		});

		return css;
	}

	@Override
	public SimpleCommandMap getSimpleCommandMap() {
		return ((ServerMock) Bukkit.getServer()).getCommandMap();
	}

	@Override
	public Map<String, HelpTopic> getHelpMap() {
		return helpMapTopics.get((HelpMapMock) Bukkit.getHelpMap());
	}

	@Override
	public ItemFactory getItemFactory() {
		return CraftItemFactory.instance();
	}

	@Override
	public org.bukkit.advancement.Advancement addAdvancement(NamespacedKey key) {
		advancementDataWorld.advancements.advancements.put(new ResourceLocation(key.toString()),
			new Advancement(new ResourceLocation(key.toString()), null, null, null, new HashMap<>(), null, false));
		return new org.bukkit.advancement.Advancement() {

			@Override
			public NamespacedKey getKey() {
				return key;
			}

			@Override
			public Collection<String> getCriteria() {
				return List.of();
			}

			@Override
			public @Nullable AdvancementDisplay getDisplay() {
				throw new IllegalStateException("getDisplay is unimplemented");
			}

			@Override
			public net.kyori.adventure.text.@NotNull Component displayName() {
				throw new IllegalStateException("displayName is unimplemented");
			}

			@Override
			public org.bukkit.advancement.@Nullable Advancement getParent() {
				throw new IllegalStateException("getParent is unimplemented");
			}

			@Override
			public @NotNull @Unmodifiable Collection<org.bukkit.advancement.Advancement> getChildren() {
				throw new IllegalStateException("getChildren is unimplemented");
			}

			@Override
			public org.bukkit.advancement.@NotNull Advancement getRoot() {
				throw new IllegalStateException("getRoot is unimplemented");
			}
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addFunction(NamespacedKey key, List<String> commands) {
		if(Bukkit.getOnlinePlayers().isEmpty()) {
			throw new IllegalStateException("You need to have at least one player on the server to add a function");
		}

		ResourceLocation resourceLocation = new ResourceLocation(key.toString());
		CommandSourceStack css = commandAPIBukkit.getBrigadierSourceFromCommandSender(new BukkitPlayer(Bukkit.getOnlinePlayers().iterator().next()));

		// So for very interesting reasons, Brigadier.getCommandDispatcher()
		// gives a different result in this method than using getBrigadierDispatcher()
		this.functions.put(resourceLocation, CommandFunction.fromLines(resourceLocation, Brigadier.getCommandDispatcher(), css, commands));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addTag(NamespacedKey key, List<List<String>> commands) {
		if(Bukkit.getOnlinePlayers().isEmpty()) {
			throw new IllegalStateException("You need to have at least one player on the server to add a function");
		}

		ResourceLocation resourceLocation = new ResourceLocation(key.toString());
		CommandSourceStack css = commandAPIBukkit.getBrigadierSourceFromCommandSender(new BukkitPlayer(Bukkit.getOnlinePlayers().iterator().next()));

		List<CommandFunction> tagFunctions = new ArrayList<>();
		for(List<String> functionCommands : commands) {
			tagFunctions.add(CommandFunction.fromLines(resourceLocation, Brigadier.getCommandDispatcher(), css, functionCommands));
		}
		this.tags.put(resourceLocation, tagFunctions);
	}

	@Override
	public Player setupMockedCraftPlayer(String name) {
		CraftPlayer player = Mockito.mock(CraftPlayer.class);

		// getLocation and getWorld is used when creating the CommandSourceStack in MockNMS
		ServerLevel serverLevel = Mockito.mock(ServerLevel.class);
		CraftWorld world = Mockito.mock(CraftWorld.class);
		Mockito.when(world.getHandle()).thenReturn(serverLevel);
		Mockito.when(serverLevel.getWorld()).thenReturn(world);

		Mockito.when(player.getLocation()).thenReturn(new Location(world, 0, 0, 0));
		Mockito.when(player.getWorld()).thenReturn(world);

		// Provide proper handle as VanillaCommandWrapper expects
		CommandSourceStack css = commandAPIBukkit.getBrigadierSourceFromCommandSender(commandAPIBukkit.wrapCommandSender(player));

		ServerPlayer handle = Mockito.mock(ServerPlayer.class);
		Mockito.when(handle.createCommandSourceStack()).thenReturn(css);

		Mockito.when(player.getHandle()).thenReturn(handle);


		// getName and getDisplayName are used when CommandSourceStack#withEntity is called
		net.minecraft.network.chat.Component nameComponent = net.minecraft.network.chat.Component.literal(name);
		Mockito.when(handle.getName()).thenReturn(nameComponent);
		Mockito.when(handle.getDisplayName()).thenReturn(nameComponent);

		return player;
	}

	@Override
	public String getBukkitPotionEffectTypeName(PotionEffectType potionEffectType) {
		// NamespacedKey#asString is PAPER ONLY, whereas NamespacedKey#toString
		// is compatible with both paper and Spigot
		return potionEffectType.getKey().toString();
	}

	@Override
	public String getNMSParticleNameFromBukkit(Particle particle) {
		// Didn't want to do it like this, but it's way easier than going via the
		// registry to do all sorts of nonsense with lookups. If you ever want to
		// change your mind, here's how to access it via the registry. This doesn't
		// scale well for pre 1.19 versions though!
		// BuiltInRegistries.PARTICLE_TYPE.getKey(CraftParticle.toNMS(particle).getType()).toString();
		CraftParticle craftParticle = CraftParticle.valueOf(particle.name());
		return MockPlatform.getFieldAs(CraftParticle.class, "minecraftKey", craftParticle, ResourceLocation.class).toString();
	}

	@Override
	public List<String> getAllItemNames() {
		return StreamSupport.stream(BuiltInRegistries.ITEM.spliterator(), false)
			.map(Object::toString)
			.map(s -> "minecraft:" + s)
			.sorted()
			.toList();
	}

	@Override
	public List<NamespacedKey> getAllRecipes() {
		return recipeManager.getRecipeIds().map(k -> new NamespacedKey(k.getNamespace(), k.getPath())).toList();
	}
}
