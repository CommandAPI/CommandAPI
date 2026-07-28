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
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.command.BukkitCommandWrapper;
import org.bukkit.craftbukkit.command.NullCommandSender;
import org.bukkit.craftbukkit.command.VanillaCommandWrapper;
import org.bukkit.craftbukkit.profile.CraftPlayerProfile;
import org.bukkit.profile.PlayerProfile;

import java.util.ArrayList;
import java.util.List;

public abstract class SpigotNMS_26_Common extends CommandAPISpigot<CommandSourceStack> {

	protected static final CommandBuildContext COMMAND_BUILD_CONTEXT;

	static {
		if (Bukkit.getServer() instanceof CraftServer server) {
			COMMAND_BUILD_CONTEXT = CommandBuildContext.simple(server.getServer().registryAccess(),
				server.getServer().getWorldData().enabledFeatures());
		} else {
			COMMAND_BUILD_CONTEXT = null;
		}
	}

	public SpigotNMS_26_Common(InternalSpigotConfig config) {
		super(config);
	}

	@Override
	public BaseComponent[] getChat(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return ComponentSerializer.parse(bukkitNMS().toJson(MessageArgument.getMessage(cmdCtx, key)));
	}

	@Override
	public BaseComponent[] getChatComponent(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return ComponentSerializer.parse(bukkitNMS().toJson(ComponentArgument.getResolvedComponent(cmdCtx, key)));
	}

	@Override
	public final List<PlayerProfile> getProfile(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		GameProfileArgument.Result result = cmdCtx.getArgument(key, GameProfileArgument.Result.class);
		return new ArrayList<>(Collections2.transform(result.getNames(cmdCtx.getSource()), CraftPlayerProfile::new));
	}

	@Override
	public abstract NMS_26_Common bukkitNMS();

	@Override
	public boolean isNullCommandSender(CommandSender sender) {
		return sender instanceof NullCommandSender;
	}

	@Override
	public CommandRegistrationStrategy<CommandSourceStack> createCommandRegistrationStrategy() {
		return new SpigotCommandRegistration<>(
			bukkitNMS().<MinecraftServer>getMinecraftServer().vanillaCommandDispatcher.getDispatcher(),
			(SimpleCommandMap) getCommandMap(),
			() -> bukkitNMS().<MinecraftServer>getMinecraftServer().getCommands().getDispatcher(),
			command -> command instanceof VanillaCommandWrapper,
			node -> new VanillaCommandWrapper(bukkitNMS().<MinecraftServer>getMinecraftServer().vanillaCommandDispatcher, node),
			node -> node.getCommand() instanceof BukkitCommandWrapper
		);
	}

}
