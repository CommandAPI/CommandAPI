package dev.jorel.commandapi.nms;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.arguments.PreviewInfo;
import dev.jorel.commandapi.commandnodes.PreviewableCommandNode;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.wrappers.PreviewableFunction;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundChatPreviewPacket;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Optional;

public abstract class NMS_1_19_Common_ChatPreviewHandler extends ChannelDuplexHandler {

	protected final CommandAPIBukkit<CommandSourceStack> platform;
	protected final Plugin plugin;
	protected final Player player;
	protected final Connection connection;

	protected NMS_1_19_Common_ChatPreviewHandler(CommandAPIBukkit<CommandSourceStack> platform, Plugin plugin, Player player) {
		this.platform = platform;
		this.plugin = plugin;
		this.player = player;
		this.connection = ((CraftPlayer) player).getHandle().connection.connection;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof ServerboundChatPreviewPacket chatPreview) {
			// make sure the result is worth consuming here
			// Is command
			if (!chatPreview.query().isEmpty() && chatPreview.query().charAt(0) == '/') {
				// Is previewable argument
				if(InitialParse.processChatPreviewQuery(chatPreview.query(), platform, player).previewableNode.getPreview().isPresent()){
					handleChatPreviewPacket(chatPreview);
					return;
				}
			}
		}

		// Normal packet handling
		super.channelRead(ctx, msg);
	}

	protected abstract void handleChatPreviewPacket(ServerboundChatPreviewPacket chatPreview);

	public MutableComponent parseChatPreviewQuery(String chatPreviewQuery) {
		// Parse the string
		final InitialParse ip = InitialParse.processChatPreviewQuery(chatPreviewQuery, platform, player);

		// Return early if the node is not previewable
		final PreviewableCommandNode<CommandSourceStack, ?> previewableNode = ip.previewableNode;
		final Optional<PreviewableFunction<?, ?>> preview = previewableNode.getPreview();
		if (preview.isEmpty()) {
			return null;
		}

		final String fullInput = ip.fullInput;
		final ParseResults<CommandSourceStack> results = ip.results;
		final ParsedCommandNode<CommandSourceStack> parsedNode = ip.parsedNode;

		// Calculate the (argument) input and generate the component to send
		String input = parsedNode.getRange().get(fullInput);

		final String jsonToSend;

		Object component;
		try {
			@SuppressWarnings("rawtypes") final PreviewInfo previewInfo;
			if (previewableNode.isLegacy()) {
				BaseComponent[] parsedInput;
				try {
					parsedInput = platform.getChat(results.getContext().build(fullInput), previewableNode.getName());
				} catch (CommandSyntaxException e) {
					throw new WrapperCommandSyntaxException(e);
				}
				previewInfo = new PreviewInfo<>(player, input, chatPreviewQuery, parsedInput);
			} else {
				Component parsedInput;
				try {
					parsedInput = platform.getAdventureChat(results.getContext().build(fullInput), previewableNode.getName());
				} catch (CommandSyntaxException e) {
					throw new WrapperCommandSyntaxException(e);
				}
				previewInfo = new PreviewInfo<>(player, input, chatPreviewQuery, parsedInput);
			}

			component = preview.get().generatePreview(previewInfo);
		} catch (WrapperCommandSyntaxException e) {
			component = TextComponent.fromLegacyText(e.getMessage() == null ? "" : e.getMessage());
		}

		if (component != null) {
			if (component instanceof BaseComponent[] baseComponent) {
				jsonToSend = ComponentSerializer.toString(baseComponent);
			} else if (platform.getPaper().isPaperPresent()) {
				if (component instanceof Component adventureComponent) {
					jsonToSend = GsonComponentSerializer.gson().serialize(adventureComponent);
				} else {
					throw new IllegalArgumentException("Unexpected type returned from chat preview, got: " + component.getClass().getSimpleName());
				}
			} else {
				throw new IllegalArgumentException("Unexpected type returned from chat preview, got: " + component.getClass().getSimpleName());
			}
		} else {
			throw new NullPointerException("Returned value from chat preview was null");
		}

		return Serializer.fromJson(jsonToSend);
	}

	private record InitialParse(String fullInput, ParseResults<CommandSourceStack> results, ParsedCommandNode<CommandSourceStack> parsedNode, PreviewableCommandNode<CommandSourceStack, ?> previewableNode){
		private static InitialParse cachedResult = null;
		public static InitialParse processChatPreviewQuery(String chatPreviewQuery, CommandAPIBukkit<CommandSourceStack> platform, Player player){
			// Substring 1 to get rid of the leading /
			final String fullInput = chatPreviewQuery.substring(1);

			if(cachedResult != null && cachedResult.fullInput.equals(fullInput)) return cachedResult;

			ParseResults<CommandSourceStack> results = platform.getBrigadierDispatcher()
				.parse(fullInput, platform.getBrigadierSourceFromCommandSender(player));

			// Get the last node
			List<ParsedCommandNode<CommandSourceStack>> nodes = results.getContext().getNodes();
			ParsedCommandNode<CommandSourceStack> parsedNode = nodes.get(nodes.size()-1);

			// Get the parsed node, if it exists
			PreviewableCommandNode<CommandSourceStack, ?> previewableNode = parsedNode.getNode() instanceof PreviewableCommandNode<CommandSourceStack, ?> pn ? pn : null;

			// Cache the result and return
			cachedResult = new InitialParse(fullInput, results, parsedNode, previewableNode);
			return cachedResult;
		}
	}
}