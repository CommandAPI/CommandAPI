package dev.jorel.commandapi;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.SuggestionProviders;
import dev.jorel.commandapi.commandsenders.AbstractCommandSender;
import dev.jorel.commandapi.commandsenders.AbstractPlayer;
import org.spongepowered.api.command.manager.CommandManager;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.List;

// See https://docs.spongepowered.org/stable/en/plugin/migrating-from-7-to-8.html#command-creation-and-registration

// TODO: How does Sponge send commands and interact with Brigadier?
public class CommandAPISponge extends CommandAPIPlatform<Argument<?>, Object, Object> {
	private CommandManager commandManager;
	private static CommandAPISponge instance;

	public CommandAPISponge() {
		instance = this;
	}

	public static CommandAPISponge get() {
		return instance;
	}

	@Override
	public void onLoad() {

	}

	@Override
	public void onEnable(Object pluginObject) {
		CommandAPISpongePluginWrapper plugin = (CommandAPISpongePluginWrapper) pluginObject;

		commandManager = plugin.getServer().commandManager();
	}

	@Override
	public void onDisable() {

	}

	@Override
	public void registerPermission(String string) {
		return; // Unsurprisingly, Sponge doesn't have a dumb permission system!
	}

	@Override
	public void unregister(String commandName, boolean force) {
//		commandManager.unregister(commandName);
	}

	@Override
	public CommandDispatcher<Object> getBrigadierDispatcher() {
		// TODO: How do we get this? Do we need access to sponge internals?
		return null;
	}

	@Override
	public CommandAPILogger getLogger() {
		// TODO: Make a default Logger
		return super.getLogger();
	}

	@Override
	public AbstractCommandSender<? extends Object> getSenderForCommand(CommandContext<Object> cmdCtx,
			boolean forceNative) {
		// TODO: Do something with this?
		cmdCtx.getSource();
		return null;
	}

	@Override
	public Object getBrigadierSourceFromCommandSender(AbstractCommandSender<?> sender) {
		return (Object) sender.getSource();
	}

	@Override
	public AbstractCommandSender<?> wrapCommandSender(Object o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SuggestionProvider<Object> getSuggestionProvider(SuggestionProviders suggestionProvider) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void preCommandRegistration(String commandName) {
		// Nothing to do?
	}

	@Override
	public void postCommandRegistration(LiteralCommandNode<Object> resultantNode, List<LiteralCommandNode<Object>> aliasNodes) {
		// Nothing to do?
	}

	@Override
	public LiteralCommandNode<Object> registerCommandNode(LiteralArgumentBuilder<Object> node) {
		return null;
	}

	@Override
	public AbstractCommandSender<?> getCommandSenderFromCommandSource(Object cs) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reloadDataPacks() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateRequirements(AbstractPlayer<?> player) {
		commandManager.updateCommandTreeForPlayer((ServerPlayer) player.getSource());
	}

	@Override
	public CommandAPICommand newConcreteCommandAPICommand(CommandMetaData<Object> meta) {
		return new CommandAPICommand(meta);
	}

	@Override
	public Argument<String> newConcreteMultiLiteralArgument(String[] literals) {
		return new MultiLiteralArgument(literals);
	}

	@Override
	public Argument<String> newConcreteLiteralArgument(String literal) {
		return new LiteralArgument(literal);
	}

}
