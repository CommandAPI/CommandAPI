package dev.jorel.commandapi.test;

import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import dev.jorel.commandapi.Brigadier;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.SafeVarHandle;
import dev.jorel.commandapi.commandsenders.AbstractCommandSender;
import dev.jorel.commandapi.commandsenders.BukkitCommandSender;
import dev.jorel.commandapi.commandsenders.BukkitPlayer;
import dev.jorel.commandapi.nms.NMS;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.ServerFunctionLibrary;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.Services;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.UserNameToIdResolver;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemFactory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.help.HelpTopic;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.help.HelpMapMock;
import org.mockito.Answers;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

public class MockNMS extends Enums {
	private static final SafeVarHandle<HelpMapMock, Map<String, HelpTopic>> helpMapTopics =
		SafeVarHandle.ofOrNull(HelpMapMock.class, "topics", "topics", Map.class);

	MinecraftServer minecraftServerMock = null;
	List<ServerPlayer> players = new ArrayList<>();
	PlayerList playerListMock;
	final RecipeManager recipeManager;
	final Map<Identifier, AdvancementHolder> advancements = new HashMap<>();
	Map<Identifier, CommandFunction<CommandSourceStack>> functions = new HashMap<>();
	Map<Identifier, Collection<CommandFunction<CommandSourceStack>>> tags = new HashMap<>();
	Stack<Integer> functionCallbackResults = new Stack<>();

	// Bukkit's registries, created like CraftServer#getRegistry. This can't have an initializer, because Bukkit
	//  registries may be looked up while MockNMS is being constructed.
	private Map<Class<?>, Registry<?>> craftRegistries;

	public MockNMS(NMS<?> baseNMS) {
		super(baseNMS);

		NMS<CommandSourceStack> nms = Mockito.spy(super.baseNMS);
		// Stub in our getMinecraftServer implementation
		Mockito.when(nms.getMinecraftServer()).thenAnswer(i -> getMinecraftServer());
		// Stub in our getSimpleCommandMap implementation
		//  Note that calling `nms.getSimpleCommandMap()` throws a
		//  class cast exception  (`CraftServer` vs `CommandAPIServerMock`),
		//  so we have to mock with `doAnswer` instead of `when`
		Mockito.doAnswer(i -> getSimpleCommandMap()).when(nms).getSimpleCommandMap();
		super.baseNMS = nms;

		// Initializes the game version and Minecraft's registries
		RegistryAccess registryAccess = MockRegistries.registryAccess();

		this.recipeManager = new RecipeManager(HolderLookup.Provider.create(Stream.of(createRecipeRegistry(registryAccess))));

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

		// CraftBukkit looks up particles, sounds, potion effects, etc. in this registry. It's normally the server's
		//  registry, so use the test registries instead. The registry is shared across all tests.
		if (CraftRegistry.getMinecraftRegistry() == null) {
			CraftRegistry.setMinecraftRegistry(registryAccess);
		}
	}

	/*************************
	 * Registry manipulation *
	 *************************/

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Keyed> Registry<T> getRegistry(Class<T> className) {
		// Bukkit's potion effects, enchantments, etc. are backed by CraftBukkit's registries
		if (craftRegistries == null) {
			craftRegistries = new HashMap<>();
		}
		Registry<T> craftRegistry = (Registry<T>) craftRegistries.computeIfAbsent(className, this::createCraftRegistry);
		return craftRegistry == null ? super.getRegistry(className) : craftRegistry;
	}

	/**
	 * Creates the Bukkit registry for the given class, like the server's {@code getRegistry}
	 *
	 * @param className the Bukkit class to get the registry for
	 * @return the registry, or null if there is no Minecraft registry for this class
	 */
	protected Registry<?> createCraftRegistry(Class<?> className) {
		try {
			return CraftRegistry.createRegistry((Class) className, MockRegistries.registryAccess());
		} catch (IllegalStateException | IllegalArgumentException missingRegistry) {
			return null;
		}
	}

	/**
	 * Since 1.21.2, the {@link RecipeManager} gets its recipes from the (reloadable) recipe registry, which is
	 * normally loaded from data packs. This creates that registry from the vanilla recipes.
	 */
	private net.minecraft.core.Registry<Recipe<?>> createRecipeRegistry(HolderLookup.Provider registries) {
		MappedRegistry<Recipe<?>> recipes = new MappedRegistry<>(Registries.RECIPE, Lifecycle.stable());

		// Recipes refer to items (and item tags) by their registry entries, so they need to be parsed with the
		//  registries. Recipes that fail to parse anyway are skipped.
		var ops = registries.createSerializationContext(JsonOps.INSTANCE);
		for (Pair<String, JsonObject> recipeJson : getRecipes(MinecraftServer.class)) {
			Recipe.DIRECT_CODEC.parse(ops, recipeJson.second()).result().ifPresent(recipe -> recipes.register(
				ResourceKey.create(Registries.RECIPE, Identifier.withDefaultNamespace(recipeJson.first())), recipe, RegistrationInfo.BUILT_IN
			));
		}
		recipes.bindAllTagsToEmpty();
		return recipes.freeze();
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
		return BuiltInRegistries.ITEM.keySet().stream()
			.map(Identifier::toString)
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
	public CommandSourceStack getBrigadierSourceFromCommandSender(AbstractCommandSender<? extends CommandSender> senderWrapper) {
		CommandSender sender = senderWrapper.getSource();
		// Resource arguments (biomes, enchantments, etc.) ask the source to suggest the registry's elements. The real
		//  implementation looks them up in the source's registryAccess, or the server's reloadable registries.
		CommandSourceStack css = Mockito.mock(CommandSourceStack.class, invocation ->
			invocation.getMethod().getName().equals("suggestRegistryElements")
				? invocation.callRealMethod()
				: Answers.RETURNS_DEFAULTS.answer(invocation));
		MockPlatform.setField(CommandSourceStack.class, "server", css, getMinecraftServer());
		Mockito.when(css.registryAccess()).thenAnswer(invocation -> MockRegistries.registryAccess());
		Mockito.when(css.getBukkitSender()).thenReturn(sender);

		if (sender instanceof Entity entity) {
			// LocationArgument
			Location loc = entity.getLocation();
			Mockito.when(css.getPosition()).thenReturn(new Vec3(loc.getX(), loc.getY(), loc.getZ()));

			// If entity gives us a ServerLevel, use it, otherwise mock it
			ServerLevel worldServerLevel;
			if (entity.getWorld() instanceof CraftWorld cw) worldServerLevel = cw.getHandle();
			else worldServerLevel = Mockito.mock(ServerLevel.class);

			Mockito.when(css.getLevel()).thenReturn(worldServerLevel);
			Mockito.when(css.getLevel().hasChunkAt(any(BlockPos.class))).thenReturn(true);
			Mockito.when(css.getLevel().isInWorldBounds(any(BlockPos.class))).thenReturn(true);
			// FunctionArgument. Since 1.21.11, game rules are per level, and running commands uses them
			Mockito.when(css.getLevel().getGameRules()).thenAnswer(i -> new GameRules(FeatureFlags.DEFAULT_FLAGS));
			Mockito.when(css.getAnchor()).thenReturn(Anchor.EYES);

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
				Mockito.when(entityPlayerMock.getDisplayName()).thenReturn(Component.literal(name)); // ChatArgument, AdventureChatArgument
				Mockito.when(entityPlayerMock.getScoreboardName()).thenReturn(name); // ScoreHolderArgument
				Mockito.when(entityPlayerMock.getType()).thenReturn((net.minecraft.world.entity.EntityType) EntityTypes.PLAYER); // EntitySelectorArgument
				players.add(entityPlayerMock);
			}

			// CommandSourceStack#levels
			Mockito.when(css.levels()).thenAnswer(invocation -> {
				Set<ResourceKey<Level>> set = new HashSet<>();
				for (World world : Bukkit.getWorlds()) {
					set.add(ResourceKey.create(Registries.DIMENSION, Identifier.parse(world.getName())));
				}
				return set;
			});

			// RotationArgument
			Mockito.when(css.getRotation()).thenReturn(new Vec2(loc.getPitch(), loc.getYaw()));

			// CommandSourceStack#getAllTeams
			Mockito.when(css.getAllTeams()).thenAnswer(invocation -> Bukkit.getScoreboardManager().getMainScoreboard().getTeams().stream().map(Team::getName).toList());

			// SoundArgument
			Mockito.when(css.getAvailableSounds()).thenAnswer(invocation -> BuiltInRegistries.SOUND_EVENT.keySet().stream());

			// ChatArgument, AdventureChatArgument. Since 1.21.11, the permission level is a PermissionSet
			Mockito.when(css.permissions()).thenAnswer(invocation -> sender.isOp() ? LevelBasedPermissionSet.OWNER : PermissionSet.NO_PERMISSIONS);

			// Suggestions
			Mockito.when(css.enabledFeatures()).thenAnswer(invocation -> FeatureFlags.DEFAULT_FLAGS);

			// FunctionArgument
			// We don't really need to do anything funky here, we'll just return the same CSS
			Mockito.when(css.withSuppressedOutput()).thenReturn(css);
			Mockito.when(css.withMaximumPermission(any())).thenReturn(css);
			Mockito.when(css.callback()).thenReturn((success, result) -> {
				functionCallbackResults.push(result);
			});
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void createDispatcherFile(File file, CommandDispatcher dispatcher) throws IOException {
		baseNMS.createDispatcherFile(file, dispatcher);
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
		// Since 1.20.5, Bukkit's particles have the same keys as Minecraft's particle types
		return particle.getKey().getKey();
	}

	@Override
	public List<NamespacedKey> getAllRecipes() {
		return recipeManager.getRecipes().stream()
			.map(recipe -> recipe.id().identifier())
			.map(id -> new NamespacedKey(id.getNamespace(), id.getPath()))
			.toList();
	}

	@Override
	public List<NamespacedKey> getNonSpecialRecipes() {
		// Like ResourceKeyArgument#getRecipe
		return recipeManager.getRecipes().stream()
			.filter(recipe -> !recipe.value().isSpecial())
			.map(recipe -> recipe.id().identifier())
			.map(id -> new NamespacedKey(id.getNamespace(), id.getPath()))
			.toList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getMinecraftServer() {
		if (minecraftServerMock != null) {
			return (T) minecraftServerMock;
		}
		minecraftServerMock = Mockito.mock(MinecraftServer.class);

		// Component serialization (e.g. in Spigot's ChatArgument)
		Mockito.when(minecraftServerMock.registryAccess()).thenAnswer(i -> MockRegistries.registryAccess());

		// LootTableArgument. Since 1.20.5, loot tables are in the server's reloadable registries
		HolderLookup.RegistryLookup<LootTable> lootTables = Mockito.mock(HolderLookup.RegistryLookup.class);
		Mockito.when(lootTables.listElementIds()).thenAnswer(i -> Stream
			.concat(
				BuiltInRegistries.ENTITY_TYPE.stream().flatMap(entityType -> entityType.getDefaultLootTable().stream()),
				BuiltInLootTables.all().stream())
			.distinct());
		HolderLookup.Provider reloadableLookup = Mockito.mock(HolderLookup.Provider.class);
		Mockito.when(reloadableLookup.lookupOrThrow(Registries.LOOT_TABLE)).thenReturn(lootTables);
		ReloadableServerRegistries.Holder reloadableRegistries = Mockito.mock(ReloadableServerRegistries.Holder.class);
		Mockito.when(reloadableRegistries.lookup()).thenReturn(reloadableLookup);
		Mockito.when(minecraftServerMock.reloadableRegistries()).thenReturn(reloadableRegistries);

		// AdvancementArgument
		ServerAdvancementManager advancementManager = Mockito.mock(ServerAdvancementManager.class);
		Mockito.when(advancementManager.get(any())).thenAnswer(i -> advancements.get(i.<Identifier>getArgument(0)));
		Mockito.when(advancementManager.getAllAdvancements()).thenAnswer(i -> advancements.values());
		Mockito.when(minecraftServerMock.getAdvancements()).thenReturn(advancementManager);

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
				return new Objective(scoreboardServerMock, objectiveName, ObjectiveCriteria.byName(bukkitObjective.getCriteria()).get(), Component.literal(bukkitObjective.getDisplayName()), switch (bukkitObjective.getRenderType()) {
					case HEARTS -> RenderType.HEARTS;
					case INTEGER -> RenderType.INTEGER;
				}, true /* displayAutoUpdate */, BlankFormat.INSTANCE /* numberFormat */);
			}
		});
		Mockito.when(minecraftServerMock.getScoreboard()).thenReturn(scoreboardServerMock); // MinecraftServer#getScoreboard

		// WorldArgument (Dimension)
		Mockito.when(minecraftServerMock.getLevel(any(ResourceKey.class))).thenAnswer(invocation -> {
			// Get the ResourceKey<World> and extract the world name from it
			ResourceKey<Level> resourceKey = invocation.getArgument(0);
			String worldName = resourceKey.identifier().getPath();

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

		// PlayerArgument. Since 1.21.9, the profile cache is part of the server's services
		UserNameToIdResolver nameToIdResolverMock = Mockito.mock(UserNameToIdResolver.class);
		Mockito.when(nameToIdResolverMock.get(anyString())).thenAnswer(invocation -> {
			String playerName = invocation.getArgument(0);
			for (ServerPlayer onlinePlayer : players) {
				if (onlinePlayer.getBukkitEntity().getName().equals(playerName)) {
					return Optional.of(new NameAndId(onlinePlayer.getBukkitEntity().getUniqueId(), playerName));
				}
			}
			return Optional.empty();
		});
		Services services = new Services(null, null, null, nameToIdResolverMock, null);
		Mockito.when(minecraftServerMock.services()).thenReturn(services);

		// RecipeArgument
		Mockito.when(minecraftServerMock.getRecipeManager()).thenAnswer(i -> this.recipeManager);

		// FunctionArgument
		// We're using 2 (gamemaster) as the function compilation level.
		Mockito.when(minecraftServerMock.getFunctionCompilationPermissions()).thenReturn(LevelBasedPermissionSet.GAMEMASTER);
		Mockito.when(minecraftServerMock.getFunctions()).thenAnswer(i -> {
			ServerFunctionLibrary serverFunctionLibrary = Mockito.mock(ServerFunctionLibrary.class);

			// Functions
			Mockito.when(serverFunctionLibrary.getFunction(any())).thenAnswer(invocation -> Optional.ofNullable(functions.get(invocation.<Identifier>getArgument(0))));
			Mockito.when(serverFunctionLibrary.getFunctions()).thenAnswer(invocation -> functions);

			// Tags
			Mockito.when(serverFunctionLibrary.getTag(any())).thenAnswer(invocation -> List.copyOf(tags.getOrDefault(invocation.<Identifier>getArgument(0), List.of())));
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

		Mockito.when(minecraftServerMock.getGameRules()).thenAnswer(i -> new GameRules(FeatureFlags.DEFAULT_FLAGS));

		setupCommandDispatchers(minecraftServerMock);

		return (T) minecraftServerMock;
	}

	/**
	 * Sets up the Brigadier and resources dispatchers, used in the platform's {@code createCommandRegistrationStrategy}.
	 * This is Spigot-specific ({@code MinecraftServer#vanillaCommandDispatcher} and the no-argument {@link Commands}
	 * constructor don't exist on Paper), so other platforms can override this.
	 *
	 * @param minecraftServerMock the mocked {@link MinecraftServer} to attach the dispatchers to
	 */
	protected void setupCommandDispatchers(MinecraftServer minecraftServerMock) {
		Commands brigadierCommands = new Commands();
		MockPlatform.setField(Commands.class, "dispatcher", brigadierCommands, getMockBrigadierDispatcher());
		minecraftServerMock.vanillaCommandDispatcher = brigadierCommands;

		Commands resourcesCommands = new Commands();
		MockPlatform.setField(Commands.class, "dispatcher", resourcesCommands, getMockResourcesDispatcher());
		Mockito.when(minecraftServerMock.getCommands()).thenReturn(resourcesCommands);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addFunction(NamespacedKey key, List<String> commands) {
		if (Bukkit.getOnlinePlayers().isEmpty()) {
			throw new IllegalStateException("You need to have at least one player on the server to add a function");
		}

		Identifier identifier = Identifier.parse(key.toString());
		CommandSourceStack css = getBrigadierSourceFromCommandSender(new BukkitPlayer(Bukkit.getOnlinePlayers().iterator().next()));

		// So for very interesting reasons, Brigadier.getCommandDispatcher()
		// gives a different result in this method than using getBrigadierDispatcher()
		this.functions.put(identifier, CommandFunction.fromLines(identifier, Brigadier.getCommandDispatcher(), css, commands));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addTag(NamespacedKey key, List<List<String>> commands) {
		if (Bukkit.getOnlinePlayers().isEmpty()) {
			throw new IllegalStateException("You need to have at least one player on the server to add a function");
		}

		Identifier identifier = Identifier.parse(key.toString());
		CommandSourceStack css = getBrigadierSourceFromCommandSender(new BukkitPlayer(Bukkit.getOnlinePlayers().iterator().next()));

		List<CommandFunction<CommandSourceStack>> tagFunctions = new ArrayList<>();
		for (List<String> functionCommands : commands) {
			tagFunctions.add(CommandFunction.fromLines(identifier, Brigadier.getCommandDispatcher(), css, functionCommands));
		}
		this.tags.put(identifier, tagFunctions);
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
		CommandSourceStack css = getBrigadierSourceFromCommandSender(CommandAPIBukkit.get().wrapCommandSender(player));

		ServerPlayer handle = Mockito.mock(ServerPlayer.class);
		Mockito.when(handle.createCommandSourceStack()).thenReturn(css);

		Mockito.when(player.getHandle()).thenReturn(handle);

		// getName and getDisplayName are used when CommandSourceStack#withEntity is called
		Component nameComponent = Component.literal(name);
		Mockito.when(handle.getName()).thenReturn(nameComponent);
		Mockito.when(handle.getDisplayName()).thenReturn(nameComponent);

		return player;
	}

	@Override
	public org.bukkit.advancement.Advancement addAdvancement(NamespacedKey key) {
		final Identifier identifier = Identifier.parse(key.toString());
		final Advancement advancement = new Advancement(Optional.empty(), Optional.empty(), null, new HashMap<>(), null, false);
		advancements.put(identifier, new AdvancementHolder(identifier, advancement));

		// Bukkit's Advancement interface has different methods in Spigot's and Paper's API, so this is mocked
		//  instead of implemented
		org.bukkit.advancement.Advancement bukkitAdvancement = Mockito.mock(org.bukkit.advancement.Advancement.class);
		Mockito.when(bukkitAdvancement.getKey()).thenReturn(key);
		Mockito.when(bukkitAdvancement.getCriteria()).thenReturn(List.of());
		return bukkitAdvancement;
	}

	@Override
	public BukkitCommandSender<? extends CommandSender> getSenderForCommand(CommandContext<CommandSourceStack> cmdCtx, boolean forceNative) {
		return baseNMS.getSenderForCommand(cmdCtx, forceNative);
	}

	@Override
	public NativeProxyCommandSender createNativeProxyCommandSender(CommandSender caller, CommandSender callee, Location location, World world) {
		return baseNMS.createNativeProxyCommandSender(caller, callee, location, world);
	}

	@Override
	public BukkitCommandSender<? extends CommandSender> getCommandSenderFromCommandSource(CommandSourceStack clw) {
		try {
			return CommandAPIBukkit.get().wrapCommandSender(clw.getBukkitSender());
		} catch (UnsupportedOperationException e) {
			return null;
		}
	}

	@Override
	public int popFunctionCallbackResult() {
		return functionCallbackResults.pop();
	}

	@Override
	public HelpTopic generateHelpTopic(String commandName, String shortDescription, String fullDescription, String permission) {
		return baseNMS.generateHelpTopic(commandName, shortDescription, fullDescription, permission);
	}

	@Override
	public Map<String, HelpTopic> getHelpMap() {
		return helpMapTopics.get((HelpMapMock) Bukkit.getHelpMap());
	}
}
