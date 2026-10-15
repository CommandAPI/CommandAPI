package dev.jorel.commandapi.test;

import com.mojang.serialization.DynamicOps;
import dev.jorel.commandapi.InternalSpigotConfig;
import dev.jorel.commandapi.nms.NMS_26_3;
import dev.jorel.commandapi.nms.NMS_26_Common;
import dev.jorel.commandapi.nms.SpigotNMS_26_3;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.Removed;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.inventory.CraftItemStack;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The Spigot platform for testing. This is the real {@link SpigotNMS_26_3}, except that its Bukkit NMS implementation
 * uses the {@link MockRegistries} and is wrapped in a {@link MockNMS}.
 * <p>
 * Since 26.1, {@link SpigotNMS_26_3#bukkitNMS()} has to return an {@link NMS_26_Common}, so it can't return the
 * {@link MockNMS} like the test implementations for older versions do. Instead, it returns the Bukkit NMS
 * implementation that the {@link MockNMS} wraps, which uses the mocked {@code MinecraftServer}. That way, the parts of
 * {@link SpigotNMS_26_3} that use {@link #bukkitNMS()} (chat arguments, the command registration strategy, etc.)
 * work as they are. The CommandAPI itself uses the {@link MockNMS}.
 */
public class TestSpigotNMS_26_3 extends SpigotNMS_26_3 {
	// This must not have an initializer. It is set by bukkitNMS(), which CommandAPISpigot's constructor
	//  calls before this class's field initializers would run.
	private MockNMS mockNMS;

	public TestSpigotNMS_26_3(InternalSpigotConfig config) {
		super(config);

		// CommandAPISpigot's constructor sets the NMS to bukkitNMS(), which is the NMS that MockNMS wraps
		this.nms = getMockNMS();
	}

	private MockNMS getMockNMS() {
		if (mockNMS == null) {
			// Same as SpigotNMS_26_3, but with the test registries instead of the CraftServer's
			mockNMS = new MockNMS(new NMS_26_3(MockRegistries::commandBuildContext, this::serializeComponents, CraftItemStack::asBukkitCopy, CraftBlockData::fromData));
		}
		return mockNMS;
	}

	@Override
	public NMS_26_Common bukkitNMS() {
		// MockNMS spies on the NMS it wraps, so that the NMS uses the mocked MinecraftServer
		return (NMS_26_Common) getMockNMS().baseNMS;
	}

	// Same as SpigotNMS_26_3#serializeComponents, which is private
	private String serializeComponents(ItemInput itemInput, HolderLookup.Provider provider) {
		DynamicOps<Tag> serializationContext = provider.createSerializationContext(NbtOps.INSTANCE);
		return itemInput.components().entrySet().stream().flatMap((entry) -> {
			DataComponentType<?> type = entry.getKey();
			Identifier identifier = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type);
			if (identifier == null) {
				return Stream.empty();
			} else {
				Object value = entry.getValue();
				if (!Removed.isRemoved(value)) {
					TypedDataComponent<?> typedDataComponent = TypedDataComponent.createUnchecked(type, value);
					return typedDataComponent.encodeValue(serializationContext).result().stream().map((tag) -> {
						String componentString = identifier.toString();
						return componentString + "=" + tag;
					});
				} else {
					return Stream.of("!" + identifier);
				}
			}
		}).collect(Collectors.joining(String.valueOf(',')));
	}
}
