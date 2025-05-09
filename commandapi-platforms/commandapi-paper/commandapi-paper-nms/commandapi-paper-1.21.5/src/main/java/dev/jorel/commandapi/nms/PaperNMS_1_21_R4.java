package dev.jorel.commandapi.nms;

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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ColorArgument;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.help.SimpleHelpMap;

import java.lang.reflect.Constructor;
import java.util.List;

public class PaperNMS_1_21_R4 extends CommandAPIPaper<CommandSourceStack> {

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
	public Component getChat(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return GsonComponentSerializer.gson().deserialize(net.minecraft.network.chat.Component.Serializer.toJson(MessageArgument.getMessage(cmdCtx, key), COMMAND_BUILD_CONTEXT));
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
	public NMS<?> bukkitNMS() {
		if (bukkitNMS == null) {
			this.bukkitNMS = new NMS_1_21_R4(COMMAND_BUILD_CONTEXT);
		}
		return bukkitNMS;
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

	@SuppressWarnings({"UnstableApiUsage"})
	@Override
	public <Source> LiteralCommandNode<Source> asPluginCommand(LiteralCommandNode<Source> commandNode, String description, List<String> aliases) {
		try {
			if (pluginCommandNodeConstructor != null) {
				metaField.set(commandNode, pluginCommandNodeConstructor.newInstance(
					CommandAPIBukkit.getConfiguration().getPlugin().getPluginMeta(),
					description,
					aliases
				));
			} else {
				commandNode.apiCommandMeta = new APICommandMeta(
					CommandAPIBukkit.getConfiguration().getPlugin().getPluginMeta(),
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
