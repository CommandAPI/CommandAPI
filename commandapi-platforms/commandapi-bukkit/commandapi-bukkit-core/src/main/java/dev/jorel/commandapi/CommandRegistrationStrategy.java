package dev.jorel.commandapi;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class CommandRegistrationStrategy<Source> {
	// Behavior methods
	public abstract CommandDispatcher<Source> getBrigadierDispatcher();

	public abstract void runTasksAfterServerStart();

	public abstract void postCommandRegistration(RegisteredCommand<CommandSender> registeredCommand, LiteralCommandNode<Source> resultantNode, List<LiteralCommandNode<Source>> aliasNodes);

	public abstract void registerCommandNode(LiteralCommandNode<Source> node, String namespace);

	public abstract void unregister(String commandName, boolean unregisterNamespaces, boolean unregisterBukkit);

	public abstract void preReloadDataPacks();
}
