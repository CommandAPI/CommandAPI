package dev.jorel.commandapi.nms;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jorel.commandapi.CommandRegistrationStrategy;
import dev.jorel.commandapi.SpigotCommandRegistration;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_20_R1.command.BukkitCommandWrapper;
import org.bukkit.craftbukkit.v1_20_R1.command.VanillaCommandWrapper;

public class SpigotNMS_1_20_R1 extends SpigotNMS_CommonWithFunctions {

	private NMS_1_20_R1 bukkitNMS;

	@Override
	public BaseComponent[] getChat(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return ComponentSerializer.parse(Component.Serializer.toJson(MessageArgument.getMessage(cmdCtx, key)));
	}

	@Override
	public BaseComponent[] getChatComponent(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return ComponentSerializer.parse(Component.Serializer.toJson(ComponentArgument.getComponent(cmdCtx, key)));
	}

	@Override
	public NMS<?> bukkitNMS() {
		if (bukkitNMS == null) {
			this.bukkitNMS = new NMS_1_20_R1();
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

}
