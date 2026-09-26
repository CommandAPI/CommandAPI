package dev.jorel.commandapi.nms;

import com.mojang.brigadier.Command;
import com.mojang.serialization.DynamicOps;
import dev.jorel.commandapi.CommandRegistrationStrategy;
import dev.jorel.commandapi.PaperCommandRegistration;
import io.papermc.paper.command.brigadier.PaperCommands;
import io.papermc.paper.command.brigadier.bukkit.BukkitCommandNode;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.Removed;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PaperNMS_26_3 implements PaperNMS<CommandSourceStack> {

	private CommandBuildContext commandBuildContext;

	private NMS_26_3 bukkitNMS;

	private CommandBuildContext getCommandBuildContext() {
		if (commandBuildContext != null) {
			return commandBuildContext;
		}
		if (Bukkit.getServer() instanceof CraftServer server) {
			commandBuildContext = CommandBuildContext.simple(server.getServer().registryAccess(),
				server.getServer().getWorldData().enabledFeatures());
			return commandBuildContext;
		} else {
			return PaperCommands.INSTANCE.getBuildContext();
		}
	}

	private String serializeComponents(ItemInput itemInput, HolderLookup.Provider provider) {
		DynamicOps<Tag> serializationContext = provider.createSerializationContext(NbtOps.INSTANCE);
		return itemInput.components().map.entrySet().stream().flatMap((entry) -> {
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

	@Override
	public NMS<CommandSourceStack> bukkitNMS() {
		if (bukkitNMS == null) {
			this.bukkitNMS = new NMS_26_3(this::getCommandBuildContext, this::serializeComponents);
		}
		return bukkitNMS;
	}

	@Override
	public CommandRegistrationStrategy<CommandSourceStack> createCommandRegistrationStrategy() {
		return new PaperCommandRegistration<>(
			() -> bukkitNMS.<MinecraftServer>getMinecraftServer().getCommands().getDispatcher(),
			node -> {
				Command<?> command = node.getCommand();
				return command instanceof BukkitCommandNode.BukkitBrigCommand;
			}
		);
	}

}
