package dev.jorel.commandapi.nms;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

public abstract class BundledNMS<Source> implements NMS<Source>, PaperNMS<Source> {

	public abstract SignedMessage getChat(CommandContext<Source> cmdCtx, String key) throws CommandSyntaxException;

	public abstract NamedTextColor getChatColor(CommandContext<Source> cmdCtx, String key);

	public abstract Component getChatComponent(CommandContext<Source> cmdCtx, String key) throws CommandSyntaxException;

	public abstract List<PlayerProfile> getProfile(CommandContext<Source> cmdCtx, String key) throws CommandSyntaxException;

}
