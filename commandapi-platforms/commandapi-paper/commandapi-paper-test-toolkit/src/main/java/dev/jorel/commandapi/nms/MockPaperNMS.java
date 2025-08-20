package dev.jorel.commandapi.nms;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jorel.commandapi.*;
import dev.jorel.commandapi.arguments.ProfileArgumentType;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

public class MockPaperNMS extends MockNMS implements BundledNMS<MockCommandSource> {
	@Override
	public NMS<MockCommandSource> bukkitNMS() {
		return this;
	}

	@Override
	public CommandRegistrationStrategy<MockCommandSource> createCommandRegistrationStrategy() {
		return MockCommandAPI.getInstance().getCommandRegistrationStrategy();
	}

	@Override
	public List<PlayerProfile> getProfile(CommandContext<MockCommandSource> cmdCtx, String key) throws CommandSyntaxException {
		return (List<PlayerProfile>) ProfileArgumentType.getProfiles(cmdCtx, key);
	}

	///////////////////////////
	// UNIMPLEMENTED METHODS //
	///////////////////////////

	@Override
	public SignedMessage getChat(CommandContext<MockCommandSource> cmdCtx, String key) throws CommandSyntaxException {
		throw new UnimplementedMethodException();
	}

	@Override
	public NamedTextColor getChatColor(CommandContext<MockCommandSource> cmdCtx, String key) {
		throw new UnimplementedMethodException();
	}

	@Override
	public Component getChatComponent(CommandContext<MockCommandSource> cmdCtx, String key) throws CommandSyntaxException {
		throw new UnimplementedMethodException();
	}
}
