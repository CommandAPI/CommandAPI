package dev.jorel.commandapi.network;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelMessageSource;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIVelocity;
import dev.jorel.commandapi.exceptions.ProtocolVersionTooOldException;
import dev.jorel.commandapi.network.packets.ProtocolVersionTooOldPacket;
import dev.jorel.commandapi.network.packets.SetVersionPacket;

/**
 * A {@link HandshakePacketHandler} for handling {@link CommandAPIPacket}s sent to Velocity.
 * <p>
 * Velocity may receive messages from a Client ({@link Player}) or a Server {@link ServerConnection}. The common
 * interface there is {@link ChannelMessageSource} (coming into Velocity). Packets may be handled differently depending
 * on their direction, so check which of these two sent the packet before continuing.
 */
public class VelocityHandshakePacketHandler implements HandshakePacketHandler<ChannelMessageSource> {
	@Override
	public void handleSetVersionPacket(ChannelMessageSource sender, SetVersionPacket packet) {
		int protocolVersion = packet.protocolVersion();
		VelocityCommandAPIMessenger messenger = CommandAPIVelocity.get().getMessenger();

		// Incoming messages are from ChannelMessageSource, while outgoing messages are ChannelMessageSink
		//  We actually want to set the version of a ChannelMessageSink. These instanceofs safely down-cast,
		//  then the method call up-casts.
		if (sender instanceof ServerConnection server) messenger.setServerProtocolVersion(server, protocolVersion);
		if (sender instanceof Player player) messenger.setPlayerProtocolVersion(player, protocolVersion);
	}

	@Override
	public void handleProtocolVersionTooOldPacket(ChannelMessageSource sender, ProtocolVersionTooOldPacket packet) {
		try {
			HandshakePacketHandler.super.handleProtocolVersionTooOldPacket(sender, packet);
		} catch (ProtocolVersionTooOldException exception) {
			if (CommandAPI.getConfiguration().shouldErrorOnFailedPacketSends()) {
				throw exception;
			} else {
				CommandAPI.logWarning(exception.getMessage());
			}
		}
	}
}
