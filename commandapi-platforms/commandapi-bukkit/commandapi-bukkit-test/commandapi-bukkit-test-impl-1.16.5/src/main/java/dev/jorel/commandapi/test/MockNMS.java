package dev.jorel.commandapi.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

import java.io.File;
import java.io.IOException;
import java.security.CodeSource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import be.seeseemelk.mockbukkit.help.HelpMapMock;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import dev.jorel.commandapi.*;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import org.bukkit.craftbukkit.v1_16_R3.CraftParticle;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemFactory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.help.HelpTopic;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.enchantments.EnchantmentMock;
import be.seeseemelk.mockbukkit.potion.MockPotionEffectType;
import dev.jorel.commandapi.commandsenders.AbstractCommandSender;
import dev.jorel.commandapi.commandsenders.BukkitCommandSender;
import dev.jorel.commandapi.commandsenders.BukkitPlayer;
import net.minecraft.server.v1_16_R3.Advancement;
import net.minecraft.server.v1_16_R3.AdvancementDataWorld;
import net.minecraft.server.v1_16_R3.ArgumentAnchor.Anchor;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import net.minecraft.server.v1_16_R3.CraftingManager;
import net.minecraft.server.v1_16_R3.CustomFunction;
import net.minecraft.server.v1_16_R3.CustomFunctionData;
import net.minecraft.server.v1_16_R3.DispenserRegistry;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.GameProfilerDisabled;
import net.minecraft.server.v1_16_R3.GameRules;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.IRecipe;
import net.minecraft.server.v1_16_R3.IRegistry;
import net.minecraft.server.v1_16_R3.IScoreboardCriteria;
import net.minecraft.server.v1_16_R3.LootTableRegistry;
import net.minecraft.server.v1_16_R3.LootTables;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import net.minecraft.server.v1_16_R3.MobEffectList;
import net.minecraft.server.v1_16_R3.PlayerList;
import net.minecraft.server.v1_16_R3.Recipes;
import net.minecraft.server.v1_16_R3.ResourceKey;
import net.minecraft.server.v1_16_R3.ScoreboardObjective;
import net.minecraft.server.v1_16_R3.ScoreboardServer;
import net.minecraft.server.v1_16_R3.ScoreboardTeam;
import net.minecraft.server.v1_16_R3.SharedConstants;
import net.minecraft.server.v1_16_R3.Tag;
import net.minecraft.server.v1_16_R3.UserCache;
import net.minecraft.server.v1_16_R3.Vec2F;
import net.minecraft.server.v1_16_R3.Vec3D;
import net.minecraft.server.v1_16_R3.WorldServer;

public class MockNMS extends Enums {
	private static final SafeVarHandle<HelpMapMock, Map<String, HelpTopic>> helpMapTopics =
		SafeVarHandle.ofOrNull(HelpMapMock.class, "topics", "topics", Map.class);

	static {
		CodeSource src = PotionEffectType.class.getProtectionDomain().getCodeSource();
		if (src != null) {
			System.err.println("Loading PotionEffectType sources from " + src.getLocation());
		}
	}

	static AdvancementDataWorld advancementDataWorld = new AdvancementDataWorld(null);
	MinecraftServer minecraftServerMock = null;
	List<EntityPlayer> players = new ArrayList<>();
	PlayerList playerListMock;
	final CraftingManager recipeManager;
	Map<MinecraftKey, CustomFunction> functions = new HashMap<>();
	Map<MinecraftKey, Collection<CustomFunction>> tags = new HashMap<>();

	public MockNMS(CommandAPIBukkit<?> baseNMS) {
		super(baseNMS);

		CommandAPIBukkit<CommandListenerWrapper> nms = Mockito.spy(super.baseNMS);
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
			super.baseNMS, new PaperImplementations(false, false, super.baseNMS));

		// Initialize WorldVersion (game version)
		SharedConstants.b();

		// MockBukkit is very helpful and registers all of the potion
		// effects and enchantments for us. We need to not do this (because
		// we call Bootstrap.bootStrap() below which does the same thing)
		unregisterAllEnchantments();
		unregisterAllPotionEffects();

		// Invoke Minecraft's registry. This also initializes all argument types.
		// How convenient!
		DispenserRegistry.init();

		// Don't use EnchantmentMock.registerDefaultEnchantments because we want
		// to specify what enchantments to mock (i.e. only 1.18 ones, and not any
		// 1.19 ones!)
		registerDefaultPotionEffects();
		registerDefaultEnchantments();

		this.recipeManager = new CraftingManager();
		this.functions = new HashMap<>();
		registerDefaultRecipes();

		// Set up playerListMock
		playerListMock = Mockito.mock(PlayerList.class);
		Mockito.when(playerListMock.getPlayer(anyString())).thenAnswer(invocation -> {
			String playerName = invocation.getArgument(0);
			for (EntityPlayer onlinePlayer : players) {
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

	private void registerPotionEffectType(int id, String name, boolean instant, int rgb) {
		PotionEffectType.registerPotionEffectType(new MockPotionEffectType(id, name, instant, Color.fromRGB(rgb)));
	}

	private void unregisterAllEnchantments() {
		getFieldAs(Enchantment.class, "byName", null, Map.class).clear();
		getFieldAs(Enchantment.class, "byKey", null, Map.class).clear();
		setField(Enchantment.class, "acceptingNew", null, true);
	}

	private void registerDefaultEnchantments() {
		for (Enchantment enchantment : getInstance().getEnchantments()) {
			if (Enchantment.getByKey(enchantment.getKey()) == null) {
				Enchantment.registerEnchantment(new EnchantmentMock(enchantment.getKey(), enchantment.getKey().getKey()));
			}
		}
	}
	
	private void registerDefaultRecipes() {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<IRecipe<?>> recipes = (List) getRecipes(MinecraftServer.class)
			.stream()
			.map(p -> CraftingManager.a(new MinecraftKey(p.first()), p.second()))
			.toList();
		
		// 1.16 and below doesn't have the lovely CraftingManager#replaceRecipes method
		// that 1.17+ has, so we'll just stub it in here:
		Map<Recipes<?>, Object2ObjectLinkedOpenHashMap<MinecraftKey, IRecipe<?>>> map = new HashMap<>();
		for(IRecipe<?> recipe : recipes) {
			Map<MinecraftKey, IRecipe<?>> innerMap = map.computeIfAbsent(recipe.g(), r -> new Object2ObjectLinkedOpenHashMap<>());
			
			final MinecraftKey recipeID = recipe.getKey();
			innerMap.put(recipeID, recipe);
		}
		
		setField(CraftingManager.class, "recipes", recipeManager, ImmutableMap.copyOf(map));
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
		return StreamSupport.stream(IRegistry.ITEM.spliterator(), false)
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

	@Override
	public CommandListenerWrapper getBrigadierSourceFromCommandSender(AbstractCommandSender<? extends CommandSender> senderWrapper) {
		CommandSender sender = senderWrapper.getSource();
		CommandListenerWrapper clw = Mockito.mock(CommandListenerWrapper.class);
		Mockito.when(clw.getBukkitSender()).thenReturn(sender);

		if (sender instanceof Entity entity) {
			// LocationArgument
			Location loc = entity.getLocation();
			Mockito.when(clw.getPosition()).thenReturn(new Vec3D(loc.getX(), loc.getY(), loc.getZ()));

			// If entity gives us a ServerLevel, use it, otherwise mock it
			WorldServer worldServer;
			if(entity.getWorld() instanceof CraftWorld cw) worldServer = cw.getHandle();
			else worldServer = Mockito.mock(WorldServer.class);

			Mockito.when(clw.getWorld()).thenReturn(worldServer);
//			Mockito.when(clw.getWorld().hasChunkAt(any(BlockPosition.class))).thenReturn(true);
//			Mockito.when(clw.getWorld().isInWorldBounds(any(BlockPosition.class))).thenReturn(true);
			Mockito.when(clw.k()).thenReturn(Anchor.EYES);

			// Get mocked MinecraftServer
			Mockito.when(clw.getServer()).thenAnswer(s -> getMinecraftServer());

			// EntitySelectorArgument
			for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
				EntityPlayer entityPlayerMock = Mockito.mock(EntityPlayer.class);
				CraftPlayer craftPlayerMock = Mockito.mock(CraftPlayer.class);

				// Extract these variables first in case the onlinePlayer is a Mockito object itself
				String name = onlinePlayer.getName();
				UUID uuid = onlinePlayer.getUniqueId();

				Mockito.when(craftPlayerMock.getName()).thenReturn(name);
				Mockito.when(craftPlayerMock.getUniqueId()).thenReturn(uuid);
				Mockito.when(entityPlayerMock.getBukkitEntity()).thenReturn(craftPlayerMock);
				Mockito.when(entityPlayerMock.getDisplayName()).thenReturn(new ChatComponentText(name)); // ChatArgument, AdventureChatArgument
				Mockito.when(entityPlayerMock.getScoreboardDisplayName()).thenReturn(new ChatComponentText(name)); // ChatArgument, AdventureChatArgument
				players.add(entityPlayerMock);
			}

			// CommandListenerWrapper#levels
			Mockito.when(clw.p()).thenAnswer(invocation -> {
				Set<ResourceKey<net.minecraft.server.v1_16_R3.World>> set = new HashSet<>();
				// We only need to implement resourceKey.a()

				for (World world : Bukkit.getWorlds()) {
					@SuppressWarnings("unchecked")
					ResourceKey<net.minecraft.server.v1_16_R3.World> key = Mockito.mock(ResourceKey.class);
					Mockito.when(key.a()).thenReturn(new MinecraftKey(world.getName()));
					set.add(key);
				}

				return set;
			});

			// Rotation argument
			Mockito.when(clw.i()).thenReturn(new Vec2F(loc.getPitch(), loc.getYaw()));

			// CommandListenerWrapper#getAllTeams
			Mockito.when(clw.m()).thenAnswer(invocation -> {
				return Bukkit.getScoreboardManager().getMainScoreboard().getTeams().stream().map(Team::getName).toList();
			});
			
			// SoundArgument
			Mockito.when(clw.n()).thenAnswer(invocation -> IRegistry.SOUND_EVENT.keySet());
			
			// RecipeArgument
			Mockito.when(clw.o()).thenAnswer(invocation -> recipeManager.d());
			
			// ChatArgument, AdventureChatArgument
			Mockito.when(clw.hasPermission(anyInt())).thenAnswer(invocation -> sender.isOp());
			Mockito.when(clw.hasPermission(anyInt(), anyString())).thenAnswer(invocation -> sender.isOp());

			// FunctionArgument
			// We don't really need to do anything funky here, we'll just return the same CSS
			Mockito.when(clw.a()).thenReturn(clw);
			Mockito.when(clw.b(anyInt())).thenReturn(clw);
		} else {
			// `getPosition` and `getRotation` are always accessed when `NMS#getSenderForCommand` is called
			//  If sender is an entity then we can give a physical location, but here we'll just give some defaults
			Mockito.when(clw.getPosition()).thenReturn(new Vec3D(0, 0, 0));
			Mockito.when(clw.i()).thenReturn(new Vec2F(0, 0));
		}
		return clw;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void createDispatcherFile(File file, CommandDispatcher dispatcher)
		throws IOException {
		baseNMS.createDispatcherFile(file, dispatcher);
	}

	@Override
	public World getWorldForCSS(CommandListenerWrapper clw) {
		return baseNMS.getWorldForCSS(clw);
	}

	@SuppressWarnings({ "deprecation", "null" })
	@Override
	public String getBukkitPotionEffectTypeName(PotionEffectType potionEffectType) {
		return MobEffectList.fromId(potionEffectType.getId()).c().replace("effect.minecraft.", "minecraft:");
	}
	
	@Override
	public String getNMSParticleNameFromBukkit(Particle particle) {
		CraftParticle craftParticle = CraftParticle.valueOf(particle.name());
		return MockPlatform.getFieldAs(CraftParticle.class, "minecraftKey", craftParticle, MinecraftKey.class).toString();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public List<NamespacedKey> getAllRecipes() {
		return recipeManager.d().map(k -> new NamespacedKey(k.getNamespace(), k.getKey())).toList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getMinecraftServer() {
		if (minecraftServerMock != null) {
			return (T) minecraftServerMock;
		}
		minecraftServerMock = Mockito.mock(MinecraftServer.class);

		// LootTableArgument
		Mockito.when(minecraftServerMock.getLootTableRegistry()).thenAnswer(invocation -> {
			LootTableRegistry lootTables = Mockito.mock(LootTableRegistry.class);
			Mockito.when(lootTables.getLootTable(any(MinecraftKey.class))).thenAnswer(i -> {
				if (LootTables.a().contains(i.getArgument(0))) {
					return net.minecraft.server.v1_16_R3.LootTable.EMPTY;
				} else {
					return null;
				}
			});
			Mockito.when(lootTables.a()).thenAnswer(i -> {
				return Streams
					.concat(
						Arrays.stream(getEntityTypes())
							.filter(e -> !e.equals(EntityType.UNKNOWN))
							.filter(e -> e.isAlive())
							.map(EntityType::getKey)
							.map(k -> new MinecraftKey("minecraft", "entities/" + k.getKey())),
						LootTables.a().stream())
					.collect(Collectors.toSet());
			});
			return lootTables;
		});

		// AdvancementArgument
		Mockito.when(minecraftServerMock.getAdvancementData()).thenAnswer(i -> advancementDataWorld);

		// TeamArgument
		ScoreboardServer scoreboardServerMock = Mockito.mock(ScoreboardServer.class);
		Mockito.when(scoreboardServerMock.getTeam(anyString())).thenAnswer(invocation -> { // Scoreboard#getTeam is used for 1.16.5 instead of Scoreboard#getPlayerTeam
			String teamName = invocation.getArgument(0);
			Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(teamName);
			if (team == null) {
				return null;
			} else {
				return new ScoreboardTeam(scoreboardServerMock, teamName);
			}
		});
		Mockito.when(scoreboardServerMock.getObjective(anyString())).thenAnswer(invocation -> { // Scoreboard#getObjective
			String objectiveName = invocation.getArgument(0);
			org.bukkit.scoreboard.Objective bukkitObjective = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(objectiveName);
			if (bukkitObjective == null) {
				return null;
			} else {
				return new ScoreboardObjective(scoreboardServerMock, objectiveName, IScoreboardCriteria.a(bukkitObjective.getCriteria()).get(), new ChatComponentText(bukkitObjective.getDisplayName()), switch(bukkitObjective.getRenderType()) {
					case HEARTS:
						yield IScoreboardCriteria.EnumScoreboardHealthDisplay.HEARTS;
					case INTEGER:
						yield IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER;
				});
			}
		});
		Mockito.when(minecraftServerMock.getScoreboard()).thenReturn(scoreboardServerMock); // MinecraftServer#getScoreboard

		// WorldArgument (Dimension)
		Mockito.when(minecraftServerMock.getWorldServer(any(ResourceKey.class))).thenAnswer(invocation -> {
			// Get the ResourceKey<World> and extract the world name from it
			ResourceKey<net.minecraft.server.v1_16_R3.World> resourceKey = invocation.getArgument(0);
			String worldName = resourceKey.a().getKey();

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
				WorldServer bukkitWorldServerMock = Mockito.mock(WorldServer.class);
				Mockito.when(bukkitWorldServerMock.getWorld()).thenReturn(craftWorldMock);
				return bukkitWorldServerMock;
			}
		});

		// Player lists
		Mockito.when(minecraftServerMock.getPlayerList()).thenAnswer(i -> playerListMock);
		Mockito.when(minecraftServerMock.getPlayerList().getPlayers()).thenAnswer(i -> players);

		// PlayerArgument
		UserCache userCacheMock = Mockito.mock(UserCache.class);
		Mockito.when(userCacheMock.getProfile(anyString())).thenAnswer(invocation -> {
			String playerName = invocation.getArgument(0);
			for (EntityPlayer onlinePlayer : players) {
				if (onlinePlayer.getBukkitEntity().getName().equals(playerName)) {
					return new GameProfile(onlinePlayer.getBukkitEntity().getUniqueId(), playerName);
				}
			}
			return null;
		});
		Mockito.when(minecraftServerMock.getUserCache()).thenReturn(userCacheMock);
		
		// RecipeArgument
		Mockito.when(minecraftServerMock.getCraftingManager()).thenAnswer(i -> this.recipeManager);

		// FunctionArgument
		// We're using 2 as the function compilation level.
		// Mockito.when(minecraftServerMock.??()).thenReturn(2);
		Mockito.when(minecraftServerMock.getFunctionData()).thenAnswer(i -> {
			CustomFunctionData customFunctionData = Mockito.mock(CustomFunctionData.class);

			// Functions
			Mockito.when(customFunctionData.a(any(MinecraftKey.class))).thenAnswer(invocation -> Optional.ofNullable(functions.get(invocation.getArgument(0))));
			Mockito.when(customFunctionData.f()).thenAnswer(invocation -> functions.keySet());

			// Tags
			Mockito.when(customFunctionData.b(any())).thenAnswer(invocation -> {
				Collection<CustomFunction> tagsFromResourceLocation = tags.getOrDefault(invocation.getArgument(0), List.of());
				return Tag.b(Set.copyOf(tagsFromResourceLocation));
			});
			Mockito.when(customFunctionData.g()).thenAnswer(invocation -> tags.keySet());
			
			// Command dispatcher
			Mockito.when(customFunctionData.getCommandDispatcher()).thenAnswer(invocation -> Brigadier.getCommandDispatcher());
			
			// Command chain length
			Mockito.when(customFunctionData.b()).thenReturn(65536);
			
			// Function execution
			Mockito.when(customFunctionData.a(any(CustomFunction.class), any(CommandListenerWrapper.class))).thenAnswer(invocation -> {
				// Call the function?
				CustomFunction customFunction = invocation.getArgument(0);
				return customFunction.b().length;
			});

			return customFunctionData;
		});
		
		Mockito.when(minecraftServerMock.getGameRules()).thenAnswer(i -> new GameRules());
		Mockito.when(minecraftServerMock.getMethodProfiler()).thenAnswer(i -> GameProfilerDisabled.a);

		// Brigadier and resources dispatcher, used in `NMS#createCommandRegistrationStrategy`
		net.minecraft.server.v1_16_R3.CommandDispatcher brigadierCommands = new net.minecraft.server.v1_16_R3.CommandDispatcher();
		MockPlatform.setField(brigadierCommands.getClass(), "b",
			brigadierCommands, getMockBrigadierDispatcher());
		minecraftServerMock.vanillaCommandDispatcher = brigadierCommands;

		net.minecraft.server.v1_16_R3.CommandDispatcher resourcesCommands = new net.minecraft.server.v1_16_R3.CommandDispatcher();
		MockPlatform.setField(resourcesCommands.getClass(), "b",
			resourcesCommands, getMockResourcesDispatcher());
		Mockito.when(minecraftServerMock.getCommandDispatcher()).thenReturn(resourcesCommands);

		return (T) minecraftServerMock;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addFunction(NamespacedKey key, List<String> commands) {
		if(Bukkit.getOnlinePlayers().isEmpty()) {
			throw new IllegalStateException("You need to have at least one player on the server to add a function");
		}

		MinecraftKey resourceLocation = new MinecraftKey(key.toString());
		CommandListenerWrapper css = getBrigadierSourceFromCommandSender(new BukkitPlayer(Bukkit.getOnlinePlayers().iterator().next()));

		// So for very interesting reasons, Brigadier.getCommandDispatcher()
		// gives a different result in this method than using getBrigadierDispatcher()
		this.functions.put(resourceLocation, CustomFunction.a(resourceLocation, Brigadier.getCommandDispatcher(), css, commands));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addTag(NamespacedKey key, List<List<String>> commands) {
		if(Bukkit.getOnlinePlayers().isEmpty()) {
			throw new IllegalStateException("You need to have at least one player on the server to add a function");
		}

		MinecraftKey resourceLocation = new MinecraftKey(key.toString());
		CommandListenerWrapper css = getBrigadierSourceFromCommandSender(new BukkitPlayer(Bukkit.getOnlinePlayers().iterator().next()));

		List<CustomFunction> tagFunctions = new ArrayList<>();
		for(List<String> functionCommands : commands) {
			tagFunctions.add(CustomFunction.a(resourceLocation, Brigadier.getCommandDispatcher(), css, functionCommands));
		}
		this.tags.put(resourceLocation, tagFunctions);
	}

	@Override
	public Player setupMockedCraftPlayer(String name) {
		CraftPlayer player = Mockito.mock(CraftPlayer.class);

		// getLocation and getWorld is used when creating the CommandSourceStack in MockNMS
		WorldServer worldServer = Mockito.mock(WorldServer.class);
		CraftWorld world = Mockito.mock(CraftWorld.class);
		Mockito.when(world.getHandle()).thenReturn(worldServer);
		Mockito.when(worldServer.getWorld()).thenReturn(world);

		Mockito.when(player.getLocation()).thenReturn(new Location(world, 0, 0, 0));
		Mockito.when(player.getWorld()).thenReturn(world);

		// Provide proper handle as VanillaCommandWrapper expects
		CommandListenerWrapper css = getBrigadierSourceFromCommandSender(wrapCommandSender(player));

		EntityPlayer handle = Mockito.mock(EntityPlayer.class);
		Mockito.when(handle.getCommandListener()).thenReturn(css);

		Mockito.when(player.getHandle()).thenReturn(handle);


		// getDisplayName and getScoreboardDisplayName are used when CommandSourceStack#withEntity is called
		IChatBaseComponent nameComponent = new ChatComponentText(name);
		Mockito.when(handle.getDisplayName()).thenReturn(nameComponent);
		Mockito.when(handle.getScoreboardDisplayName()).thenReturn(nameComponent);

		return player;
	}

	@Override
	public org.bukkit.advancement.Advancement addAdvancement(NamespacedKey key) {
		advancementDataWorld.REGISTRY.advancements.put(new MinecraftKey(key.toString()),
			new Advancement(new MinecraftKey(key.toString()), null, null, null, new HashMap<>(), null));

		return new org.bukkit.advancement.Advancement() {

			@Override
			public NamespacedKey getKey() {
				return key;
			}

			@Override
			public Collection<String> getCriteria() {
				return List.of();
			}
		};
	}

	@Override
	public BukkitCommandSender<? extends CommandSender> getSenderForCommand(CommandContext<CommandListenerWrapper> cmdCtx, boolean forceNative) {
		return baseNMS.getSenderForCommand(cmdCtx, forceNative);
	}

	@Override
	public NativeProxyCommandSender createNativeProxyCommandSender(CommandSender caller, CommandSender callee, Location location, World world) {
		return baseNMS.createNativeProxyCommandSender(caller, callee, location, world);
	}

	@Override
	public BukkitCommandSender<? extends CommandSender> getCommandSenderFromCommandSource(CommandListenerWrapper cs) {
		try {
			return wrapCommandSender(cs.getBukkitSender());
		} catch (UnsupportedOperationException e) {
			return null;
		}
	}
	
	@Override
	public HelpTopic generateHelpTopic(String commandName, String shortDescription, String fullDescription, String permission) {
		return baseNMS.generateHelpTopic(commandName, shortDescription, fullDescription, permission);
	}

	@Override
	public Map<String, HelpTopic> getHelpMap() {
		return helpMapTopics.get((HelpMapMock) Bukkit.getHelpMap());
	}

	@Override
	public CommandRegistrationStrategy<CommandListenerWrapper> createCommandRegistrationStrategy() {
		return baseNMS.createCommandRegistrationStrategy();
	}
}
