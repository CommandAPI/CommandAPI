package dev.jorel.commandapi.network;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.*;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.network.packets.SetVersionPacket;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// Based on https://gist.github.com/Xernium/95c9262c5f70b8791557861bbc09be1b
/**
 * A {@link CommandAPIMessenger} for sending and receiving messages on Velocity.
 * <p>
 * Velocity may receive messages from a Client ({@link Player}) or a Server ({@link ServerConnection}). The common
 * interface is {@link ChannelMessageSource} (coming into Velocity) and {@link ChannelMessageSink} (going out of
 * Velocity). Packets may be handled differently depending on their direction, so check which of these two sent
 * the packet before continuing.
 */
public class VelocityCommandAPIMessenger extends CommandAPIMessenger<ChannelMessageSource, ChannelMessageSink> {
	private final Object plugin;
	private final ProxyServer proxy;
	private final Map<ChannelMessageSink, Integer> protocolVersionPerOutgoingChannel;
	private final Map<Player, ServerConnection> previousServerConnectionPerPlayer;

	/**
	 * Creates a new {@link VelocityCommandAPIMessenger}.
	 *
	 * @param plugin The plugin object (annotated by {@link Plugin}) sending and receiving messages.
	 * @param proxy  The {@link ProxyServer} the plugin is running on.
	 */
	public VelocityCommandAPIMessenger(Object plugin, ProxyServer proxy) {
		super(new VelocityPacketHandlerProvider());
		this.plugin = plugin;
		this.proxy = proxy;

		this.protocolVersionPerOutgoingChannel = new HashMap<>();
		this.previousServerConnectionPerPlayer = new HashMap<>();

		// Register each channel so the PluginMessageEvent is fired
		ChannelRegistrar registrar = this.proxy.getChannelRegistrar();
		for (String channelIdentifier : CommandAPIProtocol.getAllChannelIdentifiers()) {
			registrar.register(MinecraftChannelIdentifier.from(channelIdentifier));
		}
		// Register this event handler to catch the PluginMessageEvent, and player join/leave
		this.proxy.getEventManager().register(this.plugin, this);
	}

	@Subscribe
	public void onServerConnected(ServerPostConnectEvent event) {
		// We can't send PluginMessage packets until the player is done connecting and both sides are in play mode
		//  The ServerPostConnectEvent is marked @Beta, but it fires exactly when we want and probably won't be removed

		Player player = event.getPlayer();

		// Forget protocol version for player's previous server
		//  We can get the previous RegisteredServer using event.getPreviousServer(), but we need the ServerConnection
		//  held by the player. Unfortunately, mapping it ourselves with previousServerConnectionPerPlayer seems to be
		//  the only way to do this.
		this.protocolVersionPerOutgoingChannel.remove(this.previousServerConnectionPerPlayer.get(player));

		// Send SetVersionPacket to inform player and server of our capabilities
		SetVersionPacket packet = new SetVersionPacket(CommandAPIProtocol.PROTOCOL_VERSION);

		this.sendPacket(player, packet);

		Optional<ServerConnection> wrappedServer = player.getCurrentServer();
		// This should always pass. The event indicates the player finished connecting, so their connection must be set.
		assert wrappedServer.isPresent();
		ServerConnection newServer = wrappedServer.get();

		this.sendPacket(newServer, packet);
		this.previousServerConnectionPerPlayer.put(player, newServer);
	}

	@Subscribe
	public void onPlayerLeave(DisconnectEvent event) {
		// Remove player from our protocol version map, so we don't keep track of useless data
		Player player = event.getPlayer();
		this.protocolVersionPerOutgoingChannel.remove(player);
		player.getCurrentServer().ifPresent(this.protocolVersionPerOutgoingChannel::remove);

		this.previousServerConnectionPerPlayer.remove(player);
	}

	@Override
	public void close() {
		// Unregister each channel and this listener
		ChannelRegistrar registrar = this.proxy.getChannelRegistrar();
		for (String channelIdentifier : CommandAPIProtocol.getAllChannelIdentifiers()) {
			registrar.unregister(MinecraftChannelIdentifier.from(channelIdentifier));
		}
		this.proxy.getEventManager().unregisterListener(this.plugin, this);
	}

	/**
	 * Sets the {@link CommandAPIProtocol#PROTOCOL_VERSION} being used by the given server. This is only intended to be
	 * called by {@link VelocityHandshakePacketHandler#handleSetVersionPacket(ChannelMessageSource, SetVersionPacket)}
	 * when a {@link SetVersionPacket} is received.
	 *
	 * @param server          The server that sent the {@link SetVersionPacket}.
	 * @param protocolVersion The {@link CommandAPIProtocol#PROTOCOL_VERSION} in the {@link SetVersionPacket}.
	 */
	public void setServerProtocolVersion(ServerConnection server, int protocolVersion) {
		this.protocolVersionPerOutgoingChannel.put(server, protocolVersion);
	}

	/**
	 * Sets the {@link CommandAPIProtocol#PROTOCOL_VERSION} being used by the given player. This is only intended to be
	 * called by {@link VelocityHandshakePacketHandler#handleSetVersionPacket(ChannelMessageSource, SetVersionPacket)}
	 * when a {@link SetVersionPacket} is received.
	 *
	 * @param player          The player that sent the {@link SetVersionPacket}.
	 * @param protocolVersion The {@link CommandAPIProtocol#PROTOCOL_VERSION} in the {@link SetVersionPacket}.
	 */
	public void setPlayerProtocolVersion(Player player, int protocolVersion) {
		this.protocolVersionPerOutgoingChannel.put(player, protocolVersion);
	}

	@Override
	public int getConnectionProtocolVersion(ChannelMessageSink target) {
		return this.protocolVersionPerOutgoingChannel.getOrDefault(target, 0);
	}

	@Subscribe
	public void onPluginMessageEvent(PluginMessageEvent event) {
		// A plugin message was sent to Velocity, check if it is for us
		String channel = event.getIdentifier().getId();
		CommandAPIProtocol protocol = CommandAPIProtocol.getProtocolForChannel(channel);
		if (protocol == null) return;

		// We will handle this message, set the result so that it is not forwarded
		event.setResult(PluginMessageEvent.ForwardResult.handled());

		// Handle the message
		messageReceived(protocol, event.getSource(), event.getData());
	}

	@Override
	public void sendRawBytes(CommandAPIProtocol protocol, ChannelMessageSink target, byte[] bytes) {
		target.sendPluginMessage(MinecraftChannelIdentifier.from(protocol.getChannelIdentifier()), bytes);
	}

	@Override
	protected void handlePacketException(RuntimeException exception, ChannelMessageSource source) {
		if (source instanceof Player player) {
			player.disconnect(Component.text("Sent invalid plugin message data."));
		}

		if (CommandAPI.getConfiguration().makeNetworkingExceptionsWarnings()) {
			CommandAPI.logWarning(exception.getMessage());
		} else {
			throw exception;
		}
	}
}
