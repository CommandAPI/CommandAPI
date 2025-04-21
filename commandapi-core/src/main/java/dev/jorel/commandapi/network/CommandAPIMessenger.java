package dev.jorel.commandapi.network;

import dev.jorel.commandapi.exceptions.ProtocolVersionTooOldException;
import dev.jorel.commandapi.network.packets.ProtocolVersionTooOldPacket;
import dev.jorel.commandapi.network.packets.SetVersionPacket;

import java.util.Arrays;

/**
 * Handles sending and receiving {@link CommandAPIPacket}s between instances of the CommandAPI plugin on different servers.
 * This class should be extended on each platform to hook into its specific plugin-messaging scheme.
 *
 * @param <InputChannel> The type of objects that might send a packet to this plugin
 * @param <OutputChannel> The type of objects this plugin might send messages to
 */
public abstract class CommandAPIMessenger<InputChannel, OutputChannel> {
	private final CommandAPIPacketHandlerProvider<InputChannel> packetHandlerProvider;

	/**
	 * Creates a new {@link CommandAPIMessenger}.
	 *
	 * @param packetHandlerProvider The {@link CommandAPIPacketHandlerProvider} that defines the various implementations
	 *                              of {@link CommandAPIPacketHandler} that should be used to handle incoming packets.
	 */
	protected CommandAPIMessenger(CommandAPIPacketHandlerProvider<InputChannel> packetHandlerProvider) {
		this.packetHandlerProvider = packetHandlerProvider;
	}

	/**
	 * Disables this messenger, unregistering any events and such
	 * that let it send/receive packets before the plugin disables.
	 */
	public abstract void close();

	/**
	 * Gets the {@link CommandAPIProtocol#PROTOCOL_VERSION} on the other side of an outgoing connection. This is determined
	 * by the value in the {@link SetVersionPacket} sent by this connection, or 0 if they never sent that packet.
	 *
	 * @param target The {@link OutputChannel} to get the version for.
	 * @return The {@link CommandAPIProtocol#PROTOCOL_VERSION} supported by this target.
	 */
	public abstract int getConnectionProtocolVersion(OutputChannel target);

	/**
	 * Handles a message sent to this plugin. The given byte array will be decoded into a {@link CommandAPIPacket}
	 * according to the given {@link CommandAPIProtocol}, then handled appropriately. Nothing happens if the given byte
	 * array is empty.
	 *
	 * @param protocol The protocol to handle the message with.
	 * @param sender   The source of the message.
	 * @param input    The byte array of the message.
	 * @throws IllegalStateException if the packet is malformed and cannot be read correctly.
	 */
	protected void messageReceived(CommandAPIProtocol protocol, InputChannel sender, byte[] input) {
		// Inspired by net.minecraft.PacketDecoder

		// Empty packet. Kinda weird, but nothing to do.
		if (input.length == 0) return;

		FriendlyByteBuffer buffer = new FriendlyByteBuffer(input);

		int id;
		CommandAPIPacket packet;
		try {
			// Read the id
			id = buffer.readVarInt();
			// Use the id and protocol to find and use the correct deserialize method
			packet = protocol.createPacket(id, buffer);
		} catch (IllegalStateException e) {
			throw new IllegalStateException("Exception while reading packet", e);
		}
		if (packet == null) throw new IllegalStateException("Unknown packet id: " + id);

		if (buffer.countReadableBytes() != 0) {
			// If the packet didn't read all the bytes it was given, we have a strange miscommunication
			throw new IllegalStateException(
				"Packet was larger than expected! " + buffer.countReadableBytes() + " extra byte(s) found after deserializing.\n" +
					"Given: " + Arrays.toString(input) + ", Read: " + packet
			);
		}

		// Handle the packet
		packetHandlerProvider.getHandlerForProtocol(protocol).handlePacket(sender, packet);
	}

	/**
	 * Sends a packet to the given target. The given packet will be converted into a byte array and sent using
	 * {@link CommandAPIMessenger#sendRawBytes(CommandAPIProtocol, Object, byte[])}. The packet must be registered in
	 * {@link CommandAPIProtocol} to be sent.
	 *
	 * @param target The connection to send the packet to.
	 * @param packet The {@link CommandAPIPacket} to send.
	 * @throws IllegalStateException if the given packet is not registered in {@link CommandAPIProtocol}.
	 */
	public void sendPacket(OutputChannel target, CommandAPIPacket packet) {
		// Inspired by net.minecraft.PacketEncoder
		Class<? extends CommandAPIPacket> packetType = packet.getClass();

		CommandAPIProtocol protocol = CommandAPIProtocol.getProtocolForPacket(packetType);
		if (protocol == null) {
			throw new IllegalStateException("Packet class \"" + packet.getClass().getSimpleName() +
				"\" is not associated with a CommandAPIProtocol. This packet must be registered before it can be sent.");
		}

		int id = protocol.getId(packetType);
		// The packetType should always be found in this protocol, since `getProtocolForPacket` told us this was
		//  the protocol for this packet.
		assert id != -1;


		FriendlyByteBuffer output = new FriendlyByteBuffer();

		// Write packet's id
		output.writeVarInt(id);

		try {
			// Write packet's data
			packet.write(output, target, this.getConnectionProtocolVersion(target));
		} catch (ProtocolVersionTooOldException exception) {
			// Send the exception to the other side too, so they know to update their protocol version
			this.sendPacket(target, new ProtocolVersionTooOldPacket(CommandAPIProtocol.PROTOCOL_VERSION, exception.getReason()));
			throw exception;
		}

		// Send the bytes
		sendRawBytes(protocol, target, output.toByteArray());
	}

	/**
	 * Sends an array of bytes to the given target.
	 *
	 * @param protocol The {@link CommandAPIProtocol} to use to send this packet.
	 * @param target   The connection to send this packet to.
	 * @param bytes    The array of bytes to send.
	 */
	protected abstract void sendRawBytes(CommandAPIProtocol protocol, OutputChannel target, byte[] bytes);
}
