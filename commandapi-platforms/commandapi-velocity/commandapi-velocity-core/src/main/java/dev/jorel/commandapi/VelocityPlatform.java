package dev.jorel.commandapi;

import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;

import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import dev.jorel.commandapi.abstractions.AbstractCommandSender;
import dev.jorel.commandapi.abstractions.AbstractPlatform;
import dev.jorel.commandapi.abstractions.AbstractPlayer;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.commandsenders.VelocityCommandSender;
import dev.jorel.commandapi.commandsenders.VelocityConsoleCommandSender;
import dev.jorel.commandapi.commandsenders.VelocityPlayer;

public class VelocityPlatform extends AbstractPlatform<CommandSource, CommandSource> {
	private CommandManager commandManager;
	private static VelocityPlatform instance;

	public VelocityPlatform() {
		instance = this;
	}

	public static VelocityPlatform get() {
		return instance;
	}

	@Override
	public void onLoad() {

	}

	@Override
	public void onEnable(Object pluginObject) {
		CommandAPIVelocityPluginWrapper plugin = (CommandAPIVelocityPluginWrapper) pluginObject;

		commandManager = plugin.getServer().getCommandManager();
	}

	@Override
	public void onDisable() {

	}

	@Override
	public void registerPermission(String string) {
		return; // Unsurprisingly, Velocity doesn't have a dumb permission system!
	}


	@Override
	public void registerHelp() {
		return; // Nothing to do here - TODO: Velocity doesn't have help?
	}

	@Override
	public void unregister(String commandName, boolean force) {
		commandManager.unregister(commandName);
	}

	@Override
	public CommandDispatcher<CommandSource> getBrigadierDispatcher() {
		// TODO: How do we get this? Do we need access to velocity internals?
		return null;
	}

	@Override
	public VelocityCommandSender<? extends CommandSource> getSenderForCommand(CommandContext<CommandSource> cmdCtx, boolean forceNative) {
		// TODO: This method MAY be completely identical to getCommandSenderFromCommandSource.
		// In Bukkit, this is NOT the case - we have to apply certain changes based
		// on the command context - for example, if we're proxying another entity or
		// otherwise (e.g. native sender)
		
		// I'm fairly certain we don't have to do that in Velocity, so we'll just go straight
		// for this:
		return getCommandSenderFromCommandSource(cmdCtx.getSource());
	}

	@Override
	public VelocityCommandSender<? extends CommandSource> getCommandSenderFromCommandSource(CommandSource cs) {
		// Given a Brigadier CommandContext source (result of CommandContext.getSource),
		// we need to convert that to an AbstractCommandSender.
		if(cs instanceof ConsoleCommandSource ccs)
			return new VelocityConsoleCommandSender(ccs);
		if(cs instanceof Player p)
			return new VelocityPlayer(p);
		throw new IllegalArgumentException("Unknown CommandSource: " + cs);
	}

	@Override
	public VelocityCommandSender<? extends CommandSource> wrapCommandSender(CommandSource commandSource) {
		return getCommandSenderFromCommandSource(commandSource);
	}

	@Override
	public CommandSource getBrigadierSourceFromCommandSender(AbstractCommandSender<? extends CommandSource> sender) {
		return sender.getSource();
	}

	@Override
	public SuggestionProvider<CommandSource> getSuggestionProvider(SuggestionProviders suggestionProvider) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void postCommandRegistration(LiteralCommandNode<CommandSource> resultantNode,
			List<LiteralCommandNode<CommandSource>> aliasNodes) {
		return; // Nothing left to do
	}

	@SuppressWarnings("unchecked")
	@Override
	public LiteralCommandNode<CommandSource> registerCommandNode(LiteralArgumentBuilder<CommandSource> node) {
		BrigadierCommand command = new BrigadierCommand(node);
		commandManager.register(command);
		return command.getNode();
	}

	@Override
	public void reloadDataPacks() {
		// TODO Auto-generated method stub
		//  See note for CommandAPI#reloadDatapacks
	}

	@Override
	public void updateRequirements(AbstractPlayer<?> player) {
		// TODO Auto-generated method stub
		//  See note for CommandAPI#updateRequirements
	}

	@Override
	public Execution<CommandSource> newConcreteExecution(List<Argument<?, ?, CommandSource>> argument, CustomCommandExecutor<CommandSource, AbstractCommandSender<? extends CommandSource>> executor) {
		return new VelocityExecution(argument, executor);
	}

	@Override
	public AbstractMultiLiteralArgument<?, CommandSource> newConcreteMultiLiteralArgument(String[] literals) {
		return new MultiLiteralArgument(literals);
	}

	@Override
	public AbstractLiteralArgument<?, CommandSource> newConcreteLiteralArgument(String literal) {
		return new LiteralArgument(literal);
	}
}