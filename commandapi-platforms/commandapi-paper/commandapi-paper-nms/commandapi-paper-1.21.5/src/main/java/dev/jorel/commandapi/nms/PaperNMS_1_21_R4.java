package dev.jorel.commandapi.nms;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.collect.Collections2;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPIPaper;
import dev.jorel.commandapi.CommandRegistrationStrategy;
import dev.jorel.commandapi.PaperCommandRegistration;
import dev.jorel.commandapi.SafeVarHandle;
import io.papermc.paper.command.brigadier.APICommandMeta;
import io.papermc.paper.command.brigadier.PaperCommands;
import io.papermc.paper.command.brigadier.bukkit.BukkitCommandNode;
import io.papermc.paper.plugin.configuration.PluginMeta;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ColorArgument;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.help.SimpleHelpMap;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PaperNMS_1_21_R4 implements PaperNMS<CommandSourceStack> {

	private static final CommandBuildContext COMMAND_BUILD_CONTEXT;
	private static final Constructor<?> pluginCommandNodeConstructor;
	private static final SafeVarHandle<CommandNode<?>, Object> metaField;

	private NMS_1_21_R4 bukkitNMS;

	static {
		if (Bukkit.getServer() instanceof CraftServer server) {
			COMMAND_BUILD_CONTEXT = CommandBuildContext.simple(server.getServer().registryAccess(),
				server.getServer().getWorldData().enabledFeatures());
		} else {
			COMMAND_BUILD_CONTEXT = null;
		}

		Constructor<?> pluginCommandNode;
		SafeVarHandle<CommandNode<?>, ?> metaFieldHandle = null;
		try {
			Class<?> pluginCommandMeta = Class.forName("io.papermc.paper.command.brigadier.PluginCommandMeta");
			pluginCommandNode = pluginCommandMeta.getDeclaredConstructor(PluginMeta.class, String.class, List.class);
			metaFieldHandle = SafeVarHandle.ofOrNull(CommandNode.class, "pluginCommandNode", "pluginCommandNode", pluginCommandMeta);
		} catch (ReflectiveOperationException e) {
			pluginCommandNode = null;
		}
		pluginCommandNodeConstructor = pluginCommandNode;
		metaField = (SafeVarHandle<CommandNode<?>, Object>) metaFieldHandle;
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
	public Component getChatComponent(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return GsonComponentSerializer.gson().deserialize(net.minecraft.network.chat.Component.Serializer.toJson(ComponentArgument.getResolvedComponent(cmdCtx, key), COMMAND_BUILD_CONTEXT));
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
			this.bukkitNMS = new NMS_1_21_R4(COMMAND_BUILD_CONTEXT);
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

	@SuppressWarnings("UnstableApiUsage")
	@Override
	public boolean isDispatcherValid() {
		boolean validState;
		try {
			PaperCommands.INSTANCE.getDispatcher();
			validState = true;
		} catch (IllegalStateException e) {
			validState = false;
		}
		return validState;
	}

	@SuppressWarnings({"UnstableApiUsage"})
	@Override
	public <Source> LiteralCommandNode<Source> asPluginCommand(LiteralCommandNode<Source> commandNode, String description, List<String> aliases) {
		try {
			if (pluginCommandNodeConstructor != null) {
				metaField.set(commandNode, pluginCommandNodeConstructor.newInstance(
					CommandAPIPaper.getConfiguration().getPluginMeta(),
					description,
					aliases
				));
			} else {
				commandNode.apiCommandMeta = new APICommandMeta(
					CommandAPIPaper.getConfiguration().getPluginMeta(),
					description,
					aliases,
					CommandAPIBukkit.getConfiguration().getNamespace(),
					false
				);
			}
			return commandNode;
		} catch (ReflectiveOperationException e) {
			return commandNode;
		}
	}

	@SuppressWarnings({"unchecked", "UnstableApiUsage"})
	@Override
	public <Source> CommandDispatcher<Source> getPaperCommandDispatcher() {
		return (CommandDispatcher<Source>) PaperCommands.INSTANCE.getDispatcherInternal();
	}

}
