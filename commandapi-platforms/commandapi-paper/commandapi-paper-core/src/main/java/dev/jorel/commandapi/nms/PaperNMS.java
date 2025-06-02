package dev.jorel.commandapi.nms;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.jorel.commandapi.CommandRegistrationStrategy;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

public interface PaperNMS<CommandListenerWrapper> {

	SignedMessage getChat(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException;

	NamedTextColor getChatColor(CommandContext<CommandListenerWrapper> cmdCtx, String key);

	Component getChatComponent(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException;

	<Source> NMS<Source> bukkitNMS();

	CommandRegistrationStrategy<CommandListenerWrapper> createCommandRegistrationStrategy();

	boolean isDispatcherValid();

	<Source> LiteralCommandNode<Source> asPluginCommand(LiteralCommandNode<Source> commandNode, String description, List<String> aliases);

	<Source>CommandDispatcher<Source> getPaperCommandDispatcher();

}
