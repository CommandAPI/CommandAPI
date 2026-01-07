package dev.jorel.commandapi.test.network;

import dev.jorel.commandapi.exceptions.ProtocolVersionTooOldException;
import dev.jorel.commandapi.network.packets.UpdateRequirementsPacket;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class UpdateRequirementsPacketTests extends NetworkTestBase {

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

	@Test
	void sendReceiveTestWithUpdateRequirementsPacket() {
		Player player = addPlayer();

		// Protocol version currently 0

		// Error when sending packet to target that has not declared they understand CommandAPI plugin messages
		assertThrowsWithMessage(
			ProtocolVersionTooOldException.class,
			"Tried to send a packet to " + player + ", which is using protocol version 0. " +
				"This system is using version 1. That version is too old to receive the packet. " +
				"CommandAPI version 10.0.0 or greater is required to receive UpdateRequirementsPacket",
			() -> getSentBytes(player, new UpdateRequirementsPacket())
		);

		setProtocolVersion(player, 1); // Protocol version now 1

		// Packet should be encoded as just the id since no data is included
		assertPacketEncodesAndDecodes(player,
			new UpdateRequirementsPacket(),
			new byte[]{0}
		);

		// Update commands should have been called for this player
		Mockito.verify(player, Mockito.times(1)).updateCommands();
	}
}
