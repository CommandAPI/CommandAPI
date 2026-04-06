package dev.jorel.commandapi.network;

import dev.jorel.commandapi.network.packets.UpdateRequirementsPacket;
import dev.jorel.commandapi.network.packets.ProtocolVersionTooOldPacket;
import dev.jorel.commandapi.network.packets.SetVersionPacket;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

// Inspired by net.minecraft.network.ConnectionProtocol
/**
 * An enum that defines the different channels of the network protocol of the CommandAPI. It handles converting bytes off
 * the network into {@link CommandAPIPacket}s.
 */
public enum CommandAPIProtocol {
	// Protocol states
	/**
	 * A {@link CommandAPIProtocol} for the {@code commandapi:handshake} channel. The packets in this protocol help instances
	 * of the CommandAPI communicate the network data they understand.
	 */
	HANDSHAKE("commandapi:handshake", new PacketSetBuilder()
		.register(SetVersionPacket.class, SetVersionPacket::deserialize)
		.register(ProtocolVersionTooOldPacket.class, ProtocolVersionTooOldPacket::deserialize)
	),
	/**
	 * A {@link CommandAPIProtocol} for the {@code commandapi:play} channel. The packets in this protocol handle events
	 * that happen while a server is running.
	 */
	PLAY("commandapi:play", new PacketSetBuilder()
		.register(UpdateRequirementsPacket.class, UpdateRequirementsPacket::deserialize)
	);

	// Global channel variables
	/**
	 * The current version of the protocol. This should be incremented when the protocol is updated to indicate a large
	 * change. A connection without a communicating instance of the CommandAPI (either no CommandAPI instance or a version
	 * before 10.0.0) should be treated as protocol version 0.
	 * <p>
	 * This version number is used to communicate the capabilities of the CommandAPI instance on the other end of the
	 * connection. The instance with the higher version should take responsibility to not send anything the lower version
	 * cannot understand.
	 * <p>
	 * For example, say PV2 adds packet A, which PV1 doesn't know about. When PV2 communicates with PV1, it should not
	 * send packet A, since PV1 would not know what to do with that packet.
	 */
	public static final int PROTOCOL_VERSION = 1;
	private static final Map<String, CommandAPIProtocol> channelIdentifierToProtocol;
	private static final Map<Class<? extends CommandAPIPacket>, CommandAPIProtocol> packetToProtocol;

	// Initialize static variables
	static {
		channelIdentifierToProtocol = new HashMap<>();
		packetToProtocol = new HashMap<>();

		CommandAPIProtocol previousProtocol;
		for (CommandAPIProtocol protocol : values()) {
			previousProtocol = channelIdentifierToProtocol.put(protocol.getChannelIdentifier(), protocol);
			if (previousProtocol != null) {
				throw new IllegalStateException("Protocols " + protocol + " and " + previousProtocol + " cannot " +
					"share the same channel identifier: \"" + protocol.channelIdentifier + "\"");
			}

			for (Class<? extends CommandAPIPacket> packetType : protocol.getAllPacketTypes()) {
				previousProtocol = packetToProtocol.put(packetType, protocol);
				if (previousProtocol != null) {
					throw new IllegalStateException("Packet class \"" + packetType.getSimpleName() + " is already " +
						"assigned to protocol " + previousProtocol + "\n" +
						"It cannot also be assigned to protocol " + protocol);
				}
			}
		}
	}

	// Individual channel variables
	private final String channelIdentifier;
	private final List<Function<FriendlyByteBuffer, ? extends CommandAPIPacket>> idToPacket;
	private final Map<Class<? extends CommandAPIPacket>, Integer> packetToId;

	// Initialize instance variables
	CommandAPIProtocol(String channelIdentifier, PacketSetBuilder packetSet) {
		this.channelIdentifier = channelIdentifier;

		this.idToPacket = packetSet.idToPacket;
		this.packetToId = packetSet.packetToId;
	}

	private static final class PacketSetBuilder {
		final List<Function<FriendlyByteBuffer, ? extends CommandAPIPacket>> idToPacket = new ArrayList<>();
		final Map<Class<? extends CommandAPIPacket>, Integer> packetToId = new HashMap<>();

		/**
		 * Registers a {@link CommandAPIPacket} so that it can be used on the channel of this packet set. This method
		 * assigns ids to each class of packet sequentially. So, the first packet registered to this set gets id 0,
		 * then 1, and so on.
		 *
		 * @param clazz        The class of the packet being registered.
		 * @param deserializer The method that reads this packet from a {@link FriendlyByteBuffer}.
		 */
		<Packet extends CommandAPIPacket> PacketSetBuilder register(Class<Packet> clazz, Function<FriendlyByteBuffer, Packet> deserializer) {
			if(clazz == null) throw new IllegalStateException("Class cannot be null");
			if(deserializer == null) throw new IllegalStateException("Deserializer cannot be null");
			if (packetToId.containsKey(clazz)) {
				throw new IllegalStateException("Packet class \"" + clazz.getSimpleName() + "\" is already registered");
			}

			int id = idToPacket.size();
			idToPacket.add(deserializer);
			packetToId.put(clazz, id);

			return this;
		}
	}

	// Use static variables

	/**
	 * @return A Set of all the channel identifiers used by this protocol.
	 */
	public static Set<String> getAllChannelIdentifiers() {
		return channelIdentifierToProtocol.keySet();
	}

	/**
	 * Gets the {@link CommandAPIProtocol} that can send the given {@link CommandAPIPacket} packet type.
	 *
	 * @param clazz The packet type.
	 * @return The protocol for this packet, or null if the packet is unknown.
	 */
	@Nullable
	public static CommandAPIProtocol getProtocolForPacket(Class<? extends CommandAPIPacket> clazz) {
		return packetToProtocol.get(clazz);
	}

	/**
	 * Gets the {@link CommandAPIProtocol} that handles {@link CommandAPIPacket}s on the given channel.
	 *
	 * @param channelIdentifier The channel identifier as a String.
	 * @return The protocol for this channel, or null if the channel is unknown.
	 */
	@Nullable
	public static CommandAPIProtocol getProtocolForChannel(String channelIdentifier) {
		return channelIdentifierToProtocol.get(channelIdentifier);
	}

	// Use instance variables

	/**
	 * @return The identifier for the plugin message channel of this protocol.
	 */
	public String getChannelIdentifier() {
		return channelIdentifier;
	}

	/**
	 * @return A set of all {@link CommandAPIPacket} classes that can be sent on this protocol.
	 */
	public Set<Class<? extends CommandAPIPacket>> getAllPacketTypes() {
		return packetToId.keySet();
	}

	/**
	 * Reads a {@link FriendlyByteBuffer} to create a {@link CommandAPIPacket}.
	 *
	 * @param id     The id for this packet type. Used to select the appropriate deserialize method.
	 * @param buffer The bytes of this packet.
	 * @return The deserialized packet, or null if the given id could not be found.
	 * @throws IllegalStateException when there is a problem deserializing the packet.
	 */
	@Nullable
	public CommandAPIPacket createPacket(int id, FriendlyByteBuffer buffer) {
		if (id < 0 || id >= idToPacket.size()) return null;
		return idToPacket.get(id).apply(buffer);
	}

	/**
	 * Gets the id for the given {@link CommandAPIPacket} class.
	 *
	 * @param clazz The packet type.
	 * @return The id of the given packet, or -1 if the packet could not be found.
	 */
	public int getId(Class<? extends CommandAPIPacket> clazz) {
		return packetToId.getOrDefault(clazz, -1);
	}
}
