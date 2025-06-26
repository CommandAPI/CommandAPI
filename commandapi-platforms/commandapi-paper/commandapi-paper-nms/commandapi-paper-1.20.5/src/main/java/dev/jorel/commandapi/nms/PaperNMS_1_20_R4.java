package dev.jorel.commandapi.nms;

import com.mojang.brigadier.Command;
import dev.jorel.commandapi.CommandRegistrationStrategy;
import dev.jorel.commandapi.PaperCommandRegistration;
import io.papermc.paper.command.brigadier.PaperCommands;
import io.papermc.paper.command.brigadier.bukkit.BukkitCommandNode;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.help.SimpleHelpMap;

public class PaperNMS_1_20_R4 implements PaperNMS<CommandSourceStack> {

	private CommandBuildContext commandBuildContext;

	private NMS_1_20_R4 bukkitNMS;

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

	@Override
	public <Source> NMS<Source> bukkitNMS() {
		if (bukkitNMS == null) {
			this.bukkitNMS = new NMS_1_20_R4(this::getCommandBuildContext);
		}
		return (NMS<Source>) bukkitNMS;
	}

	@Override
	public CommandRegistrationStrategy<CommandSourceStack> createCommandRegistrationStrategy() {
		return new PaperCommandRegistration<>(
			() -> bukkitNMS.<MinecraftServer>getMinecraftServer().getCommands().getDispatcher(),
			() -> {
				SimpleHelpMap helpMap = (SimpleHelpMap) Bukkit.getServer().getHelpMap();
				helpMap.clear();
				helpMap.initializeGeneralTopics();
				helpMap.initializeCommands();
			},
			node -> {
				Command<?> command = node.getCommand();
				return command instanceof BukkitCommandNode.BukkitBrigCommand;
			}
		);
	}

}
