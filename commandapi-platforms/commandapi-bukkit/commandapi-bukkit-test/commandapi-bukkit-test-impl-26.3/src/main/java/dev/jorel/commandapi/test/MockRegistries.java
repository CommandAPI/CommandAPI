package dev.jorel.commandapi.test;

import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentInitializers.PendingComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.flag.FeatureFlags;

import java.util.List;
import java.util.stream.Stream;

/**
 * The Minecraft registries used by the 26.3 test implementations. On a real server, these come from the server's
 * {@code registryAccess()}, which also has the registries that are loaded from data packs (enchantments, biomes,
 * etc.) and the tags. Here, those are loaded from the vanilla data pack, like the server does when it starts. Loading
 * them takes a while, so they're shared by all tests.
 * <p>
 * Since these registries are real, the Bukkit NMS implementation can create its argument types with a real
 * {@link CommandBuildContext}, unlike the test implementations for older versions which mocked it.
 */
public final class MockRegistries {
	private static RegistryAccess.Frozen registryAccess;
	private static CommandBuildContext commandBuildContext;

	private MockRegistries() {
	}

	/**
	 * Initializes Minecraft's game version and built-in registries. This is safe to call multiple times.
	 */
	public static void bootstrap() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	/**
	 * @return the built-in registries, plus the registries from the vanilla data pack, with their tags
	 */
	public static RegistryAccess.Frozen registryAccess() {
		if (registryAccess == null) {
			bootstrap();

			// Similar to what WorldLoader does when the server starts
			ResourceManager vanillaData = new MultiPackResourceManager(PackType.SERVER_DATA, List.of(ServerPacksSource.createVanillaPackSource().fullResources()));
			RegistryAccess.Frozen builtInRegistries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
			List<Registry.PendingTags<?>> builtInTags = TagLoader.loadTagsForExistingRegistries(vanillaData, builtInRegistries);
			List<HolderLookup.RegistryLookup<?>> builtInLookups = TagLoader.buildUpdatedLookups(builtInRegistries, builtInTags);
			RegistryAccess.Frozen worldRegistries = RegistryDataLoader.load(vanillaData, builtInLookups, RegistryDataLoader.WORLD_REGISTRIES, Runnable::run).join();

			registryAccess = new RegistryAccess.ImmutableRegistryAccess(Stream.concat(builtInRegistries.registries(), worldRegistries.registries())).freeze();

			// Like ReloadableServerResources#updateComponentsAndStaticRegistryTags. Since 26.1, the default components
			//  of items etc. are bound when the server loads its data packs, so creating an ItemStack fails without this
			builtInTags.forEach(Registry.PendingTags::apply);
			BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(registryAccess).forEach(PendingComponents::apply);
		}
		return registryAccess;
	}

	/**
	 * @return a {@link CommandBuildContext} over {@link #registryAccess()} with all features enabled
	 */
	public static CommandBuildContext commandBuildContext() {
		if (commandBuildContext == null) {
			commandBuildContext = CommandBuildContext.simple(registryAccess(), FeatureFlags.REGISTRY.allFlags());
		}
		return commandBuildContext;
	}
}
