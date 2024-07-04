package dev.jorel.commandapi.test;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.help.HelpMapMock;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.JsonOps;
import dev.jorel.commandapi.*;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import net.minecraft.SharedConstants;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.BlankFormat;
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
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_20_R4.CraftRegistry;
import org.bukkit.craftbukkit.v1_20_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftItemFactory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.help.HelpTopic;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mockito.Mockito;

import java.security.CodeSource;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.mockito.ArgumentMatchers.*;

public class MockNMS extends Enums {
	private static final SafeVarHandle<HelpMapMock, Map<String, HelpTopic>> helpMapTopics =
		SafeVarHandle.ofOrNull(HelpMapMock.class, "topics", "topics", Map.class);

	static {
		CodeSource src = PotionEffectType.class.getProtectionDomain().getCodeSource();
		if (src != null) {
			System.err.println("Loading PotionEffectType sources from " + src.getLocation());
		}
	}

	static ServerAdvancementManager advancementDataWorld = new ServerAdvancementManager(null);

	MinecraftServer minecraftServerMock = null;
	List<ServerPlayer> players = new ArrayList<>();
	PlayerList playerListMock;
	final RecipeManager recipeManager;
	Map<ResourceLocation, CommandFunction> functions = new HashMap<>();
	Map<ResourceLocation, Collection<CommandFunction>> tags = new HashMap<>();
	Stack<Integer> functionCallbackResults = new Stack<>();

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

		this.recipeManager = new RecipeManager(HolderLookup.Provider.create(Stream.of()));
		this.functions = new HashMap<>();
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
		Mockito.when(playerListMock.getPlayers()).thenAnswer(i -> players);
		
		// CraftBukkit's registry is shared across all instances of a test (or all tests?)
		// Idk if we need to actually implement this, but it seems to make certain issues
		// a little calmer
		if (CraftRegistry.getMinecraftRegistry() == null) {
			RegistryAccess registry = Mockito.mock(RegistryAccess.class);
			Mockito.when(registry.registry(any())).thenAnswer(args -> {
				ResourceKey key = args.getArgument(0);
//				System.out.println("Trying to access registry: " + key.registry());
//				Thread.currentThread().dumpStack();
				return Optional.ofNullable(null);
			});
			Mockito.when(registry.registryOrThrow(any())).thenAnswer(args -> {
				ResourceKey key = args.getArgument(0);
//				System.out.println("Trying to access registry: " + key.registry());
//				Thread.currentThread().dumpStack();
				return null;
			});
			CraftRegistry.setMinecraftRegistry(registry);
		}
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
		// PotionEffectType.stopAcceptingRegistrations();
	}

	private void registerPotionEffectType(int id, @NotNull String name, boolean instant, int rgb) {
		final NamespacedKey key = NamespacedKey.minecraft(name.toLowerCase(Locale.ROOT));
		// PotionEffectType.registerPotionEffectType(new MockPotionEffectType(key, id, name, instant, Color.fromRGB(rgb)));
	}

	private void unregisterAllEnchantments() {
		getFieldAs(Enchantment.class, "byName", null, Map.class).clear();
		getFieldAs(Enchantment.class, "byKey", null, Map.class).clear();
		setField(Enchantment.class, "acceptingNew", null, true);
	}

	private void registerDefaultEnchantments() {
		// Can't test enchantments until MockBukkit is compatible with 1.20.5 (i.e. it's using paper-api 1.20.5+)
//		for (Enchantment enchantment : getEnchantments()) {
//			if (Enchantment.getByKey(enchantment.getKey()) == null) {
//				Enchantment enchantmentToRegister = new EnchantmentMock(enchantment.getKey(), enchantment.getKey().getKey());
//				MockPlatform.getInstance().addToRegistry(Enchantment.class, enchantment.getKey(), enchantmentToRegister);
//			}
//		}
	}

	private void registerDefaultRecipes() {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<RecipeHolder<?>> recipes = (List) getRecipes(MinecraftServer.class)
			.stream()
			.map(p -> {
				// From RecipeManager#fromJson which isn't accessible
				final Recipe recipe = Recipe.CODEC.parse(JsonOps.INSTANCE, p.second()).getOrThrow(JsonParseException::new);
				return new RecipeHolder(new ResourceLocation(p.first()), recipe);
			})
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
		return StreamSupport.stream(BuiltInRegistries.ITEM.spliterator(), false)
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
			Mockito.when(css.callback()).thenReturn((success, result) -> {
				functionCallbackResults.push(result);
			});
		} else {
			// `getPosition` and `getRotation` are always accessed when `NMS#getSenderForCommand` is called
			//  If sender is an entity then we can give a physical location, but here we'll just give some defaults
			Mockito.when(css.getPosition()).thenReturn(new Vec3(0, 0, 0));
			Mockito.when(css.getRotation()).thenReturn(new Vec2(0, 0));
		}
		return css;
	}

	@Override
	public NativeProxyCommandSender getNativeProxyCommandSender(CommandContext<CommandSourceStack> cmdCtx) {
		return baseNMS.getNativeProxyCommandSender(cmdCtx);
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
		// NamespacedKey#asString is PAPER ONLY, whereas NamespacedKey#toString
		// is compatible with both paper and Spigot
		return potionEffectType.getKey().toString();
	}

	@Override
	public String getNMSParticleNameFromBukkit(Particle particle) {
		// CraftRegistry.setMinecraftRegistry(this.<MinecraftServer>getMinecraftServer().getServer().registryAccess());
		// Didn't want to do it like this, but it's way easier than going via the
		// registry to do all sorts of nonsense with lookups. If you ever want to
		// change your mind, here's how to access it via the registry. This doesn't
		// scale well for pre 1.19 versions though!
		// BuiltInRegistries.PARTICLE_TYPE.getKey(CraftParticle.toNMS(particle).getType()).toString();
		// particle.getKey();
		// return BuiltInRegistries.PARTICLE_TYPE.getKey(CraftParticle.bukkitToMinecraft(particle)).toString();
		// return particle.getKey().toString();
		
		Map<org.bukkit.Particle, String> particleMap = Map.ofEntries(
			Map.entry(Particle.valueOf("EXPLOSION_NORMAL"), "poof"), 
			Map.entry(Particle.valueOf("EXPLOSION_LARGE"), "explosion"), 
			Map.entry(Particle.valueOf("EXPLOSION_HUGE"), "explosion_emitter"), 
			Map.entry(Particle.valueOf("FIREWORKS_SPARK"), "firework"), 
			Map.entry(Particle.valueOf("WATER_BUBBLE"), "bubble"), 
			Map.entry(Particle.valueOf("WATER_SPLASH"), "splash"), 
			Map.entry(Particle.valueOf("WATER_WAKE"), "fishing"), 
			Map.entry(Particle.valueOf("SUSPENDED"), "underwater"), 
//				Map.entry(Particle.valueOf("SUSPENDED_DEPTH"), put("???"), 
			Map.entry(Particle.valueOf("CRIT"), "crit"), 
			Map.entry(Particle.valueOf("CRIT_MAGIC"), "enchanted_hit"), 
			Map.entry(Particle.valueOf("SMOKE_NORMAL"), "smoke"), 
			Map.entry(Particle.valueOf("SMOKE_LARGE"), "large_smoke"), 
			Map.entry(Particle.valueOf("SPELL"), "effect"), 
			Map.entry(Particle.valueOf("SPELL_INSTANT"), "instant_effect"), 
			Map.entry(Particle.valueOf("SPELL_MOB"), "entity_effect"), 
//				Map.entry(Particle.valueOf("SPELL_MOB_AMBIENT"), put("???"), 
			Map.entry(Particle.valueOf("SPELL_WITCH"), "witch"), 
			Map.entry(Particle.valueOf("DRIP_WATER"), "dripping_water"), 
			Map.entry(Particle.valueOf("DRIP_LAVA"), "dripping_lava"), 
			Map.entry(Particle.valueOf("VILLAGER_ANGRY"), "angry_villager"), 
			Map.entry(Particle.valueOf("VILLAGER_HAPPY"), "happy_villager"), 
			Map.entry(Particle.valueOf("TOWN_AURA"), "mycelium"), 
			Map.entry(Particle.valueOf("NOTE"), "note"), 
			Map.entry(Particle.valueOf("PORTAL"), "portal"), 
			Map.entry(Particle.valueOf("ENCHANTMENT_TABLE"), "enchant"), 
			Map.entry(Particle.valueOf("FLAME"), "flame"), 
			Map.entry(Particle.valueOf("LAVA"), "lava"), 
			Map.entry(Particle.valueOf("CLOUD"), "cloud"), 
			Map.entry(Particle.valueOf("REDSTONE"), "dust"), 
			Map.entry(Particle.valueOf("SNOWBALL"), "item_snowball"), 
			Map.entry(Particle.valueOf("SNOW_SHOVEL"), "item_slime"), 
//				Map.entry(Particle.valueOf("SLIME"), put("???"), 
			Map.entry(Particle.valueOf("HEART"), "heart"), 
			Map.entry(Particle.valueOf("ITEM_CRACK"), "item"), 
			Map.entry(Particle.valueOf("BLOCK_CRACK"), "block"), 
			Map.entry(Particle.valueOf("BLOCK_DUST"), "rain"), 
//				Map.entry(Particle.valueOf("WATER_DROP"), put("???"), 
			Map.entry(Particle.valueOf("MOB_APPEARANCE"), "elder_guardian"), 
			Map.entry(Particle.valueOf("DRAGON_BREATH"), "dragon_breath"), 
			Map.entry(Particle.valueOf("END_ROD"), "end_rod"), 
			Map.entry(Particle.valueOf("DAMAGE_INDICATOR"), "damage_indicator"), 
			Map.entry(Particle.valueOf("SWEEP_ATTACK"), "sweep_attack"), 
			Map.entry(Particle.valueOf("FALLING_DUST"), "falling_dust"), 
			Map.entry(Particle.valueOf("TOTEM"), "totem_of_undying"), 
			Map.entry(Particle.valueOf("SPIT"), "spit"), 
			Map.entry(Particle.valueOf("SQUID_INK"), "squid_ink"), 
			Map.entry(Particle.valueOf("BUBBLE_POP"), "bubble_pop"), 
			Map.entry(Particle.valueOf("CURRENT_DOWN"), "current_down"), 
			Map.entry(Particle.valueOf("BUBBLE_COLUMN_UP"), "bubble_column_up"), 
			Map.entry(Particle.valueOf("NAUTILUS"), "nautilus"), 
			Map.entry(Particle.valueOf("DOLPHIN"), "dolphin"), 
			Map.entry(Particle.valueOf("SNEEZE"), "sneeze"), 
			Map.entry(Particle.valueOf("CAMPFIRE_COSY_SMOKE"), "campfire_cosy_smoke"), 
			Map.entry(Particle.valueOf("CAMPFIRE_SIGNAL_SMOKE"), "campfire_signal_smoke"), 
			Map.entry(Particle.valueOf("COMPOSTER"), "composter"), 
			Map.entry(Particle.valueOf("FLASH"), "flash"), 
			Map.entry(Particle.valueOf("FALLING_LAVA"), "falling_lava"), 
			Map.entry(Particle.valueOf("LANDING_LAVA"), "landing_lava"), 
			Map.entry(Particle.valueOf("FALLING_WATER"), "falling_water"), 
			Map.entry(Particle.valueOf("DRIPPING_HONEY"), "dripping_honey"), 
			Map.entry(Particle.valueOf("FALLING_HONEY"), "falling_honey"), 
			Map.entry(Particle.valueOf("LANDING_HONEY"), "landing_honey"), 
			Map.entry(Particle.valueOf("FALLING_NECTAR"), "falling_nectar"), 
			Map.entry(Particle.valueOf("SOUL_FIRE_FLAME"), "soul_fire_flame"), 
			Map.entry(Particle.valueOf("ASH"), "ash"), 
			Map.entry(Particle.valueOf("CRIMSON_SPORE"), "crimson_spore"), 
			Map.entry(Particle.valueOf("WARPED_SPORE"), "warped_spore"), 
			Map.entry(Particle.valueOf("SOUL"), "soul"), 
			Map.entry(Particle.valueOf("DRIPPING_OBSIDIAN_TEAR"), "dripping_obsidian_tear"), 
			Map.entry(Particle.valueOf("FALLING_OBSIDIAN_TEAR"), "falling_obsidian_tear"), 
			Map.entry(Particle.valueOf("LANDING_OBSIDIAN_TEAR"), "landing_obsidian_tear"), 
			Map.entry(Particle.valueOf("REVERSE_PORTAL"), "reverse_portal"), 
			Map.entry(Particle.valueOf("WHITE_ASH"), "white_ash"), 
			Map.entry(Particle.valueOf("DUST_COLOR_TRANSITION"), "dust_color_transition"), 
			Map.entry(Particle.valueOf("VIBRATION"), "vibration"), 
			Map.entry(Particle.valueOf("FALLING_SPORE_BLOSSOM"), "falling_spore_blossom"), 
			Map.entry(Particle.valueOf("SPORE_BLOSSOM_AIR"), "spore_blossom_air"), 
			Map.entry(Particle.valueOf("SMALL_FLAME"), "small_flame"), 
			Map.entry(Particle.valueOf("SNOWFLAKE"), "snowflake"), 
			Map.entry(Particle.valueOf("DRIPPING_DRIPSTONE_LAVA"), "dripping_dripstone_lava"), 
			Map.entry(Particle.valueOf("FALLING_DRIPSTONE_LAVA"), "falling_dripstone_lava"), 
			Map.entry(Particle.valueOf("DRIPPING_DRIPSTONE_WATER"), "dripping_dripstone_water"), 
			Map.entry(Particle.valueOf("FALLING_DRIPSTONE_WATER"), "falling_dripstone_water"), 
			Map.entry(Particle.valueOf("GLOW_SQUID_INK"), "glow_squid_ink"), 
			Map.entry(Particle.valueOf("GLOW"), "glow"), 
			Map.entry(Particle.valueOf("WAX_ON"), "wax_on"), 
			Map.entry(Particle.valueOf("WAX_OFF"), "wax_off"), 
			Map.entry(Particle.valueOf("ELECTRIC_SPARK"), "electric_spark"), 
			Map.entry(Particle.valueOf("SCRAPE"), "scrape"), 
			Map.entry(Particle.valueOf("SONIC_BOOM"), "sonic_boom"), 
			Map.entry(Particle.valueOf("SCULK_SOUL"), "sculk_soul"), 
			Map.entry(Particle.valueOf("SCULK_CHARGE"), "sculk_charge"), 
			Map.entry(Particle.valueOf("SCULK_CHARGE_POP"), "sculk_charge_pop"), 
			Map.entry(Particle.valueOf("SHRIEK"), "shriek"), 
			Map.entry(Particle.valueOf("CHERRY_LEAVES"), "cherry_leaves"), 
			Map.entry(Particle.valueOf("EGG_CRACK"), "egg_crack"), 
			Map.entry(Particle.valueOf("BLOCK_MARKER"), "dust_plume")
		);

		final String particleName = particleMap.get(particle);
		if (particleName == null) {
			System.err.println("Couldn't find particle name for " + particle);
		}
		return particleName;
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
//		Mockito.when(minecraftServerMock.getLootData()).thenAnswer(invocation -> {
//			//.getKeys(LootDataType.TABLE)
//			LootDataManager lootDataManager = Mockito.mock(LootDataManager.class);
//			
//			Mockito.when(lootDataManager.getLootTable(any(ResourceLocation.class))).thenAnswer(i -> {
//				if (BuiltInLootTables.all().contains(i.getArgument(0))) {
//					return net.minecraft.world.level.storage.loot.LootTable.EMPTY;
//				} else {
//					return null;
//				}
//			});
//			
//			Mockito.when(lootDataManager.getKeys(any())).thenAnswer(i -> {
//				return Streams
//					.concat(
//						Arrays.stream(getEntityTypes())
//							.filter(e -> !e.equals(EntityType.UNKNOWN))
//							// TODO? These entity types don't have corresponding
//							// loot table entries! Did Spigot miss them out?
//							.filter(e -> !e.equals(EntityType.ALLAY))
//							.filter(e -> !e.equals(EntityType.FROG))
//							.filter(e -> !e.equals(EntityType.TADPOLE))
//							.filter(e -> !e.equals(EntityType.WARDEN))
//							.filter(e -> e.isAlive())
//							.map(EntityType::getKey)
//							.map(k -> new ResourceLocation("minecraft", "entities/" + k.getKey())),
//						BuiltInLootTables.all().stream())
//					.collect(Collectors.toSet());
//			});
//			return lootDataManager;
//		});

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
				}, true /* displayAutoUpdate */, BlankFormat.INSTANCE /* numberFormat */);
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
		Mockito.doAnswer(invocation -> {
			EntityTypeTest<net.minecraft.world.entity.Entity, ?> typeTest = invocation.getArgument(0);
			// Make sure we are actually looking for players first
			if (typeTest.tryCast(Mockito.mock(ServerPlayer.class)) != null) {
				((List<ServerPlayer>) invocation.getArgument(2)).addAll(players);
			}
			return null;
		}).when(entityWorld).getEntities(any(), any(), any(), anyInt());
		Mockito.when(minecraftServerMock.getAllLevels()).thenReturn(List.of(entityWorld));

		// Player lists
		Mockito.when(minecraftServerMock.getPlayerList()).thenAnswer(i -> playerListMock);

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
		MockPlatform.setField(brigadierCommands.getClass(), "h", "dispatcher",
			brigadierCommands, getMockBrigadierDispatcher());
		minecraftServerMock.vanillaCommandDispatcher = brigadierCommands;

		Commands resourcesCommands = new Commands();
		MockPlatform.setField(resourcesCommands.getClass(), "h", "dispatcher",
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

		Component nameComponent = Component.literal(name);
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
		final Advancement advancement = new Advancement(Optional.empty(), Optional.empty(), null, new HashMap<>(), null, false);
		
		// Redeclare as a new map to prevent immutability issues with Map.of() definition
		advancementDataWorld.advancements = new HashMap<>(advancementDataWorld.advancements);
		advancementDataWorld.advancements.put(new ResourceLocation(key.toString()), new AdvancementHolder(new ResourceLocation(key.toString()), advancement));
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
			public @Nullable org.bukkit.advancement.AdvancementDisplay getDisplay() {
				throw new IllegalStateException("getDisplay is unimplemented");
			}

//			@Override
//			public @NotNull Component displayName() {
//				throw new IllegalStateException("displayName is unimplemented");
//			}
//
//			@Override
//			public org.bukkit.advancement.@Nullable Advancement getParent() {
//				throw new IllegalStateException("getParent is unimplemented");
//			}
//
//			@Override
//			public @NotNull @Unmodifiable Collection<org.bukkit.advancement.Advancement> getChildren() {
//				throw new IllegalStateException("getChildren is unimplemented");
//			}
//
//			@Override
//			public org.bukkit.advancement.@NotNull Advancement getRoot() {
//				throw new IllegalStateException("getRoot is unimplemented");
//			}
		};
	}

	@Override
	public CommandSender getCommandSenderFromCommandSource(CommandSourceStack clw) {
		return baseNMS.getCommandSenderFromCommandSource(clw);
	}

	@Override
	public int popFunctionCallbackResult() {
		return functionCallbackResults.pop();
	}

//	@Override
//	public void createDispatcherFile(File file, CommandDispatcher<CommandSourceStack> dispatcher)
//		throws IOException {
//		Files
//			.asCharSink(file, StandardCharsets.UTF_8)
//			.write(new GsonBuilder()
//				.setPrettyPrinting()
//				.create()
//				.toJson(DispatcherUtil.toJSON(dispatcher, dispatcher.getRoot())));
//	}
//
//	@SuppressWarnings("rawtypes")
//	private void initializeArgumentsInArgumentTypeInfos() {
//		@SuppressWarnings("unchecked")
//		Map<Class<?>, ArgumentTypeInfo<?, ?>> map = getFieldAs(ArgumentTypeInfos.class, "a", null, Map.class);
//		map.put(BoolArgumentType.class, SingletonArgumentInfo.contextFree(BoolArgumentType::bool));
//		map.put(FloatArgumentType.class, new FloatArgumentInfo());
//		map.put(DoubleArgumentType.class, new DoubleArgumentInfo());
//		map.put(IntegerArgumentType.class, new IntegerArgumentInfo());
//		map.put(LongArgumentType.class, new LongArgumentInfo());
//		map.put(StringArgumentType.class, new StringArgumentSerializer());
//		map.put(EntityArgument.class, new EntityArgument.Info());
//		map.put(GameProfileArgument.class, SingletonArgumentInfo.contextFree(GameProfileArgument::gameProfile));
//		map.put(BlockPosArgument.class, SingletonArgumentInfo.contextFree(BlockPosArgument::blockPos));
//		map.put(ColumnPosArgument.class, SingletonArgumentInfo.contextFree(ColumnPosArgument::columnPos));
//		map.put(Vec3Argument.class, SingletonArgumentInfo.contextFree(Vec3Argument::vec3));
//		map.put(Vec2Argument.class, SingletonArgumentInfo.contextFree(Vec2Argument::vec2));
//		map.put(BlockStateArgument.class, SingletonArgumentInfo.contextAware(BlockStateArgument::block));
//		map.put(BlockPredicateArgument.class, SingletonArgumentInfo.contextAware(BlockPredicateArgument::blockPredicate));
//		map.put(ItemArgument.class, SingletonArgumentInfo.contextAware(ItemArgument::item));
//		map.put(ItemPredicateArgument.class, SingletonArgumentInfo.contextAware(ItemPredicateArgument::itemPredicate));
//		map.put(ColorArgument.class, SingletonArgumentInfo.contextFree(ColorArgument::color));
//		map.put(ComponentArgument.class, SingletonArgumentInfo.contextFree(ComponentArgument::textComponent));
//		map.put(MessageArgument.class, SingletonArgumentInfo.contextFree(MessageArgument::message));
//		map.put(CompoundTagArgument.class, SingletonArgumentInfo.contextFree(CompoundTagArgument::compoundTag));
//		map.put(NbtTagArgument.class, SingletonArgumentInfo.contextFree(NbtTagArgument::nbtTag));
//		map.put(NbtPathArgument.class, SingletonArgumentInfo.contextFree(NbtPathArgument::nbtPath));
//		map.put(ObjectiveArgument.class, SingletonArgumentInfo.contextFree(ObjectiveArgument::objective));
//		map.put(ObjectiveCriteriaArgument.class, SingletonArgumentInfo.contextFree(ObjectiveCriteriaArgument::criteria));
//		map.put(OperationArgument.class, SingletonArgumentInfo.contextFree(OperationArgument::operation));
//		map.put(ParticleArgument.class, SingletonArgumentInfo.contextFree(ParticleArgument::particle));
//		map.put(AngleArgument.class, SingletonArgumentInfo.contextFree(AngleArgument::angle));
//		map.put(RotationArgument.class, SingletonArgumentInfo.contextFree(RotationArgument::rotation));
//		map.put(ScoreboardSlotArgument.class, SingletonArgumentInfo.contextFree(ScoreboardSlotArgument::displaySlot));
//		map.put(ScoreHolderArgument.class, new ScoreHolderArgument.Info());
//		map.put(SwizzleArgument.class, SingletonArgumentInfo.contextFree(SwizzleArgument::swizzle));
//		map.put(TeamArgument.class, SingletonArgumentInfo.contextFree(TeamArgument::team));
//		map.put(SlotArgument.class, SingletonArgumentInfo.contextFree(SlotArgument::slot));
//		map.put(ResourceLocationArgument.class, SingletonArgumentInfo.contextFree(ResourceLocationArgument::id));
//		map.put(MobEffectArgument.class, SingletonArgumentInfo.contextFree(MobEffectArgument::effect));
//		map.put(FunctionArgument.class, SingletonArgumentInfo.contextFree(FunctionArgument::functions));
//		map.put(EntityAnchorArgument.class, SingletonArgumentInfo.contextFree(EntityAnchorArgument::anchor));
//		map.put(RangeArgument.Ints.class, SingletonArgumentInfo.contextFree(RangeArgument::intRange));
//		map.put(RangeArgument.Floats.class, SingletonArgumentInfo.contextFree(RangeArgument::floatRange));
//		map.put(ItemEnchantmentArgument.class, SingletonArgumentInfo.contextFree(ItemEnchantmentArgument::enchantment));
//		map.put(EntitySummonArgument.class, SingletonArgumentInfo.contextFree(EntitySummonArgument::id));
//		map.put(DimensionArgument.class, SingletonArgumentInfo.contextFree(DimensionArgument::dimension));
//		map.put(TimeArgument.class, SingletonArgumentInfo.contextFree(TimeArgument::time));
//		map.put(ResourceOrTagLocationArgument.class, new ResourceOrTagLocationArgument.Info());
//		map.put(ResourceKeyArgument.class, new ResourceKeyArgument.Info());
//		map.put(TemplateMirrorArgument.class, SingletonArgumentInfo.contextFree(TemplateMirrorArgument::templateMirror));
//		map.put(TemplateRotationArgument.class, SingletonArgumentInfo.contextFree(TemplateRotationArgument::templateRotation));
//		map.put(UuidArgument.class, SingletonArgumentInfo.contextFree(UuidArgument::uuid));
//	}
//
//	/**
//	 * An implementation of {@link ArgumentUtils} which produces JSON from a command
//	 * dispatcher and its root node. We have to avoid accessing IRegistry because it
//	 * isn't mock-able and cannot be instantiated through normal means
//	 */
//	private static class DispatcherUtil {
//
//		static Map<Class<?>, String> argumentParsers = new HashMap<>();
//
//		static {
//			argumentParsers.put(BoolArgumentType.class, "brigadier:bool");
//			argumentParsers.put(FloatArgumentType.class, "brigadier:float");
//			argumentParsers.put(DoubleArgumentType.class, "brigadier:double");
//			argumentParsers.put(IntegerArgumentType.class, "brigadier:integer");
//			argumentParsers.put(LongArgumentType.class, "brigadier:long");
//			argumentParsers.put(StringArgumentType.class, "brigadier:string");
//			argumentParsers.put(EntityArgument.class, "entity");
//			argumentParsers.put(GameProfileArgument.class, "game_profile");
//			argumentParsers.put(BlockPosArgument.class, "block_pos");
//			argumentParsers.put(ColumnPosArgument.class, "column_pos");
//			argumentParsers.put(Vec3Argument.class, "vec3");
//			argumentParsers.put(Vec2Argument.class, "vec2");
//			argumentParsers.put(BlockStateArgument.class, "block_state");
//			argumentParsers.put(BlockPredicateArgument.class, "block_predicate");
//			argumentParsers.put(ItemArgument.class, "item_stack");
//			argumentParsers.put(ItemPredicateArgument.class, "item_predicate");
//			argumentParsers.put(ColorArgument.class, "color");
//			argumentParsers.put(ComponentArgument.class, "component");
//			argumentParsers.put(MessageArgument.class, "message");
//			argumentParsers.put(CompoundTagArgument.class, "nbt_compound_tag");
//			argumentParsers.put(NbtTagArgument.class, "nbt_tag");
//			argumentParsers.put(NbtPathArgument.class, "nbt_path");
//			argumentParsers.put(ObjectiveArgument.class, "objective");
//			argumentParsers.put(ObjectiveCriteriaArgument.class, "objective_criteria");
//			argumentParsers.put(OperationArgument.class, "operation");
//			argumentParsers.put(ParticleArgument.class, "particle");
//			argumentParsers.put(AngleArgument.class, "angle");
//			argumentParsers.put(RotationArgument.class, "rotation");
//			argumentParsers.put(ScoreboardSlotArgument.class, "scoreboard_slot");
//			argumentParsers.put(ScoreHolderArgument.class, "score_holder");
//			argumentParsers.put(SwizzleArgument.class, "swizzle");
//			argumentParsers.put(TeamArgument.class, "team");
//			argumentParsers.put(SlotArgument.class, "item_slot");
//			argumentParsers.put(ResourceLocationArgument.class, "resource_location");
//			argumentParsers.put(MobEffectArgument.class, "mob_effect");
//			argumentParsers.put(FunctionArgument.class, "function");
//			argumentParsers.put(EntityAnchorArgument.class, "entity_anchor");
//			argumentParsers.put(RangeArgument.Ints.class, "int_range");
//			argumentParsers.put(RangeArgument.Floats.class, "float_range");
//			argumentParsers.put(ItemEnchantmentArgument.class, "item_enchantment");
//			argumentParsers.put(EntitySummonArgument.class, "entity_summon");
//			argumentParsers.put(DimensionArgument.class, "dimension");
//			argumentParsers.put(TimeArgument.class, "time");
//			argumentParsers.put(ResourceOrTagLocationArgument.class, "resource_or_tag");
//			argumentParsers.put(ResourceKeyArgument.class, "resource");
//			argumentParsers.put(TemplateMirrorArgument.class, "template_mirror");
//			argumentParsers.put(TemplateRotationArgument.class, "template_rotation");
//			argumentParsers.put(UuidArgument.class, "uuid");
//		}
//
//		public static <S> JsonObject toJSON(CommandDispatcher<S> dispatcher, CommandNode<S> node) {
//			JsonObject jsonObject = new JsonObject();
//
//			// Unpack nodes
//			if (node instanceof RootCommandNode) {
//				jsonObject.addProperty("type", "root");
//			} else if (node instanceof LiteralCommandNode) {
//				jsonObject.addProperty("type", "literal");
//			} else if (node instanceof ArgumentCommandNode) {
//				ArgumentCommandNode<?, ?> argumentCommandNode = (ArgumentCommandNode<?, ?>) node;
//				argToJSON(jsonObject, argumentCommandNode.getType());
//			} else {
//				jsonObject.addProperty("type", "unknown");
//			}
//
//			// Write children
//			JsonObject children = new JsonObject();
//			for (CommandNode<S> child : node.getChildren()) {
//				children.add(child.getName(), (JsonElement) toJSON(dispatcher, child));
//			}
//			if (children.size() > 0) {
//				jsonObject.add("children", (JsonElement) children);
//			}
//
//			// Write whether the command is executable
//			if (node.getCommand() != null) {
//				jsonObject.addProperty("executable", Boolean.valueOf(true));
//			}
//			if (node.getRedirect() != null) {
//				Collection<String> redirectPaths = dispatcher.getPath(node.getRedirect());
//				if (!redirectPaths.isEmpty()) {
//					JsonArray redirects = new JsonArray();
//					for (String redirectPath : redirectPaths) {
//						redirects.add(redirectPath);
//					}
//					jsonObject.add("redirect", (JsonElement) redirects);
//				}
//			}
//			return jsonObject;
//		}
//
//		@SuppressWarnings("unchecked")
//		private static <T extends ArgumentType<?>> void argToJSON(JsonObject jsonObject, T argument) {
//			ArgumentTypeInfo.Template<T> argumentInfo = ArgumentTypeInfos.unpack(argument);
//			jsonObject.addProperty("type", "argument");
//			jsonObject.addProperty("parser", argumentParsers.get(argument.getClass()));
//
//			// Properties
//			JsonObject properties = new JsonObject();
//			@SuppressWarnings("rawtypes")
//			ArgumentTypeInfo argumentTypeInfo = argumentInfo.type();
//			argumentTypeInfo.serializeToJson(argumentInfo, properties);
//			if (properties.size() > 0) {
//				jsonObject.add("properties", (JsonElement) properties);
//			}
//		}
//	}

	@Override
	public Map<String, HelpTopic> getHelpMap() {
		return helpMapTopics.get((HelpMapMock) Bukkit.getHelpMap());
	}

	@Override
	public CommandRegistrationStrategy<CommandSourceStack> createCommandRegistrationStrategy() {
		return baseNMS.createCommandRegistrationStrategy();
	}
}
