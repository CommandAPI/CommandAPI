package dev.jorel.commandapi.nms;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

public interface BundledNMS<Source> extends NMS<Source>, PaperNMS<Source> {

	SignedMessage getChat(CommandContext<Source> cmdCtx, String key) throws CommandSyntaxException;

	NamedTextColor getChatColor(CommandContext<Source> cmdCtx, String key);

	Component getChatComponent(CommandContext<Source> cmdCtx, String key) throws CommandSyntaxException;

	List<PlayerProfile> getProfile(CommandContext<Source> cmdCtx, String key) throws CommandSyntaxException;

}
