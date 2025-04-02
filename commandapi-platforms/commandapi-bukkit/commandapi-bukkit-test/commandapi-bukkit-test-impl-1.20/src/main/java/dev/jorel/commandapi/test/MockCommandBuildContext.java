package dev.jorel.commandapi.test;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import org.mockito.Mockito;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

// TODO: Can this logic be shared with other test implementation versions?
public class MockCommandBuildContext implements CommandBuildContext {
	// TODO: There might be an easier way to create the CommandBuildContext we need,
	//  but I don't think it is important enough to figure out right now. I just
	//  copied this logic from ArgumentNMS without really knowing what is going on.
	private static final HolderLookup<?> biomeLookup = Mockito.mock(HolderLookup.class);

	private static final Map<ResourceKey<?>, HolderLookup<?>> registryLookup = Map.of(
		Registries.BLOCK, BuiltInRegistries.BLOCK.asLookup(),
		Registries.ENCHANTMENT, BuiltInRegistries.ENCHANTMENT.asLookup(),
		Registries.ENTITY_TYPE, BuiltInRegistries.ENTITY_TYPE.asLookup(),
		Registries.ITEM, BuiltInRegistries.ITEM.asLookup(),
		Registries.MOB_EFFECT, BuiltInRegistries.MOB_EFFECT.asLookup(),
		Registries.PARTICLE_TYPE, BuiltInRegistries.PARTICLE_TYPE.asLookup(),
		Registries.BIOME, biomeLookup
	);

	static {
		Mockito.when(biomeLookup.get((ResourceKey)any())).thenAnswer(i -> {
			ResourceKey rk = i.getArgument(0, ResourceKey.class);
			//					if(biomes.contains(rk)) {
			//						return Optional.of(Holder.Reference.createStandAlone(new HolderOwner() { }, rk));
			//					} else {
			//						return Optional.empty();
			//					}
			// We'll return the thing anyway. Bukkit will handle if the thing exists or not...
			return Optional.of(Holder.Reference.createStandAlone(new HolderOwner() { }, rk));
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> HolderLookup<T> holderLookup(ResourceKey<? extends Registry<T>> resourceKey) {
		return (HolderLookup<T>) registryLookup.get(resourceKey);
	}
}
