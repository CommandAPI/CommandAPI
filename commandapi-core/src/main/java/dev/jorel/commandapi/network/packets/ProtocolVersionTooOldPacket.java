package dev.jorel.commandapi.network.packets;

import dev.jorel.commandapi.exceptions.ProtocolVersionTooOldException;
import dev.jorel.commandapi.network.CommandAPIPacket;
import dev.jorel.commandapi.network.CommandAPIProtocol;
import dev.jorel.commandapi.network.FriendlyByteBuffer;

/**
 * A packet that informs a connected instance of the CommandAPI their {@link CommandAPIProtocol#PROTOCOL_VERSION} is too old
 * to receive a different packet this instance wanted to send.
 * <p>
 * This packet may be sent in any direction. It is automatically sent whenever a {@link ProtocolVersionTooOldException}
 * is thrown while writing another packet. When received, a {@link ProtocolVersionTooOldException} should be thrown on the
 * receiving side that mimics the original exception.
 *
 * @param protocolVersion The {@link CommandAPIProtocol#PROTOCOL_VERSION} to send in this packet.
 * @param reason          The reason why this packet is being sent.
 */
public record ProtocolVersionTooOldPacket(
	/**
	 * @param protocolVersion The {@link CommandAPIProtocol#PROTOCOL_VERSION} to send in this packet.
	 */
	int protocolVersion,
	/**
	 * @param reason The reason why this packet is being sent.
	 */
	String reason
) implements CommandAPIPacket {
	public static final int MAX_REASON_LENGTH = 256;

	/**
	 * Reads the bytes from the given {@link FriendlyByteBuffer} to create a new {@link ProtocolVersionTooOldPacket}.
	 *
	 * @param buffer The buffer to read bytes from.
	 * @return The {@link ProtocolVersionTooOldPacket} sent to this plugin.
	 */
	public static ProtocolVersionTooOldPacket deserialize(FriendlyByteBuffer buffer) {
		int protocolVersion = buffer.readVarInt();
		String reason = buffer.readString(MAX_REASON_LENGTH);
		return new ProtocolVersionTooOldPacket(protocolVersion, reason);
	}

	@Override
	public void write(FriendlyByteBuffer buffer, Object target, int protocolVersion) {
		// No need to check `int protocolVersion`, we'll always send
		// If we did throw an exception, that would trigger another packet to be sent, causing infinite recursion anyway
		buffer.writeVarInt(this.protocolVersion);
		buffer.writeString(this.reason, MAX_REASON_LENGTH);
	}
}
