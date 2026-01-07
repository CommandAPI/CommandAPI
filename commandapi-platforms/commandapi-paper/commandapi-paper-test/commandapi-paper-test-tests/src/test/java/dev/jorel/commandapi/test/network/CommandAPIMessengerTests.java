package dev.jorel.commandapi.test.network;

import dev.jorel.commandapi.exceptions.ProtocolVersionTooOldException;
import dev.jorel.commandapi.network.BukkitPacketHandlerProvider;
import dev.jorel.commandapi.network.CommandAPIPacket;
import dev.jorel.commandapi.network.FriendlyByteBuffer;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandAPIMessengerTests extends NetworkTestBase {

	/*********
	 * Setup *
	 *********/

	@BeforeEach
	public void setUp() {
		super.setUp();
	}

	@AfterEach
	public void tearDown() {
		super.tearDown();
	}

	/*********
	 * Tests *
	 *********/

	private record UnregisteredPacket() implements CommandAPIPacket {
		@Override
		public void write(FriendlyByteBuffer buffer, Object target, int protocolVersion) throws ProtocolVersionTooOldException {
		}
	}

	@Test
	void exceptionTestWithCommandAPIMessengerSending() {
		Player player = addPlayer();

		// Unregistered packets cannot be sent
		assertThrowsWithMessage(
			IllegalStateException.class,
			"Packet class \"UnregisteredPacket\" is not associated with a CommandAPIProtocol. " +
				"This packet must be registered before it can be sent.",
			() -> getSentBytes(player, new UnregisteredPacket())
		);
	}

	@Test
	void exceptionTestWithCommandAPIMessengerReceiving() {
		Player player = addPlayer();

		// Ignore data not on our channels
		assertDoesNotThrow(() -> assertNull(getHandledPacket(player, "bungeecord:main", new byte[0])));
		assertDoesNotThrow(() -> assertNull(getHandledPacket(player, "commandapi:unimplemented", new byte[0])));

		// Empty packet is actually fine, nothing should happen
		assertDoesNotThrow(() -> assertNull(getHandledPacket(player, "commandapi:play", new byte[0])));


		// Invalid packet ids are unknown
		assertThrowsWithMessage(
			IllegalStateException.class,
			"Unknown packet id: -1",
			// ID is a VarInt, which encodes -1 as {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x0f}
			//  Also added some data after the id here
			() -> getHandledPacket(player, "commandapi:play",
				new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x0f, 0, 0})
		);
		assertThrowsWithMessage(
			IllegalStateException.class,
			"Unknown packet id: 2147483647",
			// ID is a VarInt, which encodes 2147483647 as {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x07}
			//  Also added some data after the id here
			() -> getHandledPacket(player, "commandapi:play",
				new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x07, 0, 0})
		);


		// Packet expected more bytes after given data
		IllegalStateException exception = assertThrowsWithMessage(
			IllegalStateException.class,
			"Exception while reading packet",
			// SetVersionPacket reads a VarInt, so setting the continue bit with nothing after goes out of bounds
			() -> getHandledPacket(player, "commandapi:handshake", new byte[]{0, (byte) 0x80})
		);
		Throwable cause = exception.getCause();
		assertInstanceOf(IllegalStateException.class, cause);
		assertEquals("Read index (2) cannot be greater than or equal to write index (2)", cause.getMessage());


		// Extra bytes found after expected packet data
		assertThrowsWithMessage(
			IllegalStateException.class,
			"Packet was larger than expected! 2 extra byte(s) found after deserializing.\n" +
				"Given: [0, 0, 0], Read: UpdateRequirementsPacket[]",
			// UpdateRequirementsPacket expects no data after id (0)
			() -> getHandledPacket(player, "commandapi:play", new byte[]{0, 0, 0})
		);
	}

	@Test
	void exceptionTestWithPacketHandling() {
		Player player = addPlayer();
		BukkitPacketHandlerProvider packetHandlerProvider = new BukkitPacketHandlerProvider();

		// Handling unknown packet throws exception
		assertThrowsWithMessage(
			IllegalStateException.class,
			"Tried to handle UnregisteredPacket[] with HandshakePacketHandler. HandshakePacketHandler can't handle this packet.",
			() -> packetHandlerProvider.getHandshakePacketHandler().handlePacket(player, new UnregisteredPacket())
		);
		assertThrowsWithMessage(
			IllegalStateException.class,
			"Tried to handle UnregisteredPacket[] with PlayPacketHandler. PlayPacketHandler can't handle this packet.",
			() -> packetHandlerProvider.getPlayPacketHandler().handlePacket(player, new UnregisteredPacket())
		);
	}
}
