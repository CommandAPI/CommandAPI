package dev.jorel.commandapi.test.network;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
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
		PlayerMock player = server.addPlayer();

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
		PlayerMock player = server.addPlayer();

		// Ignore data not on our channels
		assertDoesNotThrow(() -> assertNull(getHandledPacket(player, "bungeecord:main", new byte[0])));
		assertDoesNotThrow(() -> assertNull(getHandledPacket(player, "commandapi:unimplemented", new byte[0])));

		// Empty packet is actually fine, nothing should happen
		assertDoesNotThrow(() -> assertNull(getHandledPacket(player, "commandapi:play", new byte[0])));


		// Exceptions while reading are wrapped to say who sent the data and what it was
		// Invalid packet ids are unknown
		assertReadingPacketFails(player, "commandapi:play",
			// ID is a VarInt, which encodes -1 as {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x0f}
			//  Also added some data after the id here
			new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x0f, 0, 0},
			"FF, FF, FF, FF, 0F, 00, 00",
			"Unknown packet id: -1 for channel: commandapi:play"
		);
		assertReadingPacketFails(player, "commandapi:play",
			// ID is a VarInt, which encodes 2147483647 as {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x07}
			//  Also added some data after the id here
			new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x07, 0, 0},
			"FF, FF, FF, FF, 07, 00, 00",
			"Unknown packet id: 2147483647 for channel: commandapi:play"
		);


		// Packet expected more bytes after given data
		assertReadingPacketFails(player, "commandapi:handshake",
			// SetVersionPacket reads a VarInt, so setting the continue bit with nothing after goes out of bounds
			new byte[]{0, (byte) 0x80},
			"00, 80",
			"Read index (2) cannot be greater than or equal to write index (2)"
		);


		// Extra bytes found after expected packet data
		assertReadingPacketFails(player, "commandapi:play",
			// UpdateRequirementsPacket expects no data after id (0)
			new byte[]{0, 0, 0},
			"00, 00, 00",
			"Packet was larger than expected! 2 extra byte(s) found after deserializing.\n" +
				"Read: UpdateRequirementsPacket[]"
		);
	}

	private void assertReadingPacketFails(Player player, String channel, byte[] message, String hexMessage, String causeMessage) {
		IllegalStateException exception = assertThrowsWithMessage(
			IllegalStateException.class,
			"Exception while reading packet sent by " + player + " [" + hexMessage + "]",
			() -> getHandledPacket(player, channel, message)
		);
		Throwable cause = exception.getCause();
		assertInstanceOf(IllegalStateException.class, cause);
		assertEquals(causeMessage, cause.getMessage());
	}

	@Test
	void exceptionTestWithPacketHandling() {
		Player player = server.addPlayer();
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
