package dev.jorel.commandapi.nms;

import com.google.common.collect.Collections2;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jorel.commandapi.CommandAPISpigot;
import dev.jorel.commandapi.CommandRegistrationStrategy;
import dev.jorel.commandapi.InternalSpigotConfig;
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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_20_R4.CraftServer;
import org.bukkit.craftbukkit.v1_20_R4.command.BukkitCommandWrapper;
import org.bukkit.craftbukkit.v1_20_R4.command.VanillaCommandWrapper;
import org.bukkit.craftbukkit.v1_20_R4.profile.CraftPlayerProfile;
import org.bukkit.profile.PlayerProfile;

import java.util.List;

public class SpigotNMS_1_20_R4 extends CommandAPISpigot<CommandSourceStack> {

	private static final CommandBuildContext COMMAND_BUILD_CONTEXT;

	private NMS_1_20_R4 bukkitNMS;

	static {
		if (Bukkit.getServer() instanceof CraftServer server) {
			COMMAND_BUILD_CONTEXT = CommandBuildContext.simple(server.getServer().registryAccess(),
				server.getServer().getWorldData().enabledFeatures());
		} else {
			COMMAND_BUILD_CONTEXT = null;
		}
	}

	public SpigotNMS_1_20_R4(InternalSpigotConfig config) {
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
		return result instanceof GameProfileArgument.SelectorResult selectorResult
			? List.of(Collections2.transform(selectorResult.getNames(cmdCtx.getSource()), CraftPlayerProfile::new).toArray(new PlayerProfile[0]))
			: List.of(Collections2.transform(result.getNames(cmdCtx.getSource()), CraftPlayerProfile::new).toArray(new PlayerProfile[0]));
	}

	@Override
	public NMS<CommandSourceStack> bukkitNMS() {
		if (bukkitNMS == null) {
			this.bukkitNMS = new NMS_1_20_R4(() -> COMMAND_BUILD_CONTEXT);
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
