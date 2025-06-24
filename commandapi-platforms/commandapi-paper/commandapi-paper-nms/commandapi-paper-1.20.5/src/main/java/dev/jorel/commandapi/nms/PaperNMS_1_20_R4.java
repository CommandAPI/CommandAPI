package dev.jorel.commandapi.nms;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.collect.Collections2;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPIPaper;
import dev.jorel.commandapi.CommandRegistrationStrategy;
import dev.jorel.commandapi.PaperCommandRegistration;
import dev.jorel.commandapi.SpigotCommandRegistration;
import io.papermc.paper.command.brigadier.PaperCommands;
import io.papermc.paper.command.brigadier.PluginCommandNode;
import io.papermc.paper.command.brigadier.bukkit.BukkitCommandNode;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ColorArgument;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.command.BukkitCommandWrapper;
import org.bukkit.craftbukkit.command.VanillaCommandWrapper;
import org.bukkit.craftbukkit.help.SimpleHelpMap;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PaperNMS_1_20_R4 implements PaperNMS<CommandSourceStack> {

	private CommandBuildContext commandBuildContext;
	private static final boolean vanillaCommandDispatcherFieldExists;
	private static final Commands vanillaCommandDispatcher;

	private NMS_1_20_R4 bukkitNMS;

	static {
		boolean fieldExists;
		Commands commandDispatcher;
		try {
			Field vanillaCommandDispatcherField = MinecraftServer.class.getDeclaredField("vanillaCommandDispatcher");
			commandDispatcher = (Commands) vanillaCommandDispatcherField.get(CommandAPIPaper.getPaper().getNMS().<MinecraftServer>getMinecraftServer());
			fieldExists = true;
		} catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
			// Expected on Paper-1.20.6-65 or later due to https://github.com/PaperMC/Paper/pull/8235
			commandDispatcher = null;
			fieldExists = false;
		}
		vanillaCommandDispatcher = commandDispatcher;
		vanillaCommandDispatcherFieldExists = fieldExists;
	}

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
	public SignedMessage getChat(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		CompletableFuture<SignedMessage> future = new CompletableFuture<>();
		MessageArgument.resolveChatMessage(cmdCtx, key, (message) -> future.complete(message.adventureView()));
		return future.join();
	}

	@Override
	public NamedTextColor getChatColor(CommandContext<CommandSourceStack> cmdCtx, String key) {
		final Integer color = ColorArgument.getColor(cmdCtx, key).getColor();
		return color == null ? NamedTextColor.WHITE : NamedTextColor.namedColor(color);
	}

	@Override
	public Component getChatComponent(CommandContext<CommandSourceStack> cmdCtx, String key) {
		return GsonComponentSerializer.gson().deserialize(net.minecraft.network.chat.Component.Serializer.toJson(ComponentArgument.getComponent(cmdCtx, key), getCommandBuildContext()));
	}

	@Override
	public final List<PlayerProfile> getProfile(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		GameProfileArgument.Result result = cmdCtx.getArgument(key, GameProfileArgument.Result.class);
		return result instanceof GameProfileArgument.SelectorResult selectorResult
			? List.of(Collections2.transform(selectorResult.getNames(cmdCtx.getSource()), CraftPlayerProfile::new).toArray(new PlayerProfile[0]))
			: List.of(Collections2.transform(result.getNames(cmdCtx.getSource()), CraftPlayerProfile::new).toArray(new PlayerProfile[0]));
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
		if (vanillaCommandDispatcherFieldExists) {
			return new SpigotCommandRegistration<>(
				vanillaCommandDispatcher.getDispatcher(),
				(SimpleCommandMap) CommandAPIBukkit.get().getCommandMap(),
				() -> bukkitNMS.<MinecraftServer>getMinecraftServer().getCommands().getDispatcher(),
				command -> command instanceof VanillaCommandWrapper,
				node -> new VanillaCommandWrapper(vanillaCommandDispatcher, node),
				node -> node.getCommand() instanceof BukkitCommandWrapper
			);
		} else {
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

}
