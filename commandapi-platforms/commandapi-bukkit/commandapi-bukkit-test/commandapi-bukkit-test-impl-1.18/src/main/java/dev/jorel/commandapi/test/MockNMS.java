package dev.jorel.commandapi.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

import java.security.CodeSource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import be.seeseemelk.mockbukkit.help.HelpMapMock;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import dev.jorel.commandapi.*;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import net.minecraft.commands.Commands;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_18_R1.CraftParticle;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemFactory;
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

import com.google.common.collect.Streams;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.enchantments.EnchantmentMock;
import be.seeseemelk.mockbukkit.potion.MockPotionEffectType;
import io.papermc.paper.advancement.AdvancementDisplay;
import net.minecraft.SharedConstants;
import net.minecraft.advancements.Advancement;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.ServerFunctionLibrary;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.PlayerList;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.profiling.metrics.profiling.InactiveMetricsRecorder;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;

public class MockNMS extends Enums {
	private static final SafeVarHandle<HelpMapMock, Map<String, HelpTopic>> helpMapTopics =
		SafeVarHandle.ofOrNull(HelpMapMock.class, "topics", "topics", Map.class);

	static {
		CodeSource src = PotionEffectType.class.getProtectionDomain().getCodeSource();
		if (src != null) {
			System.err.println("Loading PotionEffectType sources from " + src.getLocation());
		}
		src = MinecraftServer.class.getProtectionDomain().getCodeSource();
		if (src != null) {
			System.err.println("Loading MinecraftServer sources from " + src.getLocation());
		}
	}

	static ServerAdvancementManager advancementDataWorld = new ServerAdvancementManager(null);

	MinecraftServer minecraftServerMock = null;
	List<ServerPlayer> players = new ArrayList<>();
	PlayerList playerListMock;
	final RecipeManager recipeManager;
	Map<ResourceLocation, CommandFunction> functions = new HashMap<>();
	Map<ResourceLocation, Collection<CommandFunction>> tags = new HashMap<>();

	public MockNMS(CommandAPIBukkit<?> baseNMS) {
		super(baseNMS);

		CommandAPIBukkit<CommandSourceStack> nms = Mockito.spy(super.baseNMS);
		// Stub in our getMinecraftServer implementation
		Mockito.when(nms.getMinecraftServer()).thenAnswer(i -> getMinecraftServer());
		// Stub in our getSimpleCommandMap implementation
		//  Note that calling `nms.getSimpleCommandMap()` throws a
		//  class cast exception  (`CraftServer` vs `CommandAPIServerMock`),
		//  so we have to mock with `doAnswer` instead of `when`
		Mockito.doAnswer(i -> getSimpleCommandMap()).when(nms).getSimpleCommandMap();
		super.baseNMS = nms;

		// Initialize baseNMS's paper field (with paper specific implementations disabled)
		MockPlatform.setField(CommandAPIBukkit.class, "paper",
			super.baseNMS, new PaperImplementations<>(false, false, super.baseNMS));

		// Initialize WorldVersion (game version)
		SharedConstants.tryDetectVersion();

		// MockBukkit is very helpful and registers all of the potion
		// effects and enchantments for us. We need to not do this (because
		// we call Bootstrap.bootStrap() below which does the same thing)
		unregisterAllEnchantments();
		unregisterAllPotionEffects();

		// Invoke Minecraft's registry. This also initializes all argument types.
		// How convenient!
		Bootstrap.bootStrap();

		// Don't use EnchantmentMock.registerDefaultEnchantments because we want
		// to specify what enchantments to mock (i.e. only 1.18 ones, and not any
		// 1.19 ones!)
		registerDefaultPotionEffects();
		registerDefaultEnchantments();

		this.recipeManager = new RecipeManager();
		this.functions = new HashMap<>();
		registerDefaultRecipes();

		// Set up playerListMock
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
		Mockito.when(playerListMock.getPlayers()).thenAnswer(i -> players);
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
	public ItemFactory getItemFactory() {
		return CraftItemFactory.instance();
	}

	@Override
	public List<String> getAllItemNames() {
		return StreamSupport.stream(Registry.ITEM.spliterator(), false)
			.map(Object::toString)
			.map(s -> "minecraft:" + s)
			.sorted()
			.toList();
	}

	@Override
	public String[] compatibleVersions() {
		return baseNMS.compatibleVersions();
	}

	@Override
	public SimpleCommandMap getSimpleCommandMap() {
		return ((ServerMock) Bukkit.getServer()).getCommandMap();
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	@Override
	public CommandSourceStack getBrigadierSourceFromCommandSender(CommandSender sender) {
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
			Mockito.when(css.getLevel().isInWorldBounds(any(BlockPos.class))).thenReturn(true);
			Mockito.when(css.getAnchor()).thenReturn(Anchor.EYES);

			if (entity instanceof CraftPlayer craftPlayer) {
				// If the sender is a CraftPlayer, it was probably created by `#wrapPlayerMockIntoCraftPlayer`,
				//  in which case `getHandle` will return what we want.
				net.minecraft.world.entity.Entity nmsEntity = craftPlayer.getHandle();
				Mockito.when(css.getEntity()).thenReturn(nmsEntity);
			}

			// Get mocked MinecraftServer
			Mockito.when(css.getServer()).thenAnswer(s -> getMinecraftServer());

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
			Mockito.when(css.getAllTeams()).thenAnswer(invocation -> {
				return Bukkit.getScoreboardManager().getMainScoreboard().getTeams().stream().map(Team::getName).toList();
			});

			// SoundArgument
			Mockito.when(css.getAvailableSoundEvents()).thenAnswer(invocation -> Registry.SOUND_EVENT.keySet());
			
			// RecipeArgument
			Mockito.when(css.getRecipeNames()).thenAnswer(invocation -> recipeManager.getRecipeIds());

			// ChatArgument, AdventureChatArgument
			Mockito.when(css.hasPermission(anyInt())).thenAnswer(invocation -> sender.isOp());
			Mockito.when(css.hasPermission(anyInt(), anyString())).thenAnswer(invocation -> sender.isOp());

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
		return css;
	}

	@Override
	public NativeProxyCommandSender getNativeProxyCommandSender(CommandSender sender, CommandSourceStack css) {
		return baseNMS.getNativeProxyCommandSender(sender, css);
	}

	@Override
	public Optional<JsonObject> getArgumentTypeProperties(ArgumentType<?> type) {
		return baseNMS.getArgumentTypeProperties(type);
	}

	@Override
	public World getWorldForCSS(CommandSourceStack clw) {
		return baseNMS.getWorldForCSS(clw);
	}

	@Override
	public String getBukkitPotionEffectTypeName(PotionEffectType potionEffectType) {
		return potionEffectType.getKey().asString();
	}
	
	@Override
	public String getNMSParticleNameFromBukkit(Particle particle) {
		CraftParticle craftParticle = CraftParticle.valueOf(particle.name());
		return MockPlatform.getFieldAs(CraftParticle.class, "minecraftKey", craftParticle, ResourceLocation.class).toString();
	}
	
	@Override
	public List<NamespacedKey> getAllRecipes() {
		return recipeManager.getRecipeIds().map(k -> new NamespacedKey(k.getNamespace(), k.getPath())).toList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getMinecraftServer() {
		if (minecraftServerMock != null) {
			return (T) minecraftServerMock;
		}
		minecraftServerMock = Mockito.mock(MinecraftServer.class);

		// LootTableArgument
		Mockito.when(minecraftServerMock.getLootTables()).thenAnswer(invocation -> {
			LootTables lootTables = Mockito.mock(LootTables.class);
			Mockito.when(lootTables.get(any(ResourceLocation.class))).thenAnswer(i -> {
				if (BuiltInLootTables.all().contains(i.getArgument(0))) {
					return net.minecraft.world.level.storage.loot.LootTable.EMPTY;
				} else {
					return null;
				}
			});
			Mockito.when(lootTables.getIds()).thenAnswer(i -> {
				return Streams
					.concat(
						Arrays.stream(getEntityTypes())
							.filter(e -> !e.equals(EntityType.UNKNOWN))
							.filter(e -> e.isAlive())
							.map(EntityType::getKey)
							.map(k -> new ResourceLocation("minecraft", "entities/" + k.getKey())),
						BuiltInLootTables.all().stream())
					.collect(Collectors.toSet());
			});
			return lootTables;
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
				return new Objective(scoreboardServerMock, objectiveName, ObjectiveCriteria.byName(bukkitObjective.getCriteria()).get(), new TextComponent(bukkitObjective.getDisplayName()), switch(bukkitObjective.getRenderType()) {
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

		// EntitySelectorArgument for entities wants to loop over all levels to get all the entities
		//  We'll just sneakily pass it our players as all the entities
		ServerLevel entityWorld = Mockito.mock(ServerLevel.class);
		Mockito.when(entityWorld.getEntities(any(EntityTypeTest.class), any())).thenAnswer(invocation -> {
			EntityTypeTest<net.minecraft.world.entity.Entity, ?> typeTest = invocation.getArgument(0);
			// Make sure we are actually looking for players first
			if (typeTest.tryCast(Mockito.mock(ServerPlayer.class)) != null) {
				return players;
			}
			return List.of();
		});
		Mockito.when(minecraftServerMock.getAllLevels()).thenReturn(List.of(entityWorld));

		// Player lists
		Mockito.when(minecraftServerMock.getPlayerList()).thenAnswer(i -> playerListMock);

		// Player argument
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
			Mockito.when(serverFunctionLibrary.getTag(any())).thenAnswer(invocation -> {
				Collection<CommandFunction> tagsFromResourceLocation = tags.getOrDefault(invocation.getArgument(0), List.of());
				return Tag.fromSet(Set.copyOf(tagsFromResourceLocation));
			});
			Mockito.when(serverFunctionLibrary.getTags()).thenAnswer(invocation -> {
				Map<ResourceLocation, Tag<?>> tagMap = new HashMap<>();
				for(Map.Entry<ResourceLocation, Collection<CommandFunction>> entry : tags.entrySet()) {
					tagMap.put(entry.getKey(), Tag.fromSet(Set.copyOf(entry.getValue())));
				}
				return TagCollection.of((Map) tagMap);
			});

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

	@SuppressWarnings("unchecked")
	@Override
	public void addFunction(NamespacedKey key, List<String> commands) {
		if(Bukkit.getOnlinePlayers().isEmpty()) {
			throw new IllegalStateException("You need to have at least one player on the server to add a function");
		}

		ResourceLocation resourceLocation = new ResourceLocation(key.toString());
		CommandSourceStack css = getBrigadierSourceFromCommandSender(Bukkit.getOnlinePlayers().iterator().next());

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
		CommandSourceStack css = getBrigadierSourceFromCommandSender(Bukkit.getOnlinePlayers().iterator().next());

		List<CommandFunction> tagFunctions = new ArrayList<>();
		for(List<String> functionCommands : commands) {
			tagFunctions.add(CommandFunction.fromLines(resourceLocation, Brigadier.getCommandDispatcher(), css, functionCommands));
		}
		this.tags.put(resourceLocation, tagFunctions);
	}

	@Override
	public Player wrapPlayerMockIntoCraftPlayer(Player playerMock) {
		// Create player mock objects
		CraftPlayer craftPlayerMock = Mockito.mock(CraftPlayer.class);
		ServerPlayer serverPlayerMock = Mockito.mock(ServerPlayer.class);

		// Link handle and player
		Mockito.when(craftPlayerMock.getHandle()).thenReturn(serverPlayerMock);
		Mockito.when(serverPlayerMock.getBukkitEntity()).thenReturn(craftPlayerMock);

		// Name
		String name = playerMock.getName();

		Mockito.when(craftPlayerMock.getName()).thenReturn(name);
		Mockito.when(serverPlayerMock.getScoreboardName()).thenReturn(name);

		TextComponent nameComponent = new TextComponent(name);
		Mockito.when(serverPlayerMock.getName()).thenReturn(nameComponent);
		Mockito.when(serverPlayerMock.getDisplayName()).thenReturn(nameComponent);

		// UUID
		UUID uuid = playerMock.getUniqueId();
		Mockito.when(craftPlayerMock.getUniqueId()).thenReturn(uuid);

		// World and Location
		ServerLevel serverLevel = Mockito.mock(ServerLevel.class);
		CraftWorld world = Mockito.mock(CraftWorld.class);
		Mockito.when(world.getHandle()).thenReturn(serverLevel);
		Mockito.when(serverLevel.getWorld()).thenReturn(world);

		Mockito.when(craftPlayerMock.getLocation()).thenReturn(new Location(world, 0, 0, 0));
		Mockito.when(craftPlayerMock.getWorld()).thenReturn(world);

		// EntitySelectorArgument
		Mockito.when(serverPlayerMock.getType()).thenReturn(
			(net.minecraft.world.entity.EntityType) net.minecraft.world.entity.EntityType.PLAYER
		);

		// Provide proper handle as VanillaCommandWrapper expects
		CommandSourceStack css = getBrigadierSourceFromCommandSender(craftPlayerMock);
		Mockito.when(serverPlayerMock.createCommandSourceStack()).thenReturn(css);

		// Add to player list
		players.add(serverPlayerMock);

		return craftPlayerMock;
	}

	@Override
	public org.bukkit.advancement.Advancement addAdvancement(NamespacedKey key) {
		advancementDataWorld.advancements.advancements.put(new ResourceLocation(key.toString()),
			new Advancement(new ResourceLocation(key.toString()), null, null, null, new HashMap<>(), null));
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

	@Override
	public CommandSender getCommandSenderFromCommandSource(CommandSourceStack clw) {
		return baseNMS.getCommandSenderFromCommandSource(clw);
	}

	@Override
	public Map<String, HelpTopic> getHelpMap() {
		return helpMapTopics.get((HelpMapMock) Bukkit.getHelpMap());
	}

	@Override
	public CommandRegistrationStrategy<CommandSourceStack> createCommandRegistrationStrategy() {
		return baseNMS.createCommandRegistrationStrategy();
	}
}
