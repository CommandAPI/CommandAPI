package dev.jorel.commandapi.exceptions;

import dev.jorel.commandapi.network.CommandAPIProtocol;

public class ProtocolVersionTooOldException extends RuntimeException {
	public static ProtocolVersionTooOldException whileSending(Object target, int protocolVersion, String reason) {
		return new ProtocolVersionTooOldException(
			"Tried to send a packet to " + target + ", which is using protocol version " + protocolVersion + ". " +
				"This system is using version " + CommandAPIProtocol.PROTOCOL_VERSION + ". " +
				"That version is too old to receive the packet, or CommandAPI networking is not enabled. " +
				reason,
			protocolVersion, reason
		);
	}

	public static ProtocolVersionTooOldException received(Object sender, int protocolVersion, String reason) {
		return new ProtocolVersionTooOldException(
			sender + " tried to send a packet here using protocol version " + protocolVersion + ". " +
				"This system is using version " + CommandAPIProtocol.PROTOCOL_VERSION + ". " +
				"This version is too old to receive the packet. " +
				reason,
			protocolVersion, reason
		);
	}

	private final String reason;
	private final int protocolVersion;

	private ProtocolVersionTooOldException(String message, int protocolVersion, String reason) {
		super(message);
		this.reason = reason;
		this.protocolVersion = protocolVersion;
	}

	public String getReason() {
		return reason;
	}

	public int getProtocolVersion() {
		return protocolVersion;
	}
}