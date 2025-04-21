package dev.jorel.commandapi.network.packets;

import com.mojang.brigadier.builder.ArgumentBuilder;
import dev.jorel.commandapi.exceptions.ProtocolVersionTooOldException;
import dev.jorel.commandapi.network.CommandAPIPacket;
import dev.jorel.commandapi.network.FriendlyByteBuffer;

import java.util.function.Predicate;

/**
 * A packet that signals a client wants to be sent the <a href="https://wiki.vg/Protocol#Commands">Commands packet</a>.
 * This packet is only intended to be sent in the Client to Server direction.
 * <p>
 * This packet does not contain any data. The receiver should simply update the requirements for the client the packet
 * came from.
 * <p>
 * When the Commands packet is resent, Brigadier reevaluates if that player is able to run each command. If the
 * conditions tested by {@link ArgumentBuilder#requires(Predicate)} have changed, the client is informed of those
 * changes, updating what they know about their requirements.
 */
public record UpdateRequirementsPacket() implements CommandAPIPacket {
	/**
	 * Reads the bytes from the given {@link FriendlyByteBuffer} to create a new
	 * {@link UpdateRequirementsPacket}.
	 *
	 * @param ignored This packet has no data, so nothing is read.
	 * @return The {@link UpdateRequirementsPacket} sent to this plugin.
	 */
	public static UpdateRequirementsPacket deserialize(FriendlyByteBuffer ignored) {
		// Nothing to read
		return new UpdateRequirementsPacket();
	}

	@Override
	public void write(FriendlyByteBuffer buffer, Object target, int protocolVersion) throws ProtocolVersionTooOldException {
		if (protocolVersion == 0) {
			throw ProtocolVersionTooOldException.whileSending(target, protocolVersion,
				"CommandAPI version 10.0.0 or greater is required to receive UpdateRequirementsPacket."
			);
		}
		// Nothing to write
	}
}
