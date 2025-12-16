package dev.jorel.commandapi.network;

import dev.jorel.commandapi.exceptions.ProtocolVersionTooOldException;
import dev.jorel.commandapi.network.packets.ProtocolVersionTooOldPacket;
import dev.jorel.commandapi.network.packets.SetVersionPacket;

/**
 * An interface for handling {@link CommandAPIPacket}s on the {@link CommandAPIProtocol#HANDSHAKE} channel. Each method
 * handles a different packet. These methods should be implemented on each platform to define what happens when each
 * packet is received.
 *
 * @param <InputChannel> The type of objects that might send a packet to this plugin
 */
public interface HandshakePacketHandler<InputChannel> extends CommandAPIPacketHandler<InputChannel> {
	@Override
	default void handlePacket(InputChannel sender, CommandAPIPacket packet) {
		if (packet instanceof SetVersionPacket p) handleSetVersionPacket(sender, p);
		else if (packet instanceof ProtocolVersionTooOldPacket p) handleProtocolVersionTooOldPacket(sender, p);
		else throw new IllegalStateException("Tried to handle " + packet + " with HandshakePacketHandler. " +
					"HandshakePacketHandler can't handle this packet.");
	}

	/**
	 * Handles a {@link SetVersionPacket}.
	 *
	 * @param sender The source of the packet.
	 * @param packet The data for the packet.
	 */
	void handleSetVersionPacket(InputChannel sender, SetVersionPacket packet);

	/**
	 * Handles a {@link ProtocolVersionTooOldPacket}.
	 *
	 * @param sender The source of the packet.
	 * @param packet The data for the packet.
	 */
	default void handleProtocolVersionTooOldPacket(InputChannel sender, ProtocolVersionTooOldPacket packet) {
		throw ProtocolVersionTooOldException.received(sender, packet.protocolVersion(), packet.reason());
	}
}
