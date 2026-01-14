package dev.jorel.commandapi.test.mockbukkit;

import be.seeseemelk.mockbukkit.AsyncCatcher;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMockFactory;
import be.seeseemelk.mockbukkit.entity.SimpleEntityMock;
import be.seeseemelk.mockbukkit.help.HelpMapMock;
import dev.jorel.commandapi.Brigadier;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.commandsenders.AbstractCommandSender;
import dev.jorel.commandapi.commandsenders.BukkitPlayer;
import dev.jorel.commandapi.test.MockPlatform;
import io.papermc.paper.advancement.AdvancementDisplay;
import io.papermc.paper.command.brigadier.PaperCommands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventRunner;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.minecraft.SharedConstants;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.item.crafting.RecipeManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.help.HelpTopic;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.mockito.Mockito;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

public class CommandAPIServerMock_1_20_5 extends ServerMock implements CommandAPIServerMock<CommandSourceStack> {
	public CommandAPIServerMock_1_20_5() {
		MockPlatform.setField(ServerMock.class, "unsafe", this, new CommandAPIUnsafeValues());
	}

	// Start and stop server
	private final Commands minecraftCommands;

	{
		// Invoke Minecraft's game version and registry
		//  Some ArgumentTypes need this setup to initialize properly
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();

		// Create minecraft commands
		CommandBuildContext commandBuildContext = Mockito.mock();
		minecraftCommands = new Commands(Commands.CommandSelection.DEDICATED, commandBuildContext);
		PaperCommands.INSTANCE.setDispatcher(minecraftCommands, commandBuildContext);
	}

	@Override
	public void onEnable() {
		// Register Paper commands
		LifecycleEventRunner.INSTANCE.callReloadableRegistrarEvent(
			LifecycleEvents.COMMANDS, PaperCommands.INSTANCE,
			Plugin.class, ReloadableRegistrarEvent.Cause.INITIAL
		);

		// Run other server enable events
		Bukkit.getPluginManager().callEvent(new ServerLoadEvent(ServerLoadEvent.LoadType.STARTUP));
	}

	// Command dispatch
	@Override
	public boolean serverDispatchCommand(CommandSender sender, String commandLine) {
		return super.dispatchCommand(sender, commandLine);
	}

	// Mocked objects
	@Override
	public CommandAPIPlayerMock addCommandAPIPlayer() {
		return addPlayer();
	}

	@Override
	public CommandAPIPlayerMock addCommandAPIPlayer(String name) {
		return addPlayer(name);
	}

	private final PlayerMockFactory playerFactory = MockPlatform.getFieldAs(ServerMock.class, "playerFactory", this, PlayerMockFactory.class);

	@Override
	public @NotNull PlayerMock_1_20_5 addPlayer() {
		AsyncCatcher.catchOp("player add");
		PlayerMock_1_20_5 player = new PlayerMock_1_20_5(this.playerFactory.createRandomPlayer());
		this.addPlayer(player);
		return player;
	}

	@Override
	public @NotNull PlayerMock_1_20_5 addPlayer(@NotNull String name) {
		AsyncCatcher.catchOp("player add");
		PlayerMock_1_20_5 player = new PlayerMock_1_20_5(new PlayerMock(this, name, UUID.randomUUID()));
		this.addPlayer(player);
		return player;
	}

	@Override
	public Player setupMockedCraftPlayer(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Entity createSimpleEntityMock() {
		return new SimpleEntityMock(this);
	}

	private static class CustomWorldMock extends WorldMock {

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			} else if (obj instanceof World target) {
				return this.getUID().equals(target.getUID());
			} else {
				return false; // I have no idea what this is
			}
		}

	}

	@Override
	public @NotNull WorldMock addSimpleWorld(String name) {
		CustomWorldMock world = new CustomWorldMock();
		world.setName(name);
		super.addWorld(world);
		return world;
	}

	Map<ResourceLocation, CommandFunction<CommandSourceStack>> functions = new HashMap<>();
	Map<ResourceLocation, Collection<CommandFunction<CommandSourceStack>>> tags = new HashMap<>();

	@SuppressWarnings("unchecked")
	@Override
	public void addFunction(NamespacedKey key, List<String> commands) {
		if (Bukkit.getOnlinePlayers().isEmpty()) {
			throw new IllegalStateException("You need to have at least one player on the server to add a function");
		}

		ResourceLocation resourceLocation = new ResourceLocation(key.toString());
		CommandSourceStack css = CommandAPIBukkit.<CommandSourceStack>get()
			.getBrigadierSourceFromCommandSender(new BukkitPlayer(Bukkit.getOnlinePlayers().iterator().next()));

		// So for very interesting reasons, Brigadier.getCommandDispatcher()
		// gives a different result in this method than using getBrigadierDispatcher()
		this.functions.put(resourceLocation, CommandFunction.fromLines(resourceLocation, Brigadier.getCommandDispatcher(), css, commands));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addTag(NamespacedKey key, List<List<String>> commands) {
		if (Bukkit.getOnlinePlayers().isEmpty()) {
			throw new IllegalStateException("You need to have at least one player on the server to add a function");
		}

		ResourceLocation resourceLocation = new ResourceLocation(key.toString());
		CommandSourceStack css = CommandAPIBukkit.<CommandSourceStack>get()
			.getBrigadierSourceFromCommandSender(new BukkitPlayer(Bukkit.getOnlinePlayers().iterator().next()));

		List<CommandFunction<CommandSourceStack>> tagFunctions = new ArrayList<>();
		for (List<String> functionCommands : commands) {
			tagFunctions.add(CommandFunction.fromLines(resourceLocation, Brigadier.getCommandDispatcher(), css, functionCommands));
		}
		this.tags.put(resourceLocation, tagFunctions);
	}

	private final Stack<Integer> functionCallbackResults = new Stack<>();

	@Override
	public int popFunctionCallbackResult() {
		return functionCallbackResults.pop();
	}

	private final ServerAdvancementManager advancementDataWorld = new ServerAdvancementManager(null);
	private final List<org.bukkit.advancement.Advancement> bukkitAdvancements = new ArrayList<>();

	@Override
	public void addAdvancement(NamespacedKey key) {
		final Advancement advancement = new Advancement(Optional.empty(), Optional.empty(), null, new HashMap<>(), null, false);
		ResourceLocation location = new ResourceLocation(key.toString());

		advancementDataWorld.advancements.put(location, new AdvancementHolder(location, advancement));

		bukkitAdvancements.add(new org.bukkit.advancement.Advancement() {

			@Override
			public @NotNull NamespacedKey getKey() {
				return key;
			}

			@Override
			public @NotNull Collection<String> getCriteria() {
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
		});
	}

	@Override
	public @NotNull Iterator<org.bukkit.advancement.Advancement> advancementIterator() {
		return bukkitAdvancements.iterator();
	}

	@Override
	public Map<String, HelpTopic> getHelpMapTopics() {
		HelpMapMock helpMap = getHelpMap();
		return (Map<String, HelpTopic>) MockPlatform.getFieldAs(HelpMapMock.class, "topics", helpMap, Map.class);
	}

	private final MinecraftServer minecraftServerMock;
	private final List<ServerPlayer> players = new ArrayList<>();
	private final PlayerList playerListMock;
	private final RecipeManager recipeManager = new RecipeManager(HolderLookup.Provider.create(Stream.of()));

	{
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

		minecraftServerMock = Mockito.mock(MinecraftServer.class);

		// Minecraft commands
		Mockito.when(minecraftServerMock.getCommands()).thenReturn(minecraftCommands);

		// AdvancementArgument
		Mockito.when(minecraftServerMock.getAdvancements()).thenReturn(advancementDataWorld);

		// TODO: Evaluate how these get used and if they make sense now
		//  that the server stuff is Paper instead of Spigot
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
//
//		// TeamArgument
//		ServerScoreboard scoreboardServerMock = Mockito.mock(ServerScoreboard.class);
//		Mockito.when(scoreboardServerMock.getPlayerTeam(anyString())).thenAnswer(invocation -> { // Scoreboard#getPlayerTeam
//			String teamName = invocation.getArgument(0);
//			Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(teamName);
//			if (team == null) {
//				return null;
//			} else {
//				return new PlayerTeam(scoreboardServerMock, teamName);
//			}
//		});
//		Mockito.when(scoreboardServerMock.getObjective(anyString())).thenAnswer(invocation -> { // Scoreboard#getObjective
//			String objectiveName = invocation.getArgument(0);
//			org.bukkit.scoreboard.Objective bukkitObjective = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(objectiveName);
//			if (bukkitObjective == null) {
//				return null;
//			} else {
//				return new Objective(scoreboardServerMock, objectiveName, ObjectiveCriteria.byName(bukkitObjective.getCriteria()).get(), Component.literal(bukkitObjective.getDisplayName()), switch(bukkitObjective.getRenderType()) {
//					case HEARTS:
//						yield ObjectiveCriteria.RenderType.HEARTS;
//					case INTEGER:
//						yield ObjectiveCriteria.RenderType.INTEGER;
//				}, true /* displayAutoUpdate */, BlankFormat.INSTANCE /* numberFormat */);
//			}
//		});
//		Mockito.when(minecraftServerMock.getScoreboard()).thenReturn(scoreboardServerMock); // MinecraftServer#getScoreboard
//
//		// WorldArgument (Dimension)
//		Mockito.when(minecraftServerMock.getLevel(any(ResourceKey.class))).thenAnswer(invocation -> {
//			// Get the ResourceKey<World> and extract the world name from it
//			ResourceKey<Level> resourceKey = invocation.getArgument(0);
//			String worldName = resourceKey.location().getPath();
//
//			// Get the world via Bukkit (returns a WorldMock) and create a
//			// CraftWorld clone of it for WorldServer.getWorld()
//			World world = Bukkit.getServer().getWorld(worldName);
//			if (world == null) {
//				return null;
//			} else {
//				CraftWorld craftWorldMock = Mockito.mock(CraftWorld.class);
//				Mockito.when(craftWorldMock.getName()).thenReturn(world.getName());
//				Mockito.when(craftWorldMock.getUID()).thenReturn(world.getUID());
//
//				// Create our return WorldServer object
//				ServerLevel bukkitWorldServerMock = Mockito.mock(ServerLevel.class);
//				Mockito.when(bukkitWorldServerMock.getWorld()).thenReturn(craftWorldMock);
//				return bukkitWorldServerMock;
//			}
//		});
//
//		// Player lists
//		Mockito.when(minecraftServerMock.getPlayerList()).thenAnswer(i -> playerListMock);
//		Mockito.when(minecraftServerMock.getPlayerList().getPlayers()).thenAnswer(i -> players);
//
//		// PlayerProfileArgument
//		GameProfileCache userCacheMock = Mockito.mock(GameProfileCache.class);
//		Mockito.when(userCacheMock.get(anyString())).thenAnswer(invocation -> {
//			String playerName = invocation.getArgument(0);
//			for (ServerPlayer onlinePlayer : players) {
//				if (onlinePlayer.getBukkitEntity().getName().equals(playerName)) {
//					return Optional.of(new GameProfile(onlinePlayer.getBukkitEntity().getUniqueId(), playerName));
//				}
//			}
//			return Optional.empty();
//		});
//		Mockito.when(minecraftServerMock.getProfileCache()).thenReturn(userCacheMock);
//
//		// RecipeArgument
//		Mockito.when(minecraftServerMock.getRecipeManager()).thenAnswer(i -> this.recipeManager);
//
//		// FunctionArgument
//		// We're using 2 as the function compilation level.
//		Mockito.when(minecraftServerMock.getFunctionCompilationLevel()).thenReturn(2);
//		Mockito.when(minecraftServerMock.getFunctions()).thenAnswer(i -> {
//			ServerFunctionLibrary serverFunctionLibrary = Mockito.mock(ServerFunctionLibrary.class);
//
//			// Functions
//			Mockito.when(serverFunctionLibrary.getFunction(any())).thenAnswer(invocation -> Optional.ofNullable(functions.get(invocation.getArgument(0))));
//			Mockito.when(serverFunctionLibrary.getFunctions()).thenAnswer(invocation -> functions);
//
//			// Tags
//			Mockito.when(serverFunctionLibrary.getTag(any())).thenAnswer(invocation -> tags.getOrDefault(invocation.getArgument(0), List.of()));
//			Mockito.when(serverFunctionLibrary.getAvailableTags()).thenAnswer(invocation -> tags.keySet());
//
//			return new ServerFunctionManager(minecraftServerMock, serverFunctionLibrary);
//		});
//
//		Mockito.when(minecraftServerMock.getGameRules()).thenAnswer(i -> new GameRules());
//		Mockito.when(minecraftServerMock.getProfiler()).thenAnswer(i -> InactiveMetricsRecorder.INSTANCE.getProfiler());
//
	}

	@Override
	public CommandSourceStack getBrigadierSourceFromCommandSender(AbstractCommandSender<? extends CommandSender> senderWrapper) {
		CommandSender sender = senderWrapper.getSource();

		CommandSourceStack css = Mockito.mock(CommandSourceStack.class);

		// Implement required methods
		Mockito.when(css.getBukkitSender()).thenReturn(sender);
		Mockito.when(css.getServer()).thenReturn(minecraftServerMock); // Get mocked MinecraftServer

		// Mockito does not initialize public fields
		css.currentCommand = new ConcurrentHashMap<>();

		// FunctionArgument
		// We don't really need to do anything funky here, we'll just return the same CSS
		Mockito.when(css.withSuppressedOutput()).thenReturn(css);
		Mockito.when(css.withMaximumPermission(anyInt())).thenReturn(css);
		Mockito.when(css.callback()).thenReturn((success, result) -> {
			functionCallbackResults.push(result);
		});

		// TODO: Evaluate how these get used and if they make sense now
		//  that the server stuff is Paper instead of Spigot
//		if (sender instanceof Entity entity) {
//			// LocationArgument
//			Location loc = entity.getLocation();
//			Mockito.when(css.getPosition()).thenReturn(new Vec3(loc.getX(), loc.getY(), loc.getZ()));
//
//			// If entity gives us a ServerLevel, use it, otherwise mock it
//			ServerLevel worldServerLevel;
//			if(entity.getWorld() instanceof CraftWorld cw) worldServerLevel = cw.getHandle();
//			else worldServerLevel = Mockito.mock(ServerLevel.class);
//
//			Mockito.when(css.getLevel()).thenReturn(worldServerLevel);
//			Mockito.when(css.getLevel().hasChunkAt(any(BlockPos.class))).thenReturn(true);
////			Mockito.when(css.getLevel().getBlockState(any(BlockPos.class))).thenAnswer(i -> {
////				BlockPos bp = i.getArgument(0);
////				Block b = Bukkit.getWorlds().get(0).getBlockAt(bp.getX(), bp.getY(), bp.getZ());
////				BlockState bs = Mockito.mock(BlockState.class);
////				Mockito.when(bs.is(any(net.minecraft.world.level.block.Block.class))).thenAnswer(j -> {
//////					net.minecraft.world.level.block.Block nmsBlock = j.getArgument(0);
//////					nmsBlock.equals(bs.getBlock());
////					return true;
////                });
////				return bs;
////            });
//			Mockito.when(css.getLevel().isInWorldBounds(any(BlockPos.class))).thenReturn(true);
//			Mockito.when(css.getAnchor()).thenReturn(Anchor.EYES);
//
//
//			// EntitySelectorArgument
//			for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
//				ServerPlayer entityPlayerMock = Mockito.mock(ServerPlayer.class);
//				CraftPlayer craftPlayerMock = Mockito.mock(CraftPlayer.class);
//
//				// Extract these variables first in case the onlinePlayer is a Mockito object itself
//				String name = onlinePlayer.getName();
//				UUID uuid = onlinePlayer.getUniqueId();
//
//				Mockito.when(craftPlayerMock.getName()).thenReturn(name);
//				Mockito.when(craftPlayerMock.getUniqueId()).thenReturn(uuid);
//				Mockito.when(entityPlayerMock.getBukkitEntity()).thenReturn(craftPlayerMock);
//				Mockito.when(entityPlayerMock.getDisplayName()).thenReturn(Component.literal(name)); // ChatArgument, AdventureChatArgument
//				Mockito.when(entityPlayerMock.getScoreboardName()).thenReturn(name); // ScoreHolderArgument
//				Mockito.when(entityPlayerMock.getType()).thenReturn((net.minecraft.world.entity.EntityType) net.minecraft.world.entity.EntityType.PLAYER); // EntitySelectorArgument
//				players.add(entityPlayerMock);
//			}
//
//			// CommandSourceStack#levels
//			Mockito.when(css.levels()).thenAnswer(invocation -> {
//				Set<ResourceKey<Level>> set = new HashSet<>();
//				// We only need to implement resourceKey.a()
//
//				for (World world : Bukkit.getWorlds()) {
//					ResourceKey<Level> key = Mockito.mock(ResourceKey.class);
//					Mockito.when(key.location()).thenReturn(new ResourceLocation(world.getName()));
//					set.add(key);
//				}
//
//				return set;
//			});
//
//			// RotationArgument
//			Mockito.when(css.getRotation()).thenReturn(new Vec2(loc.getPitch(), loc.getYaw()));
//
//			// CommandSourceStack#getAllTeams
//			Mockito.when(css.getAllTeams()).thenAnswer(invocation -> Bukkit.getScoreboardManager().getMainScoreboard().getTeams().stream().map(Team::getName).toList());
//
//			// SoundArgument
//			Mockito.when(css.getAvailableSounds()).thenAnswer(invocation -> BuiltInRegistries.SOUND_EVENT.keySet().stream());
//
//			// RecipeArgument
//			Mockito.when(css.getRecipeNames()).thenAnswer(invocation -> recipeManager.getRecipeIds());
//
//			// ChatArgument, AdventureChatArgument
//			Mockito.when(css.hasPermission(anyInt())).thenAnswer(invocation -> sender.isOp());
//			Mockito.when(css.hasPermission(anyInt(), anyString())).thenAnswer(invocation -> sender.isOp());
//
//			// Suggestions
//			Mockito.when(css.enabledFeatures()).thenAnswer(invocation -> FeatureFlags.DEFAULT_FLAGS);
//
//		} else {
//			// `getPosition` and `getRotation` are always accessed when `NMS#getSenderForCommand` is called
//			//  If sender is an entity then we can give a physical location, but here we'll just give some defaults
//			Mockito.when(css.getPosition()).thenReturn(new Vec3(0, 0, 0));
//			Mockito.when(css.getRotation()).thenReturn(new Vec2(0, 0));
//		}
//
//		// NativeProxyCommandSender construction (NMS#createNativeProxyCommandSender)
//		//  Make sure these builder methods work as expected
//		Mockito.when(css.withPosition(any())).thenAnswer(invocation -> {
//			Mockito.when(css.getPosition()).thenReturn(invocation.getArgument(0));
//			return css;
//		});
//		Mockito.when(css.withRotation(any())).thenAnswer(invocation -> {
//			Mockito.when(css.getRotation()).thenReturn(invocation.getArgument(0));
//			return css;
//		});
//		Mockito.when(css.withLevel(any())).thenAnswer(invocation -> {
//			Mockito.when(css.getLevel()).thenReturn(invocation.getArgument(0));
//			return css;
//		});
//		Mockito.when(css.withEntity(any())).thenAnswer(invocation -> {
//			net.minecraft.world.entity.Entity entity = invocation.getArgument(0);
//			String name = entity.getName().getString();
//			net.minecraft.network.chat.Component displayName = entity.getDisplayName();
//
//			Mockito.when(css.getEntity()).thenReturn(entity);
//			Mockito.when(css.getTextName()).thenReturn(name);
//			Mockito.when(css.getDisplayName()).thenReturn(displayName);
//			return css;
//		});

		return css;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getMinecraftServer() {
		return (T) minecraftServerMock;
	}
}
