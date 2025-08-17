package dev.jorel.commandapi.nms;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jorel.commandapi.MockCommandSource;
import dev.jorel.commandapi.UnimplementedMethodException;
import dev.jorel.commandapi.arguments.ProfileArgumentType;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.ChatColor;
import org.bukkit.profile.PlayerProfile;

import java.util.List;

public interface MockSpigotNMS extends SpigotNMS<MockCommandSource> {
	// Arguments
	@Override
	default List<PlayerProfile> getProfile(CommandContext<MockCommandSource> cmdCtx, String key) throws CommandSyntaxException {
		return ProfileArgumentType.getProfiles(cmdCtx, key);
	}

	///////////////////////////
	// UNIMPLEMENTED METHODS //
	///////////////////////////
	@Override
	default BaseComponent[] getChat(CommandContext<MockCommandSource> cmdCtx, String key) throws CommandSyntaxException {
		throw new UnimplementedMethodException();
	}

	@Override
	default ChatColor getChatColor(CommandContext<MockCommandSource> cmdCtx, String key) {
		throw new UnimplementedMethodException();
	}

	@Override
	default BaseComponent[] getChatComponent(CommandContext<MockCommandSource> cmdCtx, String key) throws CommandSyntaxException {
		throw new UnimplementedMethodException();
	}
}
