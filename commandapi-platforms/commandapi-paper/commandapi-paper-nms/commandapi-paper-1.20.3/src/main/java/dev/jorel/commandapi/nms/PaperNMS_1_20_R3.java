package dev.jorel.commandapi.nms;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.collect.Collections2;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandRegistrationStrategy;
import dev.jorel.commandapi.SpigotCommandRegistration;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ColorArgument;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.server.MinecraftServer;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_20_R3.command.BukkitCommandWrapper;
import org.bukkit.craftbukkit.v1_20_R3.command.VanillaCommandWrapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PaperNMS_1_20_R3 implements PaperNMS<CommandSourceStack> {

	private NMS_1_20_R3 bukkitNMS;

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
	public final Component getChatComponent(CommandContext<CommandSourceStack> cmdCtx, String key) throws CommandSyntaxException {
		return GsonComponentSerializer.gson().deserialize(net.minecraft.network.chat.Component.Serializer.toJson(ComponentArgument.getComponent(cmdCtx, key)));
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
			this.bukkitNMS = new NMS_1_20_R3();
		}
		return (NMS<Source>) bukkitNMS;
	}

	@Override
	public CommandRegistrationStrategy<CommandSourceStack> createCommandRegistrationStrategy() {
		return new SpigotCommandRegistration<>(
			bukkitNMS.<MinecraftServer>getMinecraftServer().vanillaCommandDispatcher.getDispatcher(),
			(SimpleCommandMap) CommandAPIBukkit.get().getCommandMap(),
			() -> bukkitNMS.<MinecraftServer>getMinecraftServer().getCommands().getDispatcher(),
			command -> command instanceof VanillaCommandWrapper,
			node -> new VanillaCommandWrapper(bukkitNMS.<MinecraftServer>getMinecraftServer().vanillaCommandDispatcher, node),
			node -> node.getCommand() instanceof BukkitCommandWrapper
		);
	}

	@Override
	public boolean isDispatcherValid() {
		return false; // This is fine, it isn't used
	}

	@Override
	public <Source> LiteralCommandNode<Source> asPluginCommand(LiteralCommandNode<Source> commandNode, String description, List<String> aliases) {
		return null; // This is fine, it isn't used
	}

	@Override
	public <Source> CommandDispatcher<Source> getPaperCommandDispatcher() {
		return null; // This is fine, it isn't used
	}

}
