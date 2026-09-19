package dev.jorel.commandapi.nms;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.InternalSpigotConfig;
import dev.jorel.commandapi.preprocessor.Differs;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.TeamColorArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Recipe;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class SpigotNMS_26_3 extends SpigotNMS_26_Common {

	private NMS_26_3 bukkitNMS;

	public SpigotNMS_26_3(InternalSpigotConfig config) {
		super(config);
	}

	private String serializeComponents(ItemInput itemInput, HolderLookup.Provider provider) {
		DynamicOps<Tag> serializationContext = provider.createSerializationContext(NbtOps.INSTANCE);
		return itemInput.components().entrySet().stream().flatMap((entry) -> {
			DataComponentType<?> type = entry.getKey();
			Identifier identifier = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type);
			if (identifier == null) {
				return Stream.empty();
			} else {
				Object value = entry.getValue();
				TypedDataComponent<?> typedDataComponent = TypedDataComponent.createUnchecked(type, value);
				return typedDataComponent.encodeValue(serializationContext).result().stream().map((tag) -> {
					String componentString = identifier.toString();
					return componentString + "=" + tag;
				});
			}
		}).collect(Collectors.joining(String.valueOf(',')));
	}

	@Differs(from = "26.1", by = "ColorArgument -> TeamColorArgument")
	@Override
	public ChatColor getChatColor(CommandContext<CommandSourceStack> cmdCtx, String key) {
		// Relies on the fact that the enums have identical names for the colors
		return ChatColor.valueOf(TeamColorArgument.getTeamColor(cmdCtx, key).name());
	}

	@Override
	public NMS_26_Common bukkitNMS() {
		if (bukkitNMS == null) {
			this.bukkitNMS = new NMS_26_3(() -> COMMAND_BUILD_CONTEXT, this::serializeComponents);
		}
		return bukkitNMS;
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

			return collection.stream()
				.map(serverPackRepository::getPack)
				.filter(Objects::nonNull)
				.flatMap(Pack::open)
				.toList();
		}).exceptionally(exception -> {
			CommandAPI.logException("Something went wrong while trying to collect resource packs!", exception);
			// Return all currently selected packs
			return bukkitNMS().<MinecraftServer>getMinecraftServer().getPackRepository().openAllSelected();
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
		}).exceptionally(exception -> {
			CommandAPI.logException("Something went wrong while trying to convert resource packs into ReloadableResources", exception);
			// Return existing resources
			return bukkitNMS().<MinecraftServer>getMinecraftServer().resources;
		});

		// Step 3: Actually load all of the resources
		CompletableFuture<Void> third = second.thenAcceptAsync(resources -> {
			bukkitNMS().<MinecraftServer>getMinecraftServer().resources.close();
			bukkitNMS().<MinecraftServer>getMinecraftServer().resources = serverResources;
			bukkitNMS().<MinecraftServer>getMinecraftServer().server.syncCommands();
			if (minecraftServerSetSelected == null) {
				bukkitNMS().<MinecraftServer>getMinecraftServer().getPackRepository().setSelected(collection);
			} else {
				try {
					minecraftServerSetSelected.invoke(bukkitNMS().<MinecraftServer>getMinecraftServer().getPackRepository(), collection, true);
				} catch (Throwable e) {
					CommandAPI.logException("Something went wrong while trying to invoke PackRepository#setSelected(Collection, boolean)", e);
				}
			}

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
			//bukkitNMS().<MinecraftServer>getMinecraftServer().resources.managers().updateStaticRegistryTags(); // TODO: Review this
			bukkitNMS().<MinecraftServer>getMinecraftServer().resources.managers().updateComponentsAndStaticRegistryTags(); // TODO: Review this
			bukkitNMS().<MinecraftServer>getMinecraftServer().resources.managers().getRecipeManager().finalizeRecipeLoading(enabledFeatures);

			// May need to be commented out, may not. Comment it out just in case.
			// For some reason, calling getPlayerList().saveAll() may just hang
			// the server indefinitely. Not sure why!
			// bukkitNMS().<MinecraftServer>getMinecraftServer().getPlayerList().saveAll();
			// bukkitNMS().<MinecraftServer>getMinecraftServer().getPlayerList().reloadResources();
			// bukkitNMS().<MinecraftServer>getMinecraftServer().getFunctions().replaceLibrary(bukkitNMS().<MinecraftServer>getMinecraftServer().resources.managers().getFunctionLibrary());
			bukkitNMS().<MinecraftServer>getMinecraftServer().getStructureTemplateManager()
				.onResourceManagerReload(bukkitNMS().<MinecraftServer>getMinecraftServer().resources.resourceManager());
		}).exceptionally(exception -> {
			CommandAPI.logException("Something went wrong while trying to load resources.", exception);
			return null;
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
