package dev.jorel.commandapi.nms;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPIHandler;
import dev.jorel.commandapi.CommandAPISpigot;
import dev.jorel.commandapi.CommandRegistrationStrategy;
import dev.jorel.commandapi.InternalSpigotConfig;
import dev.jorel.commandapi.SafeVarHandle;
import dev.jorel.commandapi.SpigotCommandRegistration;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ColorArgument;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionLibrary;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.util.Unit;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.entity.FuelValues;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_21_R2.CraftServer;
import org.bukkit.craftbukkit.v1_21_R2.command.BukkitCommandWrapper;
import org.bukkit.craftbukkit.v1_21_R2.command.VanillaCommandWrapper;
import org.bukkit.craftbukkit.v1_21_R2.profile.CraftPlayerProfile;
import org.bukkit.inventory.Recipe;
import org.bukkit.profile.PlayerProfile;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SpigotNMS_1_21_R2 extends CommandAPISpigot<CommandSourceStack> {

	private static final CommandBuildContext COMMAND_BUILD_CONTEXT;
	private static final Field serverFunctionLibraryDispatcher;
	private static final SafeVarHandle<MinecraftServer, FuelValues> minecraftServerFuelValues;

	private NMS_1_21_R2 bukkitNMS;

	static {
		if (Bukkit.getServer() instanceof CraftServer server) {
			COMMAND_BUILD_CONTEXT = CommandBuildContext.simple(server.getServer().registryAccess(),
				server.getServer().getWorldData().enabledFeatures());
		} else {
			COMMAND_BUILD_CONTEXT = null;
		}
		// For some reason, MethodHandles fails for this field, but Field works okay
		serverFunctionLibraryDispatcher = CommandAPIHandler.getField(ServerFunctionLibrary.class, "h", "dispatcher");
		minecraftServerFuelValues = SafeVarHandle.ofOrNull(MinecraftServer.class, "aE", "fuelValues", FuelValues.class);
	}

	public SpigotNMS_1_21_R2(InternalSpigotConfig config) {
		super(config);
	}

	@Override
	public BaseComponent[] getChat(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return ComponentSerializer.parse(Component.Serializer.toJson(MessageArgument.getMessage(cmdCtx, key), COMMAND_BUILD_CONTEXT));
	}

	@Override
	public ChatColor getChatColor(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return ChatColor.getByChar(ColorArgument.getColor(cmdCtx, key).getChar());
	}

	@Override
	public BaseComponent[] getChatComponent(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return ComponentSerializer.parse(Component.Serializer.toJson(ComponentArgument.getComponent(cmdCtx, key), COMMAND_BUILD_CONTEXT));
	}

	@Override
	public final List<PlayerProfile> getProfile(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		GameProfileArgument.Result result = cmdCtx.getArgument(key, GameProfileArgument.Result.class);
		return new ArrayList<>(Collections2.transform(result.getNames(cmdCtx.getSource()), CraftPlayerProfile::new));
	}

	@Override
	public NMS<CommandSourceStack> bukkitNMS() {
		if (bukkitNMS == null) {
			this.bukkitNMS = new NMS_1_21_R2(() -> COMMAND_BUILD_CONTEXT);
		}
		return bukkitNMS;
	}

	@Override
	public CommandRegistrationStrategy<CommandSourceStack> createCommandRegistrationStrategy() {
		return new SpigotCommandRegistration<>(
			bukkitNMS.<MinecraftServer>getMinecraftServer().vanillaCommandDispatcher.getDispatcher(),
			(SimpleCommandMap) getCommandMap(),
			() -> bukkitNMS.<MinecraftServer>getMinecraftServer().getCommands().getDispatcher(),
			command -> command instanceof VanillaCommandWrapper,
			node -> new VanillaCommandWrapper(bukkitNMS.<MinecraftServer>getMinecraftServer().vanillaCommandDispatcher, node),
			node -> node.getCommand() instanceof BukkitCommandWrapper
		);
	}

	@Override
	public final void reloadDataPacks() {
		CommandAPI.logNormal("Reloading datapacks...");

		// Get previously declared recipes to be re-registered later
		Iterator<Recipe> recipes = Bukkit.recipeIterator();

		// Update the commandDispatcher with the current server's commandDispatcher
		MinecraftServer.ReloadableResources serverResources = bukkitNMS().<MinecraftServer>getMinecraftServer().resources;
		serverResources.managers().commands = bukkitNMS().<MinecraftServer>getMinecraftServer().getCommands();

		// Update the ServerFunctionLibrary's command dispatcher with the new one
		try {
			serverFunctionLibraryDispatcher.set(serverResources.managers().getFunctionLibrary(),
				CommandAPIBukkit.<CommandSourceStack>get().getBrigadierDispatcher());
		} catch (IllegalAccessException ignored) {
			// Shouldn't happen, CommandAPIHandler#getField makes it accessible
		}

		// From bukkitNMS().<MinecraftServer>getMinecraftServer().reloadResources //
		// Discover new packs
		Collection<String> collection;
		{
			List<String> packIDs = new ArrayList<>(
				bukkitNMS().<MinecraftServer>getMinecraftServer().getPackRepository().getSelectedIds());
			List<String> disabledPacks = bukkitNMS().<MinecraftServer>getMinecraftServer().getWorldData()
				.getDataConfiguration().dataPacks().getDisabled();

			for (String availablePack : bukkitNMS().<MinecraftServer>getMinecraftServer().getPackRepository()
				.getAvailableIds()) {
				// Add every other available pack that is not disabled
				// and is not already in the list of existing packs
				if (!disabledPacks.contains(availablePack) && !packIDs.contains(availablePack)) {
					packIDs.add(availablePack);
				}
			}
			collection = packIDs;
		}

		// Step 1: Construct an async supplier of a list of all resource packs to
		// be loaded in the reload phase
		CompletableFuture<List<PackResources>> first = CompletableFuture.supplyAsync(() -> {
			PackRepository serverPackRepository = bukkitNMS().<MinecraftServer>getMinecraftServer().getPackRepository();

			List<PackResources> packResources = new ArrayList<>();
			for (String packID : collection) {
				Pack pack = serverPackRepository.getPack(packID);
				if (pack != null) {
					packResources.add(pack.open());
				}
			}
			return packResources;
		});

		// Step 2: Convert all of the resource packs into ReloadableResources which
		// are replaced by our custom server resources with defined commands
		CompletableFuture<MinecraftServer.ReloadableResources> second = first.thenCompose(packResources -> {
			MultiPackResourceManager resourceManager = new MultiPackResourceManager(PackType.SERVER_DATA,
				packResources);

			// TODO: I'm not sure if this is sufficient anymore - Do we not want to load tags for existing
			// registries here as well?
			// List<PendingTags<?>> TagList = TagLoader.loadTagsForExistingRegistries(resourceManager, bukkitNMS().<MinecraftServer>getMinecraftServer().registries().compositeAccess());

			// Not using packResources, because we really really want this to work
			CompletableFuture<?> simpleReloadInstance = SimpleReloadInstance.create(resourceManager,
				serverResources.managers().listeners(), bukkitNMS().<MinecraftServer>getMinecraftServer().executor,
				bukkitNMS().<MinecraftServer>getMinecraftServer(), CompletableFuture
					.completedFuture(Unit.INSTANCE) /* ReloadableServerResources.DATA_RELOAD_INITIAL_TASK */,
				LogUtils.getLogger().isDebugEnabled()).done();

			return simpleReloadInstance.thenApply(x -> serverResources);
		});

		// Step 3: Actually load all of the resources
		CompletableFuture<Void> third = second.thenAcceptAsync(resources -> {
			bukkitNMS().<MinecraftServer>getMinecraftServer().resources.close();
			bukkitNMS().<MinecraftServer>getMinecraftServer().resources = serverResources;
			bukkitNMS().<MinecraftServer>getMinecraftServer().server.syncCommands();
			bukkitNMS().<MinecraftServer>getMinecraftServer().getPackRepository().setSelected(collection);

			final FeatureFlagSet enabledFeatures = bukkitNMS().<MinecraftServer>getMinecraftServer().getWorldData().getDataConfiguration().enabledFeatures();

			// bukkitNMS().<MinecraftServer>getMinecraftServer().getSelectedPacks
			Collection<String> selectedIDs = bukkitNMS().<MinecraftServer>getMinecraftServer().getPackRepository()
				.getSelectedIds();
			List<String> enabledIDs = ImmutableList.copyOf(selectedIDs);
			List<String> disabledIDs = new ArrayList<>(
				bukkitNMS().<MinecraftServer>getMinecraftServer().getPackRepository().getAvailableIds());

			disabledIDs.removeIf(enabledIDs::contains);

			bukkitNMS().<MinecraftServer>getMinecraftServer().getWorldData()
				.setDataConfiguration(new WorldDataConfiguration(new DataPackConfig(enabledIDs, disabledIDs), enabledFeatures));
			// bukkitNMS().<MinecraftServer>getMinecraftServer().resources.managers().updateRegistryTags(registryAccess);
			bukkitNMS().<MinecraftServer>getMinecraftServer().resources.managers().updateStaticRegistryTags(); // TODO: Review this
			bukkitNMS().<MinecraftServer>getMinecraftServer().resources.managers().getRecipeManager().finalizeRecipeLoading(enabledFeatures);

			// May need to be commented out, may not. Comment it out just in case.
			// For some reason, calling getPlayerList().saveAll() may just hang
			// the server indefinitely. Not sure why!
			// bukkitNMS().<MinecraftServer>getMinecraftServer().getPlayerList().saveAll();
			// bukkitNMS().<MinecraftServer>getMinecraftServer().getPlayerList().reloadResources();
			// bukkitNMS().<MinecraftServer>getMinecraftServer().getFunctions().replaceLibrary(bukkitNMS().<MinecraftServer>getMinecraftServer().resources.managers().getFunctionLibrary());
			bukkitNMS().<MinecraftServer>getMinecraftServer().getStructureManager()
				.onResourceManagerReload(bukkitNMS().<MinecraftServer>getMinecraftServer().resources.resourceManager());

			// Set fuel values with the new loaded fuel values from the list of enabled features
			minecraftServerFuelValues.set(bukkitNMS().<MinecraftServer>getMinecraftServer(),
				FuelValues.vanillaBurnTimes(bukkitNMS().<MinecraftServer>getMinecraftServer().registries().compositeAccess(),
					enabledFeatures
				)
			);
		});

		// Step 4: Block the thread until everything's done
		if (bukkitNMS().<MinecraftServer>getMinecraftServer().isSameThread()) {
			bukkitNMS().<MinecraftServer>getMinecraftServer().managedBlock(third::isDone);
		}

		// Run the completableFuture (and bind tags?)
		try {

			// Register recipes again because reloading datapacks
			// removes all non-vanilla recipes
			CommandAPIBukkit.get().registerBukkitRecipesSafely(recipes);

			CommandAPI.logNormal("Finished reloading datapacks");
		} catch (Exception e) {
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			e.printStackTrace(printWriter);

			CommandAPI.logError(
				"Failed to load datapacks, can't proceed with normal server load procedure. Try fixing your datapacks?\n"
					+ stringWriter.toString());
		}
	}

}
